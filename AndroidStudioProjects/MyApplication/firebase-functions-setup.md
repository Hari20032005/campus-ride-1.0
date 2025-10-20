# Firebase Functions Setup for Email OTP

## Option 1: Firebase Functions + SendGrid (Recommended)

### Step 1: Setup Firebase Functions
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize Functions in your project
cd /path/to/your/project
firebase init functions
```

### Step 2: Create Email Function
Create `functions/index.js`:
```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const sgMail = require('@sendgrid/mail');

admin.initializeApp();

// Set your SendGrid API key
sgMail.setApiKey('YOUR_SENDGRID_API_KEY');

exports.sendOTPEmail = functions.database.ref('/otp_codes/{emailKey}')
  .onCreate(async (snapshot, context) => {
    const otpData = snapshot.val();
    const email = otpData.email;
    const otp = otpData.otp;
    
    const msg = {
      to: email,
      from: 'noreply@campusride.com', // Your verified sender
      subject: 'Campus Ride - Your OTP Code',
      text: `Your OTP code is: ${otp}. Valid for 5 minutes.`,
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <h2>Campus Ride - OTP Verification</h2>
          <p>Your verification code is:</p>
          <div style="background: #f0f0f0; padding: 20px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 3px;">
            ${otp}
          </div>
          <p>This code will expire in 5 minutes.</p>
          <p>If you didn't request this code, please ignore this email.</p>
        </div>
      `
    };
    
    try {
      await sgMail.send(msg);
      console.log('OTP email sent successfully to:', email);
    } catch (error) {
      console.error('Error sending email:', error);
    }
  });
```

### Step 3: Deploy Functions
```bash
firebase deploy --only functions
```

## Option 2: Third-party Email Services

### A. SendGrid (Popular choice)
- Sign up at https://sendgrid.com
- Get API key
- Add to your Android app directly

### B. EmailJS (Simple for client-side)
- Sign up at https://www.emailjs.com
- No server-side code needed

### C. AWS SES (if you use AWS)
- More complex but very reliable

## Quick Implementation for SendGrid in Android:

Add to build.gradle:
```kotlin
implementation 'com.sendgrid:sendgrid-java:4.9.3'
```

Update OTPService.java to call email service:
```java
// In generateAndSendOTP method, after storing OTP:
if (task.isSuccessful()) {
    sendEmailOTP(email, otp); // New method
    callback.onSuccess("OTP sent to your email address");
} else {
    // error handling
}

private void sendEmailOTP(String email, String otp) {
    // Implement SendGrid email sending
    // Or call Firebase Function
    // Or use EmailJS
}
```
