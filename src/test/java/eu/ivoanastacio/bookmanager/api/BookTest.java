package eu.ivoanastacio.bookmanager.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static io.dropwizard.testing.FixtureHelpers.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class BookTest
{
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private Book book;

    @Before
    public void setup() throws Exception
    {
        book = new Book();
        book.setId( "lala" );
        book.setName( "test book" );
        book.setGenre( "Drama" );
        book.setPublishYear( 1999 );
        book.setSubmittedYear( 1998 );
        book.setTags( Arrays.asList( "economy", "finance" ) );
    }

    @Test
    public void serializesToJSON() throws Exception
    {
        assertThat( MAPPER.writeValueAsString( book ) ).isEqualTo( fixture( "fixtures/book.json" ) );
    }

    @Test
    public void deserializesFromJSON() throws Exception
    {
        assertThat( MAPPER.readValue( fixture( "fixtures/book.json" ), Book.class ) ).isEqualTo( book );
    }
}
