package com.campusride;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.campusride.models.User;
import com.campusride.utils.UserProfileUtil;

public class UserDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "UserDetailActivity";
    
    private TextView nameTextView, emailTextView, mobileTextView, regNoTextView, ridesCountTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        
        initViews();
        
        // Get user ID from intent
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId != null && !userId.isEmpty()) {
            loadUserProfile(userId);
        } else {
            Log.e(TAG, "No user ID provided in intent");
            finish();
        }
    }
    
    private void initViews() {
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        mobileTextView = findViewById(R.id.mobileTextView);
        regNoTextView = findViewById(R.id.regNoTextView);
        ridesCountTextView = findViewById(R.id.ridesCountTextView);
    }
    
    private void loadUserProfile(String userId) {
        UserProfileUtil.getUserProfileById(userId, new UserProfileUtil.UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(User user) {
                // Update UI with user data
                nameTextView.setText(user.getName() != null ? user.getName() : "No name set");
                emailTextView.setText(user.getEmail() != null ? user.getEmail() : "No email");
                mobileTextView.setText(user.getMobile() != null ? user.getMobile() : "No mobile number");
                regNoTextView.setText(user.getRegNo() != null ? user.getRegNo() : "No registration number");
                
                // Calculate total rides
                int totalRides = user.getRidesAsDriver() + user.getRidesAsPassenger();
                ridesCountTextView.setText(String.valueOf(totalRides));
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load user profile: " + error);
                // Show error message in UI
                nameTextView.setText("Error loading profile");
            }
        });
    }
}