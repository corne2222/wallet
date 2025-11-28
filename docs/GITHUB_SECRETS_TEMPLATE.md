# GitHub Secrets Configuration Template

This template provides a step-by-step checklist for adding Android signing secrets to GitHub.

## Prerequisites

- [x] Android keystore generated (see `ANDROID_KEYSTORE_SETUP.md` or run `scripts/generate-android-keystore.sh`)
- [x] Repository admin or maintainer access
- [x] Keystore credentials saved securely

## Access GitHub Secrets

1. Navigate to your repository on GitHub
2. Go to: **Settings** → **Secrets and variables** → **Actions**
3. You should see "Actions secrets and variables" page

## Add Required Secrets

Follow these steps for each secret below:

### Secret 1: ANDROID_KEYSTORE_BASE64

- [ ] Click **New repository secret**
- [ ] **Name:** `ANDROID_KEYSTORE_BASE64`
- [ ] **Secret:** Paste the entire contents of `release.keystore.base64` file
  - This is a long single-line base64 string
  - Should start with something like: `/u3+7QAAAAIAAA...`
  - Length: ~3000-6000 characters depending on key size
- [ ] Click **Add secret**

### Secret 2: ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD

- [ ] Click **New repository secret**
- [ ] **Name:** `ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD`
- [ ] **Secret:** Paste the store password
  - This is the password that protects the keystore file
  - Should be 32 characters if generated with provided script
  - Example format: `Abc123XyzDef456GhiJkl789Mno012Pqr`
- [ ] Click **Add secret**

### Secret 3: ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS

- [ ] Click **New repository secret**
- [ ] **Name:** `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS`
- [ ] **Secret:** `attocash-release`
  - This is the alias/name of the key inside the keystore
  - Use the value exactly as shown (unless you used a different alias)
- [ ] Click **Add secret**

### Secret 4: ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD

- [ ] Click **New repository secret**
- [ ] **Name:** `ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD`
- [ ] **Secret:** Paste the key password
  - This is the password that protects the private key
  - Should be 32 characters if generated with provided script
  - Example format: `Xyz789AbcDef123GhiJkl456Mno789Pqr`
  - **Note:** Can be the same as store password (modern keystores)
- [ ] Click **Add secret**

## Verification Checklist

After adding all secrets:

- [ ] Verify all 4 secrets are listed in the repository secrets page
- [ ] Secret names match exactly (case-sensitive, no typos):
  - `ANDROID_KEYSTORE_BASE64`
  - `ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD`
  - `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS`
  - `ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD`
- [ ] No extra spaces before or after secret values
- [ ] Base64 keystore is a single line (no newlines)

## Test the Setup

### Option 1: Manual Workflow Trigger

1. Go to **Actions** tab
2. Select **Android Build** workflow
3. Click **Run workflow** dropdown
4. Select the branch (e.g., `main`)
5. Click **Run workflow** button
6. Monitor the workflow execution
7. Check logs for signing confirmation

### Option 2: Push a Commit

1. Make any small change (e.g., update README)
2. Commit and push to main branch
3. Workflow will trigger automatically
4. Check Actions tab for build status

### Verify Signed APK

After successful workflow:

1. Go to workflow run page
2. Download **android-apk** artifact
3. Extract the APK file
4. Verify signature locally:

```bash
jarsigner -verify -verbose -certs app-release.apk
```

Expected output:
```
jar verified.

This jar contains entries whose certificate chain is invalid.
Re-run with the -verbose option for more details.
```

Note: The "invalid" warning is expected for self-signed certificates and is fine for internal testing.

For production Play Store releases, you'll need to upload to Google Play Console and let Google sign the final APK.

## Common Issues and Solutions

### Issue: Secret not found

**Symptoms:** Workflow shows empty or missing secret values

**Solutions:**
- Verify secret names match exactly (case-sensitive)
- Check for typos in secret names
- Ensure secrets are added to the correct repository
- Try re-adding the secret

### Issue: Invalid keystore format

**Symptoms:** `keystore load error` in workflow logs

**Solutions:**
- Verify base64 encoding has no line breaks
- Re-encode: `openssl base64 -A -in release.keystore > release.keystore.base64`
- Copy entire content including no trailing newline
- Paste as-is into GitHub secret

### Issue: Incorrect password

**Symptoms:** `keystore password was incorrect`

**Solutions:**
- Double-check password value copied correctly
- No extra spaces before/after password
- Verify password matches the keystore
- Test locally: `keytool -list -keystore release.keystore -storepass "YOUR_PASSWORD"`

### Issue: Key alias not found

**Symptoms:** `Alias <attocash-release> does not exist`

**Solutions:**
- Verify key alias in keystore: `keytool -list -keystore release.keystore`
- Update `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS` secret to match actual alias
- Or regenerate keystore with correct alias

## Security Reminders

✅ **DO:**
- Store keystore backup in encrypted secure location
- Use password manager for credentials
- Limit repository admin access
- Rotate keystore every few years
- Test locally before production

❌ **DON'T:**
- Commit keystore files to repository
- Share passwords via email/chat
- Use weak passwords
- Give unnecessary people admin access
- Forget to backup keystore

## Additional Resources

- [Complete Setup Guide](ANDROID_KEYSTORE_SETUP.md)
- [Setup Summary](../KEYSTORE_SETUP_SUMMARY.md)
- [Android Signing Documentation](https://developer.android.com/studio/publish/app-signing)
- [GitHub Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

## Support

If you continue to have issues:

1. Review workflow logs for specific error messages
2. Test keystore locally with provided commands
3. Verify all prerequisites are met
4. Check repository permissions
5. Consult Android signing documentation

---

**Completion:** After all checkboxes above are marked and workflow succeeds, signing setup is complete! ✓
