package com.github.klefstad_teaching.cs122b.idm.model.response;

import com.github.klefstad_teaching.cs122b.core.result.Result;

public class AuthenticateResponse {
    private Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
