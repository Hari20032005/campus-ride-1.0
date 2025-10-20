package com.campusride;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.Ride;
import com.campusride.models.RideRequest;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RideHistoryActivity extends AppCompatActivity {

    private static final String TAG = "RideHistoryActivity";
    
    private RecyclerView rideHistoryRecyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> rideHistory;
    private ValueEventListener driverRidesListener;
    private ValueEventListener passengerRidesListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
        
        initViews();
        setupRecyclerView();
        loadRideHistory();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (driverRidesListener != null) {
            DatabaseReference ridesRef = FirebaseUtil.getDatabase().getReference("rides");
            ridesRef.removeEventListener(driverRidesListener);
        }
        if (passengerRidesListener != null) {
            DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
            requestsRef.removeEventListener(passengerRidesListener);
        }
    }
    
    private void initViews() {
        rideHistoryRecyclerView = findViewById(R.id.rideHistoryRecyclerView);
    }
    
    private void setupRecyclerView() {
        rideHistory = new ArrayList<>();
        rideAdapter = new RideAdapter(rideHistory, ride -> {
            // Handle ride click if needed
        });
        rideHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideHistoryRecyclerView.setAdapter(rideAdapter);
    }
    
    private void loadRideHistory() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view ride history", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        loadDriverRides(currentUser);
        loadPassengerRides(currentUser);
    }
    
    private void loadDriverRides(FirebaseUser currentUser) {
        DatabaseReference ridesRef = FirebaseUtil.getDatabase().getReference("rides");
        driverRidesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear existing driver rides but keep passenger rides
                List<Ride> updatedRideHistory = new ArrayList<>();
                // First, keep all passenger rides (those not driven by current user)
                for (Ride ride : rideHistory) {
                    if (!currentUser.getUid().equals(ride.getDriverId())) {
                        updatedRideHistory.add(ride);
                    }
                }
                
                // Add completed rides as driver
                for (DataSnapshot rideSnapshot : dataSnapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null && "completed".equals(ride.getStatus()) && 
                        currentUser.getUid().equals(ride.getDriverId())) {
                        updatedRideHistory.add(ride);
                    }
                }
                
                // Update the list and sort by completion time (newest first)
                rideHistory.clear();
                rideHistory.addAll(updatedRideHistory);
                rideHistory.sort((r1, r2) -> Long.compare(r2.getCompletedAt(), r1.getCompletedAt()));
                rideAdapter.updateRides(rideHistory);
                Log.d(TAG, "Loaded " + rideHistory.size() + " completed rides");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading driver rides", databaseError.toException());
                Toast.makeText(RideHistoryActivity.this, "Failed to load driver rides: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };
        
        ridesRef.orderByChild("driverId").equalTo(currentUser.getUid())
                .addValueEventListener(driverRidesListener);
    }
    
    private void loadPassengerRides(FirebaseUser currentUser) {
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
        passengerRidesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Process all accepted requests for this passenger
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    RideRequest request = requestSnapshot.getValue(RideRequest.class);
                    if (request != null && "accepted".equals(request.getStatus()) && 
                        currentUser.getUid().equals(request.getPassengerId())) {
                        // Load the ride details for this accepted request
                        loadRideForRequest(request);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading passenger rides", databaseError.toException());
                Toast.makeText(RideHistoryActivity.this, "Failed to load passenger rides: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };
        
        requestsRef.orderByChild("passengerId").equalTo(currentUser.getUid())
                .addValueEventListener(passengerRidesListener);
    }
    
    private void loadRideForRequest(RideRequest request) {
        DatabaseReference rideRef = FirebaseUtil.getDatabase().getReference("rides").child(request.getRideId());
        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Ride ride = dataSnapshot.getValue(Ride.class);
                    if (ride != null && "completed".equals(ride.getStatus())) {
                        // Check if this ride is not already in the list
                        boolean exists = false;
                        for (Ride existingRide : rideHistory) {
                            if (existingRide.getRideId().equals(ride.getRideId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            rideHistory.add(ride);
                            // Sort by completion time (newest first)
                            rideHistory.sort((r1, r2) -> Long.compare(r2.getCompletedAt(), r1.getCompletedAt()));
                            rideAdapter.updateRides(rideHistory);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading ride for request", databaseError.toException());
            }
        });
    }
}