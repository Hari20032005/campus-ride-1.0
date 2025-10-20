package com.campusride;

import android.os.Bundle;
import android.util.Log;
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

public class MyRideRequestsActivity extends AppCompatActivity {

    private static final String TAG = "MyRideRequests";
    
    private RecyclerView myRequestsRecyclerView;
    private MyRideRequestsAdapter myRequestsAdapter;
    private List<PassengerRideRequest> myRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ride_requests);

        initViews();
        setupRecyclerView();
        loadMyRideRequests();
    }

    private void initViews() {
        myRequestsRecyclerView = findViewById(R.id.myRequestsRecyclerView);
    }

    private void setupRecyclerView() {
        myRequests = new ArrayList<>();
        myRequestsAdapter = new MyRideRequestsAdapter(myRequests);
        myRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRequestsRecyclerView.setAdapter(myRequestsAdapter);
    }
    
    private void loadMyRideRequests() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view your ride requests", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
        Log.d(TAG, "Attempting to load ride requests for user: " + currentUser.getUid());
        Log.d(TAG, "Database reference: " + requestsRef.toString());
        
        requestsRef.orderByChild("passengerId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Data loaded successfully, count: " + dataSnapshot.getChildrenCount());
                        myRequests.clear();
                        for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                            PassengerRideRequest request = requestSnapshot.getValue(PassengerRideRequest.class);
                            if (request != null) {
                                myRequests.add(request);
                                Log.d(TAG, "Added request: " + request.getRequestId() + " with status " + request.getStatus());
                            }
                        }
                        // Sort by creation time (newest first)
                        myRequests.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                        myRequestsAdapter.updateRequests(myRequests);
                        Log.d(TAG, "Loaded " + myRequests.size() + " ride requests for current passenger");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error loading ride requests", databaseError.toException());
                        String errorMessage = databaseError.getMessage();
                        if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                            errorMessage = "Permission denied. Please check Firebase security rules.";
                        }
                        Toast.makeText(MyRideRequestsActivity.this, "Failed to load ride requests: " + errorMessage, 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }
}