#!/usr/bin/env bash
# Build debug APK lalu install/update ke HP yang terhubung via USB (USB debugging ON).
# Pakai: bash install_debug.sh [--no-build]
set -u
export JAVA_HOME="D:/Devs/toolchain/jdk21/jdk-21.0.13+9"
export ANDROID_HOME="D:/Devs/toolchain/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export GRADLE_USER_HOME="D:/Devs/.gradle"
ADB="$ANDROID_HOME/platform-tools/adb.exe"
PROJ=/d/Devs/SIMAP-APK
# adb.exe is a native Windows program: it cannot read MSYS paths like /d/Devs/...
APK="D:/Devs/SIMAP-APK/app/build/outputs/apk/debug/app-debug.apk"

COUNT=$("$ADB" devices | grep -cE "^\S+\s+device$")
if [ "$COUNT" -eq 0 ]; then
  echo "Tidak ada HP terdeteksi. Cek: kabel data, USB debugging ON, dan dialog 'Allow USB debugging' sudah diizinkan."
  "$ADB" devices -l
  exit 1
fi
if [ "$COUNT" -gt 1 ]; then
  echo "Ada $COUNT device terhubung; pakai 'adb -s <serial>' manual." >&2
fi

if [ "${1:-}" != "--no-build" ]; then
  echo "== build =="
  cd "$PROJ" || exit 1
  ./gradlew --no-daemon assembleDebug || exit 1
fi

echo "== install =="
"$ADB" install -r -d "$APK"
echo "== buka aplikasi =="
"$ADB" shell monkey -p com.arffsnia.simap -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 && echo "app dibuka"
