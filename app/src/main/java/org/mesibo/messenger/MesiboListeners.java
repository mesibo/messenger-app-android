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

package org.mesibo.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;

import com.mesibo.api.MesiboGroupProfile;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboPhoneContact;
import com.mesibo.api.MesiboProfile;
import com.mesibo.calls.api.MesiboCall;
import com.mesibo.calls.ui.MesiboCallUi;

import com.mesibo.api.Mesibo;
import org.mesibo.messenger.fcm.MesiboRegistrationIntentService;

import com.mesibo.messaging.MesiboUI;
import com.mesibo.uihelper.WelcomeScreen;
import com.mesibo.uihelper.MesiboLoginUiHelperListener;
import com.mesibo.uihelper.IProductTourListener;
import com.mesibo.uihelper.MesiboLoginUiHelperResultCallback;

import java.util.ArrayList;
import static org.webrtc.ContextUtils.getApplicationContext;

public class MesiboListeners implements Mesibo.ConnectionListener, MesiboLoginUiHelperListener, IProductTourListener, Mesibo.MessageListener, Mesibo.MessageFilter, Mesibo.ProfileListener, Mesibo.ProfileCustomizationListener, Mesibo.CrashListener, MesiboRegistrationIntentService.GCMListener, MesiboCall.IncomingListener, Mesibo.GroupListener, Mesibo.AppStateListener, Mesibo.EndToEndEncryptionListener {
    public static final String TAG = "MesiboListeners";
    public static Context mLoginContext = null;
    private static Gson mGson = new Gson();
    public static class MesiboNotification {
        public String subject;
        public String msg;
        public String type;
        public String action;
        public String name;
        public long gid;
        public String phone;
        public String status;
        public String members;
        public String photo;
        public long ts;
        public String tn;

        MesiboNotification() {
        }
    }

    MesiboLoginUiHelperResultCallback mMesiboLoginUiHelperResultCallback = null;
    Handler mGroupHandler = null;
    String mCode = null;
    String mPhone = null;
    boolean mSyncDone = false;
    Context mLastContext = null;

    @SuppressWarnings("all")
    private SampleAPI.ResponseHandler mHandler = new SampleAPI.ResponseHandler() {
        @Override
        public void HandleAPIResponse(SampleAPI.Response response) {
            Log.d(TAG, "Respose: " + response);
            if (null == response)
                return;

            if (response.op.equals("login")) {
                if (!TextUtils.isEmpty(SampleAPI.getToken())) {
                    MesiboProfile u = Mesibo.getSelfProfile();

                    if (TextUtils.isEmpty(u.getName())) {
                        UIManager.launchEditProfile(mLoginContext, 0, 0, true);
                    } else {
                        MesiboUI.MesiboUserListScreenOptions opts = new MesiboUI.MesiboUserListScreenOptions();
                        opts.keepRunning = true;
                        UIManager.launchMesibo(mLoginContext, opts);
                    }
                }

                if(null != mMesiboLoginUiHelperResultCallback && null == response.errmsg)
                    mMesiboLoginUiHelperResultCallback.onLoginResult(response.result.equals("OK"), -1);

            } else if (response.op.equals("setgroup")) {

                if(null != mGroupHandler) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putLong("groupid", response.gid);
                    bundle.putString("result", response.result);
                    msg.setData(bundle);
                    mGroupHandler.handleMessage(msg);
                }
            } else if (response.op.equals("getgroup")) {

                if(null != mGroupHandler) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", response.result);
                    msg.setData(bundle);
                    mGroupHandler.handleMessage(msg);
                }
            }
            //handleAPIResponse(response);
        }
    };

    @Override
    public void Mesibo_onConnectionStatus(int status) {
        Log.d(TAG, "Mesibo_onConnectionStatus: " + status);
        if (Mesibo.STATUS_SIGNOUT == status) {
            UIManager.showAlert(mLastContext, "Signed Out", "You have signed-in from other device and hence signed out here");
            SampleAPI.forceLogout();
        } else if (Mesibo.STATUS_AUTHFAIL == status) {
            UIManager.showAlert(mLastContext, "Signed Out", "Login Expired. Login again to continue.");
            SampleAPI.forceLogout();
        }

        if(Mesibo.STATUS_ONLINE == status) {
            SampleAPI.startOnlineAction();
        }
    }

    @Override
    public void Mesibo_onEndToEndEncryption(MesiboProfile profile, int status) {
        Log.d(TAG, "Mesibo_onEndToEndEncryption: " + status);
    }

    @Override
    public void Mesibo_onMessage(MesiboMessage msg) {
        if(!msg.isRealtimeMessage() || Mesibo.MSGSTATUS_OUTBOX == msg.getStatus())
            return;

        if(msg.isEndToEndEncryptionStatus())
            return;

        // if(Mesibo.isAppInForeground()) return true;

        if(Mesibo.isReading(msg))
            return;

        String message = msg.message;
        if(TextUtils.isEmpty(message))
            message = msg.title;
        if(TextUtils.isEmpty(message) && msg.hasImage())
            message = "Picture";
        if(TextUtils.isEmpty(message) && msg.hasVideo())
            message = "Video";
        if(TextUtils.isEmpty(message) && msg.hasAudio())
            message = "Audio";
        if(TextUtils.isEmpty(message) && msg.hasDocument())
            message = "Attachment";
        if(TextUtils.isEmpty(message) && msg.hasLocation())
            message = "Location";

        SampleAPI.notify(msg, message);
        return;
    }

    @Override
    public void Mesibo_onMessageStatus(MesiboMessage params) {
    }

    @Override
    public void Mesibo_onMessageUpdate(MesiboMessage mesiboMessage) {

    }

    @Override
    public void Mesibo_onProfileUpdated(MesiboProfile userProfile) {

    }

    @Override
    public boolean Mesibo_onCustomizeProfile(MesiboProfile profile) {
        return true;
    }

    @Override
    public String Mesibo_onGetProfileName(MesiboProfile profile) {
        if(profile.isGroup())
		return null;
        MesiboPhoneContact c = Mesibo.getPhoneContactsManager().getPhoneNumberInfo(profile.getAddress(), true);
        return c.formattedPhoneNumber;
    }

    @Override
    public Bitmap Mesibo_onGetProfileImage(MesiboProfile profile) {
        return null;
    }

    //Note this is not in UI thread
    @Override
    public boolean Mesibo_onMessageFilter(MesiboMessage msg) {

        // using it for notifications
        if(1 != msg.type || msg.isCall())
            return true;

        return false;
    }

    @Override
    public MesiboCall.CallProperties MesiboCall_OnIncoming(MesiboProfile userProfile, boolean video, boolean waiting) {
        MesiboCall.CallProperties cc = MesiboCall.getInstance().createCallProperties(video);
        cc.parent = getApplicationContext();
        cc.user = userProfile;
        return cc;
    }

    @Override
    public boolean MesiboCall_OnShowUserInterface(MesiboCall.Call call, MesiboCall.CallProperties callProperties) {
        return false;
    }

    @Override
    public void MesiboCall_OnError(MesiboCall.CallProperties callProperties, int error) {
    }

    @Override
    public boolean MesiboCall_onNotify(int type, MesiboProfile profile, boolean video) {
        String subject = null, message = null;

        if(true)
            return false;

        if(MesiboCall.MESIBOCALL_NOTIFY_INCOMING == type) {

        } else if(MesiboCall.MESIBOCALL_NOTIFY_MISSED == type) {
            subject = "Mesibo Missed Call";
            message = "You missed a mesibo " + (video?"video ":"") + "call from " + profile.getNameOrAddress();

        }

        return true;
    }

    @Override
    public void Mesibo_onGroupCreated(MesiboProfile groupProfile) {
        Log.d(TAG, "New group " + groupProfile.groupid);
    }

    @Override
    public void Mesibo_onGroupJoined(MesiboProfile groupProfile) {
        SampleAPI.notify(3, "Joined a group", "You have been added to the group " + groupProfile.getName());
    }

    @Override
    public void Mesibo_onGroupLeft(MesiboProfile groupProfile) {
        SampleAPI.notify(3, "Left a group", "You left the group " + groupProfile.getName());
    }

    @Override
    public void Mesibo_onGroupMembers(MesiboProfile groupProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupMembersJoined(MesiboProfile groupProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupMembersRemoved(MesiboProfile groupProfile, MesiboGroupProfile.Member[] members) {

    }

    @Override
    public void Mesibo_onGroupSettings(MesiboProfile mesiboProfile, MesiboGroupProfile.GroupSettings groupSettings, MesiboGroupProfile.MemberPermissions memberPermissions, MesiboGroupProfile.GroupPin[] groupPins) {

    }

    @Override
    public void Mesibo_onGroupError(MesiboProfile mesiboProfile, long l) {

    }

    @Override
    public void Mesibo_onForeground(boolean foreground) {
        if(foreground && MesiboCall.getInstance().isCallInProgress() && null != mLastContext) {
            MesiboCall.getInstance().callUiForExistingCall(mLastContext);
        }
    }

    @Override
    public void Mesibo_onForeground(Context context, int screenId, boolean foreground) {


        //userlist is in foreground
        if(foreground && 0 == screenId) {
            //notify count clear
            SampleAPI.notifyClear();
        }

        // if app restarted
        if(foreground && MesiboCall.getInstance().isCallInProgress()  && null == mLastContext) {
            MesiboCall.getInstance().callUiForExistingCall(context);
        }

        if(foreground) mLastContext= context;
    }

    @Override
    public void Mesibo_onCrash(String crashLogs) {
        Log.e(TAG, "Mesibo_onCrash: " + ((null != crashLogs)?crashLogs:""));
        //restart application
        Intent i = new Intent(MainApplication.getAppContext(), StartUpActivity.class);  //MyActivity can be anything which you want to start on bootup...
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        i.putExtra(StartUpActivity.STARTINBACKGROUND, !Mesibo.isAppInForeground()); ////Maintain the state of the application
        MainApplication.getAppContext().startActivity(i);
    }

    @Override
    public void onProductTourViewLoaded(View v, int index, WelcomeScreen screen) {

    }

    @Override
    public void onProductTourCompleted(Context context) {
        UIManager.launchLogin((Activity)context, MesiboListeners.getInstance());
    }

    @Override
    public boolean MesiboLoginUiHelper_onLogin(Context context, String phone, String code, MesiboLoginUiHelperResultCallback MesiboLoginUiHelperResultCallback) {
        mLoginContext = context;
        mMesiboLoginUiHelperResultCallback = MesiboLoginUiHelperResultCallback;
        mCode = code;
        mPhone = phone;
        mHandler.setContext(context);
        SampleAPI.login(phone, code, mHandler);
        return false;
    }

    @Override
    public int MesiboLoginUiHelper_getCountryCode(Context context) {
        return 0;
    }

    @Override
    public boolean MesiboLoginUiHelper_isPhoneValid(Context context, String s) {
        return true;
    }

    private static MesiboListeners _instance = null;
    public static MesiboListeners getInstance() {
        if(null==_instance)
            synchronized(MesiboListeners.class) {
                if(null == _instance) {
                    _instance = new MesiboListeners();
                }
            }

        return _instance;
    }

    @Override
    public void Mesibo_onGCMToken(String token) {
        SampleAPI.setGCMToken(token);
    }

    @Override
    public void Mesibo_onGCMMessage(/*Bundle data, */boolean inService) {
        SampleAPI.onGCMMessage(inService);
    }
}
