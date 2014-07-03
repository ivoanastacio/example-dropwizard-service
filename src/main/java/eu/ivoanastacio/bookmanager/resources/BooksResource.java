package eu.ivoanastacio.bookmanager.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import eu.ivoanastacio.bookmanager.api.Book;
import eu.ivoanastacio.bookmanager.db.BookDAO;

import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
public class BooksResource
{
    private final BookDAO bookDao;

    public BooksResource( BookDAO bookDao )
    {
        this.bookDao = bookDao;
    }

    @Timed
    @GET
    public List<Book> list()
    {
        return bookDao.getAll();
    }

    @Timed
    @GET
    @Path("/{bookId}")
    public Optional<Book> read( @PathParam( "bookId" ) String feedId )
    {
        return bookDao.getById(feedId); // automatically returns 404 if Optional contains a null object
    }

    @Timed
    @POST
    public Response create( @Valid Book book ) // automatically returns 422 Error if input is not valid
    {
        bookDao.upsert( book.getId(), book );

        return Response.created( UriBuilder.fromResource( BooksResource.class ).build( book.getId() ) ).build();
    }

    @Timed
    @PUT
    @Path("/{bookId}")
    public void update( @PathParam("bookId") String bookId, @Valid Book book )
    {
        if (!bookId.equals( book.getId() ))
        {
            Exception cause = new IllegalArgumentException(
                "The ID specified in the route param does not match the ID in the payload.");
            throw new WebApplicationException( cause, Response.Status.BAD_REQUEST );
        }

        bookDao.upsert( bookId, book );
    }

    @Timed
    @DELETE
    @Path("/{bookId}")
    public void delete( @PathParam("bookId") String bookId )
    {
        bookDao.delete( bookId );
    }

    @OPTIONS
    public Response options()
    {
        return Response.ok().build();
    }
}
