package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class DriverDashboardActivity extends AppCompatActivity {

    private MaterialCardView browseRideRequestsCard, acceptedRidesCard, pastRidesCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        initViews();
        setClickListeners();
    }

    private void initViews() {
        browseRideRequestsCard = findViewById(R.id.browseRideRequestsCard);
        acceptedRidesCard = findViewById(R.id.acceptedRidesCard);
        pastRidesCard = findViewById(R.id.pastRidesCard);
    }

    private void setClickListeners() {
        browseRideRequestsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverDashboardActivity.this, BrowseRideRequestsActivity.class));
            }
        });

        acceptedRidesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverDashboardActivity.this, MyAcceptedRidesActivity.class);
                intent.putExtra("filter", "accepted");
                startActivity(intent);
            }
        });

        pastRidesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverDashboardActivity.this, MyAcceptedRidesActivity.class);
                intent.putExtra("filter", "completed");
                startActivity(intent);
            }
        });
    }
}