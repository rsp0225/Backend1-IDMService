package com.github.klefstad_teaching.cs122b.movies.Response;

import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.Movie;

public class MovieSearchResponse {
    private Result result;
    private Movie[] movies;

    public Result getResult() {
        return result;
    }

    public MovieSearchResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public Movie[] getMovies() {
        return movies;
    }

    public MovieSearchResponse setMovies(Movie[] movies) {
        this.movies = movies;
        return this;
    }
}
