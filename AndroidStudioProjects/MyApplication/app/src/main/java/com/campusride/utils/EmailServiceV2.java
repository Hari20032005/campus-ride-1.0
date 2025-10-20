package com.campusride.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EmailServiceV2 {
    private static final String TAG = "EmailServiceV2";
    // Using form submission endpoint which is more mobile-friendly
    private static final String EMAILJS_URL = "https://api.emailjs.com/api/v1.0/email/send-form";
    
    // EmailJS configuration
    private static final String SERVICE_ID = "service_kn4q9ks";
    private static final String TEMPLATE_ID = "template_2wcv2nt"; 
    private static final String USER_ID = "zt65h0d8cGrXzdrI6";
    
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    public interface EmailCallback {
        void onEmailSent();
        void onEmailFailed(String error);
    }
    
    public EmailServiceV2() {
        this.client = new OkHttpClient();
        this.executor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void sendOTPEmail(String recipientEmail, String otp, EmailCallback callback) {
        Log.d(TAG, "📧 Starting OTP email send using form-data method to: " + recipientEmail);
        
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
                
                // Build form data instead of JSON
                FormBody.Builder formBuilder = new FormBody.Builder();
                formBuilder.add("service_id", SERVICE_ID);
                formBuilder.add("template_id", TEMPLATE_ID);
                formBuilder.add("user_id", USER_ID);
                
                // Add template parameters as form fields (matching your actual template)
                formBuilder.add("email", recipientEmail);        // Matches your template {{email}}
                formBuilder.add("otp_code", otp);                // Matches your template {{otp_code}}
                formBuilder.add("app_name", "Campus Ride");
                formBuilder.add("subject", "Your Campus Ride OTP Code");
                formBuilder.add("from_name", "Campus Ride Team");
                
                FormBody formBody = formBuilder.build();
                
                Log.d(TAG, "Sending EmailJS form request with service_id: " + SERVICE_ID);
                Log.d(TAG, "Template ID: " + TEMPLATE_ID);
                Log.d(TAG, "Recipient: " + recipientEmail);
                
                Request request = new Request.Builder()
                    .url(EMAILJS_URL)
                    .post(formBody)
                    .addHeader("User-Agent", "CampusRide-Mobile/1.0")
                    .addHeader("Accept", "text/plain")
                    .addHeader("Referer", "https://campusride.app")
                    .build();
                
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Failed to send email (form method)", e);
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
                        
                        Log.d(TAG, "EmailJS Form Response Code: " + responseCode);
                        Log.d(TAG, "EmailJS Form Response Body: " + finalResponseBody);
                        
                        mainHandler.post(() -> {
                            if (isSuccessful) {
                                Log.d(TAG, "✅ OTP email sent successfully (form method) to: " + recipientEmail);
                                callback.onEmailSent();
                            } else {
                                Log.e(TAG, "❌ Email sending failed (form method). Response: " + responseCode);
                                Log.e(TAG, "Error details: " + finalResponseBody);
                                callback.onEmailFailed("Email service error (form method): " + responseCode + ". " + finalResponseBody);
                            }
                        });
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending email (form method)", e);
                mainHandler.post(() -> {
                    callback.onEmailFailed("Email preparation failed (form method): " + e.getMessage());
                });
            }
        });
    }
}
