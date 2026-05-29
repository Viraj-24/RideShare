package com.example.sample;

public class UserRoute {
    private String source;
    private String destination;
    private String via;
    private String userId;

    public UserRoute() {}

    public UserRoute(String source, String destination, String via, String userId) {
        this.source = source;
        this.destination = destination;
        this.via = via;
        this.userId = userId;
    }

    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getVia() { return via; }
    public String getUserId() { return userId; }
}
