package com.example.stride.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("restaurants/{id}/menu")
    Call<MenuResponse> getMenuItems(@Path("id") String restaurantId);
} 