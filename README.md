# PebbleNote 📚

A modern Android application for buying and selling educational notes and PDF materials, built with Kotlin and Jetpack Compose.

**Developer:** Dipendra Kumar Sah  
**Project Type:** Individual Android Application Project

---

## 📱 About

PebbleNote is a marketplace app that connects students who want to sell their educational notes with those who need them. The app features user authentication, an admin dashboard for note management, and a user-friendly interface for browsing and purchasing notes.

---

## 🛠️ Tech Stack

### Core Technologies
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM Pattern
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)

### Firebase Services
- **Firebase Authentication** - User sign up, login, and password reset
- **Firebase Realtime Database** - Store user data and purchase records
- **Google Services** - Authentication provider

### Libraries & Dependencies
- **Jetpack Compose** - Modern UI toolkit
- **Material Icons** - UI icons
- **Firebase BOM** - Firebase dependencies management
- **Kotlin Coroutines** - Asynchronous programming

---

## 🔐 Authentication

The app uses Firebase Authentication for secure user management:

- **Sign Up:** Users can create an account with email and password
- **Sign In:** Secure login with email/password authentication
- **Password Reset:** Email-based password recovery
- **Session Management:** Automatic login with "Remember Me" functionality
- **User Profiles:** Store user information in Firebase Realtime Database

---

## ✨ Features

### User Features
- Browse available notes and PDFs
- View note details with pricing
- Purchase notes with payment integration (Khalti, eSewa)
- Download purchased PDFs
- User profile management
- Search and filter notes by category

### Admin Features
- Add new notes with preview images
- Set pricing and categories
- Enable/disable note availability
- Manage note inventory
- Upload multiple preview images

---

## 📸 Screenshots

### Welcome & Authentication
| Welcome Screen | Sign In | Registration | Password Reset |
|---|---|---|---|
| ![Welcome](Assets/Welcome%20Screen.png) | ![Sign In](Assets/Signin%20Screen.png) | ![Registration](Assets/Registration%20Screen.png) | ![Reset Password](Assets/Reset%20Password%20Screen.png) |

### Main Application
| User Dashboard | Purchase Screen | Profile | Admin Dashboard |
|---|---|---|---|
| ![Dashboard](Assets/User%20Dashboard.png) | ![Purchase](Assets/Purchase%20Screen.png) | ![Profile](Assets/Profile%20Screen.png) | ![Admin](Assets/Admin%20screen.png) |

---

## 🚀 How to Use

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or higher
- Android device or emulator (API 24+)
- Firebase project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/PebbleNote.git
   cd PebbleNote
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Configure Firebase**
   - Add your `google-services.json` file to `app/` directory
   - Ensure Firebase Authentication and Realtime Database are enabled in your Firebase console

4. **Build and Run**
   - Click "Run" or press `Shift + F10`
   - Select your device or emulator
   - The app will build and launch

### Usage

**For Users:**
1. Open the app and create an account
2. Browse available notes on the dashboard
3. Click on a note to view details
4. Purchase using Khalti or eSewa
5. Download your purchased PDF

**For Admins:**
- Admin credentials are configured separately
- Access admin dashboard to manage notes
- Add new notes with pricing and images
- Enable/disable note availability

---

## 📁 Project Structure

```
app/src/main/
├── java/com/example/pebblenote/
│   ├── MainActivity.kt              # App entry point
│   ├── LoginActivity.kt             # User login
│   ├── RegistrationActivity.kt      # User registration
│   ├── ForgotPasswordActivity.kt    # Password reset
│   ├── DashboardActivity.kt         # User dashboard
│   ├── PurchaseActivity.kt          # Payment screen
│   ├── ProfileActivity.kt           # User profile
│   ├── AdminDashboardActivity.kt    # Admin panel
│   ├── LocalNotesStore.kt           # Local data persistence
│   └── ui/theme/                    # App theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── res/
    ├── drawable/                    # Images and icons
    └── values/                      # Strings, colors, themes
```

---

## 🎨 Design

- **UI Framework:** Jetpack Compose with Material Design 3
- **Color Scheme:** Purple gradient theme with light backgrounds
- **Typography:** Modern, readable font system
- **Layout:** Responsive design for various screen sizes
- **Icons:** Material Icons library

---

## 📝 License

This is an individual project created for educational purposes.

---

## 👤 Developer

**Dipendra Kumar Sah**  
Individual Android Application Project

---

## 🙏 Acknowledgments

- Firebase for backend services
- Material Design for UI components
- Jetpack Compose for modern Android UI development
