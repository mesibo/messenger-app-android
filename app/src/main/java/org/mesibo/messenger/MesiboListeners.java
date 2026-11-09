package org.mesibo.messenger;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import static org.mesibo.messenger.MainApplication.getAppContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;


import com.mesibo.api.MesiboGroupProfile;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboPhoneContact;
import com.mesibo.api.MesiboProfile;

import com.mesibo.api.Mesibo;
import com.mesibo.messaging.MesiboDialogs;
import com.mesibo.uihelper.MesiboLoginUiHelperListener;
import com.mesibo.uihelper.MesiboLoginUiHelperResultCallback;


public class MesiboListeners implements Mesibo.ConnectionListener, MesiboLoginUiHelperListener, Mesibo.MessageListener, Mesibo.MessageFilter, Mesibo.ProfileListener, Mesibo.ProfileCustomizationListener, Mesibo.CrashListener, Mesibo.GroupListener, Mesibo.AppStateListener, Mesibo.EndToEndEncryptionListener {
    public static final String TAG = "MesiboListeners";
    @Nullable public static Context mLoginContext = null;
    MesiboLoginUiHelperResultCallback mMesiboLoginUiHelperResultCallback = null;
    @Nullable Context mLastContext = null;

    @SuppressWarnings("all")
    private SampleAPI.ResponseHandler mHandler = new SampleAPI.ResponseHandler() {
        @Override
        public void HandleAPIResponse(SampleAPI.Response response) {
            Log.d(TAG, "Respose: " + response);
            if (null == response)
                return;

            if ("login".equals(response.op)) {
                if (!TextUtils.isEmpty(SampleAPI.getToken())) {
                    if (response.isNewUser()) {
                        UIManager.launchEditProfile(null != mLoginContext ? mLoginContext : getAppContext(), true);
                    } else {
                        // via startup: requests permissions, then opens the user list
                        UIManager.launchStartupActivity(null != mLoginContext ? mLoginContext : getAppContext(), true);
                    }
                }

                if(null != mMesiboLoginUiHelperResultCallback && null == response.errmsg)
                    mMesiboLoginUiHelperResultCallback.onLoginResult("OK".equals(response.result));

            }
        }
    };

    @Override
    public void Mesibo_onConnectionStatus(int status) {
        Log.d(TAG, "Mesibo_onConnectionStatus: " + status);
        if (Mesibo.STATUS_SIGNOUT == status) {
            MesiboDialogs.alert(mLastContext, "Signed Out", "You have signed-in from other device and hence signed out here");
            SampleAPI.forceLogout();
        } else if (Mesibo.STATUS_AUTHFAIL == status) {
            MesiboDialogs.alert(mLastContext, "Signed Out", "Login Expired. Login again to continue.");
            SampleAPI.forceLogout();
        }

        if(Mesibo.STATUS_ONLINE == status) {
            SampleAPI.startOnlineAction();
        }
    }

    @Override
    public void Mesibo_onEndToEndEncryption(MesiboProfile profile, int status) {
        //Log.d(TAG, "Mesibo_onEndToEndEncryption: " + status);
    }

    @Override
    public void Mesibo_onMessage(MesiboMessage msg) {
        if(!msg.isRealtimeMessage() || Mesibo.MSGSTATUS_OUTBOX == msg.getStatus())
            return;

        msg.getProfile().removeLocalProfile();

        if(msg.isEndToEndEncryptionStatus())
            return;

        if(Mesibo.isReading(msg))
            return;

        if(Mesibo.isAppInForeground())
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
        Log.d(TAG, "Mesibo_onProfileUpdated: ");
    }

    @Override
    public boolean Mesibo_onCustomizeProfile(MesiboProfile profile) {
        return true;
    }

    @Override
    @Nullable // null: the default (no custom name)
    public String Mesibo_onGetProfileName(@NonNull MesiboProfile profile) {
        if(profile.isGroup()) {
            return null;
        }
        MesiboPhoneContact c = Mesibo.getPhoneContactsManager().getPhoneNumberInfo(profile.getAddress(), true);
        return (null != c) ? c.formattedPhoneNumber : null; // unknown number: the default
    }

    @Override
    @Nullable // null: the default (no custom picture)
    public Bitmap Mesibo_onGetProfileImage(@NonNull MesiboProfile profile) {
        return null;
    }

    @Override
    public boolean Mesibo_onMessageFilter(MesiboMessage msg) {

        // using it for notifications
        if(1 != msg.type || msg.isCall())
            return true;

        return false;
    }

    @Override
    public void Mesibo_onGroupCreated(MesiboProfile groupProfile) {
        Log.d(TAG, "New group " + groupProfile.groupid);
    }

    @Override
    public void Mesibo_onGroupJoined(MesiboProfile groupProfile) {
        SampleAPI.notify(NotifyUser.ID_GROUP_JOINED, "Joined a group", "You have been added to the group " + groupProfile.getName());
    }

    @Override
    public void Mesibo_onGroupLeft(MesiboProfile groupProfile) {
        SampleAPI.notify(NotifyUser.ID_GROUP_LEFT, "Left a group", "You left the group " + groupProfile.getName());
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
    public void Mesibo_onGroupSettings(MesiboProfile mesiboProfile, MesiboGroupProfile.GroupSettings groupSettings, MesiboGroupProfile.MemberPermissions memberPermissions, @Nullable MesiboGroupProfile.GroupPin[] groupPins) {

    }

    @Override
    public void Mesibo_onGroupError(MesiboProfile mesiboProfile, long l) {

    }

    // The call screen is no longer forced open when the app comes back during a call, so the
    // user can use the app meanwhile; the ongoing call notification (status bar) and the call
    // buttons bring the call back.
    @Override
    public void Mesibo_onForeground(boolean foreground) {
    }

    @Override
    public void Mesibo_onForeground(Context context, int screenId, boolean foreground) {


        //userlist is in foreground
        if(foreground && 0 == screenId) {
            //notify count clear
            SampleAPI.notifyClear();
        }

        if(foreground) mLastContext= context;
    }

    @Override
    public void Mesibo_onCrash(String crashLogs) {
        Log.e(TAG, "Mesibo_onCrash: " + ((null != crashLogs)?crashLogs:""));
        //restart application
        StartUpActivity.newInstance(getAppContext(), !Mesibo.isAppInForeground());
    }

    @Override
    public boolean MesiboLoginUiHelper_onLogin(Context context, String phone, String code, MesiboLoginUiHelperResultCallback MesiboLoginUiHelperResultCallback) {
        mLoginContext = context;
        mMesiboLoginUiHelperResultCallback = MesiboLoginUiHelperResultCallback;
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

    // created when the class is first used; the JVM makes this thread-safe
    private static final MesiboListeners sInstance = new MesiboListeners();

    @NonNull
    public static MesiboListeners getInstance() {
        return sInstance;
    }
}
