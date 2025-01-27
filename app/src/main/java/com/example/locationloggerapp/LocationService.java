package com.example.locationloggerapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends android.app.Service {

    private static final long INTERVAL = 10000; // 10 seconds
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final String CHANNEL_ID = "LocationLoggerChannel";

    // Firebase Database reference
    private DatabaseReference mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("locations");

        // Create the notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Logger Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Create a notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Logger")
                .setContentText("Logging location in the background...")
                .setSmallIcon(R.drawable.icon) // Use your own icon here
                .build();

        // Start the service in the foreground
        startForeground(1, notification);

        // Initialize LocationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Initialize the LocationListener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Log the location
                Log.d("LocationService", "Location: " + location.getLatitude() + ", " + location.getLongitude());

                // Send the location to Firebase
                sendLocationToFirebase(location);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("LocationService", "Provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("LocationService", "Provider disabled: " + provider);
            }
        };

        // Start requesting location updates
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e("LocationService", "Permission not granted");
                return;
            }

            // Request location updates
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    INTERVAL,   // 10 seconds
                    0,          // No minimum distance
                    locationListener);
        } catch (SecurityException e) {
            Log.e("LocationService", "Permission not granted: " + e.getMessage());
        }
    }

    private void sendLocationToFirebase(Location location) {
        // Log the coordinates to verify they're correct
        Log.d("LocationService", "Sending location to Firebase: " + location.getLatitude() + ", " + location.getLongitude());

        // Create a Location object with latitude and longitude
        String locationId = mDatabase.push().getKey();  // Generate a unique ID for the location
        LocationData locationData = new LocationData(location.getLatitude(), location.getLongitude());

        // Log the data that will be sent to Firebase
        Log.d("LocationService", "Location ID: " + locationId);
        Log.d("LocationService", "Location data: " + locationData.getLatitude() + ", " + locationData.getLongitude());

        // Send the location data to Firebase
        if (locationId != null) {
            mDatabase.child(locationId).setValue(locationData)
                    .addOnSuccessListener(aVoid -> Log.d("LocationService", "Location saved to Firebase"))
                    .addOnFailureListener(e -> Log.e("LocationService", "Failed to save location: " + e.getMessage()));
        } else {
            Log.e("LocationService", "Location ID is null. Could not push data to Firebase.");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove location updates when the service is destroyed
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't need binding, so return null
    }

    // Location data model for Firebase
    public static class LocationData {
        private double latitude;
        private double longitude;

        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
