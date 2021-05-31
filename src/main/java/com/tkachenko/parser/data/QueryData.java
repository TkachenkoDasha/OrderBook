package com.tkachenko.parser.data;

public class QueryData {
    private String type;
    private Integer price;

    public QueryData() {
    }

    public QueryData(String type) {
        this.type = type;
    }

    public QueryData(String type, Integer price) {
        this.type = type;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
