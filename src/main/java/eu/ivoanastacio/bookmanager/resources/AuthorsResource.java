package eu.ivoanastacio.bookmanager.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import eu.ivoanastacio.bookmanager.api.Author;
import eu.ivoanastacio.bookmanager.db.AuthorDAO;
import io.dropwizard.jersey.params.IntParam;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/api/authors")
@Produces( MediaType.APPLICATION_JSON)
public class AuthorsResource
{
    private final AuthorDAO dao;

    public AuthorsResource( AuthorDAO dao )
    {
        this.dao = dao;
        this.dao.createTable();
    }

    @Timed
    @POST
    public Response create( @Valid Author author )
    {
        dao.insert( author.getId(), author.getName(), author.getNationality() );
        return Response.created( UriBuilder.fromResource( AuthorsResource.class ).build( author.getId() ) ).build();
    }

    @Timed
    @GET
    @Path("/{authorId}")
    public Optional<Author> read( @PathParam( "authorId" ) IntParam authorId )
    {
        Author author = dao.getById( authorId.get() );
        Optional<Author> result = Optional.fromNullable( author );
        return result;
    }
}
