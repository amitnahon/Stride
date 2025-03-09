package com.example.stride;

public class Restaurant {
    private String name;
    private String cuisine;
    private String imageUrl;
    private float rating;

    public Restaurant(String name, String cuisine, String imageUrl, float rating) {
        this.name = name;
        this.cuisine = cuisine;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public float getRating() {
        return rating;
    }
} 