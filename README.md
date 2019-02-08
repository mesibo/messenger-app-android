## Mesibo Messenger for Android
Mesibo Messenger is an open-source app with real-time messaging, voice and video call features. This repo contains the source code for Mesibo Messenger App for Android. The GitHub repository for iOS version is [here](https://github.com/mesibo/messenger-app-ios).

### Features
- One-on-one messaging and Group chat
- High quality voice and video calling
- Rich messaging (text, picture, video, audio, other files)
- Encryption 
- Location sharing
- Message status and typing indicators
- Online status (presence) and real-time profile update
- Push notifications

Latest versions are also available through Google Play Store and Apple App Store:

<a href="https://play.google.com/store/apps/details?id=com.mesibo.mesiboapplication"><img
  alt="Get it on Google Play" height="80"
  src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" /></a> <a href="https://itunes.apple.com/us/app/mesibo-realtime-messaging-voice-video/id1222921751"><img
  alt="Get it on Apple App Store" height="80"
  src="https://mesibo.com/documentation/images/app/apple_app_store.png" /></a>

## Downloading the Source Code

### Clone the Repository (Recommended)
If you have git installed, this is a recommended approach as you can quickly sync and stay up to date with the latest version. This is also a preferred way of downloading the code if you decide to contribute to the project. 

To download, open a terminal and issue following commands:

    $ mkdir Messenger
    $ cd Messenger
    $ git clone https://github.com/mesibo/messenger-app-android.git

### Download the code as a zip file
You can also download the complete Android Messenger source code as a [zip file](https://github.com/mesibo/messenger-app-android/archive/master.zip). Although simple, the downsize of this approach is that you will have to download the complete source code everytime it is updated on the repository. 

### Stay Up-to-date
Whatever approach you take to download the code, it is important to stay up-to-date with the latest changes, new features, fixes etc. Ensure to **Star(*)** the project on GitHub to get notified whenever the source code is updated. 

## Build and Run

Before we dive into building and running a fully featured Messenger for Android, ensure that you've read the following.

 - Latest Android Studio Installed
 - An Android Device

Building the code is as simple as:

 1. Launch Android Studio
 2. Open the project from the folder where you have downloaded the code using menu `File -> Open`
 3. Build using menu `Build -> Rebuild Project`
 4. It may take a while to build the project for the first time. 
 5. Once the build is over, run on the device using menu `Run -> Run (app)`
 6. That's it, you should see the welcome screen like below.

Login using your phone number. You can even start using the app you've just built to communicate with your family and friends.

## Key SDKs user in this project

These apps use following [Mesibo SDKs](https://mesibo.com).

- Mesibo SDK
- Mesibo Messaging UI Module
- Mesibo Call UI Module

These apps also use following third party libraries/services.

- [Facebook AccountKit](https://www.accountkit.com/) for the Phone Verification
- [Google Maps](https://developers.google.com/maps/documentation/) and [Google Places](https://cloud.google.com/maps-platform/places/) SDKs for Geolocation integration 

## Documentation & Tutorials

- [Mesibo Documentation](https://mesibo.com/documentation/) 
- [Mesibo Get Started Guide](https://mesibo.com/documentation/get-started/).
- Tutorial - [A fully featured WhatsApp clone using mesibo](https://mesibo.com/documentation/tutorials/open-source-whatsapp-clone/)

