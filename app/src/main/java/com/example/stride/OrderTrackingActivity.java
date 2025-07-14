package com.example.stride;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;
import javax.annotation.Nullable;
import android.location.Geocoder;
import android.location.Address;
import android.graphics.Color;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.Locale;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.widget.Toast;
import android.os.Build;

public class OrderTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private int statusStep = 1;
    private final String[] statuses = {"Order Received", "Preparing", "Out for Delivery", "Delivered"};
    private int currentStatusIndex = 0;
    private Handler statusHandler;
    private Runnable statusUpdater;
    private DocumentReference orderRef;
    private TextView statusView;
    private ProgressBar progressBar;
    private Handler handler;
    private MapView mapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private ListenerRegistration orderListener;
    private String orderId;
    private GoogleMap googleMapInstance;
    private String directionsApiKey = "AIzaSyAc0o4v3zdLUfQYAs1Q2I70ZH36mNVW9hw";
    private LatLng restaurantLatLng;
    private LatLng deliveryLatLng;
    private String restaurantAddress;
    private String deliveryAddress;
    private boolean mapReady = false;
    private boolean dataReady = false;
    private boolean startedGeocode = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("OrderTracking", "OrderTrackingActivity started");
        setContentView(R.layout.activity_order_tracking);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        statusView = findViewById(R.id.tracking_status);
        progressBar = findViewById(R.id.tracking_progress);
        Button backToHomeButton = findViewById(R.id.back_to_home_button);
        mapView = findViewById(R.id.order_tracking_map);

        // MapView setup
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // Get order ID from intent
        orderId = getIntent().getStringExtra("order_id");
        android.util.Log.d("OrderTracking", "orderId from intent: " + orderId);
        if (orderId != null) {
            listenToOrderUpdates(orderId);
            setupAutoStatusAdvance(orderId);
        } else {
            statusView.setText("Order ID not found");
        }
        // Remove mock status logic
        // handler = new Handler(Looper.getMainLooper());
        // updateStatus();
        // autoAdvanceStatus();

        backToHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to previous screen
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void listenToOrderUpdates(String orderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference orderRef = db.collection("orders").document(orderId);
        orderListener = orderRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    statusView.setText("Error loading order status");
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String status = snapshot.getString("status");
                    statusView.setText("Order Status: " + (status != null ? status : "Unknown"));
                    progressBar.setProgress(getStatusStep(status));
                    // Fetch addresses and geocode only once
                    if (!startedGeocode) {
                        startedGeocode = true;
                        String restaurantName = snapshot.getString("restaurantName");
                        String deliveryAddr = snapshot.getString("address");
                        fetchAddressesAndGeocode(restaurantName, deliveryAddr);
                    }
                } else {
                    statusView.setText("Order not found");
                }
            }
        });
    }

    private void setupAutoStatusAdvance(String orderId) {
        orderRef = FirebaseFirestore.getInstance().collection("orders").document(orderId);
        statusHandler = new Handler(Looper.getMainLooper());
        statusUpdater = new Runnable() {
            @Override
            public void run() {
                orderRef.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String status = snapshot.getString("status");
                        int idx = getStatusStep(status);
                        if (idx < statuses.length - 1) {
                            String nextStatus = statuses[idx + 1];
                            orderRef.update("status", nextStatus);
                            statusHandler.postDelayed(this, 3000); // 3 seconds between steps
                        }
                    }
                });
            }
        };
        // Start advancing status if not delivered
        orderRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String status = snapshot.getString("status");
                int idx = getStatusStep(status);
                if (idx < statuses.length - 1) {
                    statusHandler.postDelayed(statusUpdater, 3000);
                }
            }
        });
    }

    private int getStatusStep(String status) {
        if (status == null) return 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(status)) return i;
        }
        return 0;
    }

    private void fetchAddressesAndGeocode(String restaurantName, String deliveryAddress) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants").whereEqualTo("name", restaurantName).get().addOnSuccessListener(query -> {
            if (!query.isEmpty()) {
                String restAddr = query.getDocuments().get(0).getString("address");
                this.restaurantAddress = restAddr;
                this.deliveryAddress = deliveryAddress;
                geocodeAddresses();
            }
        });
    }

    private void geocodeAddresses() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if (Build.VERSION.SDK_INT >= 33) {
            // Geocode restaurant address
            geocoder.getFromLocationName(restaurantAddress, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> restResults) {
                    if (restResults == null || restResults.isEmpty()) {
                        android.util.Log.e("OrderTracking", "Restaurant address not found: " + restaurantAddress);
                        runOnUiThread(() -> Toast.makeText(OrderTrackingActivity.this, "Restaurant address not found!", Toast.LENGTH_LONG).show());
                        return;
                    }
                    LatLng restLatLng = new LatLng(restResults.get(0).getLatitude(), restResults.get(0).getLongitude());
                    // Geocode delivery address
                    geocoder.getFromLocationName(deliveryAddress, 1, new Geocoder.GeocodeListener() {
                        @Override
                        public void onGeocode(@NonNull List<Address> delivResults) {
                            if (delivResults == null || delivResults.isEmpty()) {
                                android.util.Log.e("OrderTracking", "Delivery address not found: " + deliveryAddress);
                                runOnUiThread(() -> Toast.makeText(OrderTrackingActivity.this, "Delivery address not found!", Toast.LENGTH_LONG).show());
                                return;
                            }
                            LatLng delivLatLng = new LatLng(delivResults.get(0).getLatitude(), delivResults.get(0).getLongitude());
                            android.util.Log.d("OrderTracking", "Restaurant LatLng: " + restLatLng + ", Delivery LatLng: " + delivLatLng);
                            restaurantLatLng = restLatLng;
                            deliveryLatLng = delivLatLng;
                            dataReady = true;
                            tryShowRoute();
                        }
                        @Override
                        public void onError(@NonNull String errorMessage) {
                            android.util.Log.e("OrderTracking", "Delivery geocode error: " + errorMessage);
                            runOnUiThread(() -> Toast.makeText(OrderTrackingActivity.this, "Delivery geocode error!", Toast.LENGTH_LONG).show());
                        }
                    });
                }
                @Override
                public void onError(@NonNull String errorMessage) {
                    android.util.Log.e("OrderTracking", "Restaurant geocode error: " + errorMessage);
                    runOnUiThread(() -> Toast.makeText(OrderTrackingActivity.this, "Restaurant geocode error!", Toast.LENGTH_LONG).show());
                }
            });
        } else {
            executor.execute(() -> {
                try {
                    @SuppressWarnings("deprecation")
                    List<Address> restResults = geocoder.getFromLocationName(restaurantAddress, 1);
                    if (restResults == null || restResults.isEmpty()) {
                        mainHandler.post(() -> Toast.makeText(OrderTrackingActivity.this, "Restaurant address not found!", Toast.LENGTH_LONG).show());
                        return;
                    }
                    LatLng restLatLng = new LatLng(restResults.get(0).getLatitude(), restResults.get(0).getLongitude());
                    @SuppressWarnings("deprecation")
                    List<Address> delivResults = geocoder.getFromLocationName(deliveryAddress, 1);
                    if (delivResults == null || delivResults.isEmpty()) {
                        mainHandler.post(() -> Toast.makeText(OrderTrackingActivity.this, "Delivery address not found!", Toast.LENGTH_LONG).show());
                        return;
                    }
                    LatLng delivLatLng = new LatLng(delivResults.get(0).getLatitude(), delivResults.get(0).getLongitude());
                    android.util.Log.d("OrderTracking", "Restaurant LatLng: " + restLatLng + ", Delivery LatLng: " + delivLatLng);
                    restaurantLatLng = restLatLng;
                    deliveryLatLng = delivLatLng;
                    dataReady = true;
                    mainHandler.post(this::tryShowRoute);
                } catch (IOException e) {
                    mainHandler.post(() -> Toast.makeText(OrderTrackingActivity.this, "Geocoding error!", Toast.LENGTH_LONG).show());
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMapInstance = googleMap;
        mapReady = true;
        tryShowRoute();
    }

    // MapView lifecycle
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        if (orderListener != null) orderListener.remove();
        if (statusHandler != null && statusUpdater != null) statusHandler.removeCallbacks(statusUpdater);
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    private void fetchAndDrawRoute(LatLng origin, LatLng dest) {
        executor.execute(() -> {
            List<LatLng> polyline = null;
            String urlStr = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                origin.latitude, origin.longitude, dest.latitude, dest.longitude, directionsApiKey);
            android.util.Log.d("OrderTracking", "Directions API URL: " + urlStr);
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[1024];
                int n;
                while ((n = reader.read(buf)) > 0) sb.append(buf, 0, n);
                reader.close();
                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                    JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");
                    polyline = new java.util.ArrayList<>();
                    for (int i = 0; i < steps.length(); i++) {
                        String polylineStr = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                        polyline.addAll(decodePolyline(polylineStr));
                    }
                } else {
                    android.util.Log.e("OrderTracking", "No route found by Directions API");
                }
            } catch (Exception e) {
                android.util.Log.e("OrderTracking", "Directions API error: " + e.getMessage());
            }
            List<LatLng> finalPolyline = polyline;
            mainHandler.post(() -> {
                if (finalPolyline != null && googleMapInstance != null && !finalPolyline.isEmpty()) {
                    PolylineOptions options = new PolylineOptions().addAll(finalPolyline).color(Color.BLUE).width(8f);
                    googleMapInstance.addPolyline(options);
                } else {
                    Toast.makeText(OrderTrackingActivity.this, "No route found!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // Replace new FetchRouteTask().execute(...) with:
    // fetchAndDrawRoute(restaurantLatLng, deliveryLatLng);
    private void tryShowRoute() {
        if (mapReady && dataReady && restaurantLatLng != null && deliveryLatLng != null) {
            runOnUiThread(() -> {
                googleMapInstance.clear();
                googleMapInstance.addMarker(new MarkerOptions().position(restaurantLatLng).title("Restaurant"));
                googleMapInstance.addMarker(new MarkerOptions().position(deliveryLatLng).title("Delivery Location"));
                googleMapInstance.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantLatLng, 12f));
                fetchAndDrawRoute(restaurantLatLng, deliveryLatLng);
            });
        }
    }

    // Polyline decoder
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new java.util.ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng(lat / 1E5, lng / 1E5);
            poly.add(p);
        }
        return poly;
    }
} 