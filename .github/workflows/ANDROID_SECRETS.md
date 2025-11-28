# Android Build Secrets

This workflow requires the following GitHub repository secrets for signing release builds:

## Required Secrets

- `ANDROID_KEYSTORE_BASE64`: Base64-encoded Android keystore file (.jks or .keystore)
- `ANDROID_KEYSTORE_ALIAS`: Alias of the signing key in the keystore
- `ANDROID_KEYSTORE_PASSWORD`: Password for the keystore
- `ANDROID_KEY_PASSWORD`: Password for the signing key

## How to create the secrets

1. **Encode your keystore file:**
   ```bash
   base64 -i your-release-key.keystore
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