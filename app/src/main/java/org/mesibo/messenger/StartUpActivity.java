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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.mesibo.contactutils.ContactUtils;
import com.mesibo.uihelper.MesiboUiHelperConfig;

public class StartUpActivity extends AppCompatActivity {

    private static final String TAG = "MesiboStartupActivity";
    public final static String INTENTEXIT="exit";
    public final static String SKIPTOUR="skipTour";
    public final static String STARTINBACKGROUND ="startinbackground";
    private boolean mRunInBackground = false;
    private boolean mPermissionAlert = false;

    public static void newInstance(Context context, boolean startInBackground) {
        Intent i = new Intent(context, StartUpActivity.class);  //MyActivity can be anything which you want to start on bootup...
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(StartUpActivity.STARTINBACKGROUND, startInBackground);

        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().getBooleanExtra(INTENTEXIT, false)) {
            Log.d(TAG, "onCreate closing");
            finish();
            return;
        }



        mRunInBackground = getIntent().getBooleanExtra(STARTINBACKGROUND, false);
        if(mRunInBackground) {
            Log.e(TAG, "Moving app to background");
            moveTaskToBack(true);
        } else {
            Log.e(TAG, "Not Moving app to background");
        }


        setContentView(R.layout.activity_blank_launcher);
        startNextActivity();
    }

    void startNextActivity() {

        if(TextUtils.isEmpty(SampleAPI.getToken())) {
            MesiboUiHelperConfig.mDefaultCountry = ContactUtils.getCountryCode();
            MesiboUiHelperConfig.mPhoneVerificationBottomText = "Note, Mesibo may call instead of sending an SMS if SMS delivery to your phone fails.";
            if(null == MesiboUiHelperConfig.mDefaultCountry) {
                MesiboUiHelperConfig.mDefaultCountry = "91";
            }

            if(getIntent().getBooleanExtra(SKIPTOUR, false)) {
                UIManager.launchLogin(this, MesiboListeners.getInstance());
            } else {
                UIManager.launchWelcomeactivity(this, true, MesiboListeners.getInstance(), MesiboListeners.getInstance());
            }

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } else {
            UIManager.launchMesibo(this, 0, mRunInBackground, true);
        }

        finish();
    }

    // since this activity is singleTask, intent will be delivered here if it's running
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        if(intent.getBooleanExtra(INTENTEXIT, false)) {
            finish();
        }

        super.onNewIntent(intent);
    }
}
