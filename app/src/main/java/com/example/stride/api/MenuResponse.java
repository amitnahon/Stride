package com.example.stride.api;

import java.util.List;
import com.example.stride.MenuItem;

public class MenuResponse {
    private List<MenuItem> items;
    private String status;
    private String message;

    public List<MenuItem> getItems() {
        return items;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
} 