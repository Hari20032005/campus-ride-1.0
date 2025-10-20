package com.campusride;

import android.os.Bundle;
import android.util.Log;
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

public class RideRequestsActivity extends AppCompatActivity {

    private static final String TAG = "RideRequestsActivity";
    
    private RecyclerView requestsRecyclerView;
    private RideRequestAdapter requestAdapter;
    private List<RideRequest> allRequests;
    private List<RideRequest> pendingRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_requests);

        initViews();
        setupRecyclerView();
        loadRideRequests();
    }

    private void initViews() {
        requestsRecyclerView = findViewById(R.id.requestsRecyclerView);
    }

    private void setupRecyclerView() {
        allRequests = new ArrayList<>();
        pendingRequests = new ArrayList<>();
        requestAdapter = new RideRequestAdapter(pendingRequests, new RideRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptRequest(RideRequest request) {
                updateRequestStatus(request, "accepted");
            }

            @Override
            public void onRejectRequest(RideRequest request) {
                updateRequestStatus(request, "rejected");
            }

            @Override
            public void onCompleteRide(RideRequest request) {
                showCompleteRideDialog(request);
            }
        });
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setAdapter(requestAdapter);
    }
    
    private void loadRideRequests() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view ride requests", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allRequests.clear();
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    RideRequest request = requestSnapshot.getValue(RideRequest.class);
                    if (request != null && currentUser.getUid().equals(request.getDriverId())) {
                        // Only show requests for rides created by the current user
                        if ("pending".equals(request.getStatus()) || "accepted".equals(request.getStatus())) {
                            allRequests.add(request);
                        }
                    }
                }
                Log.d(TAG, "Loaded " + allRequests.size() + " requests for current driver from Firebase");
                showPendingRequests();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading ride requests", databaseError.toException());
                Toast.makeText(RideRequestsActivity.this, "Failed to load ride requests: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showPendingRequests() {
        pendingRequests.clear();
        pendingRequests.addAll(allRequests);
        requestAdapter.updateRequests(pendingRequests);
    }
    
    private void updateRequestStatus(RideRequest request, String status) {
        if ("accepted".equals(status)) {
            // When accepting a request, fetch the driver's mobile number and update the request
            FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
            if (currentUser != null) {
                DatabaseReference driverRef = FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid());
                driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User driver = snapshot.getValue(User.class);
                            if (driver != null) {
                                // Update the request with driver's mobile number
                                request.setDriverMobile(driver.getMobile() != null ? driver.getMobile() : "");
                                request.setStatus(status);
                                
                                DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
                                requestsRef.child(request.getRequestId()).setValue(request)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RideRequestsActivity.this, 
                                                    "Request accepted. Passenger will be notified.", Toast.LENGTH_SHORT).show();
                                                // Don't remove from list immediately, let user complete the ride
                                            } else {
                                                Toast.makeText(RideRequestsActivity.this, 
                                                    "Failed to update request: " + task.getException().getMessage(), 
                                                    Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(RideRequestsActivity.this, 
                            "Failed to load driver information: " + error.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // For rejected requests, just update the status
            request.setStatus(status);
            DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
            requestsRef.child(request.getRequestId()).setValue(request)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RideRequestsActivity.this, 
                                "Request " + status, Toast.LENGTH_SHORT).show();
                            // Remove the request from the list if it's been accepted or rejected
                            if (!"pending".equals(status)) {
                                pendingRequests.remove(request);
                                requestAdapter.updateRequests(pendingRequests);
                            }
                        } else {
                            Toast.makeText(RideRequestsActivity.this, 
                                "Failed to update request: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    
    private void showCompleteRideDialog(RideRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Complete Ride")
                .setMessage("Are you sure you want to mark this ride as completed?")
                .setPositiveButton("Complete", (dialog, which) -> completeRide(request))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void completeRide(RideRequest request) {
        // Update the ride request status to indicate completion
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("ride_requests");
        requestsRef.child(request.getRequestId()).child("status").setValue("completed")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Also update the ride status to completed
                        completeRideInDatabase(request);
                    } else {
                        Toast.makeText(RideRequestsActivity.this, 
                            "Failed to update request: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void completeRideInDatabase(RideRequest request) {
        DatabaseReference ridesRef = FirebaseUtil.getDatabase().getReference("rides");
        ridesRef.child(request.getRideId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Ride ride = dataSnapshot.getValue(Ride.class);
                    if (ride != null) {
                        // Mark ride as completed
                        ride.setStatus("completed");
                        ride.setCompletedAt(System.currentTimeMillis());
                        
                        // Update ride in database
                        ridesRef.child(request.getRideId()).setValue(ride)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RideRequestsActivity.this, 
                                            "Ride completed successfully!", Toast.LENGTH_SHORT).show();
                                        
                                        // Remove the request from the list
                                        pendingRequests.remove(request);
                                        requestAdapter.updateRequests(pendingRequests);
                                        
                                        // TODO: Update user ride counts in profiles
                                    } else {
                                        Toast.makeText(RideRequestsActivity.this, 
                                            "Failed to complete ride: " + task.getException().getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RideRequestsActivity.this, 
                    "Failed to load ride information: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}