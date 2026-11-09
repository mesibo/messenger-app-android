package org.mesibo.messenger.fcm;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/** FCM events; handled in MesiboPushHelper */
public class MesiboFcmService extends FirebaseMessagingService {

    // background thread; the push only wakes the app, the payload is not used
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        MesiboPushHelper.onMessage();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        MesiboPushHelper.setToken(token);
    }
}
