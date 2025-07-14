package com.example.stride;

public class Restaurant {
    private final String name;
    private final String cuisine;
    private final String imageUrl;
    private final float rating;
    private final String address;

    public Restaurant(String name, String cuisine, String imageUrl, float rating, String address) {
        this.name = name;
        this.cuisine = cuisine;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.address = address;
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

    public String getAddress() { return address; }
} 