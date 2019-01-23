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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.mesibo.contactutils.*;

import com.mesibo.api.Mesibo;
import org.mesibo.messenger.gcm.MesiboRegistrationIntentService;
import com.mesibo.calls.MesiboCall;
import com.mesibo.uihelper.WelcomeScreen;
import com.mesibo.uihelper.ILoginInterface;
import com.mesibo.uihelper.IProductTourListener;
import com.mesibo.uihelper.ILoginResultsInterface;

import java.util.ArrayList;

public class MesiboListeners implements Mesibo.ConnectionListener, ILoginInterface, IProductTourListener, Mesibo.MessageListener, Mesibo.UIHelperListner, Mesibo.UserProfileLookupListener, ContactUtils.ContactsListener, Mesibo.MessageFilter, Mesibo.CrashListener, MesiboRegistrationIntentService.GCMListener, MesiboCall.MesiboCallListener {
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

    ILoginResultsInterface mILoginResultsInterface = null;
    Handler mGroupHandler = null;
    String mCode = null;
    String mPhone = null;

    @SuppressWarnings("all")
    private SampleAPI.ResponseHandler mHandler = new SampleAPI.ResponseHandler() {
        @Override
        public void HandleAPIResponse(SampleAPI.Response response) {
            Log.d(TAG, "Respose: " + response);
            if (null == response)
                return;

            if (response.op.equals("login")) {
                if (!TextUtils.isEmpty(SampleAPI.getToken())) {
                    Mesibo.UserProfile u = Mesibo.getSelfProfile();

                    if (TextUtils.isEmpty(u.name)) {
                        UIManager.launchUserRegistration(mLoginContext, 0);
                    } else {
                        UIManager.launchMesibo(mLoginContext, 0, false, true);
                    }
                }

                if(null != mILoginResultsInterface)
                    mILoginResultsInterface.onLoginResult(response.result.equals("OK"), -1);

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
        Log.d(TAG, "on Mesibo Connection: " + status);
        if (Mesibo.STATUS_SIGNOUT == status) {
            //TBD, Prompt
            SampleAPI.forceLogout();
        } else if (Mesibo.STATUS_AUTHFAIL == status) {
            SampleAPI.forceLogout();
        }

        if(Mesibo.STATUS_ONLINE == status) {
            SampleAPI.startOnlineAction();
        }
    }

    @Override
    public boolean Mesibo_onMessage(Mesibo.MessageParams params, byte[] data) {
        SampleAPI.autoAddContact(params);
        if(Mesibo.isReading(params))
            return true;

        String message = "";
        try {
            message = new String(data, "UTF-8");
        } catch (Exception e) {
            return false;
        }
        SampleAPI.notify(params, message);
        return true;
    }

    @Override
    public void Mesibo_onMessageStatus(Mesibo.MessageParams params) {

    }

    @Override
    public void Mesibo_onActivity(Mesibo.MessageParams params, int i) {
        SampleAPI.autoAddContact(params); // we start fetching contact when user started typing
    }

    @Override
    public void Mesibo_onLocation(Mesibo.MessageParams params, Mesibo.Location location) {
        SampleAPI.autoAddContact(params);
        SampleAPI.notify(params, "Location");
    }

    @Override
    public void Mesibo_onFile(Mesibo.MessageParams params, Mesibo.FileInfo fileInfo) {
        SampleAPI.autoAddContact(params);
        SampleAPI.notify(params, "Attachment");
    }

    @Override
    public boolean Mesibo_onUpdateUserProfiles(Mesibo.UserProfile profile) {
        if(null == profile) {
            //This is a sub-optimal approach, use only if backend does not implement contact update support
            //SampleAPI.getContacts(null, null, true);
            return false;
        }

        if((profile.flag& Mesibo.UserProfile.FLAG_DELETED) > 0) {
            if(profile.groupid > 0) {
                profile.lookedup = true; //else getProfile will be recursive call
                SampleAPI.updateDeletedGroup(profile.groupid);
                return true;
            }
        }

        if(profile.groupid > 0) {
            profile.status = SampleAPI.groupStatusFromMembers(profile.groupMembers);
            return true;
        }

        if(!TextUtils.isEmpty(profile.address)) {
            String name = ContactUtils.reverseLookup(profile.address);
            if(null == name)
                return false;

            if(profile.name != null && profile.name.equalsIgnoreCase(name))
                return false;

            profile.name = name;
            return true;
        }

        return false; //group
    }

    @Override
    public void Mesibo_onShowProfile(Context context, Mesibo.UserProfile userProfile) {
        UIManager.launchUserProfile(context, userProfile.groupid, userProfile.address);
    }

    @Override
    public void Mesibo_onDeleteProfile(Context c, Mesibo.UserProfile u, Handler handler) {

    }

    @Override
    public int Mesibo_onGetMenuResourceId(Context context, int type, Mesibo.MessageParams params, Menu menu) {
        int id = 0;
        if (type == 0) // Setting menu in userlist
            id = R.menu.messaging_activity_menu;
        else // from User chatbox
            id = R.menu.menu_messaging;

        ((Activity)context).getMenuInflater().inflate(id, menu);

        if(1 == type && null != params && params.groupid > 0) {
            MenuItem menuItem = menu.findItem(R.id.action_call);
            menuItem.setVisible(false);
            MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);

            menuItem = menu.findItem(R.id.action_videocall);
            menuItem.setVisible(false);
            MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
        }

        return 0;
    }

    @Override
    public boolean Mesibo_onMenuItemSelected(Context context, int type, Mesibo.MessageParams params, int item) {
        if (type == 0) { // from userlist
            if (item == R.id.action_settings) {
                UIManager.launchUserSettings(context);
            } else if(item == R.id.action_calllogs) {
                MesiboCall.getInstance().launchCallLogs(context, 0);
            }
        } 
	else { // from messaging box
            if(R.id.action_call == item && 0 == params.groupid) {
                //UIManager.launchCallActivity(MainApplication.getAppContext(), params.peer, true);
                MesiboCall.getInstance().call(context, Mesibo.random(), params.profile, false);
            }
            else if(R.id.action_videocall == item && 0 == params.groupid) {
                //UIManager.launchCallActivity(MainApplication.getAppContext(), params.peer, true);
                MesiboCall.getInstance().call(context, Mesibo.random(), params.profile, true);
            }
        }

        return false;
    }

    @Override
    public void Mesibo_onSetGroup(Context context, long groupid, String name, int type, String status, String photoPath, String[] members, Handler handler) {
        mGroupHandler = handler;
        if(null == name && null == status && null == photoPath) {
            mHandler.setContext(context);
            SampleAPI.setProfilePicture(null, groupid, mHandler);
            return;
        }
        SampleAPI.setGroup(groupid, name, status, photoPath, members, mHandler);
    }

    @Override
    public void Mesibo_onGetGroup(Context context, long groupid, Handler handler) {
        mGroupHandler = handler;
        mHandler.setContext(context);
        SampleAPI.getGroup(groupid, mHandler);
    }

    @Override
    public ArrayList<Mesibo.UserProfile> Mesibo_onGetGroupMembers(Context context, long groupid) {
        Mesibo.UserProfile profile = Mesibo.getUserProfile(groupid);
        if(null == profile)
            return null;

        return SampleAPI.getGroupMembers(profile.groupMembers);
    }

    //Note this is not in UI thread
    @Override
    public boolean Mesibo_onMessageFilter(Mesibo.MessageParams messageParams, int i, byte[] data) {

        // using it for notifications
        if(1 != messageParams.type || messageParams.isCall())
            return true;

        String message = "";
        try {
            message = new String(data, "UTF-8");
        } catch (Exception e) {
            return false;
        }

        if(TextUtils.isEmpty(message))
            return false;

        MesiboNotification n = null;

        try {
            n = mGson.fromJson(message, MesiboNotification.class);
        } catch (Exception e) {
            return false;
        }

        if(null == n)
            return false;

        String name = n.name;
        if(!TextUtils.isEmpty(n.phone)) {
            name = ContactUtils.reverseLookup(n.phone);
            if(TextUtils.isEmpty(name))
                name = n.name;
        }

        if(!TextUtils.isEmpty(n.subject)) {
            n.subject = n.subject.replace("%NAME%", name);
            n.msg = n.msg.replace("%NAME%", name);
            SampleAPI.notify(NotifyUser.NOTIFYMESSAGE_CHANNEL_ID, NotifyUser.TYPE_OTHER, n.subject, n.msg);
        }

        if(!TextUtils.isEmpty(n.phone) || n.gid > 0) {
            SampleAPI.createContact(n.name, n.phone, n.gid, n.status, n.members, n.photo, n.tn, n.ts, (Mesibo.getTimestamp()-messageParams.ts)/1000, false, true, SampleAPI.VISIBILITY_UNCHANGED);
        }

        return false;
    }

    @Override
    public boolean MesiboCall_onNotify(int type, Mesibo.UserProfile profile, boolean video) {

        return true;
    }

    @Override
    public void Mesibo_onForeground(Context context, int screenId, boolean foreground) {

        //userlist is in foreground
        if(foreground && 0 == screenId) {
            //notify count clear
            SampleAPI.notifyClear();
        }
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
    public boolean onLogin(Context context, String phone, String code, ILoginResultsInterface iLoginResultsInterface) {
        mLoginContext = context;
        mILoginResultsInterface = iLoginResultsInterface;
        mCode = code;
        mPhone = phone;
        mHandler.setContext(context);
        SampleAPI.login(phone, code, mHandler);
        return false;
    }

    @Override
    public boolean onAccountKitLogin(Context context, String accesstoken, ILoginResultsInterface iLoginResultsInterface) {
        if(null == accesstoken)
            return true; //return true to relaunch accountkit

        mLoginContext = context;
        mILoginResultsInterface = iLoginResultsInterface;
        mHandler.setContext(context);
        SampleAPI.loginAccountKit(accesstoken, mHandler);
        return false;
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


    private static ArrayList<String> mContactsToSync = new ArrayList<String>();
    private static ArrayList<String> mDeletedContacts = new ArrayList<String>();
    private long mSyncTs = 0;
    //This callback is not in UI thread
    @Override
    public boolean ContactUtils_onContact(int type, String name, String phoneNumber, long ts) {

        if(ContactUtils.ContactsListener.TYPE_SYNCDELETED == type) {
            mDeletedContacts.add(phoneNumber);
            Mesibo.UserProfile profile = Mesibo.getUserProfile(phoneNumber, 0);

            if(null != profile) {
                // we don't send refresh as this usually would have happened from native
                // contact screen and when screen resumes, it will anyway refresh
                Mesibo.deleteUserProfile(profile, false, false);
            }

            return true;
        }


        if(ContactUtils.ContactsListener.TYPE_SYNC == type) {
            if(null != phoneNumber) {
                String selfPhone = SampleAPI.getPhone();
                if(!TextUtils.isEmpty(selfPhone) && selfPhone.equalsIgnoreCase(phoneNumber)) {
                    ContactUtils.synced(phoneNumber, ts, ContactUtils.ContactsListener.TYPE_SYNC);
                    return true;
                }

                mContactsToSync.add(phoneNumber);
                if(mSyncTs < ts)
                    mSyncTs = ts;
            }

            // if sync completed, sync unsent numbers
            if(null == phoneNumber || mContactsToSync.size() >= 100) {

                if(mContactsToSync.size() > 0) {
                    boolean rv = SampleAPI.getContacts(mContactsToSync, false, false);
                    if(!rv) return false;

                    String[] c = mContactsToSync.toArray(new String[mContactsToSync.size()]);
                    ContactUtils.synced(c, ts, ContactUtils.ContactsListener.TYPE_SYNC);

                    mContactsToSync.clear();
                    mSyncTs = 0;

                }
            }

            if(null == phoneNumber && mDeletedContacts.size() > 0) {
                boolean rv = SampleAPI.deleteContacts(mDeletedContacts);
                if(rv) {
                    String[] c = mDeletedContacts.toArray(new String[mDeletedContacts.size()]);
                    ContactUtils.synced(c, ts, ContactUtils.ContactsListener.TYPE_SYNCDELETED);
                    mDeletedContacts.clear();
                }
            }

            if(null == phoneNumber)
                SampleAPI.syncDone();
        }

        return true;
    }

    @Override
    public boolean ContactUtils_onSave(String contacts, long timestamp) {
        if(null == contacts)
            contacts = "";


        SampleAPI.saveLocalSyncedContacts(contacts, timestamp);
        return true;
    }


    @Override
    public void Mesibo_onGCMToken(String token) {
        SampleAPI.setGCMToken(token);
    }

    @Override
    public void Mesibo_onGCMMessage(Bundle data, boolean inService) {
        SampleAPI.onGCMMessage(inService);
    }
}
