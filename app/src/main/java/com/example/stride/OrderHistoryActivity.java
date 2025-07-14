package com.example.stride;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.TextView;
import java.util.Map;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;
    private List<Order> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.order_history_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter(orderList, this);
        recyclerView.setAdapter(adapter);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        fetchOrders();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            Log.e("OrderHistory", "User not signed in");
            return;
        }
        String userId = user.getUid();
        Log.d("OrderHistory", "Current userId: " + userId);
        Log.d("OrderHistory", "About to query Firestore");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d("OrderHistory", "Fetched " + queryDocumentSnapshots.size() + " orders");
                orderList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Log.d("OrderHistory", "Order doc id: " + doc.getId() + ", data: " + doc.getData());
                    Order order = doc.toObject(Order.class);
                    orderList.add(order);
                }
                adapter.notifyDataSetChanged();
                if (orderList.isEmpty()) {
                    Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e("OrderHistory", "Failed to fetch orders", e);
                Toast.makeText(this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
            });
        Log.d("OrderHistory", "Query launched");
    }

    // Adapter for displaying order history
    private static class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantNameView, addressView, itemsView, totalView;
        public OrderHistoryViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            restaurantNameView = itemView.findViewById(R.id.order_history_restaurant_name);
            addressView = itemView.findViewById(R.id.order_history_address);
            itemsView = itemView.findViewById(R.id.order_history_items);
            totalView = itemView.findViewById(R.id.order_history_total);
        }
    }

    private static class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryViewHolder> {
        private final List<Order> orders;
        private final android.content.Context context;
        public OrderHistoryAdapter(List<Order> orders, android.content.Context context) {
            this.orders = orders;
            this.context = context;
        }
        @NonNull
        @Override
        public OrderHistoryViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
            return new OrderHistoryViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.restaurantNameView.setText(order.getRestaurantName());
            holder.addressView.setText(order.getAddress());
            holder.totalView.setText(String.format(java.util.Locale.getDefault(), "$%.2f", order.getTotal()));
            // Build items summary string
            StringBuilder itemsSummary = new StringBuilder();
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                int quantity = ((Number) item.get("quantity")).intValue();
                if (itemsSummary.length() > 0) itemsSummary.append(", ");
                itemsSummary.append(quantity).append("x ").append(name);
            }
            holder.itemsView.setText(itemsSummary.toString());
            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context, OrderDetailsActivity.class);
                intent.putExtra("timestamp", order.getTimestamp());
                intent.putExtra("total", order.getTotal());
                intent.putExtra("address", order.getAddress());
                intent.putExtra("paymentMethod", order.getPaymentMethod());
                intent.putExtra("items", new java.io.Serializable() {
                    public Object writeReplace() {
                        return order.getItems();
                    }
                });
                context.startActivity(intent);
            });
        }
        @Override
        public int getItemCount() { return orders.size(); }
    }
} 