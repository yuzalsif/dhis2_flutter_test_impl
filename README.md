# DHIS2 Flutter Android SDK Login Test

This Flutter project demonstrates a basic integration of the DHIS2 Android SDK to test user login functionality. It utilizes Flutter Platform Channels to communicate with native Android (Kotlin) code that leverages the DHIS2 Android SDK.

## Features

*   User interface (Flutter) to input DHIS2 server URL, username, and password.
*   Platform channel communication to native Android code.
*   Native Android (Kotlin) implementation for:
    *   Initializing the DHIS2 Android SDK (`D2Manager` and `D2Configuration`).
    *   Performing user login against a DHIS2 instance.
    *   Checking if a user is currently logged in.
    *   Logging out the user.
*   Display of login status and error messages in the Flutter UI.

## Prerequisites

Before you can run this project, you need to have the following installed:

1.  **Flutter SDK:**
    *   Follow the official Flutter installation guide for your operating system: [Flutter Installation Guide](https://docs.flutter.dev/get-started/install)
    *   Verify your installation by running `flutter doctor`. Address any issues reported.
2.  **Android Studio:**
    *   Required for Android SDKs, emulator, and native Android development.
    *   Download from: [Android Studio](https://developer.android.com/studio)
    *   Ensure you have an Android Virtual Device (AVD) set up or a physical Android device connected for testing.
3.  **Java Development Kit (JDK):**
    *   Android development requires a JDK. Android Studio often bundles or helps you install a compatible version.
4.  **A DHIS2 Instance for Testing:**
    *   You'll need a DHIS2 server URL, a valid username, and a password to test the login. You can use a demo instance like those on [play.dhis2.org](https://play.dhis2.org).

## Project Setup and Running

1.  **Clone the Repository (if applicable) or Download the Code:**
    ```bash
    # If this were a git repository:
    # git clone <repository-url>
    # cd dhis2_flutter_test_impl
    ```

2.  **Install Flutter Packages:**
    Navigate to the project's root directory in your terminal and run:
    ```bash
    flutter pub get
    ```

3.  **Configure Android Native Side (DHIS2 SDK Version):**
    *   This project is configured to use DHIS2 Android SDK version `1.12.0`.
    *   The necessary dependency is added in `android/app/build.gradle`:
        ```gradle
        // android/app/build.gradle
        dependencies {
            // ... other dependencies
            implementation 'com.github.dhis2:dhis2-android-sdk:1.12.0' // DHIS2 Android SDK
            implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
            implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
            // ...
        }
        ```
    *   The `android/settings.gradle` file is configured to include `jitpack.io` as a repository source for the DHIS2 SDK and properly sets up Flutter's plugin loading.

4.  **Review Native Code (Optional but Recommended):**
    *   The core native logic resides in `android/app/src/main/kotlin/com/example/dhis2_flutter_test_impl/MainActivity.kt`.
    *   This file sets up the `MethodChannel` and handles calls from Flutter to interact with the DHIS2 SDK.

5.  **Run the Application:**
    *   Ensure an Android emulator is running or a physical Android device is connected and recognized by Flutter (`flutter devices`).
    *   From the project's root directory, run:
        ```bash
        flutter run
        ```
    *   The application should launch on your selected device/emulator.

6.  **Test Login:**
    *   The app will present fields for Server URL, Username, and Password.
    *   Pre-filled values might be present in `lib/main.dart` for convenience during development; update these with your actual test DHIS2 instance credentials.
    *   Enter your DHIS2 server details and credentials.
    *   Tap the "Login" button.
    *   Observe the status message for success or failure.

## Platform Channel Implementation

This project uses Flutter's Platform Channels to bridge communication between Dart (Flutter) and Kotlin (Native Android).

*   **Channel Name:** `dhis2.login.channel`

*   **Flutter Side (`lib/main.dart`):**
    *   A `MethodChannel` instance is created with the channel name.
    *   `platform.invokeMethod(<methodName>, <arguments>)` is used to call native methods like `login`, `logout`, and `isUserLoggedIn`.
    *   Arguments (e.g., server URL, username, password) are passed as a `Map`.
    *   Responses (success or `PlatformException` for errors) are handled asynchronously.

*   **Native Android Side (`MainActivity.kt`):**
    *   A `MethodChannel` is set up in `configureFlutterEngine`.
    *   `setMethodCallHandler` listens for method calls from Flutter.
    *   A `when` statement routes calls based on `call.method`.
    *   Arguments are extracted using `call.argument<Type>("key")`.
    *   DHIS2 SDK operations are performed (e.g., `D2Manager.blockingInstantiateD2`, `userModule().logIn()`).
        *   **Important:** DHIS2 SDK's blocking operations are executed on a background thread using Kotlin Coroutines (`CoroutineScope(Dispatchers.IO).launch`) to prevent freezing the UI.
    *   Results are sent back to Flutter using `result.success(...)` or `result.error(...)`.

## DHIS2 SDK Initialization (Native Android)

As per DHIS2 Android SDK `v1.12.0` documentation:

1.  A `D2Configuration` object is built, primarily requiring the application `context`. Other parameters like app name, version, and timeouts can also be set.
    ```kotlin
    val d2Configuration = D2Configuration.builder()
        .appName("dhis2_flutter_app")
        .appVersion("1.0.0")
        .context(applicationContext.applicationContext)
        .build()
    ```
2.  `D2Manager.blockingInstantiateD2(d2Configuration)` is called to initialize or reconfigure the SDK.
3.  The singleton `D2` instance is retrieved using `D2Manager.getD2()`.
4.  The login operation `d2Instance.userModule().logIn(username, password, serverUrl)` is then invoked.

## Project Structure Highlights

*   `lib/main.dart`: Contains the Flutter UI and platform channel client code.
*   `android/app/build.gradle`: Android module-level Gradle file, includes DHIS2 SDK dependency.
*   `android/settings.gradle`: Android project-level Gradle settings, configures repositories (like JitPack) and Flutter plugin loading.
*   `android/app/src/main/kotlin/.../MainActivity.kt`: Native Android code implementing the platform channel handler and DHIS2 SDK interaction.
*   `android/app/src/main/AndroidManifest.xml`: Includes necessary permissions like `android.permission.INTERNET`.

## Further Development & Considerations

*   **Error Handling:** Enhance error parsing from `D2Error` to provide more specific user feedback.
*   **State Management:** For a production app, use a robust Flutter state management solution (Provider, BLoC, Riverpod, etc.) to manage login state and user data.
*   **SDK Initialization Flow:** Consider initializing `D2Manager` earlier in the app lifecycle, perhaps with a splash screen, if the configuration is static.
*   **Data Synchronization:** After successful login, implement calls to synchronize metadata and data (e.g., `d2.metadataModule().blockingDownload()`).
*   **Security:** Always use HTTPS for DHIS2 instances in production. The SDK handles secure session management.
*   **Code Organization:** For larger integrations, move native DHIS2 logic out of `MainActivity.kt` into dedicated service or manager classes.
*   **iOS Integration:** This project focuses on Android. A similar platform channel implementation would be needed for iOS, using the DHIS2 iOS SDK.

## Troubleshooting

*   **Gradle Sync Issues:** Ensure your `android/settings.gradle` and `android/app/build.gradle` are correctly configured. Try `flutter clean`, then delete the `.gradle` folder in `android/`, and then run `flutter pub get` followed by `flutter run`. In Android Studio, `File > Invalidate Caches / Restart...` can also help.
*   **"Unresolved reference" in Kotlin:** Double-check imports in `MainActivity.kt` and ensure the DHIS2 SDK dependency version in `app/build.gradle` matches the API you're using.
*   **Network Errors:** Verify your DHIS2 server URL is correct and accessible. If using HTTP (not recommended for production), ensure `android:usesCleartextTraffic="true"` is set in `AndroidManifest.xml` (for Android 9+).