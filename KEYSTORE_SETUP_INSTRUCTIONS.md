# Android Keystore Setup Instructions

## 🎯 Quick Overview

This guide helps you configure GitHub Secrets to enable Android APK signing in the release workflow. Follow these step-by-step instructions to add the required secrets to your GitHub repository.

---

## 📦 What Has Been Generated

The following files have been created for you:

1. **`composeApp/release.keystore`** - The Android release keystore file
2. **`release.keystore.base64`** - Base64 encoded version of the keystore (ready to copy)

### Keystore Details

- **Format:** JKS (Java KeyStore)
- **Key Alias:** `atto_cash`
- **Key Algorithm:** RSA 2048-bit
- **Validity:** 10,000 days (~27 years)
- **Distinguished Name:**
  - CN=Atto Cash Wallet
  - OU=Engineering
  - O=Atto Cash
  - L=Remote
  - ST=Global
  - C=US

---

## 🔑 Required GitHub Secrets

You need to add **4 secrets** to your GitHub repository. Below are the exact names and where to find the values:

| # | Secret Name | Where to Get the Value | Example/Format |
|---|-------------|------------------------|----------------|
| 1 | `ANDROID_KEYSTORE_BASE64` | Copy the entire contents of `release.keystore.base64` | `/u3+7QAAAAIAAA...` (long base64 string) |
| 2 | `ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD` | Use: `AttoCashStorePass2024!` | Plain text password |
| 3 | `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS` | Use: `atto_cash` | Plain text alias |
| 4 | `ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD` | Use: `AttoCashKeyPass2024!` | Plain text password |

---

## 📋 Step-by-Step Instructions

### Step 1: Open the Base64 Keystore File

1. Open the file: **`release.keystore.base64`** (located in the project root)
2. **Select all the text** in the file (it will be a very long single line)
3. **Copy** it to your clipboard (Ctrl+C / Cmd+C)
4. Keep this window open - you'll need it in Step 3

### Step 2: Navigate to GitHub Secrets Settings

1. Open your GitHub repository in a web browser
2. Click on **Settings** (top menu bar)
3. In the left sidebar, scroll down to **Security** section
4. Click **Secrets and variables**
5. Click **Actions**
6. You should now see the "Actions secrets" page

### Step 3: Add Secret #1 - ANDROID_KEYSTORE_BASE64

1. Click the **New repository secret** button (green button)
2. In the **Name** field, enter exactly: `ANDROID_KEYSTORE_BASE64`
3. In the **Secret** field, paste the base64 string you copied in Step 1
4. Click **Add secret** button
5. ✅ You should see "Secret ANDROID_KEYSTORE_BASE64 has been added"

### Step 4: Add Secret #2 - Store Password

1. Click **New repository secret** again
2. In the **Name** field, enter exactly: `ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD`
3. In the **Secret** field, enter exactly: `AttoCashStorePass2024!`
4. Click **Add secret**
5. ✅ You should see the confirmation message

### Step 5: Add Secret #3 - Key Alias

1. Click **New repository secret** again
2. In the **Name** field, enter exactly: `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS`
3. In the **Secret** field, enter exactly: `atto_cash`
4. Click **Add secret**
5. ✅ You should see the confirmation message

### Step 6: Add Secret #4 - Key Password

1. Click **New repository secret** again
2. In the **Name** field, enter exactly: `ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD`
3. In the **Secret** field, enter exactly: `AttoCashKeyPass2024!`
4. Click **Add secret**
5. ✅ You should see the confirmation message

---

## ✅ Verification Checklist

After adding all secrets, verify you have exactly 4 secrets configured:

- [ ] `ANDROID_KEYSTORE_BASE64` - Contains the long base64 string
- [ ] `ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD` - Contains: `AttoCashStorePass2024!`
- [ ] `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS` - Contains: `atto_cash`
- [ ] `ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD` - Contains: `AttoCashKeyPass2024!`

**Important:** The secret names are case-sensitive and must match exactly!

---

## 🧪 Testing the Setup

### Option 1: Trigger a Release Workflow

1. Go to **Actions** tab in GitHub
2. Select **Release** workflow
3. Click **Run workflow** button
4. Fill in the required inputs (if any)
5. Click **Run workflow**
6. Wait for the workflow to complete
7. Check that the Android build job succeeds ✅

### Option 2: Create a Release Tag

1. Create and push a tag starting with `v`:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. The release workflow will automatically trigger
3. Check the Actions tab to monitor progress

### Expected Results

When the workflow runs successfully:
- ✅ Android build job completes without errors
- ✅ Signed APK and AAB files are generated
- ✅ Artifacts are uploaded to the workflow run
- ✅ Release is created with downloadable files

---

## 🔒 Security Notes

### ⚠️ IMPORTANT - Keep These Secure

1. **Never commit the keystore file** to version control
   - The `.gitignore` already excludes `*.keystore` files
   - This prevents accidental commits

2. **Store the keystore file securely**
   - Keep a backup in a password manager (as an attachment)
   - Store in encrypted storage (e.g., encrypted USB drive)
   - Create multiple backups in different secure locations

3. **Save the passwords securely**
   - Store in a password manager
   - Never share via email, chat, or commit to git
   - Keep separate from the keystore file location

4. **Access Control**
   - Only repository admins can view/edit secrets
   - Regularly audit who has admin access
   - Secrets are redacted in workflow logs

### 📝 Record Keeping

Keep a secure record of:
- Keystore file location (backup)
- Store password: `AttoCashStorePass2024!`
- Key password: `AttoCashKeyPass2024!`
- Key alias: `atto_cash`
- Certificate fingerprints (from keytool output)

---

## 🆘 Troubleshooting

### Secret Not Found Error

**Problem:** Workflow fails with "secret not found" or similar error

**Solution:**
- Verify all 4 secrets are added
- Check secret names match exactly (case-sensitive)
- Ensure no extra spaces in secret names

### Keystore Password Error

**Problem:** Workflow fails with "keystore password was incorrect"

**Solution:**
- Double-check the password in secrets matches: `AttoCashStorePass2024!`
- Ensure no extra spaces before/after the password
- Re-add the secret if necessary

### Base64 Decode Error

**Problem:** Workflow fails when decoding keystore

**Solution:**
1. Open `release.keystore.base64`
2. Ensure it's a single line with no line breaks
3. Copy the entire content again
4. Update the `ANDROID_KEYSTORE_BASE64` secret with the new value

### Signing Config Not Applied

**Problem:** APK builds but is not signed

**Solution:**
- Ensure all 4 secrets are present
- Check workflow logs for environment variable errors
- Verify the secrets are passed correctly in `release.yaml`

---

## 📞 Getting Help

If you encounter issues:

1. **Check workflow logs:**
   - Go to Actions tab → Select failed workflow run
   - Expand the "Run Android Build and Tests" step
   - Look for specific error messages

2. **Verify secrets:**
   - Go to Settings → Secrets and variables → Actions
   - Count that you have exactly 4 secrets
   - Secret names must match exactly

3. **Test locally:**
   - Decode the keystore locally to verify it's valid:
     ```bash
     base64 -d release.keystore.base64 > test.keystore
     keytool -list -v -keystore test.keystore -storepass "AttoCashStorePass2024!"
     ```

---

## 📚 Additional Resources

- **Detailed Guide:** See `docs/ANDROID_KEYSTORE_SETUP.md` for comprehensive documentation
- **Verification Script:** Run `scripts/verify-keystore.sh` to validate keystore locally
- **Workflow Files:**
  - `.github/workflows/release.yaml` - Main release workflow
  - `.github/workflows/android-build.yaml` - Android build workflow
- **Build Configuration:** `composeApp/build.gradle.kts` - Gradle signing setup

---

## ✨ Quick Reference Card

Copy this for easy reference:

```
REQUIRED GITHUB SECRETS (Repository Settings → Secrets → Actions)
==================================================================

1. ANDROID_KEYSTORE_BASE64
   Value: [Copy from release.keystore.base64]

2. ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD
   Value: AttoCashStorePass2024!

3. ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS
   Value: atto_cash

4. ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD
   Value: AttoCashKeyPass2024!
```

---

**Last Updated:** November 28, 2025  
**Status:** ✅ Ready for GitHub Secrets Configuration
