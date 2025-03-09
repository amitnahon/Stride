package com.example.stride;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RestaurantSelectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private List<Restaurant> restaurantList;
    private List<Restaurant> filteredList;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_selection);

        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize restaurant data
        restaurantList = new ArrayList<>();
        restaurantList.add(new Restaurant(
            "Papa John's",
            "Pizza",
            "papa_johns",
            4.3f
        ));
        restaurantList.add(new Restaurant(
            "Dig In",
            "Healthy Food",
            "dig_in",
            4.5f
        ));
        restaurantList.add(new Restaurant(
            "Sushi & Co",
            "Japanese",
            "sushi_and_co",
            4.4f
        ));
        restaurantList.add(new Restaurant(
            "Five Guys",
            "Burgers",
            "five_guys",
            4.6f
        ));

        filteredList = new ArrayList<>(restaurantList);
        adapter = new RestaurantAdapter(filteredList, this::onRestaurantSelected);
        recyclerView.setAdapter(adapter);

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterRestaurants(s.toString());
            }
        });
    }

    private void filterRestaurants(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(restaurantList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Restaurant restaurant : restaurantList) {
                if (restaurant.getName().toLowerCase().contains(lowerQuery) ||
                    restaurant.getCuisine().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(restaurant);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void onRestaurantSelected(Restaurant restaurant) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("restaurant_name", restaurant.getName());
        intent.putExtra("restaurant_cuisine", restaurant.getCuisine());
        startActivity(intent);
    }
}
