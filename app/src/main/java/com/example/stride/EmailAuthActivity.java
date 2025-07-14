package com.example.stride;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class EmailAuthActivity extends AppCompatActivity {
    private LinearLayout loginForm, signupForm;
    private Button loginToggleButton, signupToggleButton;
    private EditText loginEmailEditText, loginPasswordEditText;
    private EditText signupEmailEditText, signupPasswordEditText;
    private Button loginButton, signupButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);

        loginForm = findViewById(R.id.login_form);
        signupForm = findViewById(R.id.signup_form);
        loginToggleButton = findViewById(R.id.login_toggle_button);
        signupToggleButton = findViewById(R.id.signup_toggle_button);
        loginEmailEditText = findViewById(R.id.login_email_edit_text);
        loginPasswordEditText = findViewById(R.id.login_password_edit_text);
        signupEmailEditText = findViewById(R.id.signup_email_edit_text);
        signupPasswordEditText = findViewById(R.id.signup_password_edit_text);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);
        mAuth = FirebaseAuth.getInstance();

        // Show login form by default
        loginForm.setVisibility(View.VISIBLE);
        signupForm.setVisibility(View.GONE);

        loginToggleButton.setOnClickListener(v -> {
            loginForm.setVisibility(View.VISIBLE);
            signupForm.setVisibility(View.GONE);
        });
        signupToggleButton.setOnClickListener(v -> {
            loginForm.setVisibility(View.GONE);
            signupForm.setVisibility(View.VISIBLE);
        });

        loginButton.setOnClickListener(v -> {
            String email = loginEmailEditText.getText().toString().trim();
            String password = loginPasswordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        finish(); // or navigate to main screen
                    } else {
                        Toast.makeText(this, "Login failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
        });

        signupButton.setOnClickListener(v -> {
            String email = signupEmailEditText.getText().toString().trim();
            String password = signupPasswordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created and logged in!", Toast.LENGTH_SHORT).show();
                        finish(); // or navigate to main screen
                    } else {
                        Toast.makeText(this, "Sign up failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
        });
    }
} 