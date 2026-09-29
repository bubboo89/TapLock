# Android Studio inspections

Run **Code > Inspect Code** with the custom scope **TapLock project**. The shared
scope is defined in `.idea/scopes/TapLock_project.xml`. If it does not appear yet,
reopen the project so Android Studio reloads the scope.

This scope excludes bundled Ruby gems (`vendor/`), CodeGraph metadata, Gradle and
IDE metadata, build output, inspection exports, and the generated fastlane HTML
screenshot report. App code, tests, resources, build configuration and maintained
documentation remain included. Selecting **Whole project** instead will still
inspect dependency/generated files; the scope does not disable inspections.

Do not edit gem sources or generated screenshot HTML to clear inspection results.
Do not suppress policy warnings simply to obtain a clean report. The battery
request rationale and remaining evidence gaps are documented in
[BATTERY_EXEMPTION.md](BATTERY_EXEMPTION.md).
