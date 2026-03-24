@echo off
call gradlew build
echo build complete
adb -d install -r "app\build\outputs\apk\debug\app-debug.apk"
echo shipped!