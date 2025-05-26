package com.example.stride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<com.example.stride.MenuItem> menuItems;
    private TextView cartCountView;
    private Cart cart;
    private String restaurantName;
    private FirebaseFirestore db;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Initialize Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Log screen view
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MenuActivity");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MenuActivity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Add back press handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        cart = Cart.getInstance();
        restaurantName = getIntent().getStringExtra("restaurant_name");

        // Initialize UI components
        recyclerView = findViewById(R.id.menu_recycler_view);
        cartCountView = findViewById(R.id.cart_count);
        ImageButton cartButton = findViewById(R.id.cart_button);

        // Set up RecyclerView
        menuItems = new ArrayList<>();
        adapter = new MenuAdapter(menuItems, item -> onMenuItemSelected(item));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up cart button
        cartButton.setOnClickListener(v -> {
            if (cart.getItems().isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, OrderSummaryActivity.class);
            intent.putExtra("restaurant_name", restaurantName);
            startActivity(intent);
        });

        // Load menu items from Firestore
        loadMenuItems();
    }

    private void loadMenuItems() {
        Toast.makeText(this, "Loading menu for: " + restaurantName, Toast.LENGTH_SHORT).show();
        Log.d("MenuActivity", "Loading menu for restaurant: " + restaurantName);
        
        // First, let's check if we can get any documents at all
        db.collection("menuitems")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d("MenuActivity", "Total documents in collection: " + queryDocumentSnapshots.size());
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Log.d("MenuActivity", "Document ID: " + doc.getId() + 
                        ", restaurantId: " + doc.getString("restaurantId") +
                        ", name: " + doc.getString("name"));
                }
            })
            .addOnFailureListener(e -> {
                Log.e("MenuActivity", "Error getting all documents", e);
            });

        // Now get the filtered results
        Log.d("MenuActivity", "Querying for restaurantId: " + restaurantName);
        db.collection("menuitems")
            .whereEqualTo("restaurantId", restaurantName)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                menuItems.clear();
                Log.d("MenuActivity", "Found " + queryDocumentSnapshots.size() + " items for " + restaurantName);
                Toast.makeText(this, "Found " + queryDocumentSnapshots.size() + " items", Toast.LENGTH_SHORT).show();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Log.d("MenuActivity", "Processing document: " + document.getId());
                    FirestoreMenuItem firestoreItem = document.toObject(FirestoreMenuItem.class);
                    Log.d("MenuActivity", "Item details - name: " + firestoreItem.getName() + 
                        ", restaurantId: " + firestoreItem.getRestaurantId() +
                        ", price: " + firestoreItem.getPrice());
                    
                    com.example.stride.MenuItem menuItem = new com.example.stride.MenuItem(
                        firestoreItem.getName(),
                        firestoreItem.getDescription(),
                        firestoreItem.getPrice(),
                        firestoreItem.getImageResourceName()
                    );
                    menuItems.add(menuItem);
                }
                Log.d("MenuActivity", "Total items in menuItems list: " + menuItems.size());
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Log.e("MenuActivity", "Error loading menu", e);
                Toast.makeText(this, "Error loading menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void onMenuItemSelected(com.example.stride.MenuItem item) {
        cart.addItem(item, restaurantName);
        updateCartCount();
        Toast.makeText(this, item.getName() + " added to cart", Toast.LENGTH_SHORT).show();

        // Log menu item selection
        Bundle itemBundle = new Bundle();
        itemBundle.putString("item_name", item.getName());
        itemBundle.putString("restaurant_name", restaurantName);
        itemBundle.putDouble("item_price", item.getPrice());
        mFirebaseAnalytics.logEvent("menu_item_selected", itemBundle);
    }

    private void updateCartCount() {
        int count = cart.getItemCount();
        cartCountView.setText(String.valueOf(count));
        cartCountView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

        // Log cart update
        Bundle cartBundle = new Bundle();
        cartBundle.putInt("cart_count", count);
        cartBundle.putString("restaurant_name", restaurantName);
        mFirebaseAnalytics.logEvent("cart_updated", cartBundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 