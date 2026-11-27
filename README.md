# CanteenApp

Android campus canteen ordering and management application built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Firebase** (`Authentication` + `Cloud Firestore`).

---

## Features

- **Role-Based Flows:** Student authentication (`LoginScreen.kt`, `RegistrationScreen.kt`) and canteen administrator dashboards.
- **Student Ordering (`StudentScreens.kt`):** Browse canteen items, place orders, and track order statuses.
- **Admin Management (`AdminDashboardScreen.kt`, `AdminItemsScreen.kt`, `AdminOrdersScreen.kt`):** Add/edit menu items, view order details, and track analytics (`AnalyticsViewModel.kt`).
- **Cloud Sync (`FirebaseViewModel.kt`):** Real-time data backed by Firebase Auth and Firestore.

---

## Tech Stack

- **Language & UI:** Kotlin, Jetpack Compose, Material 3, Navigation Compose
- **Backend Services:** Firebase Auth, Cloud Firestore, Firebase Analytics
- **Build System:** Gradle Kotlin DSL (`compileSdk 36`, `minSdk 24`, `targetSdk 34`)

---

## Building Locally

1. Open the project in Android Studio or build from CLI with Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
2. Add your own `google-services.json` inside `app/` for Firebase connectivity before running on an emulator or device.
