package com.example.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MatchedUsersAdapter extends RecyclerView.Adapter<MatchedUsersAdapter.MatchedUserViewHolder> {

    private List<UserProfile> matchedUsersList;
    private UserProfile currentUser;
    private DatabaseReference databaseReference;

    public MatchedUsersAdapter(List<UserProfile> matchedUsersList, UserProfile currentUser) {
        this.matchedUsersList = matchedUsersList;
        this.currentUser = currentUser;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public MatchedUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matched_user, parent, false);
        return new MatchedUserViewHolder(view);
    }



    @Override
    public void onBindViewHolder(MatchedUserViewHolder holder, int position) {

        UserProfile userProfile = matchedUsersList.get(position);
        holder.nameTextView.setText(userProfile.getName());
        holder.emailTextView.setText(userProfile.getEmail());
        holder.phoneTextView.setText(userProfile.getPhoneNumber());
        holder.sourceTextView.setText("Source: " + userProfile.getSource());
        holder.destinationTextView.setText("Destination: " + userProfile.getDestination());
        holder.viaTextView.setText("Via: " + userProfile.getVia());

        if (userProfile.getVehicleType() != null && !userProfile.getVehicleType().isEmpty()) {
            holder.vehicleTypeTextView.setText("Vehicle Type: " + userProfile.getVehicleType());
            holder.vehicleTypeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.vehicleTypeTextView.setVisibility(View.GONE);
        }

        if (userProfile.getVehicleNumber() != null && !userProfile.getVehicleNumber().isEmpty()) {
            holder.vehicleNumberTextView.setText("Vehicle Number: " + userProfile.getVehicleNumber());
            holder.vehicleNumberTextView.setVisibility(View.VISIBLE);
        } else {
            holder.vehicleNumberTextView.setVisibility(View.GONE);
        }

        if (userProfile.getAvailableSeats() != null && !userProfile.getAvailableSeats().isEmpty()) {
            holder.availableSeatsTextView.setText("Available Seats: " + userProfile.getAvailableSeats());
            holder.availableSeatsTextView.setVisibility(View.VISIBLE);
        } else {
            holder.availableSeatsTextView.setVisibility(View.GONE);
        }
        if (userProfile.getStartTime() != null && !userProfile.getStartTime().isEmpty()) {
            holder.startTimeTextView.setText("Start Time: " + userProfile.getStartTime());
            holder.startTimeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.startTimeTextView.setVisibility(View.GONE);
        }

        if (userProfile.getExpectedReachTime() != null && !userProfile.getExpectedReachTime().isEmpty()) {
            holder.expectedReachTimeTextView.setText("Expected Reach Time: " + userProfile.getExpectedReachTime());
            holder.expectedReachTimeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.expectedReachTimeTextView.setVisibility(View.GONE);
        }
        if (userProfile.getDate() != null && !userProfile.getDate().isEmpty()) {
            holder.DateTextView.setText("Date: " + userProfile.getDate());
            holder.DateTextView.setVisibility(View.VISIBLE);
        } else {
            holder.DateTextView.setVisibility(View.GONE);
        }



        holder.bookRideButton.setOnClickListener(v -> {
            if (userProfile.getAvailableSeats() != null && Integer.parseInt(userProfile.getAvailableSeats()) > 0) {
                int updatedSeats = Integer.parseInt(userProfile.getAvailableSeats()) - 1;
                userProfile.setAvailableSeats(String.valueOf(updatedSeats));

                databaseReference.child(userProfile.getUid()).child("availableSeats").setValue(String.valueOf(updatedSeats))
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (updatedSeats == 0) {
                                    // Remove from list and Firebase
                                    matchedUsersList.remove(position);
                                    notifyItemRemoved(position);
                                    databaseReference.child(userProfile.getUid()).removeValue()
                                            .addOnCompleteListener(removeTask -> {
                                                if (removeTask.isSuccessful()) {
                                                    Toast.makeText(v.getContext(), "Ride fully booked. Removed from list.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(v.getContext(), "Failed to remove entry from Firebase.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    notifyItemChanged(position);
                                    Toast.makeText(v.getContext(), "Ride Booked!!!.", Toast.LENGTH_SHORT).show();
                                }

                                saveToHistory(userProfile, v.getContext());
                                String message = createRideMessage();
                                if ("Bike".equalsIgnoreCase(userProfile.getVehicleType())) {
                                    message += "\n\nSafety Tip: Please wear a helmet!!!";
                                } else if ("Car".equalsIgnoreCase(userProfile.getVehicleType())) {
                                    message += "\n\nSafety Tip: Please wear a seatbelt!!!";
                                }
                                sendMessageToWhatsapp(userProfile.getPhoneNumber(), message, v.getContext());
                            } else {
                                Toast.makeText(v.getContext(), "Failed to update available seats.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(v.getContext(), "No available seats left.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.phoneTextView.setOnClickListener(v -> {
            String phoneNumber = userProfile.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                showContactOptionsDialog(v.getContext(), phoneNumber, userProfile.getEmail());
            }
        });

        holder.emailTextView.setOnClickListener(v -> {
            String email = userProfile.getEmail();
            if (email != null && !email.isEmpty()) {
                showContactOptionsDialog(v.getContext(), userProfile.getPhoneNumber(), email);
            }
        });
    }

    @Override
    public int getItemCount() {
        return matchedUsersList.size();
    }

    private void showContactOptionsDialog(Context context, String phoneNumber, String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Contact Options")
                .setItems(new String[]{"Dialer", "WhatsApp", "Email"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                            context.startActivity(dialIntent);
                            break;
                        case 1:
                            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                            whatsappIntent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode("Hello, I am from Rideshare. Can we share a ride?")));
                            context.startActivity(whatsappIntent);
                            break;
                        case 2:
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Rideshare Inquiry");
                            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, I am from Rideshare. Can we share a ride?");
                            context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
                            break;
                    }
                })
                .show();
    }

    private String createRideMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Hello this from Rideshare \n Ride Request:\n");

        if (currentUser.getName() != null && !currentUser.getName().isEmpty()) {
            messageBuilder.append("Name: ").append(currentUser.getName()).append("\n");
        }
        if (currentUser.getSource() != null && !currentUser.getSource().isEmpty()) {
            messageBuilder.append("Source: ").append(currentUser.getSource()).append("\n");
        }
        if (currentUser.getDestination() != null && !currentUser.getDestination().isEmpty()) {
            messageBuilder.append("Destination: ").append(currentUser.getDestination()).append("\n");
        }

        if (currentUser.getDate() != null && !currentUser.getDate().isEmpty()) {
            messageBuilder.append("Date: ").append(currentUser.getDate()).append("\n");
        }

        return messageBuilder.toString();
    }

    private void sendMessageToWhatsapp(String phoneNumber, String message, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message)));
        context.startActivity(intent);
    }

    private void saveToHistory(UserProfile userProfile, Context context) {
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("history");
        String rideId = historyRef.push().getKey();

        if (rideId != null) {
            historyRef.child(rideId).setValue(userProfile)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Ride history saved successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to save ride history.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    static class MatchedUserViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, emailTextView, phoneTextView, sourceTextView, destinationTextView, viaTextView, vehicleTypeTextView, vehicleNumberTextView, availableSeatsTextView,startTimeTextView, expectedReachTimeTextView,DateTextView;
        Button bookRideButton;

        public MatchedUserViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            viaTextView = itemView.findViewById(R.id.viaTextView);
            vehicleTypeTextView = itemView.findViewById(R.id.vehicleTypeTextView);
            vehicleNumberTextView = itemView.findViewById(R.id.vehicleNumberTextView);
            availableSeatsTextView = itemView.findViewById(R.id.availableSeatsTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);  // Initialize
            expectedReachTimeTextView = itemView.findViewById(R.id.expectedReachTimeTextView);
            DateTextView = itemView.findViewById(R.id.dateTextView);
            bookRideButton = itemView.findViewById(R.id.bookRideButton);
        }
    }
}
