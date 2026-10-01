#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
APK_DIR="$SCRIPT_DIR/androidApp/build/outputs/apk"
APK_PATH="$APK_DIR/debug/androidApp-debug.apk"

if ! command -v adb >/dev/null 2>&1; then
    printf 'adb wurde nicht gefunden. Bitte Android SDK Platform-Tools installieren und adb im PATH verfügbar machen.\n' >&2
    exit 1
fi

cd "$SCRIPT_DIR"
./gradlew --console=plain :androidApp:assembleDebug

if [[ ! -f "$APK_PATH" ]]; then
    printf 'Build abgeschlossen, aber die Debug-APK wurde nicht gefunden: %s\n' "$APK_PATH" >&2
    exit 1
fi
printf 'APK erzeugt: %s\n' "$APK_PATH"

device_output="$(adb devices -l)"
device_serials=()
while IFS= read -r line; do
    read -r serial state _ <<< "$line"
    [[ -z "${serial:-}" || "$serial" == "List" ]] && continue
    if [[ "${state:-}" == "device" ]]; then
        device_serials+=("$serial")
    fi
done <<< "$device_output"

if (( ${#device_serials[@]} == 0 )); then
    printf 'Kein verfügbares Android-Gerät gefunden. Verbinde ein Gerät, aktiviere USB-Debugging und bestätige gegebenenfalls die Autorisierung.\n' >&2
    printf '%s\n' "$device_output" >&2
    exit 1
fi

selected_device="${device_serials[0]}"
if (( ${#device_serials[@]} > 1 )); then
    if [[ ! -t 0 ]]; then
        printf 'Mehrere Android-Geräte sind verfügbar. Starte das Skript in einem Terminal, um eines auszuwählen:\n' >&2
        printf '  %s\n' "${device_serials[@]}" >&2
        exit 1
    fi

    printf 'Mehrere Android-Geräte sind verfügbar:\n'
    for index in "${!device_serials[@]}"; do
        printf '  %d) %s\n' "$((index + 1))" "${device_serials[$index]}"
    done

    while true; do
        read -r -p 'Gerätenummer für die Installation: ' choice
        if [[ "$choice" =~ ^[0-9]+$ ]] && (( choice >= 1 && choice <= ${#device_serials[@]} )); then
            selected_device="${device_serials[$((choice - 1))]}"
            break
        fi
        printf 'Ungültige Auswahl. Bitte eine Nummer von 1 bis %d eingeben.\n' "${#device_serials[@]}" >&2
    done
fi

printf 'Installiere auf Gerät: %s\n' "$selected_device"
adb -s "$selected_device" install -r "$APK_PATH"
printf 'Installation abgeschlossen. APK: %s\n' "$APK_PATH"
