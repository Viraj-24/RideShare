package com.example.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnLogin);
        registerLink = findViewById(R.id.register_link);
        progressBar = findViewById(R.id.progress_bar);

        // Check if the user is already logged in, if yes, redirect to UserProfileActivity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, redirect to the user profile page
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
            finish(); // Close the login activity
        }

        // Set up click listener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Set up click listener for the register link
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate user input
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        // Show progress bar while logging in
        progressBar.setVisibility(View.VISIBLE);

        // Sign in with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hide progress bar after login attempt
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(MainActivity.this, "Login Successful! Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();

                            // Get the FCM token and store it in Firebase Realtime Database
                            getFCMTokenAndStore(user);

                            // Redirect to UserProfileActivity after successful login
                            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                            startActivity(intent);
                            finish(); // Close the current activity (login activity)
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to retrieve user information.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle login failure
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed.";
                        Toast.makeText(MainActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getFCMTokenAndStore(FirebaseUser user) {
        // Get FCM token for the current user
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get the FCM token
                    String token = task.getResult();
                    Log.d("FCM", "FCM Token: " + token);

                    // Store the token in Firebase Realtime Database
                    if (user != null) {
                        String userId = user.getUid();
                        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                        databaseRef.child("fcmToken").setValue(token)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d("FCM", "FCM token stored successfully");
                                    } else {
                                        Log.w("FCM", "Error storing FCM token", task1.getException());
                                    }
                                });
                    }
                });
    }
}
