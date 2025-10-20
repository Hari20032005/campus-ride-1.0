package com.campusride.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmailService {
    private static final String TAG = "EmailService";
    // Using EmailJS REST API with proper endpoint for mobile applications
    private static final String EMAILJS_URL = "https://api.emailjs.com/api/v1.0/email/send";
    
    // You need to sign up at https://www.emailjs.com and get these values
    private static final String SERVICE_ID = "service_kn4q9ks"; // Replace with your EmailJS service ID
    private static final String TEMPLATE_ID = "template_2wcv2nt"; // Replace with your template ID
    private static final String USER_ID = "zt65h0d8cGrXzdrI6"; // Replace with your EmailJS User ID (Public Key)
    
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    public interface EmailCallback {
        void onEmailSent();
        void onEmailFailed(String error);
    }
    
    public EmailService() {
        this.client = new OkHttpClient();
        this.executor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void sendOTPEmail(String recipientEmail, String otp, EmailCallback callback) {
        Log.d(TAG, "📧 Starting OTP email send to: " + recipientEmail);
        
        executor.execute(() -> {
            try {
                // Validate inputs
                if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                    Log.e(TAG, "❌ Invalid recipient email: empty or null");
                    mainHandler.post(() -> callback.onEmailFailed("Invalid email address"));
                    return;
                }
                
                if (otp == null || otp.trim().isEmpty()) {
                    Log.e(TAG, "❌ Invalid OTP: empty or null");
                    mainHandler.post(() -> callback.onEmailFailed("Invalid OTP code"));
                    return;
                }
                
                JSONObject emailData = new JSONObject();
                emailData.put("service_id", SERVICE_ID);
                emailData.put("template_id", TEMPLATE_ID);
                emailData.put("user_id", USER_ID);
                
                JSONObject templateParams = new JSONObject();
                templateParams.put("email", recipientEmail);         // Matches your template {{email}}
                templateParams.put("otp_code", otp);                 // Matches your template {{otp_code}}
                templateParams.put("app_name", "Campus Ride");
                templateParams.put("subject", "Your Campus Ride OTP Code");
                templateParams.put("from_name", "Campus Ride Team");
                
                emailData.put("template_params", templateParams);
                
                // Log the request data for debugging (without sensitive info)
                Log.d(TAG, "Sending EmailJS request with service_id: " + SERVICE_ID);
                Log.d(TAG, "Template ID: " + TEMPLATE_ID);
                Log.d(TAG, "Request JSON: " + emailData.toString());
                
                RequestBody body = RequestBody.create(
                    emailData.toString(),
                    MediaType.get("application/json")
                );
                
                Request request = new Request.Builder()
                    .url(EMAILJS_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "CampusRide-Mobile/1.0")
                    .addHeader("Accept", "application/json")
                    .addHeader("Origin", "https://campusride.app")
                    .build();
                
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Failed to send email", e);
                        // Run callback on main thread
                        mainHandler.post(() -> {
                            callback.onEmailFailed("Network error: " + e.getMessage());
                        });
                    }
                    
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        boolean isSuccessful = response.isSuccessful();
                        int responseCode = response.code();
                        String responseBody = "";
                        
                        try {
                            if (response.body() != null) {
                                responseBody = response.body().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading response body", e);
                        }
                        
                        final String finalResponseBody = responseBody;
                        response.close();
                        
                        // Log detailed information for debugging
                        Log.d(TAG, "EmailJS Response Code: " + responseCode);
                        Log.d(TAG, "EmailJS Response Body: " + finalResponseBody);
                        
                        // Run callback on main thread
                        mainHandler.post(() -> {
                            if (isSuccessful) {
                                Log.d(TAG, "OTP email sent successfully to: " + recipientEmail);
                                callback.onEmailSent();
                            } else {
                                Log.e(TAG, "Email sending failed. Response: " + responseCode);
                                Log.e(TAG, "Error details: " + finalResponseBody);
                                callback.onEmailFailed("Email service error: " + responseCode + ". " + finalResponseBody);
                            }
                        });
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending email", e);
                // Run callback on main thread
                mainHandler.post(() -> {
                    callback.onEmailFailed("Email preparation failed: " + e.getMessage());
                });
            }
        });
    }
}
