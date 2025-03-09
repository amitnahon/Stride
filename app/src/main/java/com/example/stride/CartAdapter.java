package com.example.stride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<Map.Entry<MenuItem, Integer>> cartItems;
    private Cart cart;
    private final OrderSummaryActivity activity;

    public CartAdapter(OrderSummaryActivity activity) {
        this.cart = Cart.getInstance();
        this.activity = activity;
        updateCartItems();
    }

    private void updateCartItems() {
        cartItems = new ArrayList<>(cart.getItems().entrySet());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<MenuItem, Integer> entry = cartItems.get(position);
        MenuItem item = entry.getKey();
        int quantity = entry.getValue();

        holder.nameView.setText(item.getName());
        holder.priceView.setText(String.format("$%.2f", item.getPrice() * quantity));
        holder.quantityView.setText(String.valueOf(quantity));

        holder.decreaseButton.setOnClickListener(v -> {
            if (quantity > 1) {
                cart.updateItemQuantity(item, quantity - 1);
            } else {
                cart.removeItem(item);
            }
            updateCartItems();
            notifyDataSetChanged();
            activity.updateTotalPrice();
        });

        holder.increaseButton.setOnClickListener(v -> {
            cart.updateItemQuantity(item, quantity + 1);
            updateCartItems();
            notifyDataSetChanged();
            activity.updateTotalPrice();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void notifyCartChanged() {
        updateCartItems();
        notifyDataSetChanged();
        activity.updateTotalPrice();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView priceView;
        TextView quantityView;
        ImageButton decreaseButton;
        ImageButton increaseButton;

        ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.cart_item_name);
            priceView = view.findViewById(R.id.cart_item_price);
            quantityView = view.findViewById(R.id.item_quantity);
            decreaseButton = view.findViewById(R.id.decrease_quantity);
            increaseButton = view.findViewById(R.id.increase_quantity);
        }
    }
} 