# Dream Wedding Stories (Google TV)

Dream Wedding Stories is a private premium wedding video streaming app for Google TV / Android TV.

## Tech Stack

- Kotlin
- Jetpack Compose for TV
- MVVM + Hilt
- Firebase Authentication (email/password)
- Firebase Firestore
- Vimeo API v3 (video metadata + HLS stream)
- Media3 ExoPlayer (HLS playback)

## 1) Firebase Setup (`google-services.json`)

1. Open Firebase Console and create/select your project.
2. Add an Android app with package name `com.dreamweddingstories.tv`.
3. Download `google-services.json`.
4. Place it at:
   - `app/google-services.json`
5. In Firebase Auth, enable **Email/Password** provider.
6. In Firestore, create database (production mode recommended) and configure security rules.

## 2) Vimeo Access Token Setup

Edit `app/src/main/java/com/dreamweddingstories/tv/utils/Constants.kt`:

- Replace `VIMEO_ACCESS_TOKEN` with your Vimeo API token.

Use a token with permission to read video files/metadata from your private library.

## 3) Firestore Data Model

Create collections:

### `users` collection
Document ID = Firebase Auth UID

Example document:

```json
{
  "uid": "firebase_uid_123",
  "email": "couple@example.com",
  "displayName": "Aarav & Meera",
  "assignedVideoIds": ["video_1", "video_2"]
}
```

### `videos` collection
Document ID = `video_1` style id used in `assignedVideoIds`

Example document:

```json
{
  "id": "video_1",
  "vimeoVideoId": "123456789",
  "coupleNames": "Aarav & Meera",
  "weddingDate": "2026-02-10",
  "description": "Wedding highlights and full ceremony film.",
  "thumbnailUrl": "https://i.vimeocdn.com/video/xxxx.jpg",
  "duration": "42:18",
  "userId": "firebase_uid_123"
}
```

## 4) Run on TV Emulator

1. Open Android Studio Device Manager.
2. Create a **TV device** (Google TV / Android TV image).
3. Launch emulator.
4. Build and install app.

```powershell
cd C:\Users\pranj\AndroidStudioProjects\DreamWedding
.\gradlew.bat :app:installDebug
```

## 5) Build Release APK

```powershell
cd C:\Users\pranj\AndroidStudioProjects\DreamWedding
.\gradlew.bat :app:assembleRelease
```

Output path:

- `app/build/outputs/apk/release/app-release.apk`

## Notes

- Leanback launcher and TV feature declarations are configured in `app/src/main/AndroidManifest.xml`.
- Replace `app/src/main/res/drawable/banner.xml` with your final `banner.png` (320x180) when preparing production assets.
- Current ABI filters include `arm64-v8a` for Google Play 64-bit requirement.

