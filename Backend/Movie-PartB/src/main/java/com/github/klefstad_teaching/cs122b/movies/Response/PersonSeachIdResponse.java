package com.github.klefstad_teaching.cs122b.movies.Response;

import com.github.klefstad_teaching.cs122b.core.result.Result;
import com.github.klefstad_teaching.cs122b.movies.repo.entity.PersonDetail;

public class PersonSeachIdResponse {
    private Result result;
    private PersonDetail person;

    public Result getResult() {
        return result;
    }

    public PersonSeachIdResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public PersonDetail getPerson() {
        return person;
    }

    public PersonSeachIdResponse setPerson(PersonDetail person) {
        this.person = person;
        return this;
    }

}
