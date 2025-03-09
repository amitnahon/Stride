package com.example.stride;

public class MenuItem {
    private String name;
    private String description;
    private double price;
    private String imageResourceName;

    public MenuItem(String name, String description, double price, String imageResourceName) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResourceName = imageResourceName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageResourceName() {
        return imageResourceName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageResourceName(String imageResourceName) {
        this.imageResourceName = imageResourceName;
    }
} 