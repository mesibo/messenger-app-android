/** Copyright (c) 2019 Mesibo
 * https://mesibo.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the terms and condition mentioned on https://mesibo.com
 * as well as following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions, the following disclaimer and links to documentation and source code
 * repository.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * Neither the name of Mesibo nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Documentation
 * https://mesibo.com/documentation/
 *
 * Source Code Repository
 * https://github.com/mesibo/messenger-app-android
 *
 */

package org.mesibo.messenger;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.mesibo.api.Mesibo;
import com.mesibo.mediapicker.MediaPicker;
import com.mesibo.calls.MesiboCall;
import com.mesibo.messaging.MesiboUI;

/**
 * Created by Mesibo on 29/09/17.
 */

public class MainApplication extends Application implements Mesibo.RestartListener {
    public static final String TAG = "MesiboSampleApplication";
    private static Context mContext = null;
    private static MesiboCall mCall = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Mesibo.setRestartListener(this);
        SampleAPI.init(getApplicationContext());

        mCall = MesiboCall.getInstance();
        mCall.init(this);

        MesiboUI.Config opt = MesiboUI.getConfig();
        opt.mToolbarColor = 0xff00868b;
        opt.emptyUserListMessage = "Ask your family and friends to download so that you can try out Mesibo functionalities";
        MediaPicker.setToolbarColor(opt.mToolbarColor);



    }

    public static String getRestartIntent() {
        return "com.mesibo.sampleapp.restart";
    }

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void Mesibo_onRestart() {
        Log.d(TAG, "OnRestart");
        StartUpActivity.newInstance(this, true);
    }

}

