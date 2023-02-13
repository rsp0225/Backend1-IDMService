package com.github.klefstad_teaching.cs122b.gateway.Response;

public class AuthenticateResponse {
    private CustomResult result;

    public CustomResult getResult() {
        return result;
    }

    public AuthenticateResponse setResult(CustomResult result) {
        this.result = result;
        return this;
    }
}
