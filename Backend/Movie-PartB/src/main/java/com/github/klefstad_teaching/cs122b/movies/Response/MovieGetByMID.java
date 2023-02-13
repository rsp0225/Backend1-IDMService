package com.github.klefstad_teaching.cs122b.movies.Response;

import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.Genre;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.Movie;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.MovieDetail;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.Person;

public class MovieGetByMID {
    private Result result;
    private MovieDetail movie;
    private Genre[] genres;
    private Person[] persons;

    public Result getResult() {
        return result;
    }

    public MovieGetByMID setResult(Result result) {
        this.result = result;
        return this;
    }

    public MovieDetail getMovie() {
        return movie;
    }

    public MovieGetByMID setMovie(MovieDetail movie) {
        this.movie = movie;
        return this;
    }

    public Genre[] getGenres() {
        return genres;
    }

    public MovieGetByMID setGenres(Genre[] genres) {
        this.genres = genres;
        return this;
    }

    public Person[] getPersons() {
        return persons;
    }

    public MovieGetByMID setPersons(Person[] persons) {
        this.persons = persons;
        return this;
    }
}
