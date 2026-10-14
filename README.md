# APEX SOUND

FxSound-inspired Android audio enhancer with a black / cyan / purple gaming UI.

## Current implementation
- Android app with 10 displayed EQ controls, six presets, bass boost and virtual surround controls.
- Select and play a local audio file; Android audio effects are attached to this app's playback session.
- Effects depend on the device's Android audio-effect implementation and supported band count.

## Important limitations
- This does **not** process audio system-wide. Android generally does not let an ordinary third-party app attach its audio effects to arbitrary other apps.
- The virtualizer is Android's supported `Virtualizer` effect, not guaranteed true 3D/spatial audio.
- This starter project has not been built or device-tested in this environment. Verify the GitHub Actions build before installing.

## Build
Open this project in Android Studio (JDK 17, Android SDK 35) and run the `app` configuration, or push it to GitHub and use **Actions → Build APEX SOUND APK → Run workflow**. Download the `APEX-SOUND-debug-apk` artifact from a successful run.

## GitHub setup
Create a new repository named `APEX-SOUND`, upload the contents of this folder (not the folder itself), and push to `main`. The workflow builds a debug APK on push or manual dispatch.
