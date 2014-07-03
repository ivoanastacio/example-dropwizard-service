package eu.ivoanastacio.bookmanager.api;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class Author
{
    @NotNull
    private Integer id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String nationality;

    public Integer getId()
    {
        return id;
    }

    public void setId( Integer id )
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

    public String getNationality()
    {
        return nationality;
    }

    public void setNationality( String nationality )
    {
        this.nationality = nationality;
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

        Author author = (Author) o;

        if ( !id.equals( author.id ) )
        {
            return false;
        }
        if ( !name.equals( author.name ) )
        {
            return false;
        }
        if ( !nationality.equals( author.nationality ) )
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
        result = 31 * result + nationality.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Author{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", nationality='" + nationality + '\'' +
            '}';
    }
}
