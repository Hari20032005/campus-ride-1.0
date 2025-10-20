package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.campusride.models.User;
import com.campusride.utils.FirebaseUtil;
import com.campusride.utils.OTPService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, otpEditText;
    private Button sendOtpButton, verifyOtpButton;
    private TextView resendOtpText;
    private TextInputLayout otpLayout;
    
    private OTPService otpService;
    private String currentEmail = "";
    private boolean otpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        Log.d("LoginActivity", "Initializing Firebase");
        FirebaseUtil.initialize(this);
        android.util.Log.d("LoginActivity", "Firebase initialized");
        
        // Initialize OTP Service
        Log.d("LoginActivity", "Initializing OTP Service");
        otpService = new OTPService(this);
        android.util.Log.d("LoginActivity", "OTP Service initialized");
        
        initViews();
        setClickListeners();
        
        // Test Firebase Database connection
        testFirebaseConnection();
    }
    
    private void testFirebaseConnection() {
        try {
            Log.d("LoginActivity", "Testing Firebase Database connection");
            if (FirebaseUtil.getDatabase() != null) {
                Log.d("LoginActivity", "Firebase Database connection is available");
                // Test actual database connectivity
                FirebaseUtil.getDatabase().getReference(".info/connected").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        if (connected) {
                            Log.d("LoginActivity", "✅ Firebase Database is connected");
                        } else {
                            Log.w("LoginActivity", "⚠️ Firebase Database is disconnected");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("LoginActivity", "Failed to check database connection", error.toException());
                    }
                });
            } else {
                Log.e("LoginActivity", "Firebase Database connection is not available");
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Error testing Firebase Database connection", e);
        }
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        otpEditText = findViewById(R.id.otpEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpText = findViewById(R.id.resendOtpText);
        otpLayout = findViewById(R.id.otpLayout);
    }

    private void setClickListeners() {
        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOTP();
            }
        });

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOTP();
            }
        });
        
        resendOtpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendOTP();
            }
        });
        
        // Add debug: Long press email field to test Firebase
        emailEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                android.util.Log.d("LoginActivity", "Testing Firebase connection...");
                startActivity(new Intent(LoginActivity.this, FirebaseTestActivity.class));
                return true;
            }
        });
    }

    private void sendOTP() {
        android.util.Log.d("LoginActivity", "sendOTP() called");
        String email = emailEditText.getText().toString().trim();
        android.util.Log.d("LoginActivity", "Email entered: " + email);

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        // Removed the college email restriction
        // if (!email.endsWith("@vitstudent.ac.in")) {
        //     emailEditText.setError("Please use your college email (@vitstudent.ac.in)");
        //     emailEditText.requestFocus();
        //     return;
        // }

        android.util.Log.d("LoginActivity", "Email validation passed, generating OTP...");
        currentEmail = email;
        sendOtpButton.setEnabled(false);
        sendOtpButton.setText("Sending email...");
        
        otpService.generateAndSendOTP(email, new OTPService.OTPCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                
                // Show additional helpful message if email was sent successfully
                if (message.contains("email address")) {
                    Toast.makeText(LoginActivity.this, 
                        "📧 Check your email (including spam folder). Email may take 1-2 minutes to arrive.", 
                        Toast.LENGTH_LONG).show();
                }
                
                showOtpSection();
                sendOtpButton.setEnabled(true);
                sendOtpButton.setText("Send OTP");
                otpSent = true;
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                sendOtpButton.setEnabled(true);
                sendOtpButton.setText("Send OTP");
            }
        });
    }
    
    private void resendOTP() {
        if (!TextUtils.isEmpty(currentEmail)) {
            resendOtpText.setEnabled(false);
            
            otpService.generateAndSendOTP(currentEmail, new OTPService.OTPCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(LoginActivity.this, "OTP resent: " + message, Toast.LENGTH_LONG).show();
                    resendOtpText.setEnabled(true);
                    otpEditText.setText(""); // Clear previous OTP
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    resendOtpText.setEnabled(true);
                }
            });
        }
    }

    private void verifyOTP() {
        String otp = otpEditText.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            otpEditText.setError("OTP is required");
            otpEditText.requestFocus();
            return;
        }

        if (otp.length() != 6) {
            otpEditText.setError("OTP must be 6 digits");
            otpEditText.requestFocus();
            return;
        }

        verifyOtpButton.setEnabled(false);
        verifyOtpButton.setText("Verifying...");
        
        otpService.verifyOTP(currentEmail, otp, new OTPService.OTPCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                // OTP verified, now authenticate/register user
                authenticateUser();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                verifyOtpButton.setEnabled(true);
                verifyOtpButton.setText("Verify OTP");
            }
        });
    }
    
    private void authenticateUser() {
        // Since OTP is verified, we can trust the user's identity
        // We'll check if user already exists, and if so, sign them in
        // If not, we'll create a new user with a fixed password
        android.util.Log.d("LoginActivity", "Authenticating OTP-verified user: " + currentEmail);
        
        // Try to sign in first (assuming user might already exist)
        String fixedPassword = "campusride123";
        FirebaseUtil.getAuth().signInWithEmailAndPassword(currentEmail, fixedPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            android.util.Log.d("LoginActivity", "Existing user signed in successfully");
                            Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        } else {
                            android.util.Log.d("LoginActivity", "User doesn't exist, creating new user");
                            // User doesn't exist, create a new one
                            createNewUser(fixedPassword);
                        }
                    }
                });
    }
    
    private void createNewUser(String consistentPassword) {
        android.util.Log.d("LoginActivity", "Attempting to create new user for: " + currentEmail);
        FirebaseUtil.getAuth().createUserWithEmailAndPassword(currentEmail, consistentPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        verifyOtpButton.setEnabled(true);
                        verifyOtpButton.setText("Verify OTP");
                        
                        if (task.isSuccessful()) {
                            android.util.Log.d("LoginActivity", "New user created successfully");
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null) {
                                // Store user profile in Firebase Database
                                storeUserProfile(user.getUid(), currentEmail);
                            }
                            Toast.makeText(LoginActivity.this, "Welcome to Campus Ride!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            android.util.Log.e("LoginActivity", "User creation failed: " + errorMessage);
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void storeUserProfile(String userId, String email) {
        // Store user profile in Firebase Database using the User model
        android.util.Log.d("LoginActivity", "Storing user profile for: " + email);
        try {
            // Create a basic user profile with just email for now
            // The user will complete their profile in ProfileCompletionActivity
            User user = new User(userId, "", email, "", "");
            user.setProfileComplete(false); // Profile will be marked complete after user fills in details
            
            FirebaseUtil.getDatabase().getReference("users").child(userId)
                .setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("LoginActivity", "User profile stored successfully");
                    } else {
                        android.util.Log.w("LoginActivity", "Failed to store user profile: " + task.getException());
                    }
                });
        } catch (Exception e) {
            android.util.Log.w("LoginActivity", "Error storing user profile", e);
        }
    }
    
    private void navigateToHome() {
        // Check if user profile is complete before going to home screen
        checkUserProfileCompletion();
    }
    
    private void checkUserProfileCompletion() {
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser != null) {
            FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            User user = task.getResult().getValue(User.class);
                            if (user != null && user.isProfileComplete()) {
                                // Profile is complete, go to home
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                // Profile not complete, go to profile completion
                                startActivity(new Intent(LoginActivity.this, ProfileCompletionActivity.class));
                                finish();
                            }
                        } else {
                            // User data doesn't exist or error, go to profile completion
                            startActivity(new Intent(LoginActivity.this, ProfileCompletionActivity.class));
                            finish();
                        }
                    });
        }
    }

    private void showOtpSection() {
        // Hide send OTP button to prevent overlap
        sendOtpButton.setVisibility(View.GONE);
        
        // Show OTP verification section
        otpLayout.setVisibility(View.VISIBLE);
        verifyOtpButton.setVisibility(View.VISIBLE);
        resendOtpText.setVisibility(View.VISIBLE);
        
        // Disable email editing once OTP is sent
        emailEditText.setEnabled(false);
    }
    
    @Override
    public void onBackPressed() {
        if (otpSent) {
            // If OTP was sent, allow user to go back to email entry
            hideOtpSection();
            otpSent = false;
        } else {
            super.onBackPressed();
        }
    }
    
    private void hideOtpSection() {
        // Hide OTP verification section
        otpLayout.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        resendOtpText.setVisibility(View.GONE);
        
        // Show send OTP button again
        sendOtpButton.setVisibility(View.VISIBLE);
        
        // Re-enable email editing
        emailEditText.setEnabled(true);
        otpEditText.setText("");
        currentEmail = "";
    }
}