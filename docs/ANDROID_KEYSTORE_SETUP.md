# Android Keystore Setup Guide

This document describes how to generate and configure the Android release keystore for signing APKs in the CI/CD pipeline.

## Overview

The Android release build requires a keystore for signing APKs and AABs. This keystore and its credentials are stored as GitHub Secrets to enable automated signing during CI/CD builds.

## Prerequisites

- Java Development Kit (JDK) 11 or later installed
- Access to the GitHub repository settings
- Admin or maintainer permissions to add repository secrets

## Keystore Generation

### Step 1: Generate the Keystore

> **Shortcut:** Run `./scripts/generate-android-keystore.sh` from the repository root to automate all steps below.

Run the following command to generate a new release keystore:

```bash
keytool -genkeypair -v \
  -keystore release.keystore \
  -storetype JKS \
  -alias attocash-release \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storepass "YOUR_STORE_PASSWORD" \
  -keypass "YOUR_KEY_PASSWORD" \
  -dname "CN=Atto Cash Wallet, OU=Mobile, O=Atto Cash, L=San Francisco, ST=California, C=US"
```

**Important:**
- Replace `YOUR_STORE_PASSWORD` with a strong, randomly generated password
- Replace `YOUR_KEY_PASSWORD` with a different strong, randomly generated password
- The keystore will be valid for 10,000 days (~27 years)
- Uses 4096-bit RSA key for enhanced security

### Step 2: Generate Strong Passwords

Generate cryptographically secure passwords:

```bash
# Generate store password
openssl rand -base64 32 | tr -d '=\n' | cut -c1-32

# Generate key password
openssl rand -base64 32 | tr -d '=\n' | cut -c1-32
```

**Save these passwords securely!** You will need them for GitHub Secrets configuration.

### Step 3: Verify the Keystore

> **Shortcut:** Run `./scripts/verify-keystore.sh release.keystore "YOUR_PASSWORD"` to automate verification.

Verify that the keystore was created successfully:

```bash
keytool -list -v -keystore release.keystore -storepass "YOUR_STORE_PASSWORD"
```

You should see details about the certificate including:
- Keystore type: JKS
- Alias name: attocash-release
- Entry type: PrivateKeyEntry
- Subject Public Key Algorithm: 4096-bit RSA key
- Signature algorithm name: SHA384withRSA
- Validity period: ~27 years

### Step 4: Encode Keystore to Base64

Encode the keystore file to base64 for GitHub Secrets:

```bash
openssl base64 -A -in release.keystore > release.keystore.base64
```

The resulting `release.keystore.base64` file contains a single-line base64 encoded string.

## GitHub Secrets Configuration

### Required Secrets

Add the following secrets to your GitHub repository:

1. Navigate to: **Repository Settings → Secrets and variables → Actions**
2. Click **New repository secret** for each of the following:

| Secret Name | Description | Value |
|-------------|-------------|-------|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded keystore file | Content of `release.keystore.base64` |
| `ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD` | Keystore password | The store password you generated |
| `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS` | Key alias within keystore | `attocash-release` |
| `ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD` | Private key password | The key password you generated |

### Steps to Add Secrets

For each secret:

1. Click **New repository secret**
2. Enter the **Name** exactly as shown in the table above
3. Paste the corresponding **Value**
4. Click **Add secret**

## Security Best Practices

### Keystore Storage

- **DO NOT** commit the keystore file to version control
- The `.gitignore` already excludes `*.keystore` files
- Store the keystore file in a secure location (password manager, encrypted storage)
- Create encrypted backups of the keystore in multiple secure locations

### Password Management

- Use a password manager to store keystore passwords securely
- Never share passwords via email or chat
- Use different passwords for store and key
- Rotate passwords periodically (requires generating new keystore)

### Access Control

- Limit GitHub repository admin access to trusted team members
- Regularly audit who has access to repository secrets
- Use environment-specific secrets for staging vs. production if needed

## Workflow Integration

The CI/CD workflow (`.github/workflows/android-build.yaml`) automatically:

1. **Decodes the keystore** from base64
2. **Exports environment variables** for Gradle signing configuration
3. **Builds signed APKs and AABs** using the keystore

The `composeApp/build.gradle.kts` file is configured to:

- Read signing credentials from environment variables
- Apply signing configuration only when credentials are available
- Sign the release build automatically

## Verification

### Verify Workflow Configuration

Check that the workflow has access to secrets:

1. Go to **Actions** tab in GitHub
2. Run the **Android Build** workflow manually
3. Check the workflow logs for signing confirmation

### Verify Signed APK

After a successful build:

1. Download the APK artifact from the workflow run
2. Verify the signature:

```bash
jarsigner -verify -verbose -certs your-app-release.apk
```

You should see:
```
jar verified.
```

And certificate details matching your keystore.

## Troubleshooting

### Missing Secrets Error

**Error:** `Keystore not found` or `Signing config not applied`

**Solution:** Ensure all four secrets are added to GitHub with exact names:
- Check for typos in secret names
- Verify base64 encoding has no newlines
- Re-encode and re-upload if necessary

### Invalid Keystore Password

**Error:** `keystore password was incorrect`

**Solution:**
- Verify the password in GitHub Secrets matches the keystore
- Ensure no extra spaces or characters in the secret value
- Try decoding locally: `echo "$BASE64_STRING" | base64 -d > test.keystore`

### Keystore Format Issues

**Error:** `keystore load: DerInputStream.getLength(): lengthTag=127, too big`

**Solution:**
- Ensure keystore is JKS format (not PKCS12)
- Re-generate using `-storetype JKS` flag
- Verify base64 encoding is correct

## Maintenance

### Keystore Rotation

To rotate the keystore (every few years for security):

1. Generate a new keystore using steps above
2. Update all GitHub Secrets with new values
3. Test in a non-production environment first
4. Archive the old keystore securely (may be needed for old APK updates)

### Certificate Expiration

The generated certificate is valid for ~27 years. Set a calendar reminder to rotate the keystore before expiration (~2052).

## Additional Resources

- [Android Developer Guide - Sign Your App](https://developer.android.com/studio/publish/app-signing)
- [GitHub Encrypted Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Java keytool Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)

## Support

If you encounter issues with keystore configuration:

1. Verify all secrets are configured correctly
2. Check workflow logs for detailed error messages
3. Test keystore locally before uploading to GitHub
4. Consult the Android documentation for signing-related issues
