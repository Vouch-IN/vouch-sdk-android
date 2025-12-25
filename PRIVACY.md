# Privacy Disclosure Template for Vouch Android SDK

This document provides template language for disclosing the Vouch SDK's data collection in your app's privacy policy and Google Play Data Safety form.

## Privacy Policy Template

Add the following section to your app's privacy policy:

---

### Device Fingerprinting for Security

We use Vouch SDK to collect device fingerprint information for security purposes, including fraud prevention, account protection, and authentication verification.

**What information is collected:**

The SDK automatically collects technical device characteristics, including:

- **Hardware Information**: Screen dimensions, screen density, CPU cores, device memory, device model, device manufacturer
- **System Font Information**: List of installed system fonts and a cryptographic hash of the font list
- **System Configuration**: Android version, SDK version, language preferences, locale settings, timezone
- **Storage Capabilities**: Availability of SharedPreferences, KeyStore, and file system access

**How this information is used:**

Device fingerprint data is used to detect and prevent fraudulent activity, verify account authenticity, protect against unauthorized access, and enhance security of our services.

**Important privacy notes:**

- No personally identifiable information (PII) is collected
- No location data is collected
- Only INTERNET permission is required (for API communication)
- All data collection is automatic and occurs in the background
- Data is transmitted securely to Vouch servers via HTTPS

---

## Google Play Data Safety Form

When submitting your app to Google Play, configure the Data Safety section as follows:

### Data Types Collected

#### Device or other IDs

- **Collected**: Yes
- **Shared**: Yes (with Vouch)
- **Ephemeral**: No
- **Required**: Yes
- **Purpose**: Fraud prevention, security, and account management

#### App activity > Other user-generated content

- **Collected**: No

#### App info and performance > Diagnostics

- **Collected**: Yes
- **Purpose**: Fraud prevention, security

### Data Security

- ✅ Data is encrypted in transit
- ✅ Data transfer uses HTTPS
- ⬜ Users can request data deletion (depends on your implementation)

### Data Usage and Handling

**Device or other IDs - Collection & Sharing:**

- Collected for fraud prevention, security, account management
- Shared with service provider (Vouch)
- Not used for advertising or marketing

## Permissions Disclosure

### Required Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**Purpose**: Required for API communication with Vouch servers. This is the **only** permission required.

### No Dangerous Permissions

The SDK does **not** request:

- ❌ ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION
- ❌ READ_PHONE_STATE
- ❌ GET_ACCOUNTS
- ❌ READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE
- ❌ CAMERA, RECORD_AUDIO, etc.

## GDPR Compliance (EU/UK)

### GDPR Notice

Under GDPR, device fingerprinting may be considered processing of personal data. We process this data based on:

- **Legitimate Interest**: To protect our services and users from fraud and security threats
- **Contract Performance**: To provide secure access to our services

**Your GDPR rights**: Access, deletion, objection, and lodging complaints with supervisory authorities.

## CCPA Compliance (California)

### CCPA Notice

Under the California Consumer Privacy Act (CCPA), we collect:

- **Identifiers**: Device fingerprint, device model identifier
- **Internet or Network Information**: Device characteristics, system configuration

**Your CCPA rights**: Know what data is collected, request deletion, opt-out of sale (Note: We do not sell personal information).

## Integration Checklist

- [ ] Add privacy disclosure to your app's privacy policy
- [ ] Configure Google Play Data Safety form
- [ ] Add GDPR/CCPA notices if applicable
- [ ] Update terms of service to mention fraud prevention measures
- [ ] Ensure privacy policy URL is accessible in-app and in Play Console
- [ ] Test that the SDK functions without requesting dangerous permissions
- [ ] Review Google Play Developer Program Policies

---

**Disclaimer**: This template is provided for informational purposes only and does not constitute legal advice. Consult with a qualified attorney to ensure your privacy policy complies with all applicable laws.
