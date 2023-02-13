package com.github.klefstad_teaching.cs122b.movies.Response;

import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.PersonDetail;


public class PersonSearchResponse {
    private Result result;
    private PersonDetail[] persons;

    public Result getResult() {
        return result;
    }

    public PersonSearchResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public PersonDetail[] getPersons() {
        return persons;
    }

    public PersonSearchResponse setPersons(PersonDetail[] persons) {
        this.persons = persons;
        return this;
    }
}
