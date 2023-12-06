/******************************************************************************
* By accessing or copying this work, you agree to comply with the following   *
* terms:                                                                      *
*                                                                             *
* Copyright (c) 2019-2023 mesibo                                              *
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
* Documentation: https://mesibo.com/documentation/                            *
*                                                                             *
* Source Code Repository: https://github.com/mesibo/                          *
*******************************************************************************/

package org.mesibo.messenger;

import com.google.gson.Gson;

import android.app.backup.BackupManager;
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

    private void backup() {
    }

    public Boolean isFirstTime() {
        return firstTime;
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
        spe.commit();
    }

    public static void reset() {
        mConfig.reset();
        save();
        getInstance().backup();
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
                backup();
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to updateRMS(): " + e.getMessage());
            return false;
        }

    }

    public boolean setStringValue(String key, String value) {
        try {
            synchronized (mSharedPref) {
                SharedPreferences.Editor poEditor = mSharedPref.edit();
                poEditor.putString(key, value);
                poEditor.commit();
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to set long value in RMS:" + e.getMessage());
            return false;
        }
    }

    public String getStringValue(String key, String defaultVal) {
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

    public boolean setLongValue(String key, long value) {
        try {
            synchronized (mSharedPref) {
                SharedPreferences.Editor poEditor = mSharedPref.edit();
                poEditor.putLong(key, value);
                poEditor.commit();
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to set long value in RMS:" + e.getMessage());
            return false;
        }
    }

    public long getLongValue(String key, long defaultVal) {
        try {
            synchronized (mSharedPref) {
                if (mSharedPref.contains(key))
                    return mSharedPref.getLong(key, defaultVal);
                return defaultVal;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to fet long value in RMS:" + e.getMessage());
            return defaultVal;
        }
    }

    public boolean setIntValue(String key, int value) {
        try {
            synchronized (mSharedPref) {
                SharedPreferences.Editor poEditor = mSharedPref.edit();
                poEditor.putInt(key, value);
                poEditor.commit();
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to set int value in RMS:" + e.getMessage());
            return false;
        }
    }

    public int getIntValue(String key, int defaultVal) {

        try {
            synchronized (mSharedPref) {
                if (mSharedPref.contains(key))
                    return mSharedPref.getInt(key, defaultVal);
                return defaultVal;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to get int value in RMS:" + e.getMessage());
            return defaultVal;
        }
    }

    public boolean setBooleanValue(String key, Boolean value) {
        try {
            synchronized (mSharedPref) {
                SharedPreferences.Editor poEditor = mSharedPref.edit();
                poEditor.putBoolean(key, value);
                poEditor.commit();
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to set long value in RMS:" + e.getMessage());
            return false;
        }
    }


};

