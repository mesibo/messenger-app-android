package org.mesibo.messenger;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

public class AppConfig {
    private static final String TAG = "AppSettings";
    public static final String sharedPrefKey = "org.mesibo.messenger";
    private static final String systemPreferenceKey = "mesibo-app-settings";

    public static class Configuration {
        public String token = "";
        public String phone = "";
        public SampleAPI.Invite invite = null;
        public String uploadurl = null;
        public String downloadurl = null;
        public String uniqueid = null;

        public void reset() {
            token = "";
            phone = "";
            invite = null;
            uploadurl = "";
            downloadurl = "";
            // uniqueid - DO NOT RESET
        }
    }

    //System Specific Preferences - does not change across logins
    public static Configuration mConfig = new Configuration();

    private boolean firstTime = false;
    private Context mContext;
    SharedPreferences mSharedPref = null;

    public static AppConfig getInstance() {
        return _instance;
    }

    public static Configuration getConfig() {
        return mConfig;
    }

    private static AppConfig _instance  = null;
    public AppConfig(Context c) {
        _instance = this;
        mContext = c;

        mSharedPref = c.getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE);
        firstTime = false;
        if (!mSharedPref.contains(systemPreferenceKey)) {
            firstTime = true;
        }

        getAppSetting();

        // https://developer.android.com/training/articles/user-data-ids
        if(TextUtils.isEmpty(mConfig.uniqueid)) {
            mConfig.uniqueid = UUID.randomUUID().toString();
            saveSettings();
        } else if (firstTime) {
            saveSettings();
        }
    }

    // We could use TAG also - to save/retrieve settings
    public void getAppSetting() {
        Gson gson = new Gson();
        String json = mSharedPref.getString(systemPreferenceKey, "");
        mConfig = gson.fromJson(json, Configuration.class);

        if(null == mConfig)
            mConfig = new Configuration();
    }

    private void putAppSetting(SharedPreferences.Editor spe) {
        Gson gson = new Gson();

        String json = gson.toJson(mConfig);
        spe.putString(systemPreferenceKey, json);
        spe.apply(); // async write; commit() blocked the main thread on every save
    }

    public static void reset() {
        // reset in place: callers may hold a reference to mConfig
        mConfig.reset();
        save();
    }


    public static void save() {
        getInstance().saveSettings();
    }

    public boolean saveSettings() {
        Log.d(TAG, "Updating RMS .. ");
        try {
            synchronized (mSharedPref) {
                SharedPreferences.Editor spe = mSharedPref.edit();
                putAppSetting(spe);
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to updateRMS(): " + e.getMessage());
            return false;
        }
    }
}
