package eu.ivoanastacio.bookmanager.resources;

import com.google.common.base.Optional;
import com.sun.jersey.api.client.ClientResponse;
import eu.ivoanastacio.bookmanager.api.Book;
import eu.ivoanastacio.bookmanager.db.BookDAO;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class BooksResourceTest
{
    private static final BookDAO dao = mock(BookDAO.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
                                                                     .addResource( new BooksResource( dao ) )
                                                                     .build();

    private Optional<Book> optionalBook;

    @Before
    public void setup() throws Exception
    {
        Book book = new Book();
        book.setId( "lala" );
        book.setName( "test book" );
        book.setGenre( "Drama" );
        book.setPublishYear( 1999 );
        book.setSubmittedYear( 1998 );
        book.setTags( Arrays.asList( "economy", "finance" ) );

        optionalBook = Optional.of( book );

        when( dao.getById( eq( "lala" ) ) ).thenReturn( optionalBook );
        when( dao.getById( eq( "cucu" ) ) ).thenReturn( Optional.absent() );
    }

    @Test
    public void readReturnsCorrectBook()
    {
        assertThat( resources.client().resource( "/api/books/lala" ).get( Book.class ) )
            .isEqualTo( optionalBook.get() );

        verify( dao ).getById( "lala" );
    }

    @Test
    public void readReturnsNotFoundStatus()
    {
        ClientResponse response = resources.client().resource( "/api/books/cucu" ).get( ClientResponse.class );

        assertThat( response.getStatus() ).isEqualTo( Response.Status.NOT_FOUND.getStatusCode() );
    }


    @Test
    public void createReturnsCreatedStatus()
    {
        ClientResponse response = resources.client().resource( "/api/books" ).type( MediaType.APPLICATION_JSON_TYPE )
            .post( ClientResponse.class, optionalBook.get() );

        assertThat( response.getStatus() ).isEqualTo( Response.Status.CREATED.getStatusCode() );
    }
}
