package com.campusride;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.campusride.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class OTPVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OTPVerification";
    private Button loginButton;
    private ProgressBar progressBar;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get the email and password from the intent
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        
        Log.d(TAG, "Starting account creation for email: " + email);

        initViews();
        setClickListeners();
        
        // Create user
        createUser();
    }

    private void initViews() {
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
    }

    private void createUser() {
        Log.d(TAG, "Attempting to create user with email: " + email);
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseAuth auth = FirebaseUtil.getAuth();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "User created successfully: " + user.getUid() + " with email: " + user.getEmail());
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(OTPVerificationActivity.this, 
                                    "Account created successfully! You can now log in.", 
                                    Toast.LENGTH_LONG).show();
                            } else {
                                Log.e(TAG, "User is null after successful creation");
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(OTPVerificationActivity.this, 
                                    "Unexpected error: User is null after creation", 
                                    Toast.LENGTH_LONG).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Log.e(TAG, "Registration failed", task.getException());
                            
                            // Provide more specific error messages
                            String errorMessage = "Registration failed";
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthException) {
                                    errorMessage = "Authentication error: " + task.getException().getMessage();
                                } else {
                                    errorMessage = "Registration failed: " + task.getException().getMessage();
                                }
                            }
                            
                            Toast.makeText(OTPVerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Registration failed with exception", e);
                        Toast.makeText(OTPVerificationActivity.this, 
                            "Registration failed: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void goToLogin() {
        // Redirect to login page
        startActivity(new Intent(OTPVerificationActivity.this, LoginActivity.class));
        finish();
    }
}