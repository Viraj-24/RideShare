package com.example.sample;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        // Get the current FirebaseAuth instance
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Check if the user is authenticated before trying to save the token
        if (mAuth.getCurrentUser() != null) {
            // User is authenticated, proceed to save the token
            saveFcmTokenToDatabase(token);
        } else {
            // User is not authenticated, store token temporarily
            storeTokenLocally(token);
        }
    }

    private void saveFcmTokenToDatabase(String token) {
        // Ensure the user is authenticated and has a UID
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid(); // Get the current user's ID

        // Reference the existing 'users' node and use the user's ID as the key
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Save the FCM token under the existing user's node
        userRef.child("fcmToken").setValue(token); // Save the FCM token under the 'users/{userId}/fcmToken' node
    }

    private void storeTokenLocally(String token) {
        // Save the token temporarily in SharedPreferences for later use
        SharedPreferences sharedPreferences = getSharedPreferences("appPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fcmToken", token);
        editor.apply();
    }

    // Method to sync the token when the user logs in
    public static void syncTokenWhenLoggedIn(String token) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.child("fcmToken").setValue(token);
        }
    }
}
