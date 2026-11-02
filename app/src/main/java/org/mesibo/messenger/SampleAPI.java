package org.mesibo.messenger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboHttp;
import com.mesibo.api.MesiboLocationConfig;
import com.mesibo.api.MesiboMessage;
import com.mesibo.api.MesiboPhoneContactsManager;
import com.mesibo.api.MesiboProfile;
import com.mesibo.calls.api.MesiboCall;
import org.mesibo.messenger.fcm.MesiboPushHelper;
import com.mesibo.messaging.MesiboUI;
import com.mesibo.messaging.MesiboDialogs;

import org.json.JSONObject;


public class SampleAPI  {
    private static final String TAG="SampleAPI";
    private static NotifyUser mNotifyUser = null;
    private static boolean mSyncPending = true;
    private static Context mContext = null;
    private static boolean mResetSyncedContacts = false;

    public final static String KEY_SYNCEDCONTACTS = "AppSyncedContacts";
    public final static String KEY_SYNCEDDEVICECONTACTSTIME = "AppSyncedPhoneContactTs";
    public final static String KEY_SYNCEDCONTACTSTIME = "AppSyncedTsNew";
    public final static String KEY_AUTODOWNLOAD = "autodownload";
    public final static String KEY_GCMTOKEN = "gcmtoken";
    public final static String KEY_NOTIFY_MESSAGES = "notifymessages";
    public final static String KEY_NOTIFY_GROUPS = "notifygroups";

    public static abstract class ResponseHandler implements MesiboHttp.Listener {
        private MesiboHttp http = null;
        private Bundle mRequest = null;
        private boolean mBlocking = false;
        private boolean mOnUiThread = false;
        public static boolean result = true;
        public Context mContext = null;

        @Override
        public boolean Mesibo_onHttpProgress(MesiboHttp http, int state, int percent) {
            if(percent < 0) {
                HandleAPIResponse(null);
                return true;
            }

            if(100 == percent && MesiboHttp.STATE_DOWNLOAD == state) {
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
                    parseResponse(response, context, false);
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
                            parseResponse(r, context, true);
                            HandleAPIResponse(r);
                        }
                    };
                    uiHandler.post(myRunnable);
                }
            }
            return true;
        }

        public void setOnUiThread(boolean onUiThread) {
            mOnUiThread = onUiThread;
        }

        public boolean sendRequest(JSONObject j, String filePath, String formFieldName) {

            try {
                j.put("dt", String.valueOf(Mesibo.getDeviceType()));
            } catch (Exception e) {

            }
            http = new MesiboHttp();
            http.url = mApiUrl;
            http.post = j.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

            http.contentType = "application/json";
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

    public static class Urls {
        public String upload = "";
        public String download = "";
    }

    public static class Invite {
        public String text = "";
        public String subject = "";
        public String title = "";
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
        public String errmsg;
        public String errtitle;
        public String message;
        public String title;
        public String token;
        // set with the token for a user without a profile yet; Object since it may come as
        // true, 1 or "1"
        @SerializedName("new_user")
        public Object newUser;
        public Contacts[] contacts;
        public String name;
        public String status;
        public String members;
        public String photo;
        public String phone;
        public String cc;

        public Urls urls;
        public Invite share;

        public long gid;
        public int type;
        public int profile;
        public long ts = 0;
        public String tn = null;

        public boolean isNewUser() {
            if (newUser instanceof Boolean) return (Boolean) newUser;
            if (newUser instanceof Number) return 0 != ((Number) newUser).intValue();
            if (newUser instanceof String) return "1".equals(newUser) || "true".equalsIgnoreCase((String) newUser);
            return false;
        }

        Response() {
            result = null;
            op = null;
            error = null;
            errmsg = null;
            errtitle = null;
            token = null;
            contacts = null;
            gid = 0;
            type = 0;
            profile = 0;
            urls = null;
        }
    }

    private static Gson mGson = new Gson();
    private static String mApiUrl = "https://messenger.mesibo.com";

    private static boolean invokeApi(final Context context, final JSONObject postBunlde, String filePath, String formFieldName, boolean uiThread) {
        ResponseHandler http = new ResponseHandler() {
            @Override
            public void HandleAPIResponse(Response response) {

            }
        };

        http.setContext(context);
        http.setOnUiThread(uiThread);
        return http.sendRequest(postBunlde, filePath, formFieldName);
    }

    private static boolean parseResponse(Response response, Context context, boolean uiThread) {

            if(null == response || null == response.result) {

                if(uiThread && null != context) {
                    showConnectionError(context);
                }
                return false;
            }

            if(!response.result.equalsIgnoreCase("OK") ) {
                if(null != response.error && response.error.equalsIgnoreCase("AUTHFAIL")) {
                    forceLogout();
                    return false;
                }

                if(null != response.errmsg) {
                    if(null == response.errtitle) response.errtitle = "Failed";
                    MesiboDialogs.alert(context, response.errtitle, response.errmsg);
                }
                return false;
            }

            boolean save = false;
            if(null != response.urls) {
                AppConfig.getConfig().uploadurl = response.urls.upload;
                AppConfig.getConfig().downloadurl = response.urls.download;
                save = true;
            }

            if(null != response.share) {
                AppConfig.getConfig().invite = response.share;
                save = true;
            }

            if(!TextUtils.isEmpty(response.message)) {
                MesiboDialogs.alert(context, response.title, response.message);
            }

            if("login".equals(response.op) && !TextUtils.isEmpty(response.token)) {
                AppConfig.getConfig().token = response.token; //TBD, save into preference
                AppConfig.getConfig().phone = response.phone;
                mResetSyncedContacts = true;
                mSyncPending = true;

                save = true;

                Mesibo.reset();
                if(startMesibo(true)) {
                    // need permission
                    startSync();
                }
            }
            else if("logout".equals(response.op)) {
                forceLogout();
                AppConfig.reset();
            }

            if(save)
                AppConfig.save();
        return true;
    }

    public static void showConnectionError(Context context) {
        String title = "No Internet Connection";
        String message = "Your phone is not connected to the internet. Please check your internet connection and try again later.";
        MesiboDialogs.alert(context, title, message);
    }

    public static void saveLocalSyncedContacts(String contacts, long timestamp) {
        Mesibo.setKey(SampleAPI.KEY_SYNCEDCONTACTS, contacts);
        Mesibo.setKey(SampleAPI.KEY_SYNCEDDEVICECONTACTSTIME, String.valueOf(timestamp));
    }

    public static void saveSyncedTimestamp(long timestamp) {
        Mesibo.setKey(SampleAPI.KEY_SYNCEDCONTACTSTIME, String.valueOf(timestamp));
    }

    public static void init(Context context) {
        mContext = context;

        Mesibo api = Mesibo.getInstance();
        api.init(context);

        Mesibo.initCrashHandler(MesiboListeners.getInstance());
        Mesibo.uploadCrashLogs();

        if(!TextUtils.isEmpty(AppConfig.getConfig().token)) {
            if(startMesibo(false))
                startSync();
        }
    }

    public static String getPhone() {
        if(!TextUtils.isEmpty(AppConfig.getConfig().phone)) {
            return AppConfig.getConfig().phone;
        }


        MesiboProfile u = Mesibo.getSelfProfile();

        //MUST not happen
        if(null == u) {
            forceLogout();
            return null;
        }
        AppConfig.getConfig().phone = u.address;
        AppConfig.save();
        return AppConfig.getConfig().phone;
    }


    public static String getToken() {
        return AppConfig.getConfig().token;
    }
    public static String getUploadUrl() { return AppConfig.getConfig().uploadurl; }
    public static String getDownloadUrl() { return AppConfig.getConfig().downloadurl; }

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

        startContactsSync();
    }

    public static void startContactsSync() {
        MesiboPhoneContactsManager contacts = Mesibo.getPhoneContactsManager();
        contacts.overrideProfileName(true);
        contacts.start();
    }

    public static boolean startMesibo(boolean resetContacts) {

        // sender id comes from google-services.json
        MesiboPushHelper.startRegistration(MesiboListeners.getInstance());

        String path = Mesibo.getBasePath();
        MesiboUI.setMediaPath(path);

        // add lister
        Mesibo.addListener(MesiboListeners.getInstance());
        MesiboUI.setListener(new UIListener());
        CallListeners.register();

        // add file transfer handler
        MesiboFileTransferHelper fileTransferHelper = new MesiboFileTransferHelper();
        //Mesibo.addListener(fileTransferHelper);

        // this will also register listener from the constructor
        if(null == mNotifyUser)
            mNotifyUser = new NotifyUser(MainApplication.getAppContext());

        // set access token
        if(0 != Mesibo.setAccessToken(AppConfig.getConfig().token)) {
            AppConfig.getConfig().token = "";
            AppConfig.save();
            return false;
        }

        // set database after setting access token so that it's associated with user
        Mesibo.setDatabase("mesibo.db");

        // do this after setting token and db
        if(resetContacts) {
            SampleAPI.saveLocalSyncedContacts("", 0);
            SampleAPI.saveSyncedTimestamp(0);
        }

        initAutoDownload();
        Mesibo.enableGalleryScanForMedia(true);
        // Now start mesibo
        if(0 != Mesibo.start()) {
            return false;
        }

        MesiboLocationConfig lc = new MesiboLocationConfig();
        lc.backgroundRefresh = false;

        Mesibo.getLocationManager().start(lc);

        MesiboPhoneContactsManager contactsManager = Mesibo.getPhoneContactsManager();
        contactsManager.overrideProfileName(true);

        if(resetContacts) {
            Mesibo.getPhoneContactsManager().reset();
        }

        // explicit package: implicit broadcasts are not delivered to manifest receivers (API 26+)
        Intent restartIntent = new Intent(MainApplication.getRestartIntent());
        restartIntent.setPackage(MainApplication.getAppContext().getPackageName());

        Mesibo.runInBackground(MainApplication.getAppContext(), null, restartIntent);

        return true;
    }

    public static void forceLogout(){
        mGCMTokenSent = false;
        Mesibo.setKey(KEY_GCMTOKEN, "");
        SampleAPI.saveLocalSyncedContacts("", 0);
        SampleAPI.saveSyncedTimestamp(0);
        Mesibo.stop(true);
        Mesibo.getPhoneContactsManager().reset();
        AppConfig.getConfig().reset();
        notifyClear();
        Mesibo.setAccessToken(null);

        UIManager.launchStartupActivity(mContext, true);
    }

    public static boolean startLogout() {
        if(TextUtils.isEmpty(AppConfig.getConfig().token))
            return false;
        JSONObject b = new JSONObject();
        try {
            b.put("op", "logout");
            b.put("token", AppConfig.getConfig().token);
        } catch (Exception e) {

        }
        invokeApi(null, b, null, null, false);
        return true;
    }

    public static void login(String phoneNumber, String verificationCode, ResponseHandler handler) {

        JSONObject b = new JSONObject();
        try {
            b.put("op", "login");
            b.put("appid", mContext.getPackageName());
            b.put("phone", phoneNumber);
            if (!TextUtils.isEmpty(verificationCode)) {
                b.put("otp", verificationCode);
                if(!TextUtils.isEmpty(AppConfig.getConfig().uniqueid))
                    b.put("device", AppConfig.getConfig().uniqueid);
            }
        } catch (Exception e) {

        }

        handler.setOnUiThread(true);
        handler.sendRequest(b, null, null);
    }

    public static void notify(int id, String title, String message) {
        if(null != mNotifyUser) mNotifyUser.sendNotification(id, title, message);
    }

    public static void notify(MesiboMessage params, String message) {
        // if call is in progress, we must give notification even if reading because user is in call
        // screen
        if(!MesiboCall.getInstance().isCallInProgress() && Mesibo.isReading(params))
            return;

        // TBD, create read session for unread messages in database
        if(!params.isRealtimeMessage() || params.isInOutbox() || params.isOutgoing())
            return;

        //MUST not happen for realtime message
        if(params.groupid > 0 && null == params.groupProfile)
            return;

        // calls (incl. missed calls) are notified from MesiboCall_onNotify
        if(params.isCall() || params.isMissedCall())
            return;

        if(params.groupid > 0 ? !getGroupMessageNotification() : !getMessageNotification())
            return;

        MesiboProfile profile = Mesibo.getProfile(params);

        // this will also mute message from user in group
        if(null != profile && profile.isMuted())
            return;

        String name = params.peer;
        if(null != profile) {
            name = profile.getName();
        }

        if(params.groupid > 0) {
            MesiboProfile gp = Mesibo.getProfile(params.groupid);
            if(null == gp)
                return; // must not happen

            if(gp.isMuted())
                return;

            name += " @ " + gp.getName();
        }

        if(null != mNotifyUser) mNotifyUser.sendNotificationInList(name, message);
    }

    private static String mGCMToken = null;
    private static boolean mGCMTokenSent = false;
    public static synchronized void setGCMToken(String token) {
        // FCM may rotate the token; a new token must be sent again
        if(null != token && !token.equals(mGCMToken))
            mGCMTokenSent = false;
        mGCMToken = token;
        sendGCMToken();
    }

    private static synchronized void sendGCMToken() {
        if(null == mGCMToken || mGCMTokenSent)
            return;
        mGCMTokenSent = true;
        Mesibo.setPushToken(mGCMToken);

    }

    /* Called on the FCM service thread. Block until online (bounded: FCM allows only a
       short execution window per message) and give mesibo a moment to receive messages.
     */
    private static final long PUSH_WAIT_ONLINE_MS = 15000;
    public static void onGCMMessage(boolean inService) {
        Mesibo.setForegroundContext(null, -1, true);
        if(!inService)
            return;

        long deadline = System.currentTimeMillis() + PUSH_WAIT_ONLINE_MS;
        try {
            while(Mesibo.STATUS_ONLINE != Mesibo.getConnectionStatus()
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(500);
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void notifyClear() {
        if(null != mNotifyUser) mNotifyUser.clearNotification();
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

    // notification settings: empty (default) = enabled, "0" = disabled
    private static boolean readFlag(String key) {
        return !"0".equals(Mesibo.readKey(key));
    }

    public static boolean getMessageNotification() { return readFlag(KEY_NOTIFY_MESSAGES); }
    public static boolean getGroupMessageNotification() { return readFlag(KEY_NOTIFY_GROUPS); }

    public static void setMessageNotification(boolean enable) {
        Mesibo.setKey(KEY_NOTIFY_MESSAGES, enable ? "" : "0");
    }

    public static void setGroupMessageNotification(boolean enable) {
        Mesibo.setKey(KEY_NOTIFY_GROUPS, enable ? "" : "0");
    }


}
