package eu.ivoanastacio.bookmanager;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticsearchConfig
{
    @JsonProperty
    private String configFile;

    public String getConfigFile()
    {
        return configFile;
    }
}
