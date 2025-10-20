package com.campusride;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.Ride;
import com.campusride.models.RideRequest;
import com.campusride.models.User;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RideSearchActivity extends AppCompatActivity {

    private static final String TAG = "RideSearchActivity";
    
    private EditText destinationFilterEditText, timeFilterEditText;
    private Button searchButton;
    private RecyclerView availableRidesRecyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> allRides;
    private List<Ride> filteredRides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_search);

        initViews();
        setClickListeners();
        setupRecyclerView();
        loadRides();
    }

    private void initViews() {
        destinationFilterEditText = findViewById(R.id.destinationFilterEditText);
        timeFilterEditText = findViewById(R.id.timeFilterEditText);
        searchButton = findViewById(R.id.searchButton);
        availableRidesRecyclerView = findViewById(R.id.availableRidesRecyclerView);
    }

    private void setClickListeners() {
        searchButton.setOnClickListener(v -> searchRides());
    }

    private void setupRecyclerView() {
        allRides = new ArrayList<>();
        filteredRides = new ArrayList<>();
        rideAdapter = new RideAdapter(filteredRides, ride -> {
            // Handle ride click - show confirmation dialog and then create ride request
            showRideRequestConfirmation(ride);
        });
        availableRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        availableRidesRecyclerView.setAdapter(rideAdapter);
    }

    private void loadRides() {
        DatabaseReference ridesRef = FirebaseUtil.getDatabase().getReference("rides");
        ridesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allRides.clear();
                for (DataSnapshot rideSnapshot : dataSnapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null) {
                        allRides.add(ride);
                    }
                }
                Log.d(TAG, "Loaded " + allRides.size() + " rides from Firebase");
                // Initially show all rides
                showAllRides();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading rides", databaseError.toException());
                Toast.makeText(RideSearchActivity.this, "Failed to load rides: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchRides() {
        String destination = destinationFilterEditText.getText().toString().trim();
        String time = timeFilterEditText.getText().toString().trim();
        
        filteredRides.clear();
        
        // Filter rides based on destination and time
        for (Ride ride : allRides) {
            boolean destinationMatch = destination.isEmpty() || 
                ride.getDestination().toLowerCase().contains(destination.toLowerCase());
            boolean timeMatch = time.isEmpty() || 
                ride.getTime().toLowerCase().contains(time.toLowerCase());
            
            if (destinationMatch && timeMatch) {
                filteredRides.add(ride);
            }
        }
        
        // Sort rides - put matching destination rides first
        if (!destination.isEmpty()) {
            filteredRides.sort((r1, r2) -> {
                boolean r1Matches = r1.getDestination().toLowerCase().contains(destination.toLowerCase());
                boolean r2Matches = r2.getDestination().toLowerCase().contains(destination.toLowerCase());
                
                if (r1Matches && !r2Matches) return -1;
                if (!r1Matches && r2Matches) return 1;
                return 0;
            });
        }
        
        rideAdapter.updateRides(filteredRides);
        Log.d(TAG, "Found " + filteredRides.size() + " rides matching search criteria");
        
        if (filteredRides.isEmpty()) {
            Toast.makeText(this, "No rides found matching your criteria", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showAllRides() {
        filteredRides.clear();
        filteredRides.addAll(allRides);
        rideAdapter.updateRides(filteredRides);
    }
    
    private void showRideRequestConfirmation(Ride ride) {
        new AlertDialog.Builder(this)
                .setTitle("Request Ride")
                .setMessage("Do you want to request a ride from " + ride.getSource() + 
                           " to " + ride.getDestination() + " at " + ride.getTime() + "?")
                .setPositiveButton("Request", (dialog, which) -> createRideRequest(ride))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void createRideRequest(Ride ride) {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to request a ride", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Fetch passenger details from Firebase
        DatabaseReference passengerRef = FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid());
        passengerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User passenger = snapshot.getValue(User.class);
                    if (passenger != null) {
                        // Create a new ride request
                        String requestId = UUID.randomUUID().toString();
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        
                        RideRequest rideRequest = new RideRequest(
                                requestId,
                                ride.getRideId(),
                                currentUser.getUid(),
                                ride.getDriverId(),
                                "pending", // status
                                ride.getDriverName(),
                                ride.getDriverRegNo(),
                                "", // driverMobile - will be populated when request is accepted
                                passenger.getName() != null ? passenger.getName() : "Unknown Passenger",
                                passenger.getMobile() != null ? passenger.getMobile() : "",
                                passenger.getRegNo() != null ? passenger.getRegNo() : "",
                                ride.getSource(),
                                ride.getDestination(),
                                ride.getDate(),
                                ride.getTime()
                        );
                        
                        // Save to Firebase
                        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
                        requestsRef.child(requestId).setValue(rideRequest)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RideSearchActivity.this, "Ride request sent successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RideSearchActivity.this, "Failed to send ride request: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RideSearchActivity.this, "Failed to load passenger information", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RideSearchActivity.this, "Passenger profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RideSearchActivity.this, "Failed to load passenger information: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}