package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.PassengerRideRequest;
import com.campusride.utils.FirebaseUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";
    
    private MaterialCardView postRideCard, searchRideCard, myRideRequestsCard, profileCard;
    private RecyclerView recentRidesRecyclerView;
    private RecentRidesAdapter recentRidesAdapter;
    private List<PassengerRideRequest> recentRidesList;
    
    private ValueEventListener myRidesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        FirebaseUtil.initialize(this);

        // Check if user is logged in
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setClickListeners();
        setupRecyclerView();
        loadRecentRides();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (myRidesListener != null) {
            DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
            requestsRef.removeEventListener(myRidesListener);
        }
    }

    private void initViews() {
        postRideCard = findViewById(R.id.postRideCard);
        searchRideCard = findViewById(R.id.searchRideCard);
        myRideRequestsCard = findViewById(R.id.myRideRequestsCard);
        profileCard = findViewById(R.id.profileCard);
        recentRidesRecyclerView = findViewById(R.id.recentRidesRecyclerView);
        MaterialCardView driverCard = findViewById(R.id.driverCard);
        
        // Set click listener for driver card
        driverCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, DriverDashboardActivity.class));
            }
        });
    }

    private void setClickListeners() {
        postRideCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, PostRideRequestActivity.class));
            }
        });

        searchRideCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, BrowseRideRequestsActivity.class));
            }
        });

        myRideRequestsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, MyRideRequestsActivity.class));
            }
        });

        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
        });
    }

    private void setupRecyclerView() {
        recentRidesList = new ArrayList<>();
        recentRidesAdapter = new RecentRidesAdapter(recentRidesList);
        recentRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentRidesRecyclerView.setAdapter(recentRidesAdapter);
    }
    
    private void loadRecentRides() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
        myRidesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recentRidesList.clear();
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    PassengerRideRequest request = requestSnapshot.getValue(PassengerRideRequest.class);
                    if (request != null && currentUser.getUid().equals(request.getPassengerId())) {
                        // Only show requests made by the current user
                        recentRidesList.add(request);
                    }
                }
                // Sort by creation time (newest first) and limit to 5 most recent
                recentRidesList.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                if (recentRidesList.size() > 5) {
                    recentRidesList.subList(5, recentRidesList.size()).clear();
                }
                recentRidesAdapter.updateRequests(recentRidesList);
                Log.d(TAG, "Loaded " + recentRidesList.size() + " recent ride requests");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading ride requests", databaseError.toException());
                Toast.makeText(HomeActivity.this, "Failed to load ride requests: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };
        
        requestsRef.orderByChild("passengerId").equalTo(currentUser.getUid())
                .addValueEventListener(myRidesListener);
    }
}