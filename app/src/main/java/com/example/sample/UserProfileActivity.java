package com.example.sample;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {

    private EditText nameEditText, phoneNumberEditText, vehicleNumberEditText,
            availableSeatsEditText, startTimeEditText, expectedReachTimeEditText, dateEditText;
    private AutoCompleteTextView genderEditText, vehicleTypeEditText;
    private Button saveButton;
    private FloatingActionButton uploadImageButton;
    private ImageView profileImageView;

    private Uri imageUri;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase instances
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        auth = FirebaseAuth.getInstance();
        usersRef = database.getReference("users");

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        vehicleTypeEditText = findViewById(R.id.vehicleTypeEditText);
        genderEditText = findViewById(R.id.genderEditText);
        vehicleNumberEditText = findViewById(R.id.vehicleNumberEditText);
        availableSeatsEditText = findViewById(R.id.availableSeatsEditText);
        startTimeEditText = findViewById(R.id.startTimeEditText);
        expectedReachTimeEditText = findViewById(R.id.expectedReachTimeEditText);
        dateEditText = findViewById(R.id.dateEditText);
        saveButton = findViewById(R.id.saveButton);
        profileImageView = findViewById(R.id.profileImageView);
        uploadImageButton = findViewById(R.id.uploadImageButton);

        // Setup Gender Dropdown
        String[] genders = new String[]{"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        genderEditText.setAdapter(genderAdapter);

        // Setup Vehicle Type Dropdown
        String[] vehicleTypes = new String[]{"Bike", "Car"};
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleTypes);
        vehicleTypeEditText.setAdapter(vehicleAdapter);

        // Force vehicle number input to uppercase
        vehicleNumberEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // Setup click listeners for time and date pickers
        startTimeEditText.setOnClickListener(view -> showTimePickerDialog(startTimeEditText));
        expectedReachTimeEditText.setOnClickListener(view -> showTimePickerDialog(expectedReachTimeEditText));
        dateEditText.setOnClickListener(view -> showDatePickerDialog());

        // Button click listeners
        saveButton.setOnClickListener(view -> saveUserProfile());
        uploadImageButton.setOnClickListener(v -> openGallery());
        profileImageView.setOnClickListener(v -> openGallery());

        // Load existing profile image from Firebase Database if available
        usersRef.child(auth.getUid()).child("profileImage").get().addOnSuccessListener(dataSnapshot -> {
            String url = dataSnapshot.getValue(String.class);
            if (url != null) {
                Glide.with(this).load(url).into(profileImageView);
            }
        });
    }

    // Open gallery to pick image
    private void openGallery() {
        // Check if user has entered their name before allowing image selection
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name before uploading the profile photo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1000);
    }

    // Handle image selected result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadToFirebase(imageUri);
        }
    }

    // Upload image to Firebase Storage with sanitized username + timestamp filename
    private void uploadToFirebase(Uri uri) {
        String name = nameEditText.getText().toString().trim();
        String sanitizedFileName = sanitizeFileName(name);
        long timestamp = System.currentTimeMillis();
        String fileName = sanitizedFileName + "_" + timestamp + ".jpg";

        StorageReference fileRef = storageReference.child("profileImages/" + fileName);
        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                usersRef.child(auth.getUid()).child("profileImage").setValue(downloadUri.toString());
                Toast.makeText(this, "Profile image uploaded!", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Replace spaces and special chars with underscores for safe file naming
    private String sanitizeFileName(String input) {
        return input.replaceAll("[^a-zA-Z0-9]", "_");
    }

    // Show time picker dialog and set selected time in EditText
    private void showTimePickerDialog(EditText timeEditText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            boolean isPM = selectedHour >= 12;
            int hourIn12 = selectedHour % 12 == 0 ? 12 : selectedHour % 12;
            String time = String.format("%02d:%02d %s", hourIn12, selectedMinute, isPM ? "PM" : "AM");
            timeEditText.setText(time);
        }, hour, minute, false);
        timePickerDialog.show();
    }

    // Show date picker dialog and set selected date in EditText
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            dateEditText.setText(formattedDate);
        }, year, month, day);

        // Prevent selecting past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    // Validate phone number format (10 digits)
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }

    // Save user profile info to Firebase Database
    private void saveUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String vehicleType = vehicleTypeEditText.getText().toString().trim();
        String gender = genderEditText.getText().toString().trim();
        String vehicleNumber = vehicleNumberEditText.getText().toString().trim();
        String availableSeats = availableSeatsEditText.getText().toString().trim();
        String startTime = startTimeEditText.getText().toString().trim();
        String expectedReachTime = expectedReachTimeEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        // Check required fields
        if (name.isEmpty() || phoneNumber.isEmpty() || gender.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone number
        if (!isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            HashMap<String, Object> userProfile = new HashMap<>();
            userProfile.put("name", name);
            userProfile.put("phoneNumber", phoneNumber);
            userProfile.put("gender", gender);
            userProfile.put("date", date);
            userProfile.put("vehicleType", vehicleType.isEmpty() ? null : vehicleType);
            userProfile.put("vehicleNumber", vehicleNumber.isEmpty() ? null : vehicleNumber);
            userProfile.put("availableSeats", availableSeats.isEmpty() ? null : availableSeats);
            userProfile.put("startTime", startTime.isEmpty() ? null : startTime);
            userProfile.put("expectedReachTime", expectedReachTime.isEmpty() ? null : expectedReachTime);

            // Update database
            usersRef.child(user.getUid()).updateChildren(userProfile)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                        // Start RoleSelectionActivity with profile details as extras
                        Intent intent = new Intent(UserProfileActivity.this, RoleSelectionActivity.class);
                        intent.putExtra("name", name);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("gender", gender);
                        intent.putExtra("vehicleType", vehicleType);
                        intent.putExtra("vehicleNumber", vehicleNumber);
                        intent.putExtra("availableSeats", availableSeats);
                        intent.putExtra("startTime", startTime);
                        intent.putExtra("expectedReachTime", expectedReachTime);
                        intent.putExtra("date", date);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
