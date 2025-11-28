# Android Build Secrets

This workflow requires the following GitHub repository secrets for signing release builds:

## Required Secrets

- `ANDROID_KEYSTORE_BASE64`: Base64-encoded Android keystore file (.jks or .keystore)
- `ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD`: Password for the keystore
- `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS`: Alias of the signing key in the keystore
- `ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD`: Password for the signing key

## How to create the secrets

1. **Encode your keystore file:**
   ```bash
   base64 -i your-release-key.keystore > release.keystore.base64
   ```

2. **Add the secrets to your GitHub repository:**
   - Go to Settings → Secrets and variables → Actions
   - Click "New repository secret"
   - Add each of the four secrets listed above

## Usage

The workflow will automatically use these secrets when:
- Building release APKs and AABs
- The secrets are available (no manual configuration needed)
- The keystore is decoded to `composeApp/release.keystore` during the build

If the secrets are not set, the workflow will build unsigned debug/release APKs for testing purposes.

## Setup Instructions

For complete step-by-step instructions on adding GitHub Secrets, see:
- **`KEYSTORE_SETUP_INSTRUCTIONS.md`** - Detailed guide with exact secret names and values
- **`KEYSTORE_SETUP_SUMMARY.md`** - Quick reference and summary