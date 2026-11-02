package org.mesibo.messenger.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Fetches the FCM token and forwards token/push events to the app.
 * Replaces the old FirebaseInstanceId + JobIntentService based registration.
 */
public final class MesiboPushHelper {
    private static final String TAG = "MesiboPushHelper";
    private static volatile PushListener mListener = null;

    public interface PushListener {
        void Mesibo_onGCMToken(String token);
        void Mesibo_onGCMMessage(boolean inService);
    }

    private MesiboPushHelper() {}

    public static void startRegistration(PushListener listener) {
        if (null != listener)
            mListener = listener;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "FCM token fetch failed", task.getException());
                return;
            }
            onNewToken(task.getResult());
        });
    }

    static void onNewToken(String token) {
        PushListener l = mListener;
        if (null != l && null != token)
            l.Mesibo_onGCMToken(token);
    }

    static void onMessage(boolean inService) {
        PushListener l = mListener;
        if (null != l)
            l.Mesibo_onGCMMessage(inService);
    }
}
