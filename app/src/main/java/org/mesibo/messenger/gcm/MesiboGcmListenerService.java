/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mesibo.messenger.gcm;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import org.mesibo.messenger.MainApplication;

public class MesiboGcmListenerService extends GcmListenerService {

    private static final String TAG = "MesiboGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        //Note if we send 'notification' instead of 'data', it will be under notification key
        //https://developers.google.com/cloud-messaging/concept-options#notifications_and_data_messages

        /*
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }
        */

        MesiboRegistrationIntentService.sendMessageToListener(data, false);

        Intent intent = new Intent("com.mesibo.someintent");
        intent.putExtras(data);
        MesiboJobIntentService.enqueueWork(MainApplication.getAppContext(), intent);

    }
}
