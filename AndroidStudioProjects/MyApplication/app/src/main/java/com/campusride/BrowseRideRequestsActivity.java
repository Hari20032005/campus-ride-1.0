package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.PassengerRideRequest;
import com.campusride.models.User;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BrowseRideRequestsActivity extends AppCompatActivity {

    private static final String TAG = "BrowseRideRequests";
    
    private RecyclerView rideRequestsRecyclerView;
    private BrowseRideRequestsAdapter rideRequestAdapter;
    private List<PassengerRideRequest> allRequests;
    private List<PassengerRideRequest> filteredRequests;
    private EditText destinationFilterEditText, timeFilterEditText;
    private Button searchButton, clearFiltersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_ride_requests);

        initViews();
        setupRecyclerView();
        setClickListeners();
        loadAllRideRequests();
    }

    private void initViews() {
        rideRequestsRecyclerView = findViewById(R.id.rideRequestsRecyclerView);
        destinationFilterEditText = findViewById(R.id.destinationFilterEditText);
        timeFilterEditText = findViewById(R.id.timeFilterEditText);
        searchButton = findViewById(R.id.searchButton);
        clearFiltersButton = findViewById(R.id.clearFiltersButton);
    }

    private void setupRecyclerView() {
        allRequests = new ArrayList<>();
        filteredRequests = new ArrayList<>();
        rideRequestAdapter = new BrowseRideRequestsAdapter(filteredRequests, new BrowseRideRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptRequest(PassengerRideRequest request) {
                showAcceptConfirmation(request);
            }
            
            @Override
            public void onViewDetails(PassengerRideRequest request) {
                viewRideRequestDetails(request);
            }
            
            @Override
            public void onViewOnMap(PassengerRideRequest request) {
                viewRideOnMap(request);
            }
            
            @Override
            public void onNavigate(PassengerRideRequest request) {
                navigateToRide(request);
            }
        });
        rideRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideRequestsRecyclerView.setAdapter(rideRequestAdapter);
    }
    
    private void setClickListeners() {
        searchButton.setOnClickListener(v -> filterRideRequests());
        
        clearFiltersButton.setOnClickListener(v -> {
            destinationFilterEditText.setText("");
            timeFilterEditText.setText("");
            showAllRideRequests();
        });
        
        // Add text watcher for real-time filtering
        destinationFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRideRequests();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        timeFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRideRequests();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadAllRideRequests() {
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
        Log.d(TAG, "Attempting to load all ride requests");
        Log.d(TAG, "Database reference: " + requestsRef.toString());
        
        // Listen for all requests, then filter on client side
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Data loaded successfully, count: " + dataSnapshot.getChildrenCount());
                allRequests.clear();
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    PassengerRideRequest request = requestSnapshot.getValue(PassengerRideRequest.class);
                    if (request != null) {
                        // Only show pending requests or requests accepted by current driver
                        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
                        if (currentUser != null) {
                            if ("pending".equals(request.getStatus())) {
                                // Show all pending requests
                                allRequests.add(request);
                                Log.d(TAG, "Added pending request: " + request.getRequestId() + " from " + request.getPassengerName());
                            } else if ("accepted".equals(request.getStatus()) && 
                                      currentUser.getUid().equals(request.getDriverId())) {
                                // Show requests accepted by current driver
                                allRequests.add(request);
                                Log.d(TAG, "Added accepted request (by current driver): " + request.getRequestId());
                            }
                        }
                    }
                }
                // Sort by creation time (newest first)
                allRequests.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                
                // Initially show all requests
                showAllRideRequests();
                
                Log.d(TAG, "Loaded " + allRequests.size() + " ride requests (pending + accepted by current driver)");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading ride requests", databaseError.toException());
                String errorMessage = databaseError.getMessage();
                if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                    errorMessage = "Permission denied. Please check Firebase security rules.";
                }
                Toast.makeText(BrowseRideRequestsActivity.this, "Failed to load ride requests: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void showAllRideRequests() {
        filteredRequests.clear();
        filteredRequests.addAll(allRequests);
        rideRequestAdapter.updateRequests(filteredRequests);
    }
    
    private void filterRideRequests() {
        String destinationFilter = destinationFilterEditText.getText().toString().trim().toLowerCase();
        String timeFilter = timeFilterEditText.getText().toString().trim().toLowerCase();
        
        filteredRequests.clear();
        
        // Filter requests based on destination and time
        for (PassengerRideRequest request : allRequests) {
            boolean destinationMatch = destinationFilter.isEmpty() || 
                request.getDestination().toLowerCase().contains(destinationFilter);
            boolean timeMatch = timeFilter.isEmpty() || 
                request.getTime().toLowerCase().contains(timeFilter);
            
            if (destinationMatch && timeMatch) {
                filteredRequests.add(request);
            }
        }
        
        // Sort filtered requests - put matching destination requests first
        if (!destinationFilter.isEmpty()) {
            filteredRequests.sort((r1, r2) -> {
                boolean r1Matches = r1.getDestination().toLowerCase().contains(destinationFilter);
                boolean r2Matches = r2.getDestination().toLowerCase().contains(destinationFilter);
                
                if (r1Matches && !r2Matches) return -1;
                if (!r1Matches && r2Matches) return 1;
                return 0;
            });
        }
        
        rideRequestAdapter.updateRequests(filteredRequests);
        Log.d(TAG, "Found " + filteredRequests.size() + " requests matching search criteria");
        
        if (filteredRequests.isEmpty()) {
            Toast.makeText(this, "No ride requests found matching your criteria", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showAcceptConfirmation(PassengerRideRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Ride Request")
                .setMessage("Do you want to accept this ride request from " + request.getPassengerName() + 
                           " from " + request.getSource() + " to " + request.getDestination() + "?")
                .setPositiveButton("Accept", (dialog, which) -> acceptRideRequest(request))
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void acceptRideRequest(PassengerRideRequest request) {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to accept ride requests", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if this request is already accepted by this driver
        if ("accepted".equals(request.getStatus()) && currentUser.getUid().equals(request.getDriverId())) {
            // Request is already accepted by this driver, just show details
            viewRideRequestDetails(request);
            return;
        }
        
        // Check if this request is already accepted by another driver
        if ("accepted".equals(request.getStatus())) {
            // Request is already accepted by another driver
            Toast.makeText(this, "This ride request has already been accepted by another driver", Toast.LENGTH_SHORT).show();
            // Refresh the list to remove this request
            allRequests.remove(request);
            filterRideRequests();
            return;
        }
        
        // Fetch driver details from Firebase
        DatabaseReference userRef = FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User driver = snapshot.getValue(User.class);
                    if (driver != null) {
                        // Update the ride request with driver information
                        request.setStatus("accepted");
                        request.setDriverId(currentUser.getUid());
                        request.setDriverName(driver.getName() != null ? driver.getName() : "Unknown Driver");
                        request.setDriverMobile(driver.getMobile() != null ? driver.getMobile() : "");
                        request.setAcceptedAt(System.currentTimeMillis());
                        
                        // Save to Firebase
                        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
                        requestsRef.child(request.getRequestId()).setValue(request)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(BrowseRideRequestsActivity.this, 
                                            "Ride request accepted successfully! Contact details shared with passenger.", Toast.LENGTH_LONG).show();
                                        
                                        // Update the request in the list to show "View Details" button
                                        for (int i = 0; i < allRequests.size(); i++) {
                                            if (allRequests.get(i).getRequestId().equals(request.getRequestId())) {
                                                allRequests.set(i, request);
                                                break;
                                            }
                                        }
                                        filterRideRequests();
                                    } else {
                                        Toast.makeText(BrowseRideRequestsActivity.this, 
                                            "Failed to accept ride request: " + task.getException().getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(BrowseRideRequestsActivity.this, "Failed to load driver information", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BrowseRideRequestsActivity.this, "Driver profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BrowseRideRequestsActivity.this, "Failed to load driver information: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void viewRideRequestDetails(PassengerRideRequest request) {
        Intent intent = new Intent(this, DriverRideRequestDetailsActivity.class);
        intent.putExtra("REQUEST_ID", request.getRequestId());
        startActivity(intent);
    }
    
    /**
     * Shows the ride request locations on a map
     * @param request The ride request to display on map
     */
    private void viewRideOnMap(PassengerRideRequest request) {
        // Create intent to show the locations on a map
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("SOURCE_LAT", request.getSourceLat());
        intent.putExtra("SOURCE_LNG", request.getSourceLng());
        intent.putExtra("DESTINATION_LAT", request.getDestinationLat());
        intent.putExtra("DESTINATION_LNG", request.getDestinationLng());
        intent.putExtra("SOURCE_NAME", request.getSource());
        intent.putExtra("DESTINATION_NAME", request.getDestination());
        intent.putExtra("MODE", "view_locations");
        startActivity(intent);
    }
    
    /**
     * Opens Google Maps to navigate from driver's current location to source (pickup point)
     * @param request The ride request to navigate to
     */
    private void navigateToRide(PassengerRideRequest request) {
        try {
            // Navigate to the source (pickup) location first
            // After picking up passenger, driver should use "Navigate to Destination" button
            String uri = "google.navigation:q=" + request.getSourceLat() + "," + request.getSourceLng();
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            
            // Check if Google Maps is installed
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(this, "Navigating to pickup location. After picking up passenger, use 'Navigate to Destination' in ride details.", Toast.LENGTH_LONG).show();
            } else {
                // If Google Maps is not installed, open in browser with directions
                // This will show directions from current location to source
                String webUri = "https://www.google.com/maps/dir/?api=1&destination=" + 
                               request.getSourceLat() + "," + request.getSourceLng();
                Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri));
                startActivity(webIntent);
                Toast.makeText(this, "Opened navigation in browser. After picking up passenger, use 'Navigate to Destination' in ride details.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening navigation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}