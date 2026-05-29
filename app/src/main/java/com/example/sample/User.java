package com.example.sample;

public class User {
    private String uid;
    private String name; // Corresponds to "usernameTextView" in the adapter
    private String role;
    private String email;
    private String destination;

    public User() {
        // Default constructor for Firebase
    }

    public User(String uid, String name, String role, String email) {
        this.uid = uid;
        this.name = name;
        this.role = role;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() { // Corresponds to usernameTextView
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
