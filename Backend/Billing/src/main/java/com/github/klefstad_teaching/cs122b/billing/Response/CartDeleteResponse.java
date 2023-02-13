package com.github.klefstad_teaching.cs122b.billing.Response;

import com.github.klefstad_teaching.cs122b.core.result.Result;

public class CartDeleteResponse {
    private Result result;

    public Result getResult() {
        return result;
    }

    public CartDeleteResponse setResult(Result result) {
        this.result = result;
        return this;
    }
}
