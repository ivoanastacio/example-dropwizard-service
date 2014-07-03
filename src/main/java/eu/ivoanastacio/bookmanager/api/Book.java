package eu.ivoanastacio.bookmanager.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dropwizard.validation.OneOf;
import io.dropwizard.validation.ValidationMethod;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Book
{
    @NotEmpty
    private String id;

    @NotEmpty
    private String name;

    private List<String> tags;

    @NotNull
    private Integer publishYear;

    @NotNull
    private Integer submittedYear;

    @OneOf(value = {"Drama", "Adventure"})
    private String genre;

    public Book()
    {
        tags = new ArrayList<>();
    }

    @JsonIgnore
    @ValidationMethod(message="Publish year cannot be previous to submitted year.")
    public boolean isValidYear()
    {
        return publishYear >= submittedYear;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags( List<String> tags )
    {
        this.tags = tags;
    }

    public Integer getPublishYear()
    {
        return publishYear;
    }

    public void setPublishYear( Integer publishYear )
    {
        this.publishYear = publishYear;
    }

    public String getGenre()
    {
        return genre;
    }

    public void setGenre( String genre )
    {
        this.genre = genre;
    }

    public void setSubmittedYear( Integer submittedYear )
    {
        this.submittedYear = submittedYear;
    }

    public Integer getSubmittedYear()
    {
        return submittedYear;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Book book = (Book) o;

        if ( !genre.equals( book.genre ) )
        {
            return false;
        }
        if ( !id.equals( book.id ) )
        {
            return false;
        }
        if ( !name.equals( book.name ) )
        {
            return false;
        }
        if ( !publishYear.equals( book.publishYear ) )
        {
            return false;
        }
        if ( !submittedYear.equals( book.submittedYear ) )
        {
            return false;
        }
        if ( tags != null ? !tags.equals( book.tags ) : book.tags != null )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + ( tags != null ? tags.hashCode() : 0 );
        result = 31 * result + publishYear.hashCode();
        result = 31 * result + submittedYear.hashCode();
        result = 31 * result + genre.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Book{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", tags=" + Arrays.toString( tags.toArray() ) +
            ", publishYear=" + publishYear +
            ", submittedYear=" + submittedYear +
            ", genre='" + genre + '\'' +
            '}';
    }
}
