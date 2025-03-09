package com.example.stride;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<com.example.stride.MenuItem> menuItems;
    private Map<String, List<com.example.stride.MenuItem>> restaurantMenus;
    private TextView cartCountView;
    private Cart cart;
    private String restaurantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

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
        String cuisine = getIntent().getStringExtra("restaurant_cuisine");

        TextView titleTextView = findViewById(R.id.restaurant_title);
        TextView cuisineTextView = findViewById(R.id.restaurant_cuisine);
        cartCountView = findViewById(R.id.cart_count);
        ImageButton cartButton = findViewById(R.id.cart_button);

        titleTextView.setText(restaurantName);
        cuisineTextView.setText(cuisine);
        updateCartCount();

        recyclerView = findViewById(R.id.menu_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initializeDummyMenus();
        menuItems = restaurantMenus.get(restaurantName);
        
        adapter = new MenuAdapter(menuItems, item -> onMenuItemSelected(item));
        recyclerView.setAdapter(adapter);

        cartButton.setOnClickListener(v -> {
            if (cart.getItemCount() > 0) {
                Intent intent = new Intent(this, OrderSummaryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCartCount() {
        int count = cart.getItemCount();
        cartCountView.setText(String.valueOf(count));
        cartCountView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    private void onMenuItemSelected(com.example.stride.MenuItem item) {
        if (!restaurantName.equals(cart.getRestaurantName()) && cart.getItemCount() > 0) {
            new AlertDialog.Builder(this)
                .setTitle("Clear Cart?")
                .setMessage("Adding items from " + restaurantName + " will clear your current cart from " + cart.getRestaurantName() + ". Do you want to proceed?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    cart.clearCart();
                    cart.addItem(item, restaurantName);
                    updateCartCount();
                    Toast.makeText(this, "Cart cleared and new item added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
        } else {
            cart.addItem(item, restaurantName);
            updateCartCount();
            Toast.makeText(this, "Added to cart: " + item.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeDummyMenus() {
        restaurantMenus = new HashMap<>();

        // Papa John's menu
        List<com.example.stride.MenuItem> papaJohnsMenu = new ArrayList<>();
        papaJohnsMenu.add(new com.example.stride.MenuItem("Pepperoni Pizza", "Classic pepperoni and cheese pizza", 14.99, "pepperoni_pizza"));
        papaJohnsMenu.add(new com.example.stride.MenuItem("BBQ Chicken Pizza", "Grilled chicken with BBQ sauce", 16.99, "bbq_chicken_pizza"));
        papaJohnsMenu.add(new com.example.stride.MenuItem("Garden Fresh Pizza", "Mushrooms, onions, green peppers, and tomatoes", 15.99, "garden_pizza"));
        restaurantMenus.put("Papa John's", papaJohnsMenu);

        // Dig In menu
        List<com.example.stride.MenuItem> digInMenu = new ArrayList<>();
        digInMenu.add(new com.example.stride.MenuItem("Mediterranean Bowl", "Quinoa, falafel, hummus, and veggies", 12.99, "mediterranean_bowl"));
        digInMenu.add(new com.example.stride.MenuItem("Harvest Bowl", "Sweet potatoes, brussels sprouts, wild rice", 11.99, "harvest_bowl"));
        digInMenu.add(new com.example.stride.MenuItem("Chicken & Avocado", "Grilled chicken, avocado, mixed greens", 13.99, "chicken_avocado"));
        restaurantMenus.put("Dig In", digInMenu);

        // Sushi & Co menu
        List<com.example.stride.MenuItem> sushiMenu = new ArrayList<>();
        sushiMenu.add(new com.example.stride.MenuItem("California Roll", "Crab, avocado, and cucumber", 8.99, "california_roll"));
        sushiMenu.add(new com.example.stride.MenuItem("Spicy Tuna Roll", "Fresh tuna with spicy sauce", 10.99, "spicy_tuna"));
        sushiMenu.add(new com.example.stride.MenuItem("Dragon Roll", "Eel, cucumber, avocado", 15.99, "dragon_roll"));
        restaurantMenus.put("Sushi & Co", sushiMenu);

        // Five Guys menu
        List<com.example.stride.MenuItem> fiveGuysMenu = new ArrayList<>();
        fiveGuysMenu.add(new com.example.stride.MenuItem("Hamburger", "Hand-formed patty with unlimited toppings", 8.99, "hamburger"));
        fiveGuysMenu.add(new com.example.stride.MenuItem("Cheeseburger", "Hand-formed patty with American cheese", 9.99, "cheeseburger"));
        fiveGuysMenu.add(new com.example.stride.MenuItem("Bacon Cheeseburger", "Hand-formed patty with bacon and cheese", 11.99, "bacon_cheeseburger"));
        restaurantMenus.put("Five Guys", fiveGuysMenu);
    }
} 