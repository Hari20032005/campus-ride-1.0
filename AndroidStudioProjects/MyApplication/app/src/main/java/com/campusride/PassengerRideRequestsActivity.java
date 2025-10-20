package com.campusride;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.RideRequest;
import com.campusride.utils.FirebaseUtil;
import com.campusride.OldPassengerRideRequestAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PassengerRideRequestsActivity extends AppCompatActivity {

    private static final String TAG = "PassengerRequests";
    
    private RecyclerView passengerRequestsRecyclerView;
    private OldPassengerRideRequestAdapter requestAdapter;
    private List<RideRequest> passengerRequests;
    private List<String> previousRequestStatuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_ride_requests);

        initViews();
        setupRecyclerView();
        loadPassengerRideRequests();
    }

    private void initViews() {
        passengerRequestsRecyclerView = findViewById(R.id.passengerRequestsRecyclerView);
    }

    private void setupRecyclerView() {
        passengerRequests = new ArrayList<>();
        previousRequestStatuses = new ArrayList<>();
        requestAdapter = new OldPassengerRideRequestAdapter(passengerRequests);
        passengerRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        passengerRequestsRecyclerView.setAdapter(requestAdapter);
    }
    
    private void loadPassengerRideRequests() {
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
                // Store previous statuses to detect changes
                List<String> currentRequestStatuses = new ArrayList<>();
                List<RideRequest> currentRequests = new ArrayList<>();
                
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    RideRequest request = requestSnapshot.getValue(RideRequest.class);
                    if (request != null && currentUser.getUid().equals(request.getPassengerId())) {
                        // Only show requests made by the current user
                        currentRequests.add(request);
                        currentRequestStatuses.add(request.getRequestId() + ":" + request.getStatus());
                    }
                }
                
                // Check for status changes and notify user
                checkForStatusChanges(currentRequests, currentRequestStatuses);
                
                // Update the UI
                passengerRequests.clear();
                passengerRequests.addAll(currentRequests);
                previousRequestStatuses.clear();
                previousRequestStatuses.addAll(currentRequestStatuses);
                requestAdapter.updateRequests(passengerRequests);
                
                Log.d(TAG, "Loaded " + passengerRequests.size() + " requests for current passenger from Firebase");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading passenger ride requests", databaseError.toException());
                Toast.makeText(PassengerRideRequestsActivity.this, "Failed to load ride requests: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void checkForStatusChanges(List<RideRequest> currentRequests, List<String> currentRequestStatuses) {
        // Check if this is the first load
        if (previousRequestStatuses.isEmpty()) {
            return;
        }
        
        // Check for status changes
        for (int i = 0; i < currentRequestStatuses.size(); i++) {
            String currentStatus = currentRequestStatuses.get(i);
            boolean found = false;
            
            for (int j = 0; j < previousRequestStatuses.size(); j++) {
                String previousStatus = previousRequestStatuses.get(j);
                
                // Check if this is the same request
                if (currentStatus.startsWith(previousStatus.split(":")[0])) {
                    found = true;
                    
                    // Check if status has changed
                    if (!currentStatus.equals(previousStatus)) {
                        // Status has changed, notify user
                        String[] parts = currentStatus.split(":");
                        String requestId = parts[0];
                        String status = parts[1];
                        
                        // Find the request to get more details
                        RideRequest request = null;
                        for (RideRequest r : currentRequests) {
                            if (r.getRequestId().equals(requestId)) {
                                request = r;
                                break;
                            }
                        }
                        
                        if (request != null) {
                            String message = "Your ride request to " + request.getPassengerName() + " has been " + status;
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                }
            }
        }
    }
}