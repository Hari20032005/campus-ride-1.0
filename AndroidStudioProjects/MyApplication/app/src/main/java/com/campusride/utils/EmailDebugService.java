package com.campusride.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

public class EmailDebugService {
    private static final String TAG = "EmailDebugService";
    private static final String EMAILJS_URL = "https://api.emailjs.com/api/v1.0/email/send";
    
    // Current EmailJS configuration from EmailService
    private static final String SERVICE_ID = "service_kn4q9ks";
    private static final String TEMPLATE_ID = "template_2wcv2nt"; 
    private static final String USER_ID = "zt65h0d8cGrXzdrI6";
    
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    public interface DebugCallback {
        void onResult(String result);
    }
    
    public EmailDebugService() {
        this.client = new OkHttpClient();
        this.executor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Comprehensive test of EmailJS configuration
     */
    public void runDiagnostics(String testEmail, DebugCallback callback) {
        executor.execute(() -> {
            StringBuilder result = new StringBuilder();
            result.append("🔍 EMAIL OTP DIAGNOSTICS REPORT\n");
            result.append("=====================================\n\n");
            
            // 1. Check EmailJS Configuration
            result.append("📋 EMAILJS CONFIGURATION:\n");
            result.append("Service ID: ").append(SERVICE_ID).append("\n");
            result.append("Template ID: ").append(TEMPLATE_ID).append("\n");
            result.append("User ID: ").append(USER_ID).append("\n");
            result.append("API URL: ").append(EMAILJS_URL).append("\n\n");
            
            // 2. Test Internet connectivity
            result.append("🌐 NETWORK CONNECTIVITY TEST:\n");
            if (testInternetConnectivity()) {
                result.append("✅ Internet connection: WORKING\n");
            } else {
                result.append("❌ Internet connection: FAILED\n");
                result.append("→ Check device internet connection\n");
            }
            result.append("\n");
            
            // 3. Test EmailJS API endpoint
            result.append("📡 EMAILJS API TEST:\n");
            String apiTestResult = testEmailJSAPI(testEmail);
            result.append(apiTestResult).append("\n\n");
            
            // 4. Common troubleshooting tips
            result.append("🛠️ TROUBLESHOOTING TIPS:\n");
            result.append("1. Check EmailJS dashboard for service status\n");
            result.append("2. Verify email template exists and is published\n");
            result.append("3. Check EmailJS account limits (free tier: 200 emails/month)\n");
            result.append("4. Look for emails in spam/junk folder\n");
            result.append("5. Try with different email address to test\n");
            result.append("6. Check EmailJS service settings allow @vitstudent.ac.in domain\n\n");
            
            result.append("📧 EMAIL DELIVERY TIPS:\n");
            result.append("• Emails can take 1-5 minutes to arrive\n");
            result.append("• Check ALL folders (inbox, spam, promotions, etc.)\n");
            result.append("• Some email providers block automated emails\n");
            result.append("• College email systems may have additional filters\n");
            
            // Return result on main thread
            mainHandler.post(() -> callback.onResult(result.toString()));
        });
    }
    
    /**
     * Test basic internet connectivity
     */
    private boolean testInternetConnectivity() {
        try {
            Request request = new Request.Builder()
                .url("https://www.google.com")
                .head()
                .build();
            
            Response response = client.newCall(request).execute();
            boolean isConnected = response.isSuccessful();
            response.close();
            return isConnected;
        } catch (Exception e) {
            Log.e(TAG, "Internet connectivity test failed", e);
            return false;
        }
    }
    
    /**
     * Test EmailJS API with actual request
     */
    private String testEmailJSAPI(String testEmail) {
        try {
            JSONObject emailData = new JSONObject();
            emailData.put("service_id", SERVICE_ID);
            emailData.put("template_id", TEMPLATE_ID);
            emailData.put("user_id", USER_ID);
            
            JSONObject templateParams = new JSONObject();
            templateParams.put("email", testEmail);                       // Matches your template {{email}}
            templateParams.put("otp_code", "123456");                    // Matches your template {{otp_code}}
            templateParams.put("app_name", "Campus Ride - DEBUG TEST");
            
            emailData.put("template_params", templateParams);
            
            Log.d(TAG, "Testing EmailJS with payload: " + emailData.toString());
            
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
            
            Response response = client.newCall(request).execute();
            int responseCode = response.code();
            String responseBody = "";
            
            if (response.body() != null) {
                responseBody = response.body().string();
            }
            response.close();
            
            StringBuilder apiResult = new StringBuilder();
            apiResult.append("HTTP Response Code: ").append(responseCode).append("\n");
            
            if (response.isSuccessful()) {
                apiResult.append("✅ EmailJS API: SUCCESS\n");
                apiResult.append("→ Test email should arrive in 1-2 minutes\n");
                apiResult.append("→ Check ").append(testEmail).append(" (including spam folder)\n");
            } else {
                apiResult.append("❌ EmailJS API: FAILED\n");
                apiResult.append("Response: ").append(responseBody).append("\n");
                
                // Provide specific error guidance
                switch (responseCode) {
                    case 400:
                        apiResult.append("→ Bad Request: Check service/template/user IDs\n");
                        break;
                    case 401:
                        apiResult.append("→ Unauthorized: Check user ID (public key)\n");
                        break;
                    case 402:
                        apiResult.append("→ Payment Required: EmailJS account limit reached\n");
                        break;
                    case 404:
                        apiResult.append("→ Not Found: Service or template doesn't exist\n");
                        break;
                    case 429:
                        apiResult.append("→ Too Many Requests: Rate limit exceeded\n");
                        break;
                    default:
                        apiResult.append("→ Server Error: Try again later\n");
                }
            }
            
            return apiResult.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "EmailJS API test failed", e);
            return "❌ EmailJS API Test Exception: " + e.getMessage() + "\n";
        }
    }
}
