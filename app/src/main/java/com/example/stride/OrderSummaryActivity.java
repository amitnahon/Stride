package com.example.stride;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Map;

public class OrderSummaryActivity extends AppCompatActivity {
    private Cart cart;
    private CartAdapter cartAdapter;
    private TextView totalPriceView;
    private EditText buildingInput;
    private Spinner paymentMethodSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

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
        initializeViews();
        setupRecyclerView();
        setupPaymentSpinner();
        updateTotalPrice();

        Button confirmButton = findViewById(R.id.confirm_order_button);
        confirmButton.setOnClickListener(v -> confirmOrder());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        totalPriceView = findViewById(R.id.total_price);
        buildingInput = findViewById(R.id.building_room_input);
        paymentMethodSpinner = findViewById(R.id.payment_method_spinner);
    }

    private void setupRecyclerView() {
        RecyclerView cartRecyclerView = findViewById(R.id.cart_recycler_view);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this);
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupPaymentSpinner() {
        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, paymentMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(adapter);
    }

    public void updateTotalPrice() {
        double total = cart.getTotalPrice();
        totalPriceView.setText(String.format("Total: $%.2f", total));
    }

    private void confirmOrder() {
        String building = buildingInput.getText().toString();
        if (building.isEmpty()) {
            Toast.makeText(this, "Please enter building and room number", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();
        // Here you would typically send the order to a backend server
        Toast.makeText(this, "Order confirmed! Payment method: " + paymentMethod, Toast.LENGTH_LONG).show();
        cart.clearCart();

        // Navigate back to RestaurantSelectionActivity and clear activity stack
        Intent intent = new Intent(this, RestaurantSelectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
