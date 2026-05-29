package com.example.sample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SourceDestinationActivity extends AppCompatActivity {

    private AutoCompleteTextView sourceEditText, destinationEditText;
    private EditText etVia;
    private Button saveButton;
    private DatabaseReference usersRef, destinationRef;

    // Comprehensive list of locations for Dakshina Kannada (Taluks/Villages) and Bengaluru
    private static final String[] INDIAN_CITIES = {
            // --- Dakshina Kannada: Mangalore Taluk ---
            "Mangalore City", "Hampankatta", "Kankanady", "Bejai", "Kadri", "Pumpwell", "Thokkottu", 
            "Deralakatte", "Surathkal", "Panambur", "Bajpe", "Kottara", "Kulshekar", "Bondel", 
            "Urwa", "Mannagudda", "Shaktinagar", "Gurupura", "Vamanjoor", "Kateel", "Mulki",

            // --- Dakshina Kannada: Puttur Taluk ---
            "Puttur Town", "Darbe", "Kombettu", "Bannur", "Parladka", "Kabaka", "Muraliya", 
            "Nellyadi", "Uppinangady", "Vittla", "Keyyur", "Kumbra", "Kedila", "Mani", 
            "Ishwaramangala", "Kaniyooru", "Sampya", "Kuriya", "Aryapu",

            // --- Dakshina Kannada: Bantwal Taluk ---
            "Bantwal Town", "B.C. Road", "Farangipete", "Melkar", "Kalladka", "Panemangalore", 
            "Vittal", "Siddakatte", "Poonajalu", "Loretto", "Modankap",

            // --- Dakshina Kannada: Belthangady Taluk ---
            "Belthangady Town", "Ujire", "Dharmasthala", "Mundaje", "Kokkada", "Guruvayanakere", 
            "Venur", "Madanthyar", "Laila", "Nidle",

            // --- Dakshina Kannada: Sullia Taluk ---
            "Sullia Town", "Subramanya", "Jali", "Bellare", "Guthigar", "Aivernad", "Sampaje", 
            "Aranthodu", "Mandekolu",

            // --- Dakshina Kannada: Kadaba Taluk ---
            "Kadaba Town", "Ramakunja", "Alankaru", "Koila", "Noojibalthila",

            // --- Dakshina Kannada: Moodabidri Taluk ---
            "Moodabidri Town", "Alangar", "Puthige", "Handelu", "Belvai",

            // --- Bengaluru Locations ---
            "Majestic, Bengaluru", "Indiranagar, Bengaluru", "Koramangala, Bengaluru", "Whitefield, Bengaluru",
            "Electronic City, Bengaluru", "MG Road, Bengaluru", "Jayanagar, Bengaluru", "HSR Layout, Bengaluru",
            "Yeshwanthpur, Bengaluru", "Hebbal, Bengaluru", "Marathahalli, Bengaluru", "Bannerghatta Road, Bengaluru",
            "Malleshwaram, Bengaluru", "Kalyan Nagar, Bengaluru", "RT Nagar, Bengaluru", "BTM Layout, Bengaluru",
            "Kempegowda International Airport (BLR)", "Rajajinagar, Bengaluru", "Banashankari, Bengaluru", 
            "Silk Board, Bengaluru", "Bellandur, Bengaluru", "Yelahanka, Bengaluru", "Kengeri, Bengaluru",
            "Varthur, Bengaluru", "Sarjapur Road, Bengaluru", "Vijayanagar, Bengaluru",

            // --- Major Indian Cities ---
            "Mumbai, Maharashtra", "Delhi, NCR", "Hyderabad, Telangana", "Ahmedabad, Gujarat", 
            "Chennai, Tamil Nadu", "Kolkata, West Bengal", "Surat, Gujarat", "Pune, Maharashtra", 
            "Jaipur, Rajasthan", "Lucknow, Uttar Pradesh", "Kanpur, Uttar Pradesh", "Nagpur, Maharashtra", 
            "Indore, Madhya Pradesh", "Thane, Maharashtra", "Bhopal, Madhya Pradesh", "Visakhapatnam, AP", 
            "Patna, Bihar", "Vadodara, Gujarat", "Ludhiana, Punjab", "Coimbatore, TN", "Agra, UP", 
            "Nashik, Maharashtra", "Faridabad, Haryana", "Rajkot, Gujarat", "Varanasi, UP", 
            "Amritsar, Punjab", "Ranchi, Jharkhand", "Chandigarh", "Mysuru, Karnataka", "Hubli, Karnataka"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_destination);

        // Initialize views
        sourceEditText = findViewById(R.id.sourceEditText);
        etVia = findViewById(R.id.etVia); // "Via" field
        destinationEditText = findViewById(R.id.destinationEditText);
        saveButton = findViewById(R.id.saveButton);

        // Set up AutoComplete
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, INDIAN_CITIES);
        sourceEditText.setAdapter(adapter);
        destinationEditText.setAdapter(adapter);

        // Initialize Firebase Realtime Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        destinationRef = database.getReference("destinations");

        // Set onClick listener for the save button
        saveButton.setOnClickListener(v -> saveSourceAndDestination());
    }

    private void saveSourceAndDestination() {
        String source = sourceEditText.getText().toString().trim();
        String via = etVia.getText().toString().trim();
        String destination = destinationEditText.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (!source.isEmpty() && !destination.isEmpty() && !via.isEmpty()) {
                String uid = user.getUid();

                // Save source, destination, and via to the user's profile
                usersRef.child(uid).child("source").setValue(source);
                usersRef.child(uid).child("via").setValue(via); // Save "via"
                usersRef.child(uid).child("destination").setValue(destination);

                // Add user to the destination node with source and via details
                destinationRef.child(destination).child("users").child(uid).setValue(new UserRoute(source, destination, via, uid))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Details saved!", Toast.LENGTH_SHORT).show();
                            navigateToMatchedUsersActivity(destination);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToMatchedUsersActivity(String destination) {
        Intent intent = new Intent(SourceDestinationActivity.this, MatchedUsersActivity.class);
        intent.putExtra("destination", destination);
        startActivity(intent);
        finish();
    }
}
