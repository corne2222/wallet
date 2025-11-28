#!/bin/bash

# Script to verify Android keystore configuration
# Run this after generating keystore to ensure it's valid

set -e

KEYSTORE_FILE="${1:-release.keystore}"
STORE_PASSWORD="${2}"

if [ -z "$STORE_PASSWORD" ]; then
    echo "Usage: $0 <keystore-file> <store-password>"
    echo "Example: $0 release.keystore 'yourpassword'"
    exit 1
fi

if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "Error: Keystore file not found: $KEYSTORE_FILE"
    exit 1
fi

echo "================================================================"
echo "Android Keystore Verification"
echo "================================================================"
echo ""
echo "Keystore file: $KEYSTORE_FILE"
echo ""

echo "1. Checking keystore validity..."
if keytool -list -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASSWORD" > /dev/null 2>&1; then
    echo "   ✓ Keystore is valid and password is correct"
else
    echo "   ✗ Failed to access keystore (check password)"
    exit 1
fi

echo ""
echo "2. Keystore details:"
echo "----------------------------------------------------------------"
keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASSWORD" | head -30
echo "----------------------------------------------------------------"

echo ""
echo "3. Checking key specifications..."
KEYSTORE_TYPE=$(keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASSWORD" | grep "Keystore type:" | awk '{print $3}')
KEY_SIZE=$(keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASSWORD" | grep "Subject Public Key Algorithm:" | grep -oP '\d+(?=-bit)')
SIG_ALG=$(keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASSWORD" | grep "Signature algorithm name:" | awk '{print $4}')

echo "   Keystore Type: $KEYSTORE_TYPE"
echo "   Key Size: ${KEY_SIZE}-bit"
echo "   Signature Algorithm: $SIG_ALG"

# Recommendations
echo ""
echo "4. Security recommendations:"
if [ "$KEY_SIZE" -ge 4096 ]; then
    echo "   ✓ Key size is strong (${KEY_SIZE}-bit)"
elif [ "$KEY_SIZE" -ge 2048 ]; then
    echo "   ⚠ Key size is acceptable but consider 4096-bit for better security"
else
    echo "   ✗ Key size is weak (${KEY_SIZE}-bit), regenerate with 4096-bit"
fi

if [[ "$SIG_ALG" == *"SHA256"* ]] || [[ "$SIG_ALG" == *"SHA384"* ]] || [[ "$SIG_ALG" == *"SHA512"* ]]; then
    echo "   ✓ Signature algorithm is secure ($SIG_ALG)"
else
    echo "   ⚠ Consider using SHA256 or better for signature algorithm"
fi

echo ""
echo "5. Base64 encoding test..."
BASE64_FILE="${KEYSTORE_FILE}.base64"
openssl base64 -A -in "$KEYSTORE_FILE" > "$BASE64_FILE"
BASE64_SIZE=$(wc -c < "$BASE64_FILE")
echo "   ✓ Base64 file created: $BASE64_FILE"
echo "   ✓ Size: $BASE64_SIZE characters"

echo ""
echo "6. Decoding verification..."
TEMP_KEYSTORE="/tmp/test-decode.keystore"
openssl base64 -d -A -in "$BASE64_FILE" > "$TEMP_KEYSTORE"
if keytool -list -keystore "$TEMP_KEYSTORE" -storepass "$STORE_PASSWORD" > /dev/null 2>&1; then
    echo "   ✓ Base64 encoding/decoding works correctly"
    rm "$TEMP_KEYSTORE"
else
    echo "   ✗ Base64 decoding failed"
    rm "$TEMP_KEYSTORE"
    exit 1
fi

echo ""
echo "================================================================"
echo "Verification Complete!"
echo "================================================================"
echo ""
echo "Your keystore is ready for use. Next steps:"
echo ""
echo "1. Store keystore securely (encrypted backup)"
echo "2. Add to GitHub Secrets:"
echo "   - ANDROID_KEYSTORE_BASE64: $(head -c 60 "$BASE64_FILE")..."
echo "   - ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD: [your password]"
echo "   - ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS: [your alias]"
echo "   - ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD: [your key password]"
echo ""
echo "3. See docs/GITHUB_SECRETS_TEMPLATE.md for detailed instructions"
echo ""
