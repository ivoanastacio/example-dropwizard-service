package eu.ivoanastacio.bookmanager;

import eu.ivoanastacio.bookmanager.db.AuthorDAO;
import eu.ivoanastacio.bookmanager.managed.ElasticsearchStorage;
import eu.ivoanastacio.bookmanager.db.BookDAO;
import eu.ivoanastacio.bookmanager.health.ElasticsearchHealthCheck;
import eu.ivoanastacio.bookmanager.resources.AuthorsResource;
import eu.ivoanastacio.bookmanager.resources.BooksResource;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

public class ServiceApplication extends Application<ServiceConfig>
{

    public static void main( String[] args ) throws Exception
    {
        new ServiceApplication().run( args );
    }

    @Override
    public void initialize( Bootstrap<ServiceConfig> serviceConfigurationBootstrap )
    {
    }

    @Override
    public void run( ServiceConfig config, Environment environment ) throws Exception
    {
        // jdbi instances are automatically managed and healthchecked
        final DBI jdbiStorage = new DBIFactory().build(environment, config.getDataSourceFactory(), "h2");

        final ElasticsearchStorage esStorage = new ElasticsearchStorage( config.getElasticsearch().getConfigFile() );
        environment.lifecycle().manage( esStorage );
        environment.healthChecks().register("Elasticsearch health", new ElasticsearchHealthCheck( esStorage ) );

        environment.jersey().register( new AuthorsResource( jdbiStorage.onDemand( AuthorDAO.class) ) );
        environment.jersey().register( new BooksResource( new BookDAO( esStorage ) ) );
    }
}
