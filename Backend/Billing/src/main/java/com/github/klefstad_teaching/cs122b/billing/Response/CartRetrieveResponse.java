package com.github.klefstad_teaching.cs122b.billing.Response;

import com.github.klefstad_teaching.cs122b.billing.entity.Item;
import com.github.klefstad_teaching.cs122b.core.result.Result;

import java.math.BigDecimal;

public class CartRetrieveResponse {
    private Result result;
    private BigDecimal total;
    private Item[] items;

    public Result getResult() {
        return result;
    }

    public CartRetrieveResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public CartRetrieveResponse setTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public Item[] getItems() {
        return items;
    }

    public CartRetrieveResponse setItems(Item[] items) {
        this.items = items;
        return this;
    }
}
