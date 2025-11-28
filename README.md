# AttoCash Wallet

A multi-platform cryptocurrency wallet built with Kotlin Compose.

## Running from Source

### Desktop Application
```bash
./gradlew composeApp:run
```

### Web Application (Development)
```bash
./gradlew wasmJsBrowserDevelopmentRun 
```

## Build and Release Workflows

This project uses GitHub Actions for automated builds and releases. The workflows are organized as follows:

### Workflows

- **`build.yaml`** - Core reusable workflow for building desktop and web applications
  - Uses a matrix strategy to build for Linux (DEB), macOS (DMG), and Windows (MSI)
  - Builds WASM web distribution and publishes to GitHub Pages
  - Artifacts are named with format: `AttoCashWallet-{os}-{version}.{ext}`

- **`android-build.yaml`** - Android application build workflow
  - Builds APK and AAB files
  - Runs Android instrumented tests
  - Handles code signing with keystore secrets

- **`pipeline.yaml`** - CI pipeline for main branch
  - Triggers on push to main branch
  - Runs all builds and deploys web to GitHub Pages
  - Does not create releases

- **`pull-request.yaml`** - CI pipeline for pull requests
  - Runs all builds to validate changes
  - Does not create deployments or releases

- **`release.yaml`** - Release workflow for creating official releases
  - Triggers on git tags (e.g., `v1.0.0`) or manual dispatch
  - Builds all platforms and creates GitHub release
  - Generates SHA256 checksums for all binaries
  - Deploys web to GitHub Pages (production or staging)

### Required Secrets and Permissions

For Android builds:
- `ANDROID_KEYSTORE_BASE64` - Base64-encoded release keystore
- `ORG_GRADLE_PROJECT_RELEASE_STORE_PASSWORD` - Keystore password
- `ORG_GRADLE_PROJECT_RELEASE_KEY_ALIAS` - Key alias (use `attocash-release`)
- `ORG_GRADLE_PROJECT_RELEASE_KEY_PASSWORD` - Private key password

See [Android Keystore Setup Guide](docs/ANDROID_KEYSTORE_SETUP.md) for detailed instructions and [GitHub Secrets Template](docs/GITHUB_SECRETS_TEMPLATE.md) for a configuration checklist.

For GitHub Pages deployment:
- Repository permissions: `contents: read`, `pages: write`, `id-token: write`

For releases:
- Repository permissions: `contents: write`

### Release Process

1. Update version in `composeApp/build.gradle.kts`
2. Create and push a tag: `git tag v1.0.0 && git push origin v1.0.0`
3. The release workflow will automatically:
   - Build all platform binaries
   - Create a GitHub release with all artifacts
   - Generate SHA256 checksums
   - Deploy web version to GitHub Pages

Or trigger manually via the GitHub Actions UI with optional staging deployment.

## Development

### Project Structure
- `composeApp/` - Main application code
- `composeApp/build.gradle.kts` - Build configuration and version management
- `.github/workflows/` - CI/CD workflows

### Testing
```bash
# Run all tests
./gradlew allTests

# Run specific platform tests
./gradlew :composeApp:test
./gradlew :composeApp:connectedDebugAndroidTest
``` 
