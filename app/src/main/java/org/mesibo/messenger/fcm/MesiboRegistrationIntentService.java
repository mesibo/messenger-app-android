/******************************************************************************
* By accessing or copying this work, you agree to comply with the following   *
* terms:                                                                      *
*                                                                             *
* Copyright (c) 2019-2024 mesibo                                              *
* https://mesibo.com                                                          *
* All rights reserved.                                                        *
*                                                                             *
* Redistribution is not permitted. Use of this software is subject to the     *
* conditions specified at https://mesibo.com . When using the source code,    *
* maintain the copyright notice, conditions, disclaimer, and  links to mesibo * 
* website, documentation and the source code repository.                      *
*                                                                             *
* Do not use the name of mesibo or its contributors to endorse products from  *
* this software without prior written permission.                             *
*                                                                             *
* This software is provided "as is" without warranties. mesibo and its        *
* contributors are not liable for any damages arising from its use.           *
*                                                                             *
* Documentation: https://docs.mesibo.com/                                     *
*                                                                             *
* Source Code Repository: https://github.com/mesibo/                          *
*******************************************************************************/

package org.mesibo.messenger.fcm;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;


import java.io.IOException;


public class MesiboRegistrationIntentService extends JobIntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private static String SENDER_ID= "";
    private static GCMListener mListener = null;

    public MesiboRegistrationIntentService() {
        super();
    }

    public interface GCMListener {
        void Mesibo_onGCMToken(String token);
        void Mesibo_onGCMMessage( boolean inService);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
      onHandleIntent(intent);
    }

    //@Override
    protected void onHandleIntent(Intent intent) {
        String token = null;
        try {

            FirebaseInstanceId instanceID = (FirebaseInstanceId) FirebaseInstanceId.getInstance(FirebaseApp.initializeApp(this));
            if(null == instanceID) {
                Log.d(TAG, "FCM getInstance Failed");
                return;
            }
          token = instanceID.getToken();
            Log.d(TAG, "FCM Registration Token: " + token);


        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        if(null != mListener) {
            mListener.Mesibo_onGCMToken(token);
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        Log.d("Token", token);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *

     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
//        GcmPubSub pubSub = GcmPubSub.getInstance(this);
//        for (String topic : TOPICS) {
//            pubSub.subscribe(token, "/topics/" + topic, null);
//        }
    }
    // [END subscribe_topics]

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return (resultCode == ConnectionResult.SUCCESS);
    }

    public static final int JOB_ID = 1;
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MesiboRegistrationIntentService.class, JOB_ID, work);
    }

    public static void startRegistration(Context context, String senderId, GCMListener listener) {
        if(!TextUtils.isEmpty(senderId))
            SENDER_ID = senderId;

        if(listener != null)
            mListener = listener;

        try {
            Intent intent = new Intent(context, MesiboRegistrationIntentService.class);
            //context.startService(intent);
            enqueueWork(context, intent);
        } catch (Exception e) {

        }
    }

    public static void sendMessageToListener( boolean inService) {
        if(null != mListener) {
            mListener.Mesibo_onGCMMessage( inService);
        }
    }
}
