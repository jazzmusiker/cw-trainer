#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
APK_DIR="$SCRIPT_DIR/androidApp/build/outputs/apk"

cd "$SCRIPT_DIR"
./gradlew --console=plain :androidApp:assembleDebug

if [[ ! -d "$APK_DIR" ]]; then
    printf 'Build abgeschlossen, aber das APK-Verzeichnis wurde nicht gefunden: %s\n' "$APK_DIR" >&2
    exit 1
fi

found_apk=0
while IFS= read -r -d '' apk_path; do
    printf 'APK erzeugt: %s\n' "$apk_path"
    found_apk=1
done < <(find "$APK_DIR" -type f -name '*.apk' -print0)

if [[ "$found_apk" -eq 0 ]]; then
    printf 'Build abgeschlossen, aber es wurde keine APK-Datei gefunden.\n' >&2
    exit 1
fi
