# Firebase Integration Summary

This document summarizes the Firebase features implemented in the PebbleNote app and how the app reads/writes data across Authentication and Realtime Database.

## Overview
- Authentication: Email/Password via Firebase Authentication.
- Database: Firebase Realtime Database used for user profiles, notes catalog, and purchases.
- Google Services: Project is configured with `google-services.json` and the Gradle Google Services plugin.

## Authentication
- Login (`LoginActivity`): Uses `FirebaseAuth.signInWithEmailAndPassword(email, password)` for user login. Admin login remains a dummy path.
- Registration (`RegistrationActivity`): Uses `FirebaseAuth.createUserWithEmailAndPassword(email, password)`. On success, writes a basic user profile to `/users/{uid}` in Realtime Database.
- Auto-login: When "Remember Me" is enabled, app checks `FirebaseAuth.getInstance().currentUser` to auto-route into the dashboard.
- Forgot Password (`ForgotPasswordActivity`): Calls `FirebaseAuth.sendPasswordResetEmail(email)` and shows a message; user completes reset via email link.

## Realtime Database
- Users: `/users/{uid}` — stores basic profile fields (name, email, bio optionally) on registration.
- Notes: `/notes` — admin manages the catalog. Each child (key is `id`) contains:
  - `id`: number
  - `title`: string
  - `price`: number (stored in DB) or rendered as string in UI
  - `pdfUri`: string (content URI if locally selected)
  - `previewImageUris`: array of strings (URIs)
  - `category`: string
  - `description`: string
  - `enabled`: boolean
  - Optional metrics: `views`, `downloads`, `likes`
- Purchases: `/purchases/{uid}/{timestamp}` — records successful purchases with:
  - `noteId`: number
  - `title`: string
  - `price`: string (UI formatted)
  - `method`: string (`Khalti` or `eSewa`)
  - `status`: string (e.g., `success`)
  - `timestamp`: string (epoch millis)

## Screen Integration
- Dashboard (`DashboardActivity`):
  - Subscribes to `/notes` using a `ValueEventListener` and maps DB rows into `PDFItem` list.
  - Falls back to dummy data when the DB list is empty or unavailable.
  - Passes `noteId`, `title`, and `price` to the purchase flow.

- Admin Dashboard (`AdminDashboardActivity`):
  - Create: On upload, writes a new child under `/notes/{id}` with full note payload.
  - Edit: Overwrites `/notes/{id}` with updated payload.
  - Delete: Removes `/notes/{id}`.
  - Toggle Enabled: Updates `/notes/{id}/enabled`.
  - File pickers: Calls `takePersistableUriPermission` for selected `pdfUri` and each `previewImageUris` to retain read permission.

- Purchase (`PurchaseActivity`):
  - On Pay button, writes a purchase record to `/purchases/{uid}/{timestamp}`.
  - Shows a toast if the record fails to write.
  - Displays success dialog and redirects back to dashboard.

- Profile (`ProfileActivity`):
  - Stores profile fields in `SharedPreferences` (local-only UI persistence).
  - Calls `takePersistableUriPermission` for avatar URI and shows a confirmation toast.

## Dependencies & Build
- `com.google.gms.google-services` plugin applied in `app/build.gradle.kts`.
- Firebase libraries declared via versions catalog:
  - `firebase-auth`
  - `firebase-database`
- `google-services.json` present under `app/`.

## Notes & Considerations
- Security Rules: Ensure Realtime Database rules align with access patterns:
  - Users can read `/notes` and write their own `/purchases/{uid}`.
  - Only admins can write under `/notes`.
- Data Types: UI formats price as string (`$X.XX`) while DB stores numeric price for admin edits.
- URI Handling: Content URIs are device-local; for cross-device access consider uploading files to Firebase Storage and storing download URLs in DB.
- Error Handling: Basic toasts added for failure cases (purchase record, profile saves). Consider adding snackbars and more robust validation.

## Quick References
- Read `/notes` in Dashboard: `FirebaseDatabase.getInstance().reference.child("notes").addValueEventListener(...)`
- Create/Edit/Delete in Admin: `ref.setValue(data)` / `ref.removeValue()`
- Record purchase: `ref = /purchases/{uid}/{timestamp}; ref.setValue(data)`
- Password reset: `FirebaseAuth.getInstance().sendPasswordResetEmail(email)`

