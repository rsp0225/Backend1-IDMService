package com.github.klefstad_teaching.cs122b.idm.model.response;

import com.github.klefstad_teaching.cs122b.core.result.Result;
import jdk.nashorn.internal.parser.Token;

public class LoginResponse {
    private Result result;
    private String accessToken;
    private String refreshToken;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
