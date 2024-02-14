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
import android.os.Handler;
import androidx.core.app.JobIntentService;
import android.widget.Toast;

/**
 * Require WAKE_LOCK persmission for API level earlier than Android O
 */

public class MesiboJobIntentService extends JobIntentService {
    static final int JOB_ID = 1000;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        try {
        enqueueWork(context, MesiboJobIntentService.class, JOB_ID, work);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onHandleWork(Intent intent) {
        try {
        MesiboRegistrationIntentService.sendMessageToListener( true);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    final Handler mHandler = new Handler();
    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(MesiboJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
