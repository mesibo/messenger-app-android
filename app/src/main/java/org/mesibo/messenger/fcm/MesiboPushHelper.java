package org.mesibo.messenger.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.mesibo.api.Mesibo;

/**
 * All push (FCM) handling of the app:
 *
 * - fetches the FCM token and gives it to mesibo (Mesibo.setPushToken), again whenever FCM
 *   rotates it (MesiboFcmService.onNewToken)
 * - a push only wakes the app so that mesibo connects and receives pending messages; the
 *   payload is not used (MesiboFcmService.onMessageReceived)
 *
 * The sender id comes from google-services.json.
 */
public final class MesiboPushHelper {
    private static final String TAG = "MesiboPushHelper";

    // FCM allows only a short execution window per message
    private static final long PUSH_WAIT_ONLINE_MS = 15000;

    private static String mToken = null;
    private static boolean mTokenSent = false;

    private MesiboPushHelper() {}

    /** fetches the token; call when mesibo starts */
    public static void start() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "FCM token fetch failed", task.getException());
                return;
            }
            setToken(task.getResult());
        });
    }

    /** sends the token to mesibo if not sent yet, e.g. once online */
    public static synchronized void sendToken() {
        if (null == mToken || mTokenSent)
            return;
        mTokenSent = true;
        Mesibo.setPushToken(mToken);
    }

    /** logout: the token is to be sent again for the next user */
    public static synchronized void reset() {
        mTokenSent = false;
    }

    static synchronized void setToken(String token) {
        if (null == token)
            return;
        // FCM may rotate the token; a new token must be sent again
        if (!token.equals(mToken))
            mTokenSent = false;
        mToken = token;
        sendToken();
    }

    /**
     * a push arrived, on the FCM service thread: wake mesibo, wait (bounded) until it is online and
     * give it a moment to receive the messages
     */
    static void onMessage() {
        Mesibo.setForegroundContext(null, -1, true);

        long deadline = System.currentTimeMillis() + PUSH_WAIT_ONLINE_MS;
        try {
            while (Mesibo.STATUS_ONLINE != Mesibo.getConnectionStatus()
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(500);
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
