package com.campusride.utils;

import android.util.Log;

import com.campusride.api.ApiClient;
import com.campusride.api.EmailApi;
import com.campusride.api.EmailRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Retrofit-based EmailService following the recommended implementation
 * Uses proper API interface, models, and Retrofit client
 */
public class RetrofitEmailService {
    private static final String TAG = "RetrofitEmailService";
    
    // EmailJS Configuration - following the recommended approach
    private static final String SERVICE_ID = "service_kn4q9ks";
    private static final String TEMPLATE_ID = "template_2wcv2nt";
    private static final String PUBLIC_KEY = "zt65h0d8cGrXzdrI6";  // This is the user_id/public key
    
    private final EmailApi emailApi;
    
    public interface EmailCallback {
        void onEmailSent(String otp);
        void onEmailFailed(String error);
    }
    
    public RetrofitEmailService() {
        // Initialize Retrofit API following recommended approach
        this.emailApi = ApiClient.getClient().create(EmailApi.class);
    }
    
    /**
     * Send OTP email using the recommended Retrofit approach
     * Follows the exact structure from the provided steps
     */
    public void sendOTPEmail(String userEmail, EmailCallback callback) {
        Log.d(TAG, "📧 Sending OTP using recommended Retrofit approach to: " + userEmail);
        
        // Generate random 6-digit OTP (as recommended)
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        
        // Create template parameters map (matching your actual template)
        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("email", userEmail);        // Matching your template {{email}}
        templateParams.put("otp_code", otp);           // Matching your template {{otp_code}}
        templateParams.put("app_name", "Campus Ride");
        
        // Create EmailRequest following the recommended model
        EmailRequest request = new EmailRequest(SERVICE_ID, TEMPLATE_ID, PUBLIC_KEY, templateParams);
        
        Log.d(TAG, "Making API call with service_id: " + SERVICE_ID);
        Log.d(TAG, "Template ID: " + TEMPLATE_ID);
        Log.d(TAG, "Public Key: " + PUBLIC_KEY);
        Log.d(TAG, "User Email: " + userEmail);
        Log.d(TAG, "Generated OTP: " + otp);
        
        // Make API call using Retrofit (following recommended implementation)
        emailApi.sendEmail(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ OTP sent successfully using Retrofit to: " + userEmail);
                    callback.onEmailSent(otp);
                } else {
                    Log.e(TAG, "❌ Failed to send email. Response code: " + response.code());
                    String errorMsg = "Failed: " + response.code();
                    
                    // Provide specific error guidance based on response code
                    switch (response.code()) {
                        case 400:
                            errorMsg += " - Bad Request: Check service/template/user IDs";
                            break;
                        case 401:
                            errorMsg += " - Unauthorized: Check public key";
                            break;
                        case 402:
                            errorMsg += " - Payment Required: EmailJS account limit reached";
                            break;
                        case 403:
                            errorMsg += " - Forbidden: API calls disabled for non-browser apps";
                            break;
                        case 404:
                            errorMsg += " - Not Found: Service or template doesn't exist";
                            break;
                        case 429:
                            errorMsg += " - Too Many Requests: Rate limit exceeded";
                            break;
                        default:
                            errorMsg += " - Server Error";
                    }
                    
                    callback.onEmailFailed(errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error while sending email", t);
                callback.onEmailFailed("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Test method to verify EmailJS configuration
     */
    public void testConfiguration(EmailCallback callback) {
        Log.d(TAG, "🧪 Testing EmailJS configuration...");
        sendOTPEmail("test@example.com", new EmailCallback() {
            @Override
            public void onEmailSent(String otp) {
                Log.d(TAG, "✅ Configuration test successful! Test OTP: " + otp);
                callback.onEmailSent(otp);
            }
            
            @Override
            public void onEmailFailed(String error) {
                Log.e(TAG, "❌ Configuration test failed: " + error);
                callback.onEmailFailed(error);
            }
        });
    }
}
