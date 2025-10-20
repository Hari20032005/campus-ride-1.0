package com.campusride.utils;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OTPService {
    private static final String TAG = "OTPService";
    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALIDITY_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds
    
    private Context context;
    private DatabaseReference otpRef;
    private EmailService emailService;
    private EmailServiceV2 emailServiceV2;
    private RetrofitEmailService retrofitEmailService; // Recommended approach
    
    public interface OTPCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
    
    public OTPService(Context context) {
        this.context = context;
        Log.d(TAG, "Initializing OTPService");
        
        try {
            // Check if Firebase is initialized
            if (FirebaseUtil.getDatabase() != null) {
                this.otpRef = FirebaseUtil.getDatabase().getReference("otp_codes");
                Log.d(TAG, "✅ OTP reference created successfully");
            } else {
                Log.e(TAG, "❌ Firebase Database is null during OTPService initialization");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during OTP reference creation", e);
        }
        
        this.emailService = new EmailService();
        this.emailServiceV2 = new EmailServiceV2();
        this.retrofitEmailService = new RetrofitEmailService(); // Recommended approach
        Log.d(TAG, "OTPService initialized with all email services");
    }
    
    /**
     * Generates and stores OTP for the given email
     */
    public void generateAndSendOTP(String email, OTPCallback callback) {
        Log.d(TAG, "Starting OTP generation for email: " + email);
        
        // Check if Firebase is initialized
        if (FirebaseUtil.getDatabase() == null) {
            Log.e(TAG, "Firebase Database is null!");
            callback.onFailure("Firebase not initialized. Please check your configuration.");
            return;
        }
        
        // Check if otpRef is valid
        if (otpRef == null) {
            Log.e(TAG, "OTP reference is null, trying to recreate it");
            try {
                otpRef = FirebaseUtil.getDatabase().getReference("otp_codes");
                if (otpRef == null) {
                    Log.e(TAG, "Still unable to create OTP reference");
                    callback.onFailure("Unable to connect to database. Please try again.");
                    return;
                } else {
                    Log.d(TAG, "✅ OTP reference recreated successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception while recreating OTP reference", e);
                callback.onFailure("Database connection error: " + e.getMessage());
                return;
            }
        }
        
        // Generate 6-digit OTP
        String otp = generateOTP();
        long timestamp = System.currentTimeMillis();
        Log.d(TAG, "Generated OTP: " + otp + " at timestamp: " + timestamp);
        
        // Create OTP data
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("email", email);
        otpData.put("timestamp", timestamp);
        otpData.put("used", false);
        
        // Store in Firebase Database
        String otpKey = email.replace(".", "_").replace("@", "_at_");
        Log.d(TAG, "Storing OTP with key: " + otpKey);
        
        Log.d(TAG, "Attempting to store OTP in Firebase Database");
        otpRef.child(otpKey).setValue(otpData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "✅ OTP successfully stored in Firebase!");
                            Log.d(TAG, "OTP generated and stored: " + otp + " for email: " + email);
                            
                            // Try Retrofit approach first (RECOMMENDED METHOD)
                            Log.d(TAG, "Attempting OTP send using recommended Retrofit approach...");
                            retrofitEmailService.sendOTPEmail(email, new RetrofitEmailService.EmailCallback() {
                                @Override
                                public void onEmailSent(String generatedOtp) {
                                    Log.d(TAG, "✅ OTP sent successfully using Retrofit to: " + email);
                                    // Store the generated OTP in the existing OTP data
                                    otpRef.child(email.replace(".", "_").replace("@", "_at_")).child("otp").setValue(generatedOtp);
                                    callback.onSuccess("OTP sent to your email address. Please check your inbox.");
                                }
                                
                                @Override
                                public void onEmailFailed(String error) {
                                    Log.w(TAG, "📧 Retrofit method failed: " + error);
                                    Log.d(TAG, "Trying fallback JSON method...");
                                    
                                    // Fallback 1: Try original EmailService (JSON method)
                                    emailService.sendOTPEmail(email, otp, new EmailService.EmailCallback() {
                                        @Override
                                        public void onEmailSent() {
                                            Log.d(TAG, "✅ Email sent successfully (JSON fallback) to: " + email);
                                            callback.onSuccess("OTP sent to your email address. Please check your inbox.");
                                        }
                                        
                                        @Override
                                        public void onEmailFailed(String error2) {
                                            Log.w(TAG, "📧 JSON fallback method failed: " + error2);
                                            Log.d(TAG, "Trying form-data method as final fallback...");
                                            
                                            // Fallback 2: try EmailServiceV2 (form method)
                                            emailServiceV2.sendOTPEmail(email, otp, new EmailServiceV2.EmailCallback() {
                                                @Override
                                                public void onEmailSent() {
                                                    Log.d(TAG, "✅ Email sent successfully (form fallback) to: " + email);
                                                    callback.onSuccess("OTP sent to your email address. Please check your inbox (form method worked).");
                                                }
                                                
                                                @Override
                                                public void onEmailFailed(String error3) {
                                                    Log.e(TAG, "❌ All email methods failed. Retrofit: " + error + ", JSON: " + error2 + ", Form: " + error3);
                                                    // Final fallback: show OTP in app for development/debugging
                                                    callback.onSuccess("⚠️ All EmailJS methods failed! Use this OTP: " + otp + "\n\nCheck EmailJS dashboard settings.");
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        } else {
                            Log.e(TAG, "❌ Failed to store OTP in Firebase!", task.getException());
                            String errorMsg = task.getException() != null ? 
                                task.getException().getMessage() : "Unknown error";
                            callback.onFailure("Failed to generate OTP: " + errorMsg);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Firebase operation failed!", e);
                    callback.onFailure("Database connection failed: " + e.getMessage());
                });
    }
    
    /**
     * Verifies the provided OTP for the given email
     */
    public void verifyOTP(String email, String enteredOTP, OTPCallback callback) {
        Log.d(TAG, "Verifying OTP for email: " + email);
        
        // Check if Firebase is initialized
        if (FirebaseUtil.getDatabase() == null) {
            Log.e(TAG, "Firebase Database is null during OTP verification!");
            callback.onFailure("Database connection lost. Please restart the app.");
            return;
        }
        
        // Check if otpRef is valid
        if (otpRef == null) {
            Log.e(TAG, "OTP reference is null during verification");
            callback.onFailure("Database connection error. Please try again.");
            return;
        }
        
        String otpKey = email.replace(".", "_").replace("@", "_at_");
        
        otpRef.child(otpKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedOTP = dataSnapshot.child("otp").getValue(String.class);
                    Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                    Boolean used = dataSnapshot.child("used").getValue(Boolean.class);
                    
                    if (storedOTP != null && timestamp != null) {
                        long currentTime = System.currentTimeMillis();
                        
                        // Check if OTP is expired
                        if (currentTime - timestamp > OTP_VALIDITY_DURATION) {
                            callback.onFailure("OTP has expired. Please request a new one.");
                            return;
                        }
                        
                        // Check if OTP is already used
                        if (used != null && used) {
                            callback.onFailure("OTP has already been used. Please request a new one.");
                            return;
                        }
                        
                        // Verify OTP
                        if (storedOTP.equals(enteredOTP)) {
                            // Mark OTP as used
                            otpRef.child(otpKey).child("used").setValue(true);
                            callback.onSuccess("OTP verified successfully!");
                        } else {
                            callback.onFailure("Invalid OTP. Please try again.");
                        }
                    } else {
                        callback.onFailure("Invalid OTP data. Please request a new OTP.");
                    }
                } else {
                    callback.onFailure("No OTP found for this email. Please request OTP first.");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error during OTP verification", databaseError.toException());
                callback.onFailure("Database error. Please try again.");
            }
        });
    }
    
    /**
     * Cleans up expired OTPs (optional utility method)
     */
    public void cleanupExpiredOTPs() {
        long currentTime = System.currentTimeMillis();
        
        otpRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot otpSnapshot : dataSnapshot.getChildren()) {
                    Long timestamp = otpSnapshot.child("timestamp").getValue(Long.class);
                    if (timestamp != null && (currentTime - timestamp > OTP_VALIDITY_DURATION)) {
                        otpSnapshot.getRef().removeValue();
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error cleaning up expired OTPs", databaseError.toException());
            }
        });
    }
    
    /**
     * Generates a random 6-digit OTP
     */
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate 6-digit number
        return String.valueOf(otp);
    }
}
