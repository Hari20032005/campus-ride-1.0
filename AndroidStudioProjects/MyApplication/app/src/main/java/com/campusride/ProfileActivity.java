package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.campusride.models.User;
import com.campusride.models.User;
import com.campusride.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    
    private TextView nameTextView, emailTextView, mobileTextView, regNoTextView, ridesCountTextView;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserProfile();
        setClickListeners();
    }

    private void initViews() {
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        mobileTextView = findViewById(R.id.mobileTextView);
        regNoTextView = findViewById(R.id.regNoTextView);
        ridesCountTextView = findViewById(R.id.ridesCountTextView);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());
            
            // Load additional profile info from Firebase Database
            FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // Get user data from Firebase
                            User user = task.getResult().getValue(User.class);
                            
                            if (user != null) {
                                // Update UI with user data
                                nameTextView.setText(user.getName() != null ? user.getName() : "No name set");
                                mobileTextView.setText(user.getMobile() != null ? user.getMobile() : "No mobile number");
                                regNoTextView.setText(user.getRegNo() != null ? user.getRegNo() : "No registration number");
                                
                                // Calculate total rides
                                int totalRides = user.getRidesAsDriver() + user.getRidesAsPassenger();
                                ridesCountTextView.setText(String.valueOf(totalRides));
                            }
                        } else {
                            Log.e(TAG, "Failed to load user profile", task.getException());
                            nameTextView.setText("No name set");
                            mobileTextView.setText("No mobile number");
                            regNoTextView.setText("No registration number");
                            ridesCountTextView.setText("0");
                        }
                    });
        }
    }

    private void setClickListeners() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from Firebase Auth
                FirebaseUtil.getAuth().signOut();
                
                // Reset Firebase instances to ensure proper re-initialization on next login
                FirebaseUtil.resetFirebaseInstances();
                
                // Also clear any stored data
                getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();
                
                // Go back to login screen
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}