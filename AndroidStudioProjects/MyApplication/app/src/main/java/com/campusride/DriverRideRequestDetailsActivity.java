package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.campusride.models.PassengerRideRequest;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DriverRideRequestDetailsActivity extends AppCompatActivity {

    private static final String TAG = "DriverRideRequestDetails";
    
    private TextView passengerNameTextView, passengerMobileTextView, passengerRegNoTextView,
                    sourceTextView, destinationTextView, dateTextView, timeTextView, statusTextView;
    private Button completeRideButton, navigateToSourceButton, navigateToDestinationButton;
    
    private String requestId;
    private PassengerRideRequest currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_ride_request_details);
        
        requestId = getIntent().getStringExtra("REQUEST_ID");
        if (requestId == null || requestId.isEmpty()) {
            Toast.makeText(this, "Invalid ride request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadRideRequestDetails();
        setClickListeners();
    }
    
    private void initViews() {
        passengerNameTextView = findViewById(R.id.passengerNameTextView);
        passengerMobileTextView = findViewById(R.id.passengerMobileTextView);
        passengerRegNoTextView = findViewById(R.id.passengerRegNoTextView);
        sourceTextView = findViewById(R.id.sourceTextView);
        destinationTextView = findViewById(R.id.destinationTextView);
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        completeRideButton = findViewById(R.id.completeRideButton);
        navigateToSourceButton = findViewById(R.id.navigateToSourceButton);
        navigateToDestinationButton = findViewById(R.id.navigateToDestinationButton);
    }
    
    private void setClickListeners() {
        completeRideButton.setOnClickListener(v -> showCompleteRideDialog());
        navigateToSourceButton.setOnClickListener(v -> navigateToSource());
        navigateToDestinationButton.setOnClickListener(v -> navigateToDestination());
    }
    
    /**
     * Navigates to the source (pickup) location using Google Maps
     */
    private void navigateToSource() {
        if (currentRequest == null) {
            Toast.makeText(this, "Ride request details not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Create Google Maps navigation intent to source location
            String uri = "google.navigation:q=" + currentRequest.getSourceLat() + "," + currentRequest.getSourceLng();
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            
            // Check if Google Maps is installed
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(this, "Navigating to pickup location (" + currentRequest.getSource() + ")", Toast.LENGTH_SHORT).show();
            } else {
                // If Google Maps is not installed, open in browser
                String webUri = "https://www.google.com/maps/dir/?api=1&destination=" + 
                               currentRequest.getSourceLat() + "," + currentRequest.getSourceLng();
                Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri));
                startActivity(webIntent);
                Toast.makeText(this, "Opened navigation in browser to pickup location", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening navigation to pickup: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Navigates to the destination location using Google Maps
     */
    private void navigateToDestination() {
        if (currentRequest == null) {
            Toast.makeText(this, "Ride request details not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Create Google Maps navigation intent to destination location
            String uri = "google.navigation:q=" + currentRequest.getDestinationLat() + "," + currentRequest.getDestinationLng();
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            
            // Check if Google Maps is installed
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(this, "Navigating to destination (" + currentRequest.getDestination() + ")", Toast.LENGTH_SHORT).show();
            } else {
                // If Google Maps is not installed, open in browser
                String webUri = "https://www.google.com/maps/dir/?api=1&destination=" + 
                               currentRequest.getDestinationLat() + "," + currentRequest.getDestinationLng();
                Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri));
                startActivity(webIntent);
                Toast.makeText(this, "Opened navigation in browser to destination", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening navigation to destination: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadRideRequestDetails() {
        DatabaseReference requestRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests").child(requestId);
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentRequest = dataSnapshot.getValue(PassengerRideRequest.class);
                    if (currentRequest != null) {
                        updateUI();
                    } else {
                        Toast.makeText(DriverRideRequestDetailsActivity.this, "Failed to load ride request details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DriverRideRequestDetailsActivity.this, "Ride request not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading ride request details", databaseError.toException());
                Toast.makeText(DriverRideRequestDetailsActivity.this, "Failed to load ride request details: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI() {
        passengerNameTextView.setText(currentRequest.getPassengerName());
        passengerMobileTextView.setText(currentRequest.getPassengerMobile());
        passengerRegNoTextView.setText(currentRequest.getPassengerRegNo());
        sourceTextView.setText(currentRequest.getSource());
        destinationTextView.setText(currentRequest.getDestination());
        dateTextView.setText(currentRequest.getDate());
        timeTextView.setText(currentRequest.getTime());
        
        // Set status with appropriate color
        statusTextView.setText(currentRequest.getStatus());
        switch (currentRequest.getStatus()) {
            case "accepted":
                statusTextView.setTextColor(getResources().getColor(R.color.green_500));
                completeRideButton.setEnabled(true);
                // Enable navigation buttons for accepted rides
                navigateToSourceButton.setEnabled(true);
                navigateToDestinationButton.setEnabled(true);
                break;
            case "completed":
                statusTextView.setTextColor(getResources().getColor(R.color.green_500));
                completeRideButton.setEnabled(false);
                completeRideButton.setText("Ride Completed");
                // Disable navigation buttons for completed rides
                navigateToSourceButton.setEnabled(false);
                navigateToDestinationButton.setEnabled(false);
                break;
            default:
                statusTextView.setTextColor(getResources().getColor(R.color.orange_500));
                completeRideButton.setEnabled(false);
                // Disable navigation buttons for other statuses
                navigateToSourceButton.setEnabled(false);
                navigateToDestinationButton.setEnabled(false);
                break;
        }
    }
    
    private void showCompleteRideDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Complete Ride");
        builder.setMessage("Are you sure you want to mark this ride as completed?");
        builder.setPositiveButton("Complete", (dialog, which) -> completeRide());
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void completeRide() {
        if (currentRequest == null) {
            return;
        }
        
        currentRequest.setStatus("completed");
        currentRequest.setCompletedAt(System.currentTimeMillis());
        
        DatabaseReference requestRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests").child(requestId);
        requestRef.setValue(currentRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DriverRideRequestDetailsActivity.this, "Ride completed successfully!", Toast.LENGTH_SHORT).show();
                        completeRideButton.setEnabled(false);
                        completeRideButton.setText("Ride Completed");
                    } else {
                        Toast.makeText(DriverRideRequestDetailsActivity.this, "Failed to complete ride: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
}