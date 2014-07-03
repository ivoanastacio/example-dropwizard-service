package eu.ivoanastacio.bookmanager.managed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ElasticsearchStorage implements Managed
{
    private static final Logger log = LoggerFactory.getLogger( ElasticsearchStorage.class );

    private static final String MAPPINGS_FILE_FORMAT = "elasticsearch-%s-mapping.json";
    private static final TimeValue SCAN_CACHE_TIMEOUT_MILLIS = new TimeValue( 10 * 60 * 1000 );
    private static final TimeValue SCAN_SCROLL_TIMEOUT_MILLIS = new TimeValue( 60 * 1000 );
    public static final int SCROLL_PAGE_SIZE = 250;

    private final Node node;
    private final Client client;
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private final Set<String> verifiedTypes;
    private final Set<String> verifiedIndices;


    public ElasticsearchStorage( String configFilename )
    {
        verifiedIndices = new HashSet<>();
        verifiedTypes = new HashSet<>();

        Settings settings = loadSettingsFile( configFilename );
        NodeBuilder builder = NodeBuilder.nodeBuilder();
        node = builder.settings( settings ).node();
        client = node.client();

        log.debug( "Waiting for the Elasticsearch cluster" );
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        log.debug( "Finished initialization of the Elasticsearch client" );
    }

    @Override
    public void start() throws Exception
    {
    }

    @Override
    public void stop() throws Exception
    {
        close();
    }

    /**
     * Return true if the cluster is at a yellow or green status.
     */
    public boolean isUp()
    {
        ClusterHealthResponse response = client.admin().cluster().prepareHealth().execute().actionGet();
        return response.getStatus() == ClusterHealthStatus.GREEN || response.getStatus() == ClusterHealthStatus.YELLOW;
    }


    /**
     * Retrieve stream of all objects matching the given conditions. Results are unordered.
     */
    public <T> List<T> scan( String esIndex, String esType, Class<T> objClass )
    {
        List<T> result = new ArrayList<>();



        SearchResponse response = client.prepareSearch( esIndex )
                                        .setTypes( esType )
                                        .setSearchType( SearchType.SCAN )
                                        .setQuery( new HashMap() ) // TODO
                                        .setScroll( SCAN_SCROLL_TIMEOUT_MILLIS )
                                        .setSize( SCROLL_PAGE_SIZE )
                                        .execute().actionGet();

        // the response of the previous call didn't include any hits, just the total hits
        // and the scroll id that allows to start the scroll process.

        long totalHits = response.getHits().getTotalHits();
        long processedHits = 0;
        while ( processedHits < totalHits )
        {
            response = client.prepareSearchScroll( response.getScrollId() )
                             .setScroll( SCAN_CACHE_TIMEOUT_MILLIS )
                             .execute().actionGet();

            for (SearchHit hit : response.getHits())
            {
                try
                {
                    T obj = MAPPER.readValue( hit.getSourceAsString(), objClass );
                    result.add( obj );
                }
                catch ( IOException e )
                {
                    log.error( "Failed to deserialize object. {}", e.getMessage() );
                }
                processedHits++;
            }
        }

        return result;
    }


    /**
     * Read a record from elasticsearch.
     */
    public <T> Optional<T> read( String index, String type, String id, Class<T> objClass )
    {
        Optional<T> result = Optional.absent();
        try
        {
            GetResponse response = client.prepareGet( index, type, id ).execute().actionGet();
            if (response.isExists())
            {
                result = Optional.of( MAPPER.readValue( response.getSourceAsBytes(), objClass ) );
            }
        }
        catch ( ElasticsearchException e )
        {
            log.error( "Failed to query elasticsearch storage. {}", e.getMessage() );
        }
        catch ( IOException e )
        {
            log.error( "Failed to deserialize object. {}", e.getMessage() );
        }

        return result;
    }


    /**
     * Update a record in elasticsearch.
     */
    public Optional<Boolean> index( String index, String type, Object obj, String id )
    {
        createIndexIfNotExist( index );
        createMappingsIfNotExist( index, type );

        Optional<Boolean> isNew = Optional.absent();
        try
        {
            String json = MAPPER.writeValueAsString( obj );
            IndexResponse response = client.prepareIndex( index, type, id )
                                           .setSource( json )
                                           .execute().actionGet();

            isNew = Optional.of( response.getVersion() == 1 );
        }
        catch ( JsonProcessingException e )
        {
            log.error( "Failed to serialize object to json. {}", e.getMessage() );
        }

        return isNew;
    }


    /**
     * Delete record based on the specified index, type, and id.
     */
    public boolean delete( String index, String type, String id )
    {
        DeleteResponse response = client.prepareDelete( index, type, id )
                                        .execute().actionGet();

        return response.isFound();
    }


    /**
     * Dispose of all allocated resources.
     */
    public void close()
    {
        client.close();
        node.close();
    }


    private boolean createMappingsIfNotExist( String index, String type )
    {
        String indexTypeName = String.format( "%s ~ %s", index, type );
        boolean isSuccess = verifiedTypes.contains( indexTypeName );
        if ( isSuccess )
        {
            return true;
        }

        ClusterState cs = client.admin().cluster().prepareState().setIndices( index ).execute().actionGet().getState();

        IndexMetaData indexMetadata = cs.getMetaData().index( index );

        if ( indexMetadata == null || indexMetadata.mapping( type ) == null )
        {
            try
            {
                String mappingsJson =
                    Resources.toString( Resources.getResource( String.format( MAPPINGS_FILE_FORMAT, type ) ),
                                        Charsets.UTF_8 );

                PutMappingRequestBuilder mappingRequest =
                    client.admin().indices().preparePutMapping( index ).setType( type ).setSource( mappingsJson );

                PutMappingResponse response = mappingRequest.execute().actionGet();

                if ( response.isAcknowledged() )
                {
                    isSuccess = true;
                    verifiedTypes.add( indexTypeName );
                }
            }
            catch ( IOException e )
            {
                log.error( "Failed to load mappings file for type: '{}'", type );
                log.error( e.getMessage() );
            }
        }
        else
        {
            isSuccess = true;
        }

        return isSuccess;
    }


    private boolean createIndexIfNotExist( String index )
    {
        boolean isSuccess = verifiedIndices.contains( index );
        if ( isSuccess )
        {
            return true;
        }

        IndicesExistsRequest existsRequest = client.admin().indices().prepareExists( index ).request();
        IndicesExistsResponse existResponse = client.admin().indices().exists( existsRequest ).actionGet();

        if ( !existResponse.isExists() )
        {
            log.debug( "Creating Elasticsearch index '{}'", index );

            CreateIndexRequestBuilder request = client.admin().indices().prepareCreate( index );

            CreateIndexResponse response = request.execute().actionGet();
            if ( response.isAcknowledged() )
            {
                isSuccess = true;
                verifiedIndices.add( index );
            }
            else
            {
                log.error( "Failed to create Elasticsearch index '{}'", index );
            }
        }
        else
        {
            log.debug( "Elasticsearch index '{}' already exists", index );
        }

        return isSuccess;
    }


    private Settings loadSettingsFile( String configFile )
    {
        log.debug( "Loading Elasticsearch settings" );
        Settings settings;

        try
        {
            String settingsContent = Resources.toString( Resources.getResource( configFile ), Charsets.UTF_8 );
            settings = ImmutableSettings.settingsBuilder().loadFromSource( settingsContent ).build();
        }
        catch ( IOException e )
        {
            log.error( "Unable to read the specified Elasticsearch config file: {}, using default.", configFile );
            log.error( "Exception: {}", e.getMessage() );
            settings = ImmutableSettings.EMPTY;
        }

        return settings;
    }
}