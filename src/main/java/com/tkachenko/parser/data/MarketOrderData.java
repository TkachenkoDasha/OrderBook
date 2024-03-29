package com.tkachenko.parser.data;

public class MarketOrderData {
    private String type;
    private Integer size;

    public MarketOrderData() {
    }

    public MarketOrderData(String type, Integer size) {
        this.type = type;
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
