# 📚 Bookify - Android E-Commerce Book App

Bookify is a modern Android e-commerce application for buying books, built with Kotlin and Firebase. The app provides a seamless book shopping experience with features like category browsing, cart management, and secure payment processing.

## 📱 Features

### Authentication
- Email & Password Login
- User Registration
- Profile Management

### Shopping Experience
- Browse Books by Categories
- Search Functionality
- Detailed Book Information
- Shopping Cart Management
- Secure Checkout Process

### Payment Integration
- Multiple Payment Methods
- Bank Transfer (Mandiri, BCA, BNI)
- E-Wallet Integration (Gopay, Dana, OVO)

## 🛠 Tech Stack

- **Language:** Kotlin
- **Platform:** Android
- **Minimum SDK:** 21 (Android 5.0)
- **Target SDK:** 33 (Android 13)
- **Backend:** Firebase Realtime Database
- **Authentication:** Firebase Auth
- **Image Loading:** Glide
- **UI Components:** Material Design

## ⚙️ Prerequisites

Before you begin, ensure you have:
- Android Studio Arctic Fox or newer
- JDK 11 or newer
- Android SDK with minimum API 21
- Google Play Services in your Android Emulator
- Firebase Account

## 📥 Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/Bookify.git
   cd Bookify
   ```

2. **Firebase Setup**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app in your Firebase project:
     - Package name: `id.usk.ac.bookify`
     - Download `google-services.json`
     - Place `google-services.json` in the app/ directory

3. **Firebase Database Rules**
   Copy these rules to your Firebase Realtime Database:
   ```json
   {
     "rules": {
       ".read": true,
       ".write": "auth != null",
       "users": {
         "$uid": {
           ".read": "$uid === auth.uid",
           ".write": "$uid === auth.uid"
         }
       },
       "books": {
         ".indexOn": ["isBestDeal", "isTopBook", "isLatestBook", "category"]
       },
       "cart": {
         "$uid": {
           ".read": "$uid === auth.uid",
           ".write": "$uid === auth.uid"
         }
       }
     }
   }
   ```

4. **Configure Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

5. **Add Required Dependencies**
   Make sure these dependencies are in your app/build.gradle:
   ```gradle
   dependencies {
       // Firebase
       implementation platform('com.google.firebase:firebase-bom:32.0.0')
       implementation 'com.google.firebase:firebase-database-ktx'
       implementation 'com.google.firebase:firebase-auth-ktx'

       // Image Loading
       implementation 'com.github.bumptech.glide:glide:4.12.0'
       annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'

       // Material Design
       implementation 'com.google.android.material:material:1.9.0'

       // RecyclerView
       implementation 'androidx.recyclerview:recyclerview:1.3.0'

       // CardView
       implementation 'androidx.cardview:cardview:1.0.0'
   }
   ```

## 🚀 Running the App

1. **Connect Device/Emulator**
   - Connect your Android device via USB with USB debugging enabled, or
   - Start your Android Emulator

2. **Build and Run**
   - Click the 'Run' button in Android Studio, or
   - Use the keyboard shortcut (⇧F10 on Windows/Linux, ^R on macOS)

## 📊 Database Structure
