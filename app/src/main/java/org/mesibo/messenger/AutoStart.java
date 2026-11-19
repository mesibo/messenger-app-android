package org.mesibo.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Wake-up receiver for boot and mesibo's restart broadcast (see SampleAPI.startMesibo).
 * Receiving the broadcast starts the app process; MainApplication.onCreate() then starts
 * mesibo, so nothing else is needed here. (Starting an activity from the background is
 * blocked on Android 10+.)
 */
public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Mesibo", "Mesibo AutoStart: " + intent.getAction());
    }
}
