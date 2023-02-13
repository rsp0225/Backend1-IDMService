package com.github.klefstad_teaching.cs122b.gateway.Response;

public class CustomResult {
    private Integer code;
    private String message;

    public Integer getCode() {
        return code;
    }

    public CustomResult setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CustomResult setMessage(String message) {
        this.message = message;
        return this;
    }
}
