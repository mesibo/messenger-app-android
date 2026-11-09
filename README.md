# Mesibo Messenger for Android

[Mesibo Messenger](https://mesibo.com) is an open-source real-time messaging, voice & video calling, and conferencing application. This repository contains the source code for the **Mesibo Messenger App for Android**.

---

## 🌐 Source Code Repositories

Mesibo Messenger is available across various native and hybrid platforms, alongside its backend components:

### Mobile & Web Clients
* **Android (Java):** [messenger-app-android](https://github.com/mesibo/messenger-app-android)
* **Android (Kotlin):** [messenger-app-android-kotlin](https://github.com/mesibo/messenger-app-android-kotlin)
* **iOS (Swift):** [messenger-app-ios-swift](https://github.com/mesibo/messenger-app-ios-swift)
* **iOS (Objective-C):** [messenger-app-ios](https://github.com/mesibo/messenger-app-ios)
* **Web Messenger:** [messenger-javascript](https://github.com/mesibo/messenger-javascript)
* **Flutter (Android & iOS):** [messenger-app-flutter](https://github.com/mesibo/messenger-app-flutter)

### Backend Services
* **Backend APIs:** [messenger-app-backend](https://github.com/mesibo/messenger-app-backend)

---

## ✨ Features

* **Messaging:** One-on-one messaging and Group chat.
* **Voice & Video:** High-quality voice and video calling + multi-party conferencing.
* **Multi-Device Sync:** Cross-device real-time synchronization for up to 8 active devices.
* **Rich Media:** Send and receive text, pictures, videos, audio, and arbitrary files.
* **Rich Media & Image Editing:** Share text, video, audio, and arbitrary files, featuring a built-in image editor to adjust visuals before sending.
* **Security:** End-to-End Encryption (E2EE) built-in.
* **Geolocation:** Real-time location sharing.
* **User Experience:** Real-time message status (sent, delivered, read) and typing indicators.
* **Presence:** Online/offline status indicators and real-time profile updates.
* **Notifications:** Seamless push notification infrastructure.

---

## 🔑 Generating a Verification Code (OTP) for the Demo

To make testing easier, this demo version removes the need for commercial SMS gateways. The application still requires a valid phone number and verification to log in, but you can generate the verification OTP manually:

1. Log into your [Mesibo Console](https://console.mesibo.com).
2. Generate an OTP for any phone number you wish to use for testing.
3. Enter that code into the app's login screen.

> 🔒 **Important Security & Sandbox Notes:**
> * **Privacy:** All users are restricted to your specific Mesibo app/account. This demo app cannot see or communicate with users from other Mesibo accounts.
> * **Contact Synchronization:** The demo app utilizes local contact synchronization to discover other users under your account. Ensure that your test devices use correct phone numbers and that those numbers are saved in each other's phone books.

---

## 📚 Documentation & Reference

For comprehensive setup steps, refer to our multi-part tutorial guide:  
👉 **[Build a fully featured WhatsApp clone using Mesibo](https://docs.mesibo.com/tutorials/open-source-whatsapp-clone/)**

The documentation covers:
* Compilation & platform-specific configuration instructions.
* How to download backend source code and host your private API server.
* Hosting the entire real-time messaging, voice, and video platform on-premise.
* Deep customizations, themes, and white-labeling/rebranding guidelines.

