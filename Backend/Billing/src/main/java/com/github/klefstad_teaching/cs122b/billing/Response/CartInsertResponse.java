package com.github.klefstad_teaching.cs122b.billing.Response;

import com.github.klefstad_teaching.cs122b.core.result.Result;

public class CartInsertResponse {
    private Result result;

    public Result getResult() {
        return result;
    }

    public CartInsertResponse setResult(Result result) {
        this.result = result;
        return this;
    }
}
