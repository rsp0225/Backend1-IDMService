package com.github.klefstad_teaching.cs122b.movies.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;
import com.github.klefstad_teaching.cs122b.movies.Request.MovieSearchRequest;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.Movie;
import org.springframework.beans.factory.annotation.Autowired;
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
}
