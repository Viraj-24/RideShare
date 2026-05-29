package com.example.sample;
public class UserProfile {
    private String uid;
    private String name;
    private String phoneNumber;
    private String gender;
    private String vehicleType;
    private String vehicleNumber;
    private String role;
    private String email;  // Email field added
    private String source; // Source field added
    private String destination; // Destination field added
    private String via; // Via field added
    private String date; // Date field added
    private String availableSeats; // New field for available seats
    private String startTime; // New field for start time
    private String expectedReachTime; // New field for expected reach time

    // Default constructor required for Firebase deserialization
    public UserProfile() {
    }

    // Constructor with all fields (including email, source, destination, via, date, availableSeats, startTime, and expectedReachTime)
    public UserProfile(String uid, String name, String phoneNumber, String gender,
                       String vehicleType, String vehicleNumber, String role, String email,
                       String source, String destination, String via, String date,
                       String availableSeats, String startTime, String expectedReachTime) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.role = role;
        this.email = email;  // Assign email
        this.source = source;  // Assign source
        this.destination = destination;  // Assign destination
        this.via = via;  // Assign via
        this.date = date;  // Assign date
        this.availableSeats = availableSeats;  // Assign availableSeats
        this.startTime = startTime;  // Assign startTime
        this.expectedReachTime = expectedReachTime;  // Assign expectedReachTime
    }

    // Getter and setter methods for all fields

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(String availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getExpectedReachTime() {
        return expectedReachTime;
    }

    public void setExpectedReachTime(String expectedReachTime) {
        this.expectedReachTime = expectedReachTime;
    }
}
