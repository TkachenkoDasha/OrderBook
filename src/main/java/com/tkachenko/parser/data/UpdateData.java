package com.tkachenko.parser.data;

public class UpdateData {
    private String type;
    private Integer price;
    private Integer size;

    public UpdateData() {
    }

    public UpdateData(String type, Integer price, Integer size) {
        this.type = type;
        this.price = price;
        this.size = size;
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

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
