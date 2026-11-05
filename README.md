# Sensa

Sensa is an offline Android expense tracker. It reads payment notifications and logs the spend — no manual entry, no accounts, no network.

## How it works

- A `NotificationListenerService` watches for payment notifications from supported apps.
- Each notification is parsed, deduplicated against recent ones, and categorized by merchant keywords.
- High-confidence captures save automatically; ambiguous ones and P2P transfers go to a Review inbox.

## Requirements

Android Studio, JDK 21, Android SDK 36 (compile/target), minSdk 26. Create `local.properties` with `sdk.dir` pointing at your SDK — it is not committed.

## Build

```powershell
.\gradlew.bat :app:assembleDebug      # build APK
.\gradlew.bat testDebugUnitTest       # JVM unit tests (all modules)
.\gradlew.bat :app:lintDebug          # report: app/build/reports/lint-results-debug.txt
```

## Running it

Install the debug APK, then in onboarding grant Notification Access and the battery optimization exemption. Without Notification Access nothing is captured.

Supported sources today: GoPay and BCA mobile.

## Project structure

```
:app                     Activity, Koin start-up, NavHost, bottom bar
:build-logic/convention  Gradle convention plugins
:core:domain      (JVM)  models, repository contracts, dedupe, categorizer, use cases
:core:database           Room schema, DAOs, category seeding
:core:data               repositories, categorizer impl, DataStore
:core:capture            notification service, parsers, capture pipeline
:core:presentation       rupiah formatting, UI models
:core:design-system      theme tokens, components, charts
:feature:<name>          MVI screen + nav graph + Koin module
```
