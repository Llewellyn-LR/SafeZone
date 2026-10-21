# SafeZone (Android)

## Run the app

1. Install Android Studio and JDK 17.
2. Open Android Studio, choose **Open**, and select this project folder.
3. Wait for the Gradle sync to finish.
4. Pick an emulator or connect a phone, then press **Run**.

## Test mode (no setup)

If there is no `google-services.json` file, the app runs with sample data. Nothing is saved. Log in with any email containing `@` and a password of 6 or more characters.

## Real data

To use real data, follow [FIREBASE_SETUP.md](FIREBASE_SETUP.md).

## Run the tests

```
./gradlew test
```

## Who sees what

- **Residents and guests** see the main app (Home, Map, Report, Alerts, Profile).
- **Moderators** see the moderation dashboard. They are set up in Firebase (see FIREBASE_SETUP.md).
