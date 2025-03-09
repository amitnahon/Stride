package com.example.stride;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private static Cart instance;
    private Map<MenuItem, Integer> items;
    private String restaurantName;

    private Cart() {
        items = new HashMap<>();
    }

    public static Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }

    public boolean hasItemsFromDifferentRestaurant(String newRestaurant) {
        return !items.isEmpty() && restaurantName != null && !restaurantName.equals(newRestaurant);
    }

    public void addItem(MenuItem item, String restaurant) {
        restaurantName = restaurant;
        items.put(item, items.getOrDefault(item, 0) + 1);
    }

    public void removeItem(MenuItem item) {
        items.remove(item);
        if (items.isEmpty()) {
            restaurantName = null;
        }
    }

    public void updateItemQuantity(MenuItem item, int quantity) {
        if (quantity <= 0) {
            removeItem(item);
        } else {
            items.put(item, quantity);
        }
    }

    public Map<MenuItem, Integer> getItems() {
        return items;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void clearCart() {
        items.clear();
        restaurantName = null;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Map.Entry<MenuItem, Integer> entry : items.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    public int getItemCount() {
        int count = 0;
        for (int quantity : items.values()) {
            count += quantity;
        }
        return count;
    }
} 