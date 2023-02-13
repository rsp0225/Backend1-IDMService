package com.github.klefstad_teaching.cs122b.billing.Response;

import com.github.klefstad_teaching.cs122b.billing.entity.Sale;
import com.github.klefstad_teaching.cs122b.core.result.Result;

public class OrderListResponse {
    private Result result;
    private Sale[] sales;

    public Result getResult() {
        return result;
    }

    public OrderListResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public Sale[] getSales() {
        return sales;
    }

    public OrderListResponse setSales(Sale[] sales) {
        this.sales = sales;
        return this;
    }
}
