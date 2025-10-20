# EmailJS Setup Guide - Send Real OTP Emails

## 🚀 Quick Setup to Send Real Emails

### Step 1: Create EmailJS Account
1. Go to https://www.emailjs.com
2. Sign up for a free account
3. Verify your email address

### Step 2: Connect Email Service
1. In EmailJS dashboard, go to "Email Services"
2. Click "Add New Service"
3. Choose your email provider:
   - **Gmail** (recommended for testing)
   - **Outlook/Hotmail** 
   - **Yahoo Mail**
   - Or any SMTP service

#### For Gmail:
1. Select "Gmail"
2. Click "Connect Account"
3. Authorize EmailJS to use your Gmail
4. Note down the **Service ID** (e.g., "service_abc123")

### Step 3: Create Email Template
1. Go to "Email Templates" 
2. Click "Create New Template"
3. Use this template:

**Template Name**: `otp_verification`

**Subject**: `Campus Ride - Your OTP Code`

**Content**:
```html
<div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="text-align: center; margin-bottom: 30px;">
        <h1 style="color: #6366f1;">Campus Ride</h1>
    </div>
    
    <div style="background: #f8fafc; border-radius: 8px; padding: 24px; margin-bottom: 24px;">
        <h2 style="color: #1f2937; margin-top: 0;">OTP Verification</h2>
        <p style="color: #4b5563; font-size: 16px;">
            Hello! Here's your verification code for Campus Ride:
        </p>
        
        <div style="background: white; border: 2px solid #e5e7eb; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0;">
            <div style="font-size: 36px; font-weight: bold; color: #6366f1; letter-spacing: 8px;">
                {{otp_code}}
            </div>
        </div>
        
        <p style="color: #6b7280; font-size: 14px; margin-bottom: 0;">
            • This code expires in <strong>5 minutes</strong><br>
            • If you didn't request this, please ignore this email<br>
            • For security, don't share this code with anyone
        </p>
    </div>
    
    <div style="text-align: center; color: #9ca3af; font-size: 12px;">
        <p>Campus Ride - VIT University</p>
    </div>
</div>
```

4. **Template Variables to add**:
   - `otp_code` 
   - `to_email`
   - `app_name`

5. Click "Save"
6. Note down the **Template ID** (e.g., "template_xyz789")

### Step 4: Get API Keys
1. Go to "Account" → "General"
2. Find your **Public Key** (e.g., "user_abcdef123456")
3. Copy all three values:
   - Service ID
   - Template ID  
   - Public Key

### Step 5: Update Android Code

Replace the placeholder values in `EmailService.java`:

```java
// Replace these with your actual values from EmailJS
private static final String SERVICE_ID = "service_abc123";        // Your Service ID
private static final String TEMPLATE_ID = "template_xyz789";      // Your Template ID  
private static final String PUBLIC_KEY = "user_abcdef123456";     // Your Public Key
```

### Step 6: Update OTPService.java

Modify the OTPService to actually send emails:

```java
// Add at the top of OTPService.java
private EmailService emailService;

// In constructor, add:
public OTPService(Context context) {
    this.context = context;
    this.otpRef = FirebaseUtil.getDatabase().getReference("otp_codes");
    this.emailService = new EmailService(); // Add this line
}

// In generateAndSendOTP method, replace the success callback:
if (task.isSuccessful()) {
    Log.d(TAG, "✅ OTP successfully stored in Firebase!");
    
    // Send real email instead of showing OTP in toast
    emailService.sendOTPEmail(email, otp, new EmailService.EmailCallback() {
        @Override
        public void onEmailSent() {
            callback.onSuccess("OTP sent to your email address");
        }
        
        @Override
        public void onEmailFailed(String error) {
            Log.e(TAG, "Email sending failed: " + error);
            // Fallback: show OTP in app for development
            callback.onSuccess("OTP (email failed): " + otp);
        }
    });
} else {
    // existing error handling
}
```

## 🧪 Testing Steps

1. **Update EmailService.java** with your EmailJS credentials
2. **Rebuild project** in Android Studio
3. **Test with your real email**:
   - Enter your actual email address (must be @vitstudent.ac.in)
   - Click "Send OTP"
   - Check your email inbox (and spam folder!)
   - Enter the OTP from email

## 📧 Email Template Preview

Your users will receive an email like this:

```
Subject: Campus Ride - Your OTP Code

Campus Ride

OTP Verification
Hello! Here's your verification code for Campus Ride:

┌─────────────────┐
│   123456        │  ← The actual OTP
└─────────────────┘

• This code expires in 5 minutes
• If you didn't request this, please ignore this email  
• For security, don't share this code with anyone

Campus Ride - VIT University
```

## 🔧 Troubleshooting

**Email not received?**
- Check spam/junk folder
- Ensure EmailJS service is connected properly
- Verify template variables are correct
- Check Android Logcat for errors

**EmailJS limits:**
- Free tier: 200 emails/month
- Paid plans available for higher usage

## 💰 Cost
- **Free tier**: 200 emails/month (perfect for testing)
- **Paid plans**: Start from $15/month for 1000 emails

## ✅ Final Result
After setup, your users will:
1. Enter their @vitstudent.ac.in email
2. Click "Send OTP" 
3. Receive professional email with OTP code
4. Enter OTP to authenticate
5. Get logged in automatically

This provides a much better user experience than showing OTP in the app!
