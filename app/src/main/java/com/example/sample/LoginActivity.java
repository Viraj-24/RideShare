package com.example.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
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

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, RoleSelectionActivity.class));
            finish();
        }

        // Set up click listener for the login button
        loginButton.setOnClickListener(view -> loginUser());

        // Set up click listener for the register link
        registerLink.setOnClickListener(view -> {
            // Redirect to RegisterActivity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

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

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Get the current user ID
                        String userId = mAuth.getCurrentUser().getUid();
                        String userNodePath = "users/" + userId;  // Specify path in the existing "users" node

                        // Obtain FCM token
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                            if (!tokenTask.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Get the FCM token
                            String token = tokenTask.getResult();

                            // Log the path and token for debugging
                            System.out.println("Saving token to path: " + userNodePath);
                            System.out.println("FCM Token: " + token);

                            // Save the FCM token to Firebase Realtime Database under the existing 'users' node
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                            database.child(userNodePath).child("fcmToken").setValue(token)
                                    .addOnCompleteListener(saveTokenTask -> {
                                        if (saveTokenTask.isSuccessful()) {
                                            // Redirect to RoleSelectionActivity after successful login
                                            Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // If token save fails, show an error message
                                            Toast.makeText(LoginActivity.this, "Failed to save token", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        });
                    } else {
                        // Show login failure message
                        Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
