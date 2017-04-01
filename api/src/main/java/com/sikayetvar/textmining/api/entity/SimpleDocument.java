package com.sikayetvar.textmining.api.entity;

public class SimpleDocument implements Document {
    private String category;
    private String body;

    public SimpleDocument(String body) {
        this(null, body);
    }

    public SimpleDocument(String category, String body) {
        this.category = category;
        this.body = body;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getBody() {
        return body;
    }
}
