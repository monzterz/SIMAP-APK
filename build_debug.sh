#!/usr/bin/env bash
set -u
export JAVA_HOME="D:/Devs/toolchain/jdk21/jdk-21.0.13+9"
export ANDROID_HOME="D:/Devs/toolchain/android-sdk"
export ANDROID_SDK_ROOT="D:/Devs/toolchain/android-sdk"
export GRADLE_USER_HOME="D:/Devs/.gradle"
export PATH="$JAVA_HOME/bin:$PATH"
cd /d/Devs/SIMAP-APK || exit 1

echo "== java =="
java -version 2>&1 | head -2

echo "== assembleDebug =="
./gradlew --no-daemon assembleDebug 2>&1
echo "EXIT_CODE=$?"
