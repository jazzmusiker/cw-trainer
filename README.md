# CW-Trainer

Compose Multiplatform Morse trainer for Android, macOS and Linux.

## Run

- Desktop: `./gradlew :desktopApp:run`
- Android: open the project in Android Studio or run `./gradlew :androidApp:installDebug`

Profiles can be added, renamed and deleted (the last profile is kept) and are stored locally on each device. The training statistics described in `cw.md` are intentionally not implemented because the learner feedback interaction is still unspecified.
