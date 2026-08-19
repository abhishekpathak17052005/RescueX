This screenshot shows a **different issue from the role-routing problem**.

Your login screen is displaying:

> **“Failed to get document because the client is offline.”**

So Firebase Authentication may be working, but the app cannot currently read the Firestore `users/{uid}` document to retrieve the user's role.

That explains why role-based routing can fail.

### First check your phone's internet

On the physical phone, open Chrome and verify that a website loads.

Then make sure the phone has either:

```text
Wi-Fi ✅
or
Mobile data ✅
```

### Then check Firebase/Firestore

In Firebase Console:

**Build → Firestore Database**

Make sure the Firestore database has actually been created.

You should have:

```text
users
  └── <user UID>
       ├── name
       ├── email
       ├── phone
       └── role
```

### Important: don't make the app silently fall back

Your app should behave like this:

```text
Firebase Auth login
       ↓
User UID
       ↓
Firestore users/{uid}
       ↓
role retrieved
       ↓
PATIENT / AMBULANCE / HOSPITAL
```

If Firestore is offline:

```text
"Cannot connect to Firebase. Check your internet connection."
```

not:

```text
Patient Dashboard
```

because that would hide the actual problem.

### Give Gemini this prompt

```text
Fix the Firebase offline/profile retrieval issue in RescueX.

Current login screen shows:

"Failed to get document because the client is offline."

The app needs to retrieve:

users/{authenticatedUserUid}

from Cloud Firestore after Firebase Authentication login.

IMPORTANT:
Do not modify Vapi, Gemini, SOS, ambulance, hospital, or dashboard business logic.

Investigate only Firebase Authentication + Firestore user-profile retrieval.

1. Verify Firebase is initialized correctly using the real google-services.json.
2. Verify Firestore is initialized correctly.
3. Verify the app has INTERNET permission.
4. Verify Firebase Authentication login completes before Firestore profile lookup.
5. After login, obtain:

FirebaseAuth.currentUser?.uid

6. Read:

FirebaseFirestore.getInstance()
    .collection("users")
    .document(uid)

7. Add detailed logs:

[AUTH] Login successful
[AUTH] UID present = true
[FIRESTORE] Starting user profile read
[FIRESTORE] users/{uid} read started
[FIRESTORE] User profile read successful
[FIRESTORE] Role = PATIENT/AMBULANCE/HOSPITAL
[FIRESTORE] User profile read failed: <safe error>

Do not log passwords, tokens, API keys, or sensitive credentials.

8. Handle Firestore errors separately:

OFFLINE
PERMISSION_DENIED
NOT_FOUND
UNAVAILABLE
OTHER

9. If offline:
show a clear error:
"Unable to connect to RescueX services. Check your internet connection."

Do not silently route the user to a dashboard.

10. If the user document does not exist:
show:
"User profile not configured."

Do not default to PATIENT.

11. If role is missing:
show:
"User role not configured."

12. Verify that the registration flow creates:

users/{uid}

with:

uid
name
email
phone
role
createdAt

13. Verify that the Firestore document is created under the SAME Firebase project represented by google-services.json.

14. Verify Firestore security rules allow the authenticated user to read their own users/{uid} document.

15. Build and run the app.

Acceptance test:

Login
→ Firebase Auth success
→ UID obtained
→ Firestore users/{uid} read
→ role retrieved
→ correct dashboard selected.

Also report the exact root cause if Firestore is offline, permission denied, wrong Firebase project, missing document, or incorrect initialization.
```

### One thing I'd check immediately

Since you recently added `google-services.json`, verify that the Firebase project in the console is:

```text
RescueX-AI
```

and that the Android app registered in that project is:

```text
com.rescuex
```

Also make sure the phone is online.

The screenshot itself doesn't show a Vapi or role-routing error yet; it specifically shows a **Firestore client offline error**, so fix this layer first.
