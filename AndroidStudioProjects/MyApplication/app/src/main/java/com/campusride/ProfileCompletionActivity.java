package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.campusride.models.User;
import com.campusride.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class ProfileCompletionActivity extends AppCompatActivity {

    private static final String TAG = "ProfileCompletion";
    
    private EditText nameEditText, mobileEditText, regNoEditText;
    private Button saveButton;
    
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_completion);
        
        initViews();
        setClickListeners();
        
        // Get current user ID
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // If no user is logged in, redirect to login
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
    
    private void initViews() {
        nameEditText = findViewById(R.id.nameEditText);
        mobileEditText = findViewById(R.id.mobileEditText);
        regNoEditText = findViewById(R.id.regNoEditText);
        saveButton = findViewById(R.id.saveButton);
    }
    
    private void setClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }
    
    private void saveProfile() {
        String name = nameEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String regNo = regNoEditText.getText().toString().trim();
        
        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(mobile)) {
            mobileEditText.setError("Mobile number is required");
            mobileEditText.requestFocus();
            return;
        }
        
        if (mobile.length() != 10) {
            mobileEditText.setError("Mobile number must be 10 digits");
            mobileEditText.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(regNo)) {
            regNoEditText.setError("Registration number is required");
            regNoEditText.requestFocus();
            return;
        }
        
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");
        
        // Save profile to Firebase
        saveProfileToFirebase(name, mobile, regNo);
    }
    
    private void saveProfileToFirebase(String name, String mobile, String regNo) {
        // Create user object
        User user = new User(userId, name, FirebaseUtil.getAuth().getCurrentUser().getEmail(), mobile, regNo);
        user.setProfileComplete(true);
        
        // Save to Firebase Database
        DatabaseReference userRef = FirebaseUtil.getDatabase().getReference("users").child(userId);
        userRef.setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Profile saved successfully");
                            Toast.makeText(ProfileCompletionActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                            
                            // Navigate to home screen
                            startActivity(new Intent(ProfileCompletionActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Log.e(TAG, "Failed to save profile", task.getException());
                            Toast.makeText(ProfileCompletionActivity.this, "Failed to save profile: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                                Toast.LENGTH_LONG).show();
                            saveButton.setEnabled(true);
                            saveButton.setText("Save Profile");
                        }
                    }
                });
    }
}