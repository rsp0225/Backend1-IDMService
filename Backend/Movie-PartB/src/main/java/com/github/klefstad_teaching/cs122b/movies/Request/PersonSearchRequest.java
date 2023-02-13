package com.github.klefstad_teaching.cs122b.movies.Request;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PersonSearchRequest {
    private String name;
    private String birthday;
    private String movieTitle;
    private Integer limit;
    private Integer page;
    private String orderBy;
    private String direction;

    public String getName() {
        return name;
    }

    public PersonSearchRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getBirthday() {
        return birthday;
    }

    public PersonSearchRequest setBirthday(String birthday) {
        this.birthday = birthday;
        return this;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public PersonSearchRequest setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public PersonSearchRequest setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public PersonSearchRequest setPage(Integer page) {
        this.page = page;
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public PersonSearchRequest setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String getDirection() {
        return direction;
    }

    public PersonSearchRequest setDirection(String direction) {
        this.direction = direction;
        return this;
    }
}
