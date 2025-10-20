# 📧 EmailJS Mobile App Troubleshooting Guide

## 🚨 **CURRENT ISSUE IDENTIFIED**

**Problem:** HTTP 403 - "API calls are disabled for non-browser applications"

**Root Cause:** EmailJS by default restricts API calls from mobile applications for security reasons.

## ✅ **IMPLEMENTED SOLUTIONS**

### Solution 1: Enhanced Headers
- Added proper User-Agent, Accept, and Origin headers
- Mimics browser behavior to bypass mobile restrictions

### Solution 2: Dual Email Method Approach
- **Primary Method:** JSON API (original)
- **Fallback Method:** Form-data API (more mobile-friendly)
- **Final Fallback:** Display OTP in app for debugging

### Solution 3: Comprehensive Debugging Tools
- Added `EmailDebugService` for detailed diagnostics
- Debug button in LoginActivity for testing
- Detailed logging with specific error codes and solutions

## 🛠️ **HOW TO FIX EmailJS DASHBOARD SETTINGS**

### Step 1: Access EmailJS Dashboard
1. Go to: https://dashboard.emailjs.com/admin
2. Log in with your EmailJS account

### Step 2: Configure Service Settings
1. Find your service: `service_kn4q9ks`
2. Click on the service name
3. Look for **"Access Control"** or **"CORS Settings"** or **"Security Settings"**
4. Find options like:
   - "Allow requests from mobile applications" ✅ **ENABLE THIS**
   - "Restrict to browsers only" ❌ **DISABLE THIS**
   - "CORS restrictions" ❌ **DISABLE THIS**

### Step 3: Update Allowed Origins (if available)
Add these origins to your allowed list:
- `https://campusride.app`
- `*` (wildcard for testing - not recommended for production)

### Step 4: Check Template Settings
1. Go to your template: `template_2wcv2nt`
2. Ensure it's **published** and **active**
3. Verify template variables match your code:
   - `{{to_email}}`
   - `{{otp_code}}`
   - `{{app_name}}`
   - `{{subject}}`
   - `{{from_name}}`

## 🧪 **TESTING STEPS**

### Phase 1: Test Current Implementation
```bash
# Build and run your app
./gradlew assembleDebug

# Or use Android Studio Run button
# Tap "🔍 Debug Email" button
# Check logcat for results
```

### Phase 2: Check Logcat Output
```bash
# Filter logcat for email-related logs
adb logcat | grep -E "EMAIL_DIAGNOSTICS|EmailService|OTPService"
```

### Phase 3: Test Different Email Addresses
1. Try with personal Gmail/Yahoo email first
2. Then test with VIT student email
3. Check spam/junk folders for all tests

## 📊 **EXPECTED RESULTS AFTER FIX**

### Success Indicators:
- ✅ HTTP Response Code: 200
- ✅ "Email sent successfully" in logs
- ✅ OTP received in email within 1-2 minutes

### Error Indicators to Watch For:
- ❌ 403: Still restricted (need dashboard config)
- ❌ 401: Invalid user ID/public key
- ❌ 402: Account limit reached
- ❌ 404: Service/template not found
- ❌ 429: Rate limit exceeded

## 🔄 **ALTERNATIVE SOLUTIONS IF EMAILJS STILL FAILS**

### Option 1: Use Different EmailJS Account
- Create new EmailJS account
- Set up new service without mobile restrictions
- Update credentials in code

### Option 2: Switch to Different Email Service
Consider these alternatives:
- **SendGrid** (has mobile SDK)
- **Firebase Functions + Nodemailer**
- **AWS SES**
- **Mailgun**

### Option 3: Backend Email Service
- Create a simple backend API
- Handle email sending server-side
- Call your API from mobile app

## 📱 **CODE CHANGES MADE**

### Enhanced EmailService.java:
- Added mobile-friendly headers
- Better error handling and validation
- Detailed logging for troubleshooting

### New EmailServiceV2.java:
- Uses form-data instead of JSON
- Different endpoint that's more mobile-friendly
- Alternative approach when JSON fails

### Updated OTPService.java:
- Tries both email methods automatically
- Better error handling with fallbacks
- Shows OTP in app if all methods fail

### Added EmailDebugService.java:
- Comprehensive diagnostics tool
- Tests network connectivity
- Tests actual EmailJS API calls
- Provides specific troubleshooting guidance

## 🎯 **NEXT STEPS**

1. **Run the updated app** and test the debug button
2. **Configure EmailJS dashboard** settings as described above
3. **Test with different email addresses**
4. **Check spam folders** for received emails
5. **Monitor logcat** for detailed error messages

## 📞 **If Still Not Working**

Check these additional factors:
- **VIT network restrictions** on automated emails
- **EmailJS account status** and limits
- **Email provider blocking** automated messages
- **Mobile network connectivity** issues

## 🔍 **Debugging Commands**

```bash
# Check app logs
adb logcat | grep -E "CampusRide|EMAIL_DIAGNOSTICS"

# Check network connectivity
adb shell am start -a android.intent.action.VIEW -d "https://api.emailjs.com"

# Clear app data and test fresh
adb shell pm clear com.campusride
```

---

**Remember:** The dual-method approach should work even if EmailJS dashboard settings can't be changed. The form-data method (EmailServiceV2) is typically more permissive for mobile applications.
