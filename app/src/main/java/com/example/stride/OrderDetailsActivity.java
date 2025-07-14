package com.example.stride;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.widget.Button;

public class OrderDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        TextView dateView = findViewById(R.id.order_details_date);
        TextView totalView = findViewById(R.id.order_details_total);
        TextView addressView = findViewById(R.id.order_details_address);
        TextView paymentView = findViewById(R.id.order_details_payment);
        LinearLayout itemsLayout = findViewById(R.id.order_details_items_layout);

        // Get order data from intent
        Bundle extras = getIntent().getExtras();
        if (extras == null) return;
        long timestamp = extras.getLong("timestamp");
        double total = extras.getDouble("total");
        String address = extras.getString("address");
        String payment = extras.getString("paymentMethod");
        @SuppressWarnings({"deprecation", "unchecked"})
        List<Map<String, Object>> items = (List<Map<String, Object>>) extras.getSerializable("items");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        dateView.setText(sdf.format(new Date(timestamp)));
        totalView.setText(String.format(Locale.getDefault(), "$%.2f", total));
        addressView.setText(address);
        paymentView.setText(payment);

        // Display items
        if (items != null) {
            for (Map<String, Object> item : items) {
                String name = (String) item.get("name");
                long quantity = (long) item.get("quantity");
                double price = (double) item.get("price");
                TextView itemView = new TextView(this);
                itemView.setText(String.format(Locale.getDefault(), "%s x%d - $%.2f", name, quantity, price));
                itemsLayout.addView(itemView);
            }
        }
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