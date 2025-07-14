package com.example.stride;

import java.util.List;
import java.util.Map;

public class Order {
    private String userId;
    private String restaurantName;
    private List<Map<String, Object>> items;
    private double total;
    private String address;
    private String paymentMethod;
    private long timestamp;
    private String status;
    private String location;

    public Order() {}

    public Order(String userId, String restaurantName, List<Map<String, Object>> items, double total, String address, String paymentMethod, long timestamp, String status, String location) {
        this.userId = userId;
        this.restaurantName = restaurantName;
        this.items = items;
        this.total = total;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
        this.status = status;
        this.location = location;
    }

    public String getUserId() { return userId; }
    public String getRestaurantName() { return restaurantName; }
    public List<Map<String, Object>> getItems() { return items; }
    public double getTotal() { return total; }
    public String getAddress() { return address; }
    public String getPaymentMethod() { return paymentMethod; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
    public void setTotal(double total) { this.total = total; }
    public void setAddress(String address) { this.address = address; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
} 