package eu.ivoanastacio.bookmanager.health;

import com.codahale.metrics.health.HealthCheck;
import eu.ivoanastacio.bookmanager.managed.ElasticsearchStorage;

public class ElasticsearchHealthCheck extends HealthCheck
{
    private final ElasticsearchStorage storage;

    public ElasticsearchHealthCheck(ElasticsearchStorage storage)
    {
        this.storage = storage;
    }


    @Override
    protected Result check() throws Exception
    {
        Result result;

        if (storage.isUp()) {
            result = Result.healthy();
        } else {
            result = Result.unhealthy("Cannot connect to elasticsearch");
        }

        return result;
    }
}
