package com.example.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MatchedUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MatchedUsersAdapter adapter;
    private ArrayList<UserProfile> matchedUsersList;
    private DatabaseReference destinationsRef;
    private String destination;
    private TextView noMatchesTextView;
    private Button logoutButton, homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_users);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noMatchesTextView = findViewById(R.id.noMatchesTextView);
        logoutButton = findViewById(R.id.logoutButton);
        homeButton = findViewById(R.id.homeButton);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        destinationsRef = database.getReference("destinations");

        // Get the destination from the intent
        destination = getIntent().getStringExtra("destination");
        if (destination == null || destination.isEmpty()) {
            Toast.makeText(this, "Destination not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize matched users list
        matchedUsersList = new ArrayList<>();

        // Find matched users
        findMatchedUsers();

        // Button click listeners
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MatchedUsersActivity.this, RoleSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void findMatchedUsers() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String currentUid = user.getUid();
            // Fetch current user's role
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserProfile currentUserProfile = dataSnapshot.getValue(UserProfile.class);
                    if (currentUserProfile != null) {
                        String userRole = currentUserProfile.getRole();

                        // Query to get all users for the selected destination
                        Query query = destinationsRef.child(destination).child("users");
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                matchedUsersList.clear();

                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String uid = userSnapshot.getKey();

                                    // Fetch user details from the 'users' node
                                    usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot userSnapshot) {
                                            UserProfile userProfile = userSnapshot.getValue(UserProfile.class);

                                            if (userProfile != null) {
                                                String otherUserRole = userProfile.getRole();

                                                // Null check for the role
                                                if (userRole != null && otherUserRole != null) {
                                                    // Match opposite roles
                                                    if ((userRole.equals("Rider") && otherUserRole.equals("Passenger")) ||
                                                            (userRole.equals("Passenger") && otherUserRole.equals("Rider"))) {
                                                        matchedUsersList.add(userProfile);
                                                    }
                                                }

                                                // Show "No matches found" message if list is still empty
                                                if (matchedUsersList.isEmpty()) {
                                                    noMatchesTextView.setVisibility(View.VISIBLE);
                                                } else {
                                                    noMatchesTextView.setVisibility(View.GONE);
                                                }

                                                // Notify adapter only once after all data is fetched
                                                adapter = new MatchedUsersAdapter(matchedUsersList, currentUserProfile); // Pass both matched users and current user profile
                                                recyclerView.setAdapter(adapter);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Toast.makeText(MatchedUsersActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MatchedUsersActivity.this, "Failed to fetch matched users.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MatchedUsersActivity.this, "Failed to fetch user role.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut(); // Firebase sign-out
        Intent intent = new Intent(MatchedUsersActivity.this, LoginActivity.class); // Navigate to LoginActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // This function can be used for the phone and email click action to show options
    public void showContactOptionsDialog(Context context, String phoneNumber, String email) {
        // Create an AlertDialog with options: Dialer, WhatsApp, or Email
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Contact Options")
                .setItems(new String[]{"Dialer", "WhatsApp", "Email"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Dialer
                            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                            context.startActivity(dialIntent);
                            break;
                        case 1:
                            // WhatsApp
                            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                            whatsappIntent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=Hello%20I%20am%20from%20Rideshare%20can%20we%20share%20a%20ride?"));
                            context.startActivity(whatsappIntent);
                            break;
                        case 2:
                            // Email
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Rideshare Inquiry");
                            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, I am from Rideshare. Can we share a ride?");
                            context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
                            break;
                    }
                })
                .show();
    }
}
