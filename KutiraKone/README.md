# Kutira-Kone - Fabric Scrap Exchange App

## Setup Instructions (IMPORTANT - Read before running)

### Step 1: Set up Firebase
1. Go to https://console.firebase.google.com
2. Click "Add Project" → name it "KutiraKone"
3. Enable these services:
   - **Authentication** → Email/Password
   - **Firestore Database** → Start in test mode
   - **Storage** → Start in test mode

### Step 2: Add google-services.json
1. In Firebase Console → Project Settings → Your Apps → Add Android App
2. Package name: `com.kutira.kone`
3. Download the `google-services.json` file
4. Replace the placeholder file at: `app/google-services.json`

### Step 3: Firestore Rules (set in Firebase Console)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Step 4: Storage Rules
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Step 5: Open in Android Studio
1. Open Android Studio
2. File → Open → Select the `KutiraKone` folder
3. Wait for Gradle sync to complete
4. Run on device or emulator (API 24+)

## Features
- Register/Login with email
- Upload fabric scraps with image, size, material type
- Browse catalog in grid view
- Filter by material (Silk, Cotton, Wool, etc.)
- Filter by radius (1km, 2km, 5km, 10km)
- Send swap/exchange requests
- Accept or reject incoming requests
- View your own listings in Profile
