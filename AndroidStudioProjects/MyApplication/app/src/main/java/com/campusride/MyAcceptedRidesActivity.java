package com.campusride;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.PassengerRideRequest;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyAcceptedRidesActivity extends AppCompatActivity {

    private static final String TAG = "MyAcceptedRides";
    
    private RecyclerView ridesRecyclerView;
    private DriverRidesAdapter ridesAdapter;
    private List<PassengerRideRequest> ridesList;
    private String filter; // "accepted" or "completed"
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_accepted_rides);

        filter = getIntent().getStringExtra("filter");
        if (filter == null) {
            filter = "accepted";
        }

        initViews();
        setupRecyclerView();
        loadRides();
    }

    private void initViews() {
        ridesRecyclerView = findViewById(R.id.ridesRecyclerView);
        titleTextView = findViewById(R.id.titleTextView);
        
        if ("completed".equals(filter)) {
            titleTextView.setText("My Completed Rides");
        } else {
            titleTextView.setText("My Accepted Rides");
        }
    }

    private void setupRecyclerView() {
        ridesList = new ArrayList<>();
        ridesAdapter = new DriverRidesAdapter(this, ridesList, filter);
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ridesRecyclerView.setAdapter(ridesAdapter);
    }
    
    private void loadRides() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view your rides", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
        Log.d(TAG, "Loading " + filter + " rides for driver: " + currentUser.getUid());
        
        requestsRef.orderByChild("driverId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Data loaded successfully, count: " + dataSnapshot.getChildrenCount());
                        ridesList.clear();
                        
                        for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                            PassengerRideRequest request = requestSnapshot.getValue(PassengerRideRequest.class);
                            if (request != null && filter.equals(request.getStatus())) {
                                ridesList.add(request);
                                Log.d(TAG, "Added ride: " + request.getRequestId() + " with status " + request.getStatus());
                            }
                        }
                        
                        // Sort by creation time (newest first)
                        ridesList.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                        ridesAdapter.updateRides(ridesList);
                        Log.d(TAG, "Loaded " + ridesList.size() + " " + filter + " rides for current driver");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error loading rides", databaseError.toException());
                        String errorMessage = databaseError.getMessage();
                        if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                            errorMessage = "Permission denied. Please check Firebase security rules.";
                        }
                        Toast.makeText(MyAcceptedRidesActivity.this, "Failed to load rides: " + errorMessage, 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    public void completeRide(PassengerRideRequest ride) {
        ride.setStatus("completed");
        ride.setCompletedAt(System.currentTimeMillis());
        
        DatabaseReference requestRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests").child(ride.getRequestId());
        requestRef.setValue(ride)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MyAcceptedRidesActivity.this, "Ride completed successfully!", Toast.LENGTH_SHORT).show();
                        // Refresh the list
                        loadRides();
                    } else {
                        Toast.makeText(MyAcceptedRidesActivity.this, "Failed to complete ride: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
}