package com.example.stride;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Profile button
        ImageButton profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        // Test Crash button
        Button crashButton = findViewById(R.id.crash_button);
        crashButton.setOnClickListener(v -> {
            FirebaseCrashlytics.getInstance().log("Test crash button clicked");
            throw new RuntimeException(getString(R.string.test_crash));
        });

        // Initialize empty lists
        restaurantList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new RestaurantAdapter(filteredList, this::onRestaurantSelected);
        recyclerView.setAdapter(adapter);

        // Load restaurants from Firestore
        loadRestaurantsFromFirestore(restaurantList, filteredList, adapter);

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterRestaurants(s.toString(), restaurantList, filteredList, adapter);
            }
        });
    }

    private void loadRestaurantsFromFirestore(List<Restaurant> restaurantList, List<Restaurant> filteredList, RestaurantAdapter adapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                restaurantList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String name = doc.getString("name");
                    String cuisine = doc.getString("cuisine");
                    String imageUrl = doc.getString("imageUrl");
                    Double ratingDouble = doc.getDouble("rating");
                    float rating = ratingDouble != null ? ratingDouble.floatValue() : 0f;
                    String address = doc.getString("address");
                    // Skip if required fields are missing
                    if (name == null || cuisine == null || imageUrl == null) {
                        continue;
                    }
                    Restaurant restaurant = new Restaurant(name, cuisine, imageUrl, rating, address);
                    restaurantList.add(restaurant);
                }
                filteredList.clear();
                filteredList.addAll(restaurantList);
                // Full list refresh: notifyDataSetChanged is required because the entire dataset may change from Firestore
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.failed_to_load_restaurants), Toast.LENGTH_SHORT).show());
    }

    private void filterRestaurants(String query, List<Restaurant> restaurantList, List<Restaurant> filteredList, RestaurantAdapter adapter) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(restaurantList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Restaurant restaurant : restaurantList) {
                String rName = restaurant.getName();
                String rCuisine = restaurant.getCuisine();
                if (rName != null && rCuisine != null &&
                    (rName.toLowerCase().contains(lowerQuery) ||
                     rCuisine.toLowerCase().contains(lowerQuery))) {
                    filteredList.add(restaurant);
                }
            }
        }
        // Full list refresh: notifyDataSetChanged is required because the filtered dataset may change completely
        adapter.notifyDataSetChanged();
    }

    private void onRestaurantSelected(Restaurant restaurant) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("restaurant_name", restaurant.getName());
        intent.putExtra("restaurant_cuisine", restaurant.getCuisine());
        startActivity(intent);
    }
}
