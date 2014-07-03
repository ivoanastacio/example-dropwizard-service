package eu.ivoanastacio.bookmanager.db;

import com.google.common.base.Optional;
import eu.ivoanastacio.bookmanager.api.Book;
import eu.ivoanastacio.bookmanager.managed.ElasticsearchStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class BookDAO
{
    private static final Logger log = LoggerFactory.getLogger( BookDAO.class );

    private final ElasticsearchStorage elasticsearch;
    private static final String ES_INDEX = "bookmanager";
    private static final String ES_TYPE = "book";

    public BookDAO( ElasticsearchStorage esStorage )
    {
        this.elasticsearch = esStorage;
    }


    public List<Book> getAll()
    {
        List<Book> result = elasticsearch.scan( ES_INDEX, ES_TYPE, Book.class );
        return result;
    }


    public Optional<Book> getById( String id )
    {
        return elasticsearch.read( ES_INDEX, ES_TYPE, id, Book.class );
    }


    public Optional<Boolean> upsert(String id, Book book )
    {
        return elasticsearch.index( ES_INDEX, ES_TYPE, book, id );
    }


    public boolean delete( String id )
    {
        return elasticsearch.delete( ES_INDEX, ES_TYPE, id );
    }
}