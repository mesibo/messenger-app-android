package org.mesibo.messenger.fcm;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MesiboGcmListenerService extends FirebaseMessagingService {

    // Runs on a background thread; the payload content is irrelevant, the push only
    // wakes the app so that mesibo can connect and fetch pending messages.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        MesiboPushHelper.onMessage(true);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        MesiboPushHelper.onNewToken(token);
    }
}
