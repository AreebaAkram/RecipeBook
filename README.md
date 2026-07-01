# Community Recipe Book

An Android app for sharing recipes within a community, built with Kotlin and Jetpack Compose.

## Tech Stack

- **Kotlin** - Primary language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - UI components
- **Firebase Auth** - Email/Password authentication
- **Firebase Realtime Database** - Data storage
- **Firebase Storage** - Image uploads
- **Coil** - Image loading
- **Google Fonts** - Typography (Nunito)

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Firebase project

## Setup Instructions

### 1. Firebase Configuration (REQUIRED)

This app requires Firebase. You must:

1. Go to https://console.firebase.google.com
2. Create a new project: "CommunityRecipeBook"
3. Add an Android app with package name: `com.recipebook.community`
4. Download `google-services.json` and place it in `/app/google-services.json`
5. Enable in Firebase Console:
   - **Authentication** -> Sign-in method -> Email/Password (Enable)
   - **Realtime Database** -> Create database -> Start in test mode
   - **Storage** -> Get started -> Start in test mode

### 2. Firebase Security Rules

Apply these rules to your Realtime Database:

```json
{
  "rules": {
    "communities": {
      "$communityId": {
        "info": {
          ".read": "auth != null && root.child('users').child(auth.uid).child('communityId').val() === $communityId",
          ".write": "auth != null && (data.child('adminUid').val() === auth.uid || !data.exists())"
        },
        "members": {
          ".read": "auth != null && root.child('users').child(auth.uid).child('communityId').val() === $communityId",
          "$uid": {
            ".write": "auth != null && (root.child('communities').child($communityId).child('info').child('adminUid').val() === auth.uid || auth.uid === $uid)"
          }
        },
        "recipes": {
          ".read": "auth != null && root.child('users').child(auth.uid).child('communityId').val() === $communityId",
          "$recipeId": {
            ".write": "auth != null && root.child('users').child(auth.uid).child('communityId').val() === $communityId"
          }
        },
        "activity": {
          ".read": "auth != null && root.child('users').child(auth.uid).child('communityId').val() === $communityId",
          ".write": "auth != null && root.child('users').child(auth.uid).child('communityId').val() === $communityId"
        }
      }
    },
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid === $uid",
        ".write": "auth != null && auth.uid === $uid"
      }
    },
    "inviteCodes": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

### 3. Build & Run

Open the project in Android Studio and run, or use command line:

```bash
./gradlew assembleDebug
```

## Project Structure

```
app/src/main/java/com/recipebook/community/
├── MyApp.kt                    # Application class
├── MainActivity.kt             # Main activity
├── data/
│   ├── FirebaseService.kt      # Firebase helpers
│   └── model/
│       └── Models.kt            # Data classes
├── navigation/
│   └── NavGraph.kt             # Navigation setup
├── ui/
│   ├── screens/
│   │   ├── LoginScreen.kt
│   │   ├── SignUpScreen.kt
│   │   ├── CommunitySetupScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── AddRecipeScreen.kt
│   │   ├── ApprovalsScreen.kt
│   │   ├── ActivityFeedScreen.kt
│   │   ├── ProfileScreen.kt
│   │   └── RecipeDetailScreen.kt
│   ├── components/
│   │   ├── DoodleBorder.kt
│   │   ├── DoodleDivider.kt
│   │   ├── AnimatedFAB.kt
│   │   ├── SkeletonLoader.kt
│   │   ├── AvatarView.kt
│   │   └── CategoryChip.kt
│   └── theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
└── viewmodel/
    ├── AuthViewModel.kt
    ├── RecipeViewModel.kt
    └── CommunityViewModel.kt
```

## Features

- User registration and login
- Create or join a community with invite codes
- Add recipes with images
- Admin approval workflow for recipes
- Activity feed tracking all changes
- Export community data as JSON
