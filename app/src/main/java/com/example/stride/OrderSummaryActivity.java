package com.example.stride;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.stride.Order;
import java.util.ArrayList;
import java.util.HashMap;
import android.os.Build;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderSummaryActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Cart cart;
    private CartAdapter cartAdapter;
    private TextView totalPriceView;
    private EditText addressInput;
    private Button fetchLocationButton;
    private ProgressBar locationProgress;
    private Spinner paymentMethodSpinner;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService geocodeExecutor = Executors.newSingleThreadExecutor();

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

        // Initialize Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Log screen view
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "OrderSummaryActivity");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "OrderSummaryActivity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        cart = Cart.getInstance();
        initializeViews();
        setupRecyclerView();
        setupPaymentSpinner();
        updateTotalPrice();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fetchLocationButton.setOnClickListener(v -> fetchLocation());
        // Optionally, fetch location automatically on open
        fetchLocation();

        Button confirmButton = findViewById(R.id.confirm_order_button);
        confirmButton.setOnClickListener(v -> confirmOrder());
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        totalPriceView = findViewById(R.id.total_price);
        addressInput = findViewById(R.id.address_input);
        fetchLocationButton = findViewById(R.id.fetch_location_button);
        locationProgress = findViewById(R.id.location_progress);
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
        totalPriceView.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        locationProgress.setVisibility(View.VISIBLE);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1)
                .build();
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this);
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        android.util.Log.d("OrderSummaryActivity", "Location received: lat=" + location.getLatitude() + ", lon=" + location.getLongitude());
                        getAddressFromLocation(location);
                    } else {
                        locationProgress.setVisibility(View.GONE);
                        addressInput.setHint(getString(R.string.unable_to_get_location));
                    }
                } else {
                    locationProgress.setVisibility(View.GONE);
                    addressInput.setHint(getString(R.string.unable_to_get_location));
                }
            }
        }, null);
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if (Build.VERSION.SDK_INT >= 33) {
            geocoder.getFromLocation(
                location.getLatitude(),
                location.getLongitude(),
                1,
                new Geocoder.GeocodeListener() {
                    @Override
                    public void onGeocode(List<Address> addresses) {
                        runOnUiThread(() -> {
                            if (!addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                StringBuilder addressStr = new StringBuilder();
                                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                    addressStr.append(address.getAddressLine(i));
                                    if (i != address.getMaxAddressLineIndex()) {
                                        addressStr.append(", ");
                                    }
                                }
                                addressInput.setText(addressStr.toString());
                            } else {
                                addressInput.setText(getString(R.string.unable_to_get_address));
                            }
                            locationProgress.setVisibility(View.GONE);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> addressInput.setHint(getString(R.string.error_fetching_address)));
                        locationProgress.setVisibility(View.GONE);
                    }
                }
            );
        } else {
            geocodeExecutor.execute(() -> {
                try {
                    @SuppressWarnings("deprecation")
                    List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1
                    );
                    runOnUiThread(() -> {
                        if (!addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            StringBuilder addressStr = new StringBuilder();
                            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                addressStr.append(address.getAddressLine(i));
                                if (i != address.getMaxAddressLineIndex()) {
                                    addressStr.append(", ");
                                }
                            }
                            addressInput.setText(addressStr.toString());
                        } else {
                            addressInput.setText(getString(R.string.unable_to_get_address));
                        }
                        locationProgress.setVisibility(View.GONE);
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> addressInput.setHint(getString(R.string.error_fetching_address)));
                    locationProgress.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                addressInput.setHint(getString(R.string.location_permission_denied));
            }
        }
    }

    private void confirmOrder() {
        String address = addressInput.getText().toString();
        if (address.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_fetch_address), Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();
        double total = cart.getTotalPrice();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, getString(R.string.user_not_signed_in), Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();
        String restaurantName = cart.getRestaurantName();
        // Prepare items for Firestore
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<com.example.stride.MenuItem, Integer> entry : cart.getItems().entrySet()) {
            com.example.stride.MenuItem menuItem = entry.getKey();
            int quantity = entry.getValue();
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", menuItem.getName());
            map.put("quantity", quantity);
            map.put("price", menuItem.getPrice());
            items.add(map);
        }

        long timestamp = System.currentTimeMillis();
        // Add status and location fields
        String status = "Order Received";
        Order order = new Order(userId, restaurantName, items, total, address, paymentMethod, timestamp, status, address);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders")
            .add(order)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, getString(R.string.order_placed), Toast.LENGTH_SHORT).show();
                // Proceed to confirmation screen
                Intent intent = new Intent(this, OrderConfirmationActivity.class);
                intent.putExtra("restaurant_name", cart.getRestaurantName());
                intent.putExtra("total", cart.getTotalPrice());
                intent.putExtra("order_id", documentReference.getId()); // Pass order ID
                cart.clearCart();
                startActivity(intent);
            })
            .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.failed_to_place_order), Toast.LENGTH_SHORT).show());
    }
}
