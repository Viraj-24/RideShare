package com.example.sample;

public class RideHistory {
    private String name;
    private String email;
    private String phoneNumber;
    private String source;
    private String destination;
    private String via;
    private String vehicleType;
    private String vehicleNumber;
    private String availableSeats;
    private String userUid; // User's UID to relate this ride to the current user
    private String dateTime; // Timestamp of the booking

    public RideHistory(String name, String email, String phoneNumber, String source, String destination, String via,
                       String vehicleType, String vehicleNumber, String availableSeats, String userUid, String dateTime) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.source = source;
        this.destination = destination;
        this.via = via;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.availableSeats = availableSeats;
        this.userUid = userUid;
        this.dateTime = dateTime;
    }

    // Getters and Setters
}
