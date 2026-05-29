package com.example.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoleSelectionActivity extends AppCompatActivity {

    private Button riderButton, passengerButton;
    private DatabaseReference usersRef, destinationsRef;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        destinationsRef = database.getReference("destinations");

        // Initialize UI
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnMenu = findViewById(R.id.btnMenu);
        riderButton = findViewById(R.id.riderButton);
        passengerButton = findViewById(R.id.passengerButton);

        // Hamburger Menu click
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Navigation Menu item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(RoleSelectionActivity.this, UserProfileActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Update Nav Header with user info
        updateNavHeader();

        // Set button click listeners
        riderButton.setOnClickListener(v -> saveUserRole("Rider"));
        passengerButton.setOnClickListener(v -> saveUserRole("Passenger"));
    }

    private void updateNavHeader() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView userNameTv = headerView.findViewById(R.id.nav_user_name);
            TextView userStatusTv = headerView.findViewById(R.id.nav_user_status);

            usersRef.child(user.getUid()).get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        userNameTv.setText(name);
                        userStatusTv.setText("Welcome back!");
                    } else {
                        userNameTv.setText("Guest User");
                        userStatusTv.setText("Please complete your profile");
                    }
                } else {
                    userNameTv.setText("New User");
                    userStatusTv.setText("Please complete your profile");
                }
            });
        }
    }

    private void saveUserRole(String role) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            // Since we skipped profile, these might be null. 
            // We just update the role for now.
            usersRef.child(uid).child("role").setValue(role)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, role + " selected successfully!", Toast.LENGTH_SHORT).show();
                        navigateToSourceDestination(role);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSourceDestination(String role) {
        Intent intent = new Intent(RoleSelectionActivity.this, SourceDestinationActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }
}
