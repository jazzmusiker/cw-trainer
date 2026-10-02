#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
APK_DIR="$SCRIPT_DIR/androidApp/build/outputs/apk/release"
BUILD_APK_PATH="$APK_DIR/androidApp-release.apk"
VERSION="$(tr -d '[:space:]' < "$SCRIPT_DIR/version.txt")"
if [[ ! "$VERSION" =~ ^V[0-9]+(\.[0-9]+)*$ ]]; then
    printf 'Ungültige Version in version.txt: %s (erwartet wird zum Beispiel V1.0.0)\n' "$VERSION" >&2
    exit 1
fi
VERSION_TAG="${VERSION//./_}"
APK_PATH="$APK_DIR/cw_trainer_${VERSION_TAG}.apk"

cd "$SCRIPT_DIR"
./gradlew --console=plain :androidApp:assembleRelease

if [[ ! -f "$BUILD_APK_PATH" ]]; then
    printf 'Build abgeschlossen, aber die Release-APK wurde nicht gefunden: %s\n' "$BUILD_APK_PATH" >&2
    exit 1
fi

cp "$BUILD_APK_PATH" "$APK_PATH"
printf 'Release-APK erzeugt: %s\n' "$APK_PATH"
