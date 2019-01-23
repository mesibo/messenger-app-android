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
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.mesibo.api.Mesibo;
import com.mesibo.contactutils.ContactUtils;
import org.mesibo.messenger.gcm.MesiboRegistrationIntentService;
import com.mesibo.calls.MesiboCall;

import java.util.ArrayList;

public class SampleAPI  {
    private static final String TAG="SampleAPI";
    public static final String mSharedPrefKey = "org.mesibo.messenger";
    private static SharedPreferences mSharedPref = null;
    private static NotifyUser mNotifyUser = null;
    private static boolean mHasUtf8Db = false; // enable this is your db is utf-8 compliant
    private static boolean mSyncPending = true;
    private static boolean mContactSyncOver = false;
    private static Context mContext = null;
    private static boolean mResetSyncedContacts = false;
    private static String mAkClientToken = null;
    private static String mAkAppId = null;

    public final static String KEY_SYNCEDCONTACTS = "syncedContacts";
    public final static String KEY_SYNCEDDEVICECONTACTSTIME = "syncedPhoneContactTs";
    public final static String KEY_SYNCEDCONTACTSTIME = "syncedTs";
    public final static String KEY_AUTODOWNLOAD = "autodownload";
    public final static String KEY_GCMTOKEN = "gcmtoken";

    public static final int VISIBILITY_HIDE = 0;
    public static final int VISIBILITY_VISIBLE = 1;
    public static final int VISIBILITY_UNCHANGED = 2;

    private static final String DEFAULT_FILE_URL = "https://media.mesibo.com/files/";


    public static abstract class ResponseHandler implements Mesibo.HttpListener {
        private Mesibo.Http http = null;
        private Bundle mRequest = null;
        private boolean mBlocking = false;
        private boolean mOnUiThread = false;
        public static boolean result = true;
        public Context mContext = null;
        @Override
        public boolean Mesibo_onHttpProgress(Mesibo.Http http, int state, int percent) {
            if(percent < 0) {
                HandleAPIResponse(null);
                return true;
            }

            if(100 == percent && Mesibo.Http.STATE_DOWNLOAD == state) {
                String strResponse = http.getDataString();
                Response response = null;

                if (null != strResponse) {
                    try {
                        response = mGson.fromJson(strResponse, Response.class);
                    } catch (Exception e) {
                        result = false;
                    }
                }

                if(null == response)
                    result = false;

                final Context context = (null == this.mContext)?SampleAPI.mContext:this.mContext;

                if(!mOnUiThread) {
                    parseResponse(response, mRequest, context, false);
                    HandleAPIResponse(response);
                }
                else {
                    final Response r = response;

                    if(null == context)
                        return true;

                    Handler uiHandler = new Handler(context.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            parseResponse(r, mRequest, context, true);
                            HandleAPIResponse(r);
                        }
                    };
                    uiHandler.post(myRunnable);
                }
            }
            return true;
        }

        public void setBlocking(boolean blocking) {
            mBlocking = blocking;
        }

        public void setOnUiThread(boolean onUiThread) {
            mOnUiThread = onUiThread;
        }

        public boolean sendRequest(Bundle postBunlde, String filePath, String formFieldName) {

            postBunlde.putString("dt", String.valueOf(Mesibo.getDeviceType()));
            int nwtype = Mesibo.getNetworkConnectivity();
            if(nwtype == 0xFF) {

            }

            mRequest = postBunlde;
            http = new Mesibo.Http();
            http.url = mApiUrl;
            http.postBundle = postBunlde;
            http.uploadFile = filePath;
            http.uploadFileField = formFieldName;
            http.notifyOnCompleteOnly = true;
            http.concatData = true;
            http.listener = this;
            if(mBlocking)
                return http.executeAndWait();
            return http.execute();
        }

        public void setContext(Context context) {
            this.mContext = context;
        }

        public Context getContext() {
            return this.mContext;
        }

        public abstract void HandleAPIResponse(Response response);
    }

    public static class Contacts {
        public String name = "";
        public String phone = "";
        public long   gid = 0;
        public long   ts = 0;
        public String status = "";
        public String photo = "";
        public String tn = "";
        public String members = "";
    }

    public static class Response {
        public String result;
        public String op;
        public String error;
        public String token;
        public String uploadurl;
        public String downloadurl;
        public Contacts[] contacts;
        public String name;
        public String status;
        public String members;
        public String photo;
        public String phone;
        public long gid;
        public int type;
        public int profile;
        public long ts = 0;
        public String tn = null;

        Response() {
            result = null;
            op = null;
            error = null;
            token = null;
            downloadurl = DEFAULT_FILE_URL;
            uploadurl = mApiUrl;
            contacts = null;
            gid = 0;
            type = 0;
            profile = 0;
        }
    }

    private static Gson mGson = new Gson();
    private static String mApiUrl = "https://app.mesibo.com/api.php";
    private static String mDownloadUrl = "";
    private static String mUploadUrl = "";
    private static String mToken = null;
    private static String mPhone = null;
    private static long mContactTs = 0;

    private static boolean invokeApi(final Context context, final Bundle postBunlde, String filePath, String formFieldName, boolean uiThread) {
        ResponseHandler http = new ResponseHandler() {
            @Override
            public void HandleAPIResponse(Response response) {

            }
        };

        http.setContext(context);
        http.setOnUiThread(uiThread);
        return http.sendRequest(postBunlde, filePath, formFieldName);
    }

    public static String phoneBookLookup(String phone) {
        if(TextUtils.isEmpty(phone))
            return null;

        return  ContactUtils.reverseLookup(phone);
    }

    public static void updateDeletedGroup(long gid) {
        if(0 == gid) return;
        Mesibo.UserProfile u = Mesibo.getUserProfile(gid);
        if(null == u) return;
        u.flag |= Mesibo.UserProfile.FLAG_DELETED;
        u.status = "Not a group member"; // can be better handle dynamically
        Mesibo.setUserProfile(u, false);
    }

    public static void createContact(String name, String phone,  long groupid, String status, String members, String photo, String tnBasee64, long ts, long when, boolean selfProfile, boolean refresh, int visibility) {
        Mesibo.UserProfile u = new Mesibo.UserProfile();
        u.address = phone;
        u.groupid = groupid;

        if(!selfProfile && 0 == u.groupid)
            u.name = phoneBookLookup(phone);

        if(TextUtils.isEmpty(u.name))
            u.name = name;

        if(TextUtils.isEmpty(u.name)) {
            u.name = phone;
            if(TextUtils.isEmpty(u.name))
                u.name = "Group-" + groupid;
        }

        if(groupid == 0 && !TextUtils.isEmpty(phone) && phone.equalsIgnoreCase("0")) {
            u.name = "hello";
            return;
        }

        u.status = status; // Base64.decode(c[i].status, Base64.DEFAULT).toString();

        if(groupid > 0) {
            u.groupMembers = members;
            String p = getPhone();
            if(null == p) return;
            //if members empty or doesn't contain myself, it means I am not a member or group deleted
            if(!members.contains(getPhone())) {
                updateDeletedGroup(groupid);
                return;
            }
            u.status = groupStatusFromMembers(members);
        }

        if(null == u.status) {
            u.status = "";
        }

        u.picturePath = photo;
        u.timestamp = ts;
        if(ts > 0 && u.timestamp > mContactTs)
            mContactTs = u.timestamp;

        if(when >= 0) {
            u.lastActiveTime = Mesibo.getTimestamp() - (when*1000);
        }

        if(!TextUtils.isEmpty(tnBasee64)) {
            byte[] tn = null;
            try {
                tn = Base64.decode(tnBasee64, Base64.DEFAULT);

                if(Mesibo.createFile(Mesibo.getFilePath(Mesibo.FileInfo.TYPE_PROFILETHUMBNAIL), photo, tn, true)) {
                    //u.tnPath = photo;
                }
            } catch (Exception e) {}
        }

        if(visibility == VISIBILITY_HIDE)
            u.flag |= Mesibo.UserProfile.FLAG_HIDDEN;
        else if(visibility == VISIBILITY_UNCHANGED) {
            Mesibo.UserProfile tp = Mesibo.getUserProfile(phone, groupid);
            if(null != tp && (tp.flag&Mesibo.UserProfile.FLAG_HIDDEN) >0)
                u.flag |= Mesibo.UserProfile.FLAG_HIDDEN;
        }

        if(selfProfile) {
            mPhone = u.address;
            Mesibo.setSelfProfile(u);
        }
        else
            Mesibo.setUserProfile(u, refresh);
    }

    private static boolean parseResponse(Response response, Bundle request, Context context, boolean uiThread) {

            if(null == response) {
                if(request.getString("op").equalsIgnoreCase("getcontacts")) {
                    mSyncPending = true;
                }

                if(uiThread && null != context) {
                    showConnectionError(context);
                }
                return false;
            }

            if(!response.result.equalsIgnoreCase("OK") ) {
                if(response.error.equalsIgnoreCase("AUTHFAIL")) {
                    forceLogout();
                }
                return false;
            }

            if(response.op.equals("login") && !TextUtils.isEmpty(response.token)) {
                mToken = response.token; //TBD, save into preference
                mPhone = response.phone;
                if(TextUtils.isEmpty(mDownloadUrl))
                    mDownloadUrl = response.downloadurl;
                if(TextUtils.isEmpty(mUploadUrl))
                    mUploadUrl = response.uploadurl;
                mContactTs = 0;
                mResetSyncedContacts = true;
                mSyncPending = true;

                setStringValue("token", mToken);
                setStringValue("downloadurl", mDownloadUrl);
                setStringValue("uploadurl", mUploadUrl);
                Mesibo.reset();
                startMesibo(true);

                createContact(response.name, response.phone, 0, response.status, "", response.photo, response.tn, response.ts, 0, true, false, VISIBILITY_VISIBLE);

                // we need to get permission
                startSync();
            }

            else if(response.op.equals("getcontacts")) {
                Contacts[] c = response.contacts;

                String h = request.getString("hidden");
                int visibility = VISIBILITY_VISIBLE;
                if(null != h && h.equalsIgnoreCase("1")) {
                    visibility = VISIBILITY_HIDE;
                }

                if(null != c) {
                    int count = c.length;

                    for (int i = 0; i < count; i++) {
                        createContact(c[i].name, c[i].phone, c[i].gid, c[i].status, c[i].members, c[i].photo, c[i].tn, c[i].ts, response.ts - c[i].ts, false, true, visibility);
                    }

                    // update only if count > 0
                    saveSyncedTimestamp(mContactTs);
                }

                mResetSyncedContacts = false;
                if(TextUtils.isEmpty(request.getString("phones"))) {

                    // update table with group messages if any
                    if(null != c && c.length > 0) {
                        if (uiThread)
                            Mesibo.setUserProfile(null, true);
                        else {

                            Handler uiHandler = new Handler(mContext.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    Mesibo.setUserProfile(null, true);
                                }
                            };
                            uiHandler.post(myRunnable);
                        }
                    }

                    if(VISIBILITY_VISIBLE == visibility)
                        startContactsSync();
                }
            }
            else if(response.op.equals("getgroup") || response.op.equals("setgroup")) {
                if((!TextUtils.isEmpty(response.phone) || response.gid > 0)) {
                    createContact(response.name, "", response.gid, response.status, response.members, response.photo, response.tn, response.ts, 0, false, true, VISIBILITY_VISIBLE);
                }
            }
            else if(response.op.equals("editmembers") || response.op.equals("setadmin")) {
                Mesibo.UserProfile u = null;
                if(response.gid > 0)
                    u = Mesibo.getUserProfile(response.gid);
                if(null != u) {
                    u.groupMembers = response.members;
                    u.status = groupStatusFromMembers(response.members);
                    Mesibo.setUserProfile(u, false);
                }
            }
            else if(response.op.equals("delgroup")) {
                updateDeletedGroup(response.gid);
            }
            else if(response.op.equals("upload")) {
                if(response.profile > 0)
                    createContact(response.name, response.phone, response.gid, response.status, response.members, response.photo, response.tn, response.ts, 0, true, true, VISIBILITY_VISIBLE);
            }
            else if(response.op.equals("logout")) {
                forceLogout();
            }



        return true;
    }


    public static void showConnectionError(Context context) {
        String title = "No Internet Connection";
        String message = "Your phone is not connected to the internet. Please check your internet connection and try again later.";
        UIManager.showAlert(context, title, message);
    }

    public static String groupStatusFromMembers(String members) {
        if (TextUtils.isEmpty(members))
            return null;

        String[] s = members.split("\\:");
        if (null == s || s.length < 2)
            return null;

        String[] users = s[1].split("\\,");
        if (null == users)
            return "";

        String status = "";
        for (int i = 0; i < users.length; i++) {
            if (!TextUtils.isEmpty(status))
                status += ", ";

            if (getPhone().equalsIgnoreCase(users[i])) {
                status += "You";
            } else {
                Mesibo.UserProfile u = Mesibo.getUserProfile(users[i], 0);

                //TBD, use only the first name
                if (u != null)
                    status += u.name;
                else
                    status += users[i];
            }

            if (status.length() > 32)
                break;
        }
        return status;
    }

    public static ArrayList<Mesibo.UserProfile> getGroupMembers(String members) {
        if (TextUtils.isEmpty(members))
            return null;

        String[] s = members.split("\\:");
        if (null == s || s.length < 2)
            return null;

        String[] users = s[1].split("\\,");
        if (null == users)
            return null;

        ArrayList<Mesibo.UserProfile> profiles = new ArrayList<Mesibo.UserProfile>();

        String status = "";
        for (int i = 0; i < users.length; i++) {

            //TBD, check about self profile
            Mesibo.UserProfile u = Mesibo.getUserProfile(users[i], 0);
            if(null == u) {
                u = Mesibo.createUserProfile(users[i], 0, users[i]);
            }

            profiles.add(u);
        }
        return profiles;
    }


    public static void saveLocalSyncedContacts(String contacts, long timestamp) {
        Mesibo.setKey(SampleAPI.KEY_SYNCEDCONTACTS, contacts);
        Mesibo.setKey(SampleAPI.KEY_SYNCEDDEVICECONTACTSTIME, String.valueOf(timestamp));
    }

    public static void saveSyncedTimestamp(long timestamp) {
        Mesibo.setKey(SampleAPI.KEY_SYNCEDCONTACTSTIME, String.valueOf(timestamp));
    }

    public static void init(Context context) {
        if(null != mSharedPref)
            return;

        mContext = context;

        Mesibo api = Mesibo.getInstance();
        api.init(context);

        Mesibo.initCrashHandler(MesiboListeners.getInstance());
        Mesibo.uploadCrashLogs();
        Mesibo.setSecureConnection(true);

        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (Exception e) {}

        mAkClientToken = ai.metaData.getString("com.facebook.accountkit.ClientToken");
        mAkAppId = ai.metaData.getString("com.facebook.sdk.ApplicationId");


        mSharedPref = context.getSharedPreferences(mSharedPrefKey, Context.MODE_PRIVATE);
        mToken = getStringValue("token", null);
        mDownloadUrl = getStringValue("downloadurl", DEFAULT_FILE_URL);
        mUploadUrl = getStringValue("uploadurl", DEFAULT_FILE_URL);

        if(!TextUtils.isEmpty(mToken)) {
            startMesibo(false);
            startSync();
        }
    }

    public static String getPhone() {
        if(null != mPhone)
            return mPhone;

        Mesibo.UserProfile u = Mesibo.getSelfProfile();

        //MUST not happen
        if(null == u) {
            forceLogout();
            return null;
        }
        mPhone = u.address;
        return mPhone;
    }


    public static String getToken() {
        return mToken;
    }
    public static String getApiUrl() { return mApiUrl; }
    public static String getFileUrl() { return mDownloadUrl; }
    public static String getUploadUrl() { return mUploadUrl; }

    public static void startOnlineAction() {
        sendGCMToken();
        startSync();
    }


    private static void startSync() {

        synchronized (SampleAPI.class) {
            if(!mSyncPending)
                return;
            mSyncPending = false;
        }

        SampleAPI.getContacts(null, false, true);
    }

    // this is called to indicate first round of sync is done
    //TBD, this may trigger getcontact with hidden=1 to reach server before last contact sysc getconatct request
    public static void syncDone() {
        synchronized (SampleAPI.class) {
            mContactSyncOver = true;
        }

        autoAddContact(null);
    }

    public static void startContactsSync() {
        String synced = Mesibo.readKey(KEY_SYNCEDCONTACTS);
        String syncedts = Mesibo.readKey(KEY_SYNCEDDEVICECONTACTSTIME);
        long ts = 0;
        if(!TextUtils.isEmpty(syncedts)) {
            try {
                ts = Long.parseLong(syncedts);
            } catch (Exception e) {}
        }
        ContactUtils.sync(synced, ts, false, MesiboListeners.getInstance());
    }

    public static boolean startMesibo(boolean resetContacts) {

        MesiboRegistrationIntentService.startRegistration(mContext, "946969055788", MesiboListeners.getInstance());

        // set path for storing DB and messaging files
        Mesibo.setPath(Environment.getExternalStorageDirectory().getAbsolutePath());

        // add lister
        Mesibo.addListener(MesiboListeners.getInstance());
        MesiboCall.getInstance().setListener(MesiboListeners.getInstance());

        // add file transfer handler
        MesiboFileTransferHelper fileTransferHelper = new MesiboFileTransferHelper();
        Mesibo.addListener(fileTransferHelper);

        // this will also register listener from the constructor
        mNotifyUser = new NotifyUser(MainApplication.getAppContext());

        // set access token
        if(0 != Mesibo.setAccessToken(mToken)) {
            return false;
        }

        //Mesibo.sendTv(null, 0, 0, 0, null);

        // set database after setting access token so that it's associated with user
        Mesibo.setDatabase("mesibo.db", resetContacts?Mesibo.DBTABLE_PROFILES:0);

        // do this after setting token and db
        if(resetContacts) {
            SampleAPI.saveLocalSyncedContacts("", 0);
            SampleAPI.saveSyncedTimestamp(0);
        }

        initAutoDownload();

        // Now start mesibo
        if(0 != Mesibo.start()) {
            return false;
        }

        String ts = Mesibo.readKey(KEY_SYNCEDCONTACTSTIME);
        if(!TextUtils.isEmpty(ts))
            mContactTs = Long.parseLong(ts);

        ContactUtils.init(mContext);
        if(resetContacts)
            ContactUtils.syncReset();

        Intent restartIntent = new Intent(MainApplication.getRestartIntent());

        Mesibo.runInBackground(MainApplication.getAppContext(), null, restartIntent);

        return true;
    }

    public static boolean startLogout() {
        if(TextUtils.isEmpty(mToken))
            return false;

        Bundle b = new Bundle();
        b.putString("op", "logout");
        b.putString("token", mToken);

        invokeApi(null, b, null, null, false);
        return true;
    }

    public static void forceLogout(){
        mGCMTokenSent = false;
        Mesibo.setKey(KEY_GCMTOKEN, "");
        SampleAPI.saveLocalSyncedContacts("", 0);
        SampleAPI.saveSyncedTimestamp(0);
        Mesibo.stop(true);
        mToken = null;
        mPhone = null;
        setStringValue("token", "");
        setStringValue("ts", "0");
        mNotifyUser.clearNotification();
        Mesibo.reset();
        //Mesibo.resetDB();
        ContactUtils.syncReset();

        UIManager.launchStartupActivity(mContext, true);
    }

    public static void login(String phoneNumber, String verificationCode, ResponseHandler handler) {
        //Mesibo.resetDB();

        Bundle b = new Bundle();
        b.putString("op", "login");
        b.putString("appid", mContext.getPackageName());
        b.putString("phone", phoneNumber);
        if(!TextUtils.isEmpty(verificationCode))
            b.putString("code", verificationCode);

        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);
    }

    public static void loginAccountKit(String accessToken, ResponseHandler handler) {
        //Mesibo.resetDB();

        Bundle b = new Bundle();
        b.putString("op", "login");
        b.putString("appid", mContext.getPackageName());
        b.putString("aktoken", accessToken);
        b.putString("akct", mAkClientToken);
        b.putString("akaid", mAkAppId);
        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);
    }

    public static boolean setProfile(String name, String status, long groupid, ResponseHandler handler) {
        if(TextUtils.isEmpty(mToken))
            return false;

        Bundle b = new Bundle();
        b.putString("op", "profile");
        b.putString("token", mToken);
        b.putString("name", name);
        b.putString("status", status);
        b.putLong("gid", groupid);

        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);

        return true;
    }

    public static boolean setProfilePicture(String filePath, long groupid, ResponseHandler handler) {
        if(TextUtils.isEmpty(mToken))
            return false;

        Bundle b = new Bundle();
        b.putString("op", "upload");
        b.putString("token", mToken);
        b.putLong("mid", 0);
        b.putInt("profile", 1);
        b.putLong("gid", groupid);

        handler.setOnUiThread(true);

        if(TextUtils.isEmpty(filePath)) {
            b.putInt("delete", 1);
            handler.sendRequest(b, null, null);
            return true;
        }

        handler.sendRequest(b, filePath, "photo");
        return true;
    }

    public static boolean getContacts(ArrayList<String> contacts, boolean hidden, boolean async) {
        if(TextUtils.isEmpty(mToken))
            return false;

        //if((System.currentTimeMillis() - mContactFetchTs) < 5000)
        //  return true;

        Bundle b = new Bundle();
        b.putString("op", "getcontacts");
        b.putString("token", mToken);

        if(hidden && (null == contacts || contacts.size() == 0))
            return false;

        b.putString("hidden", hidden?"1":"0");

        if(!hidden && mResetSyncedContacts) {
            mContactTs = 0;
            mResetSyncedContacts = false; // we are doing it here because if old messages are stored,
            // it will start querying with hidden flag before response and then every request
            // will have reset
            b.putString("reset", "1");
        }

        b.putLong("ts", mContactTs);
        if(null != contacts && contacts.size() > 0) {
            String[] c = contacts.toArray(new String[contacts.size()]);
            b.putString("phones", array2String(c));
            //b.putStringArray("phones", c);
        }


        ResponseHandler http = new ResponseHandler() {
            @Override
            public void HandleAPIResponse(Response response) {
            }
        };

        http.setBlocking(!async);
        http.sendRequest(b, null, null);
        return http.result;
    }

    public static boolean deleteContacts(ArrayList<String> contacts) {
        if(TextUtils.isEmpty(mToken) || null == contacts || 0 == contacts.size())
            return false;

        //if((System.currentTimeMillis() - mContactFetchTs) < 5000)
        //  return true;

        Bundle b = new Bundle();
        b.putString("op", "delcontacts");
        b.putString("token", mToken);

        String[] c = contacts.toArray(new String[contacts.size()]);
        b.putString("phones", array2String(c));

        ResponseHandler http = new ResponseHandler() {
            @Override
            public void HandleAPIResponse(Response response) {
            }
        };

        http.setBlocking(true);
        http.sendRequest(b, null, null);
        return http.result;
    }

    // groupid is 0 for new group else pass actual value to add/remove members
    public static boolean setGroup(long groupid, String name, String status, String photoPath, String[] members, ResponseHandler handler) {
        if(TextUtils.isEmpty(mToken))
            return false;

        Bundle b = new Bundle();
        b.putString("op", "setgroup");
        b.putString("token", mToken);
        b.putString("name", name);
        b.putLong("gid", groupid);
        b.putString("status", status);
        if(null != members)
            b.putString("m", array2String(members));

        handler.setOnUiThread(true);
        handler.sendRequest(b, photoPath, "photo");
        return true;
    }

    public static boolean deleteGroup(long groupid, ResponseHandler handler) {
        if(TextUtils.isEmpty(mToken) || 0 == groupid)
            return false;

        Bundle b = new Bundle();
        b.putString("op", "delgroup");
        b.putString("token", mToken);
        b.putLong("gid", groupid);

        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);

        return true;
    }

    public static boolean getGroup(long groupid, ResponseHandler handler) {
        if(TextUtils.isEmpty(mToken) || 0 == groupid)
            return false;

        Bundle b = new Bundle();
        b.putString("op", "getgroup");
        b.putString("token", mToken);
        b.putLong("gid", groupid);

        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);
        return true;
    }

    public static boolean editMembers(long groupid, String[] members, boolean remove, ResponseHandler handler) {
        if(TextUtils.isEmpty(mToken) || 0 == groupid || null == members)
            return false;

        Bundle b = new Bundle();
        b.putString("op", "editmembers");
        b.putString("token", mToken);
        b.putLong("gid", groupid);
        b.putString("m", array2String(members));
        b.putInt("delete", remove?1:0);

        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);
        return true;
    }

    public static boolean setAdmin(long groupid, String member, boolean admin, ResponseHandler handler) {
        if(TextUtils.isEmpty(mToken) || 0 == groupid || TextUtils.isEmpty(member))
            return false;

        Bundle b = new Bundle();
        b.putString("op", "setadmin");
        b.putString("token", mToken);
        b.putLong("gid", groupid);
        b.putString("m", member);
        b.putInt("admin", admin?1:0);

        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);
        return true;
    }

    public static String array2String(String[] a) {
        String str = "";
        for( int i = 0; i < a.length; i++) {
            if(i > 0)
                str += ",";
            str += a[i];
        }

        return str;
    }


    public static boolean setStringValue(String key, String value) {
        try {
            synchronized (mSharedPref) {
                SharedPreferences.Editor poEditor = mSharedPref.edit();
                poEditor.putString(key, value);
                poEditor.commit();
                //backup();
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to set long value in RMS:" + e.getMessage());
            return false;
        }
    }

    public static String getStringValue(String key, String defaultVal) {
        try {
            synchronized (mSharedPref) {
                if (mSharedPref.contains(key))
                    return mSharedPref.getString(key, defaultVal);
                return defaultVal;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to fet long value in RMS:" + e.getMessage());
            return defaultVal;
        }
    }

    public static void notify(String channelid, int id, String title, String message) {
        mNotifyUser.sendNotification(channelid, id, title, message);
    }

    public static void notify(Mesibo.MessageParams params, String message) {
        // if call is in progress, we must give notification even if reading because user is in call
        // screen
        if(!MesiboCall.getInstance().isCallInProgress() && Mesibo.isReading(params))
            return;

        if(Mesibo.ORIGIN_REALTIME != params.origin || Mesibo.MSGSTATUS_OUTBOX == params.getStatus())
            return;

        //MUST not happen for realtime message
        if(params.groupid > 0 && null == params.groupProfile)
            return;

        Mesibo.UserProfile profile = Mesibo.getUserProfile(params);

        // this will also mute message from user in group
        if(null != profile && profile.isMuted())
            return;

        String name = params.peer;
        if(null != profile) {
            name = profile.name;
        }

        if(params.groupid > 0) {
            Mesibo.UserProfile gp = Mesibo.getUserProfile(params.groupid);
            if(null == gp)
                return; // must not happen

            if(gp.isMuted())
                return;

            name += " @ " + gp.name;
        }

        if(params.isMissedCall()) {
                String subject = "Mesibo Missed Call";
                message = "You missed a mesibo " + (params.isVideoCall()?"video ":"") + "call from " + profile.name;
                SampleAPI.notify(NotifyUser.NOTIFYCALL_CHANNEL_ID, 2, subject, message);
                return;
        }

        // outgoing or incoming call
        if(params.isCall()) return;

        mNotifyUser.sendNotificationInList(name, message);
    }

    public static void addContacts(ArrayList<Mesibo.UserProfile> profiles, boolean hidden) {

        ArrayList<String> c = new ArrayList<String>();

        for(int i=0; i < profiles.size(); i++) {
            Mesibo.UserProfile profile = profiles.get(i);

            if ((profile.flag & Mesibo.UserProfile.FLAG_TEMPORARY) > 0 && (profile.flag & Mesibo.UserProfile.FLAG_PROFILEREQUESTED) == 0 && null != profile.address) {
                profile.flag |= Mesibo.UserProfile.FLAG_PROFILEREQUESTED;
                c.add(profile.address);
            }
        }

        if(c.size() == 0)
            return;

        getContacts(c, hidden, true);
    }

    private static ArrayList<Mesibo.UserProfile> mPendingHiddenContacts = null;
    public static synchronized  void autoAddContact(Mesibo.MessageParams params) {
        if(null == params) {
            if(null != mPendingHiddenContacts) {
                addContacts(mPendingHiddenContacts, true);
                mPendingHiddenContacts = null;
            }
            return;
        }

        // the logic is if user replies, we will see contact details, else not */
        if(/*Mesibo.ORIGIN_REALTIME != params.origin || */Mesibo.MSGSTATUS_OUTBOX == params.getStatus())
            return;

        if((params.profile.flag& Mesibo.UserProfile.FLAG_TEMPORARY) == 0 || (params.profile.flag & Mesibo.UserProfile.FLAG_PROFILEREQUESTED) > 0 )
            return;

        if(null == mPendingHiddenContacts)
            mPendingHiddenContacts = new ArrayList<Mesibo.UserProfile>();

        mPendingHiddenContacts.add(params.profile);

        if(!mContactSyncOver) {
            return;
        }

        addContacts(mPendingHiddenContacts, true);
        mPendingHiddenContacts = null;
    }

    private static String mGCMToken = null;
    private static boolean mGCMTokenSent = false;
    public static void setGCMToken(String token) {
        mGCMToken = token;
        sendGCMToken();
    }

    private static void sendGCMToken() {
        if(null == mGCMToken || mGCMTokenSent) {
            return;
        }

        synchronized (SampleAPI.class) {
            if(mGCMTokenSent)
                return;
            mGCMTokenSent = true;
        }

        String gcmtoken = Mesibo.readKey(KEY_GCMTOKEN);
        if(!TextUtils.isEmpty(gcmtoken) && gcmtoken.equalsIgnoreCase(mGCMToken)) {
            mGCMTokenSent = true;
            return;
        }

        Bundle b = new Bundle();
        b.putString("op", "setnotify");
        b.putString("token", mToken);
        b.putString("notifytoken", mGCMToken);

        ResponseHandler http = new ResponseHandler() {
            @Override
            public void HandleAPIResponse(Response response) {
                if(null != response && response.result.equalsIgnoreCase("OK") ) {
                    Mesibo.setKey(KEY_GCMTOKEN, mGCMToken);
                } else
                    mGCMTokenSent = false;
            }
        };

        http.sendRequest(b, null, null);
    }

    /* if it is called from service, it's okay to block, we should wait till
       we are online. As soon as we return, service will be destroyed
     */
    public static void onGCMMessage(boolean inService) {
        Mesibo.setAppInForeground(null, -1, true);

        while(inService) {
            if(Mesibo.STATUS_ONLINE == Mesibo.getConnectionStatus())
                break;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        // wait for messages to receive etc
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    public static void notifyClear() {
        mNotifyUser.clearNotification();
    }

    private static boolean mAutoDownload = true;

    private static void initAutoDownload() {
        String autodownload = Mesibo.readKey(KEY_AUTODOWNLOAD);
        mAutoDownload = (TextUtils.isEmpty(autodownload));
    }

    public static void setMediaAutoDownload(boolean autoDownload) {
        mAutoDownload = autoDownload;
        Mesibo.setKey(KEY_AUTODOWNLOAD, mAutoDownload?"":"0");
    }

    public static boolean getMediaAutoDownload() {
        return mAutoDownload;
    }


}
