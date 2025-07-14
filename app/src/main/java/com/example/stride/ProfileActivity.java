package com.example.stride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView nameView = findViewById(R.id.profile_name);
        TextView emailView = findViewById(R.id.profile_email);
        Button orderHistoryButton = findViewById(R.id.view_order_history_button);
        Button logoutButton = findViewById(R.id.logout_button);
        Button backToMainButton = findViewById(R.id.back_to_main_button);

        // Get current user from FirebaseAuth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            nameView.setText(user.getDisplayName() != null ? user.getDisplayName() : "No Name");
            emailView.setText(user.getEmail() != null ? user.getEmail() : "No Email");
        } else {
            nameView.setText("Not signed in");
            emailView.setText("");
        }

        orderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();
            // Sign out from Google
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build());
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                // Go back to LoginActivity
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });

        backToMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
} 