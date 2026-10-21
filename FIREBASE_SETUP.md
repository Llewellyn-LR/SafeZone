# Firebase Setup

Skip this file to run the app with sample data. Follow it to use real data.

## 1. Create a project

Go to https://console.firebase.google.com and create a project.

## 2. Add the Android app

Project settings -> Add app -> Android. Use the package name `com.safezone.app`.

## 3. Add the config file

Download `google-services.json` and put it in the `app/` folder, next to `app/build.gradle`. Run the app again. To go back to sample data, delete the file.

## 4. Turn on the services

- **Authentication** -> Sign-in method -> enable **Email/Password** and **Anonymous**.
- **Firestore Database** -> Create database (production mode).
- **Storage** -> Get started (production mode, needs the Blaze plan).
- **Cloud Messaging** -> nothing to set up. To send alerts, use "Compose notification" and target the topic `nmb_alerts`.

## 5. Firestore rules

Firestore -> Rules. Paste this and publish:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function isModerator() {
      return request.auth != null
        && exists(/databases/$(database)/documents/moderators/$(request.auth.uid));
    }

    match /moderators/{uid} {
      allow read: if request.auth != null && request.auth.uid == uid;
      allow write: if false;
    }

    match /hotspots/{hotspotId} {
      allow read: if true;
      allow write: if isModerator();
    }

    match /wanted_persons/{personId} {
      allow read: if true;
      allow write: if isModerator();
    }

    match /reports/{reportId} {
      allow create: if request.auth != null
        && request.resource.data.reporterUid == request.auth.uid
        && request.resource.data.status == 'pendingReview';
      allow read: if request.auth != null
        && (resource.data.reporterUid == request.auth.uid || isModerator());
      allow update: if isModerator();
    }
  }
}
```

## 6. Storage rules

Storage -> Rules. Paste this and publish:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /report_photos/{uid}/{fileName} {
      allow read: if request.auth != null
        && firestore.exists(/databases/(default)/documents/moderators/$(request.auth.uid));
      allow write: if request.auth != null
        && request.auth.uid == uid
        && request.resource.size < 5 * 1024 * 1024
        && request.resource.contentType.matches('image/.*');
    }

    match /wanted_photos/{uid}/{fileName} {
      allow read: if true;
      allow write: if request.auth != null
        && request.auth.uid == uid
        && firestore.exists(/databases/(default)/documents/moderators/$(request.auth.uid))
        && request.resource.size < 5 * 1024 * 1024
        && request.resource.contentType.matches('image/.*');
    }
  }
}
```

## 7. Create a moderator

1. Authentication -> Users -> **Add user**. Enter an email and password.
2. Copy that user's **User UID**.
3. Firestore Database -> Start collection -> ID `moderators`.
4. Set the Document ID to the UID you copied. Add any field (for example `name`).
5. Sign in to the app with that email and password. It opens the moderation dashboard.

To remove a moderator, delete their document in `moderators`.
