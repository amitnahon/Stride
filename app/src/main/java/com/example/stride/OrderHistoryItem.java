package com.example.stride;

public class OrderHistoryItem {
    private String restaurantName;
    private String dateTime;
    private double total;
    private String status;
    private String address;

    public OrderHistoryItem(String restaurantName, String dateTime, double total, String status, String address) {
        this.restaurantName = restaurantName;
        this.dateTime = dateTime;
        this.total = total;
        this.status = status;
        this.address = address;
    }

    public String getRestaurantName() { return restaurantName; }
    public String getDateTime() { return dateTime; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getAddress() { return address; }
} 