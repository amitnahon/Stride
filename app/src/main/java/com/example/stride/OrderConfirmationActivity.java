package com.example.stride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

public class OrderConfirmationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        TextView orderSummary = findViewById(R.id.order_summary);
        Button trackOrderButton = findViewById(R.id.track_order_button);
        Button backToHomeButton = findViewById(R.id.back_to_home_button);

        // Get order details from intent
        String restaurantName = getIntent().getStringExtra("restaurant_name");
        double total = getIntent().getDoubleExtra("total", 0.0);
        String orderId = getIntent().getStringExtra("order_id");
        orderSummary.setText("Your order from " + restaurantName + " is being prepared.\nTotal: $" + String.format("%.2f", total));

        trackOrderButton.setOnClickListener(v -> {
            Log.d("OrderConfirmation", "Track Order button clicked");
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
            // Do NOT call finish() here so user can go back
        });

        backToHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
} 