package com.github.klefstad_teaching.cs122b.movies.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;
import com.github.klefstad_teaching.cs122b.movies.Request.MovieSearchRequest;
import com.github.klefstad_teaching.cs122b.movies.Request.PersonSearchRequest;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;

@Component
public class MovieRepo
{
    private final ObjectMapper objectMapper;
    private final NamedParameterJdbcTemplate template;

    @Autowired
    public MovieRepo(ObjectMapper objectMapper, NamedParameterJdbcTemplate template)
    {
        this.objectMapper = objectMapper;
        this.template = template;
    }

    public Movie[] movieSearch(MovieSearchRequest request, boolean showHidden)
    {
        String sql;
        MapSqlParameterSource source = new MapSqlParameterSource();
        boolean whereAdded = false;

        //id, title, year, director, rating, backdropPath, posterPath, hidden
        sql = "SELECT JSON_ARRAYAGG(JSON_OBJECT('id', sub.movie_id, 'title', sub.movie_title, 'year', sub.movie_year, 'director', sub.director_name, 'rating', sub.movie_rating, 'backdropPath', sub.backdrop_path, 'posterPath', sub.poster_path, 'hidden', sub.hidden)) "
                + "FROM (SELECT m.id AS movie_id, m.title AS movie_title, m.year AS movie_year, p.name AS director_name, m.rating AS movie_rating, m.backdrop_path, m.poster_path, m.hidden "
                + "FROM movies.movie m "
                + "JOIN movies.person p ON p.id = m.director_id ";

        if(request.getGenre() != null){
            sql += " JOIN movies.movie_genre mg on mg.movie_id = m.id ";
            sql += " JOIN movies.genre g on g.id = mg.genre_id ";
        }

        if(request.getTitle() != null){
            if (whereAdded) {
                sql += " AND ";
            }
            else {
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += " m.title LIKE :title ";

            // This allows for WILDCARD Search
            String wildcardSearch = '%' + request.getTitle() + '%';

            source.addValue("title", wildcardSearch, Types.VARCHAR);
        }

        if(request.getYear() != null){
            if (whereAdded) {
                sql += " AND ";
            }
            else {
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += " m.year = :year ";
            source.addValue("year", request.getYear(), Types.INTEGER);
        }

        if(request.getDirector() != null){
            if (whereAdded) {
                sql += " AND ";
            }
            else {
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += " p.name LIKE :director ";

            // This allows for WILDCARD Search
            String wildcardSearch = '%' + request.getDirector() + '%';

            source.addValue("director", wildcardSearch, Types.VARCHAR);
        }

        if(request.getGenre() != null){
            if (whereAdded) {
                sql += " AND ";
            }
            else {
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += " g.name LIKE :genre ";

            // This allows for WILDCARD Search
            String wildcardSearch = '%' + request.getGenre() + '%';

            source.addValue("genre", wildcardSearch, Types.VARCHAR);
        }

        if(showHidden == false){
            if (whereAdded) {
                sql += " AND ";
            }
            else {
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += "m.hidden = 0 ";
        }

        if(request.getOrderBy() == null){
            sql += " ORDER BY m.title ";
        }
        else{
            if(request.getOrderBy() == "title" || request.getOrderBy() == "rating" || request.getOrderBy() == "year"){
                sql += " ORDER BY m." + request.getOrderBy() + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_ORDER_BY);
            }
        }

        if(request.getDirection() == null){
            sql += " asc ";
        }
        else{
            if(request.getDirection() == "asc" || request.getDirection() == "desc"){
                sql += " " + request.getDirection() + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_DIRECTION);
            }
        }
        sql += " , m.id ";
        if(request.getLimit() == null){
            sql += " LIMIT 10 ";
        }
        else{
            if(request.getLimit() == 10 || request.getLimit() == 25 || request.getLimit() == 50 || request.getLimit() == 100){
                int temp = request.getLimit();
                sql += " LIMIT " + temp;
            }
            else{
                throw new ResultError(MoviesResults.INVALID_LIMIT);
            }
        }

        if(request.getPage() == null){
            sql += " OFFSET 0 ";
        }
        else{
            if(request.getPage() > 0 ){
                String temp = String.valueOf((request.getPage() - 1) * request.getLimit());
                sql +=  " OFFSET " + temp + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_PAGE);
            }
        }

        sql += ") sub;";
        System.out.println(sql);
        String result = this.template.queryForObject(sql, source, (rs,rowNum) -> rs.getString(1));
        if(result == null){
            throw new ResultError(MoviesResults.NO_MOVIES_FOUND_WITHIN_SEARCH);
        }

        else{
            try{
                return objectMapper.readValue(result, Movie[].class);
            }
            catch (JsonProcessingException e){
                throw new ResultError(MoviesResults.NO_MOVIES_FOUND_WITHIN_SEARCH);
            }
        }
    }

    public Movie[] movieSearchPID(Long personID, MovieSearchRequest request, boolean showHidden){
        String sql;
        MapSqlParameterSource source = new MapSqlParameterSource();
        boolean whereAdded = false;

        //id, title, year, director, rating, backdropPath, posterPath, hidden
        sql = "SELECT JSON_ARRAYAGG(JSON_OBJECT('id', sub.movie_id, 'title', sub.movie_title, 'year', sub.movie_year, 'director', sub.director_name, 'rating', sub.movie_rating, 'backdropPath', sub.backdrop_path, 'posterPath', sub.poster_path, 'hidden', sub.hidden)) "
                + "FROM (SELECT m.id AS movie_id, m.title AS movie_title, m.year AS movie_year, director.name AS director_name, m.rating AS movie_rating, m.backdrop_path, m.poster_path, m.hidden "
                + "FROM movies.movie_person p "
                + "JOIN movies.movie m ON m.id = p.movie_id "
                + "JOIN movies.person director ON director.id = m.director_id "
                + "WHERE p.person_id = :personID ";

        source.addValue("personID", personID, Types.INTEGER);

        if(showHidden == false){
            sql += "AND ";
            sql += "m.hidden = 0 ";
        }

        if(request.getOrderBy() == null){
            sql += " ORDER BY m.title ";
        }
        else{
            if(request.getOrderBy() == "title" || request.getOrderBy() == "rating" || request.getOrderBy() == "year"){
                sql += " ORDER BY m." + request.getOrderBy() + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_ORDER_BY);
            }
        }

        if(request.getDirection() == null){
            sql += " asc ";
        }
        else{
            if(request.getDirection() == "asc" || request.getDirection() == "desc"){
                sql += " " + request.getDirection() + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_DIRECTION);
            }
        }
        sql += " , m.id ";
        if(request.getLimit() == null){
            request.setLimit(10);
            sql += " LIMIT 10 ";
        }
        else{
            if(request.getLimit() == 10 || request.getLimit() == 25 || request.getLimit() == 50 || request.getLimit() == 100){
                int temp = request.getLimit();
                sql += " LIMIT " + temp;
            }
            else{
                throw new ResultError(MoviesResults.INVALID_LIMIT);
            }
        }

        if(request.getPage() == null){
            sql += " OFFSET 0 ";
        }
        else{
            if(request.getPage() > 0 ){
                String temp = String.valueOf((request.getPage() - 1) * request.getLimit());
                sql +=  " OFFSET " + temp + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_PAGE);
            }
        }

        sql += ") sub;";
        String result = this.template.queryForObject(sql, source, (rs,rowNum) -> rs.getString(1));
        if(result == null){
            throw new ResultError(MoviesResults.NO_MOVIES_WITH_PERSON_ID_FOUND);
        }

        else{
            try{
                return objectMapper.readValue(result, Movie[].class);
            }
            catch (JsonProcessingException e){
                throw new ResultError(MoviesResults.NO_MOVIES_WITH_PERSON_ID_FOUND);
            }
        }
    }

    public MovieDetail movieGetMID(Long movieId, boolean showHidden) {
        String sql;
        MapSqlParameterSource source = new MapSqlParameterSource();
        boolean whereAdded = false;

        //id, title, year, director, rating, numVotes, budget, revenue, overview, backdropPath, posterPath, hidden
        sql = "SELECT JSON_OBJECT('id', m.id, 'title', m.title, 'year', m.year, 'director', p.name, 'rating', m.rating, 'numVotes', m.num_votes, 'budget', m.budget, 'revenue', m.revenue, 'overview', m.overview, 'backdropPath', m.backdrop_path, 'posterPath', m.poster_path, 'hidden', m.hidden) "
                +   "FROM movies.movie m "
                +   "JOIN movies.person p ON p.id = m.director_id "
                +   "WHERE m.id = :movieId ";

        source.addValue("movieId", movieId, Types.INTEGER);

        if(showHidden == false){
            sql += "AND ";
            sql += "m.hidden = 0; ";
        }
        //System.out.println(sql);
        String result = null;

        try{
            result = this.template.queryForObject(sql, source, (rs,rowNum) -> rs.getString(1));
        }
        catch (EmptyResultDataAccessException e){
            throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
        }
        System.out.println(result);
        if(result == null){
            throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
        }

        else{
            try{
                return objectMapper.readValue(result, MovieDetail.class);
            }
            catch (JsonProcessingException e){
                throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
            }
        }
    }

    public Genre[] movieGetGenre(Long movieId, boolean showHidden){
        String sql;
        MapSqlParameterSource source = new MapSqlParameterSource();
        boolean whereAdded = false;

        //id, name
        sql = "SELECT JSON_ARRAYAGG(JSON_OBJECT('id', t.id, 'name', t.name)) "
                +   "FROM (SELECT DISTINCT g.id, g.name "
                +   "FROM movies.genre g "
                +   "JOIN movies.movie_genre mg on mg.genre_id = g.id "
                +   "WHERE mg.movie_id = :movieId "
                +   "ORDER BY g.name)t; ";

        source.addValue("movieId", movieId, Types.INTEGER);

        String result = null;
        try{
            result = this.template.queryForObject(sql, source, (rs,rowNum) -> rs.getString(1));
        }
        catch (EmptyResultDataAccessException e){
            //throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
            System.out.println("EmptyResultDataAccessException");
            return new Genre[0];
        }
        System.out.println(result);
        if(result == null){
            //throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
            System.out.println(("result == null"));
            return new Genre[0];
        }

        else{
            try{
                return objectMapper.readValue(result, Genre[].class);
            }
            catch (JsonProcessingException e){
                //throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
                System.out.println("JsonProcessingException");
                return new Genre[0];
            }
        }
    }

    public Person[] movieGetPerson(Long movieId, boolean showHidden){
        String sql;
        MapSqlParameterSource source = new MapSqlParameterSource();
        boolean whereAdded = false;

        //id, name
        sql = "SELECT JSON_ARRAYAGG(JSON_OBJECT('id', t.id, 'name', t.name)) "
                +   "FROM (SELECT DISTINCT p.id, p.name, p.popularity "
                +   "FROM movies.person p "
                +   "JOIN movies.movie_person mp on mp.person_id = p.id "
                +   "WHERE mp.movie_id = :movieId "
                +   "ORDER BY p.popularity DESC, p.id ASC)t ";

        source.addValue("movieId", movieId, Types.INTEGER);

        String result = null;
        try{
            result = this.template.queryForObject(sql, source, (rs,rowNum) -> rs.getString(1));
        }
        catch (EmptyResultDataAccessException e){
            //throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
            return new Person[0];
        }

        if(result == null){
            //throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
            return new Person[0];
        }

        else{
            try{
                return objectMapper.readValue(result, Person[].class);
            }
            catch (JsonProcessingException e){
                //throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
                return new Person[0];
            }
        }
    }

    public PersonDetail[] personSearch(PersonSearchRequest request, boolean showHidden){
        String sql;
        MapSqlParameterSource source = new MapSqlParameterSource();
        boolean whereAdded = false;

        //id, name, birthday, biography, birthplace, popularity, profilePath
        sql = "SELECT JSON_ARRAYAGG(JSON_OBJECT('id', t.id, 'name', t.name, 'birthday', t.birthday, 'biography', t.biography, 'birthplace', t.birthplace, 'popularity', t.popularity, 'profilePath', t.profile_path)) "
                +   "FROM(SELECT DISTINCT p.id, p.name, p.birthday, p.biography, p.birthplace, p.popularity, p.profile_path "
                +   "FROM movies.person p ";

        if(request.getMovieTitle() != null){
            sql += "JOIN movies.movie_person mp ON p.id = mp.person_id "
                    +   "JOIN movies.movie m on m.id = mp.movie_id ";
        }

        if(request.getName() != null) {
            if(whereAdded) {
                sql += " AND ";
            }
            else {
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += " p.name LIKE :name";

            // This allows for WILDCARD Search
            String wildcardSearch = '%' + request.getName() + '%';

            source.addValue("name", wildcardSearch, Types.VARCHAR);
        }

        if(request.getBirthday() != null){
            if (whereAdded) {
                sql += " AND ";
            }
            else{
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += " p.birthday = :birthday ";
            source.addValue("birthday", request.getBirthday(), Types.VARCHAR);
        }

        if(request.getMovieTitle() != null) {
            if(whereAdded) {
                sql += " AND ";
            }
            else{
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += " m.title LIKE :movie";

            // This allows for WILDCARD Search
            String wildcardSearch = '%' + request.getMovieTitle() + '%';

            source.addValue("movie", wildcardSearch, Types.VARCHAR);
        }

        if(showHidden == false){
            if (whereAdded) {
                sql += " AND ";
            }
            else {
                sql += " WHERE ";
                whereAdded = true;
            }
            sql += "m.hidden = 0 ";
        }

        if(request.getOrderBy() == null){
            sql += " ORDER BY p.name ";
        }
        else{
            if(request.getOrderBy() == "name" || request.getOrderBy() == "popularity" || request.getOrderBy() == "birthday"){
                sql += " ORDER BY p." + request.getOrderBy() + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_ORDER_BY);
            }
        }

        if(request.getDirection() == null){
            sql += " asc ";
        }
        else{
            if(request.getDirection() == "asc" || request.getDirection() == "desc"){
                sql += " " + request.getDirection() + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_DIRECTION);
            }
        }

        sql += " , p.id ";
        if(request.getLimit() == null){
            request.setLimit(10);
            sql += " LIMIT 10 ";
        }
        else{
            if(request.getLimit() == 10 || request.getLimit() == 25 || request.getLimit() == 50 || request.getLimit() == 100){
                int temp = request.getLimit();
                sql += " LIMIT " + temp;
            }
            else{
                throw new ResultError(MoviesResults.INVALID_LIMIT);
            }
        }

        if(request.getPage() == null){
            sql += " OFFSET 0 ";
        }
        else{
            if(request.getPage() > 0 ){
                System.out.println("TESTING");
                System.out.println((request.getPage() - 1));
                String temp = String.valueOf((request.getPage() - 1) * request.getLimit());
                sql +=  " OFFSET " + temp + " ";
            }
            else{
                throw new ResultError(MoviesResults.INVALID_PAGE);
            }
        }

        sql += ") t;";
        System.out.println(sql);
        String result = this.template.queryForObject(sql, source, (rs,rowNum) -> rs.getString(1));
        if(result == null){
            throw new ResultError(MoviesResults.NO_PERSONS_FOUND_WITHIN_SEARCH);
        }

        else{
            try{
                return objectMapper.readValue(result, PersonDetail[].class);
            }
            catch (JsonProcessingException e){
                throw new ResultError(MoviesResults.NO_PERSONS_FOUND_WITHIN_SEARCH);
            }
        }
    }

    public PersonDetail personSearchId(Long personId, PersonSearchRequest request, boolean showHidden) {
        String sql;
        MapSqlParameterSource source = new MapSqlParameterSource();
        boolean whereAdded = false;

        sql = "SELECT JSON_OBJECT('id', p.id, 'name', p.name, 'birthday', p.birthday, 'biography', p.biography, 'birthplace', p.birthplace, 'popularity', p.popularity, 'profilePath', p.profile_path) "
                + "FROM movies.person p "
                + "WHERE p.id = :personId ";

        source.addValue("personId", personId, Types.INTEGER);


        String result = null;
        try{
            result = this.template.queryForObject(sql, source, (rs,rowNum) -> rs.getString(1));
        }
        catch (EmptyResultDataAccessException e){
            //throw new ResultError(MoviesResults.NO_MOVIE_WITH_ID_FOUND);
            System.out.println("EmptyResultDataAccessException");
        }

        if (result == null) {
            throw new ResultError(MoviesResults.NO_PERSON_WITH_ID_FOUND);
        } else {
            try {
                return objectMapper.readValue(result, PersonDetail.class);
            } catch (JsonProcessingException e) {
                throw new ResultError(MoviesResults.NO_PERSON_WITH_ID_FOUND);
            }
        }
    }
}
