## Mesibo Messenger for Android
Mesibo Messenger is an open-source app with real-time messaging, voice and video call features. This repo contains the source code for Mesibo Messenger App for Android. The GitHub repository for iOS version is [here](https://github.com/mesibo/messenger-app-ios).

![messenger](https://mesibo.com/assets/images/phone-img6.png)


### Features
- One-on-one messaging and Group chat
- High quality voice and video calling
- Video and Voice Conferencing
- Rich messaging (text, picture, video, audio, other files)
- End-to-End Encryption 
- Location sharing
- Message status and typing indicators
- Online status (presence) and real-time profile update
- Push notifications

Latest versions are also available from [Google Play Store](https://play.google.com/store/apps/details?id=com.mesibo.mesiboapplication) OR [Apple AppStore](https://itunes.apple.com/us/app/mesibo-realtime-messaging-voice-video/id1222921751)

## Prebuilt Messenger Apps
If you prefer to try Pre-built messenger apps instead of building it, you can download it from:

<a href="https://play.google.com/store/apps/details?id=com.mesibo.mesiboapplication"><img alt="Get it on Google Play" height="80" src="https://mesibo.com/images/android-app.png" /></a> 
<a href="https://itunes.apple.com/us/app/mesibo-realtime-messaging-voice-video/id1222921751"> <img alt="Get it on Apple App Store" height="80" src="https://mesibo.com/images/iphone-app.png" /></a>
<br/><br/>
<p>&nbsp;</p>

### Generating OTP for the demo
The messenger App requires a valid phone number and OTP to login. Note that, we do not send OTP for App login. Instead, you can generate OTP for any number from the [mesibo console](https://mesibo.com/console)

Note that, all the users are private to your app/account. The demo app can not see or communicate with users from other apps. Also, the demo app uses contact synchronization to find other users of your app and hence ensure to use the correct phone number and other users are in your phone book. 

### Documentation
The documentation for the messenger is available here - [A fully featured WhatsApp clone using mesibo](https://mesibo.com/documentation/tutorials/open-source-whatsapp-clone/)

It describes 
- Compilation Instructions (Trivial though)
- Download [backend source code](https://github.com/mesibo/messenger-app-backend) and host it on your server (the default one is located on the mesibo server)
- Hosting entire real-time messaging and call server in your premise
- Other Customizations and Rebranding

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

Login using your phone number and OTP from the mesibo console. You can even start using the app you've just built to communicate with your family and friends.

## Key SDKs user in this project

These apps use following [Mesibo SDKs](https://mesibo.com).

- Mesibo SDK
- Mesibo Messaging UI Module
- Mesibo Call UI Module

These apps also use following third party libraries/services.

- [Google Maps](https://developers.google.com/maps/documentation/) and [Google Places](https://cloud.google.com/maps-platform/places/) SDKs for Geolocation integration 

# Backend
The backend code is here https://github.com/mesibo/messenger-app-backend

## Documentation & Tutorials

- [Mesibo Documentation](https://mesibo.com/documentation/) 
- [Mesibo Get Started Guide](https://mesibo.com/documentation/get-started/).
- Tutorial - [A fully featured WhatsApp clone using mesibo](https://mesibo.com/documentation/tutorials/open-source-whatsapp-clone/)

