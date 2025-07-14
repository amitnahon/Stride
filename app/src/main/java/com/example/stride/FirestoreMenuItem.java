package com.example.stride;

public class FirestoreMenuItem {
    private String name;
    private String description;
    private double price;
    private String imageResourceName;
    private String restaurantId;
    private String category;

    // Empty constructor needed for Firestore
    public FirestoreMenuItem() {}

    public FirestoreMenuItem(String name, String description, double price, String imageResourceName, String restaurantId, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResourceName = imageResourceName;
        this.restaurantId = restaurantId;
        this.category = category;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageResourceName() { return imageResourceName; }
    public void setImageResourceName(String imageResourceName) { this.imageResourceName = imageResourceName; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
} 