package com.example.locationloggerapp;

public class LocationData {
    private double latitude;
    private double longitude;

    // Constructor
    public LocationData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter methods
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
