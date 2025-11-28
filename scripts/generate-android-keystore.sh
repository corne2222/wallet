#!/bin/bash

# Script to generate Android release keystore for signing APKs
# This script should be run on a secure machine, not in CI/CD

set -e

echo "================================================================"
echo "Android Keystore Generation Script"
echo "================================================================"
echo ""
echo "This script will generate a new Android release keystore."
echo "The keystore and credentials should be stored securely and"
echo "added to GitHub Secrets for CI/CD signing."
echo ""

# Check for required tools
if ! command -v keytool &> /dev/null; then
    echo "Error: keytool not found. Please install Java JDK 11 or later."
    exit 1
fi

if ! command -v openssl &> /dev/null; then
    echo "Error: openssl not found. Please install OpenSSL."
    exit 1
fi

# Set keystore parameters
KEYSTORE_FILE="release.keystore"
KEY_ALIAS="attocash-release"
KEY_SIZE=4096
VALIDITY_DAYS=10000
STORE_TYPE="JKS"

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo "Warning: $KEYSTORE_FILE already exists!"
    read -p "Do you want to overwrite it? (yes/no): " OVERWRITE
    if [ "$OVERWRITE" != "yes" ]; then
        echo "Aborting."
        exit 1
    fi
    rm "$KEYSTORE_FILE"
fi

echo "Generating secure passwords..."
STORE_PASSWORD=$(openssl rand -base64 32 | tr -d '=\n' | cut -c1-32)
KEY_PASSWORD=$(openssl rand -base64 32 | tr -d '=\n' | cut -c1-32)

echo ""
echo "----------------------------------------------------------------"
echo "IMPORTANT: Save these credentials securely!"
echo "----------------------------------------------------------------"
echo "Store Password: $STORE_PASSWORD"
echo "Key Password:   $KEY_PASSWORD"
echo "Key Alias:      $KEY_ALIAS"
echo "----------------------------------------------------------------"
echo ""
echo "Press Enter to continue..."
read

echo "Generating keystore..."
keytool -genkeypair -v \
    -keystore "$KEYSTORE_FILE" \
    -storetype "$STORE_TYPE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize "$KEY_SIZE" \
    -validity "$VALIDITY_DAYS" \
    -storepass "$STORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "CN=Atto Cash Wallet, OU=Mobile, O=Atto Cash, L=San Francisco, ST=California, C=US"

echo ""
echo "Verifying keystore..."
keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASSWORD" | head -25

echo ""
echo "Generating base64 encoding for GitHub Secrets..."
BASE64_FILE="${KEYSTORE_FILE}.base64"
openssl base64 -A -in "$KEYSTORE_FILE" > "$BASE64_FILE"

echo ""
echo "================================================================"
echo "Keystore Generation Complete!"
echo "================================================================"
echo ""
echo "Files created:"
echo "  - $KEYSTORE_FILE (binary keystore file)"
echo "  - $BASE64_FILE (base64 encoded for GitHub)"
echo ""
echo "Next steps:"
echo ""
echo "1. Store these files securely (encrypted backup recommended)"
echo "2. Add GitHub Secrets (Settings → Secrets and variables → Actions):"
echo ""
echo "   Secret Name: ANDROID_KEYSTORE_BASE64"
echo "   Value: (paste entire contents of $BASE64_FILE)"
echo ""
echo "   Secret Name: ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD"
echo "   Value: $STORE_PASSWORD"
echo ""
echo "   Secret Name: ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS"
echo "   Value: $KEY_ALIAS"
echo ""
echo "   Secret Name: ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD"
echo "   Value: $KEY_PASSWORD"
echo ""
echo "3. DO NOT commit these files to version control!"
echo "4. See docs/ANDROID_KEYSTORE_SETUP.md for complete guide"
echo ""
echo "================================================================"
