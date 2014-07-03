package eu.ivoanastacio.bookmanager.db;

import eu.ivoanastacio.bookmanager.api.Author;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface AuthorDAO
{
    @SqlUpdate("create table if not exists author (id int primary key, name varchar(100), nationality varchar(10))")
    void createTable();

    @SqlUpdate("insert into author (id, name, nationality) values (:id, :name, :nationality)")
    void insert(@Bind("id") int id, @Bind("name") String name, @Bind("nationality") String nationality);

    @SqlQuery("select name from author where id = :id")
    String getNameById(@Bind("id") int id);

    @SqlQuery( "sql-queries/GetAuthorById" )
    @Mapper(AuthorMapper.class)
    Author getById( @Bind("id") Integer id );

    public class AuthorMapper implements ResultSetMapper<Author>
    {
        @Override
        public Author map( int index, ResultSet r, StatementContext ctx ) throws SQLException
        {
            Author author = new Author();
            author.setId( r.getInt( "id" ) );
            author.setName( r.getString( "name" ) );
            author.setNationality( r.getString( "nationality" ) );
            return author;
        }
    }
}