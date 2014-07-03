package eu.ivoanastacio.bookmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ServiceConfig
    extends Configuration
{
    @NotNull
    @JsonProperty
    ElasticsearchConfig elasticsearch;

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDataSourceFactory()
    {
        return database;
    }

    public ElasticsearchConfig getElasticsearch()
    {
        return elasticsearch;
    }
}
