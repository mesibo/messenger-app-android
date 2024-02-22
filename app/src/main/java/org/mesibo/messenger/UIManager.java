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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.mesibo.mediapicker.AlbumListData;
import com.mesibo.mediapicker.MediaPicker;

import org.mesibo.messenger.AppSettings.SettingsActivity;
import com.mesibo.uihelper.MesiboLoginUiHelperListener;
import com.mesibo.uihelper.IProductTourListener;
import com.mesibo.uihelper.MesiboUiHelper;
import com.mesibo.uihelper.MesiboUiHelperConfig;
import com.mesibo.uihelper.WelcomeScreen;
import com.mesibo.messaging.MesiboUI;


import java.util.ArrayList;
import java.util.List;

public class UIManager {

    public static void launchStartupActivity(Context context, boolean skipTour) {
        Intent intent = new Intent(context, StartUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(StartUpActivity.SKIPTOUR, skipTour);
        context.startActivity(intent);
    }

    public static boolean mMesiboLaunched = false;
    public static void launchMesibo(Context context, MesiboUI.MesiboUserListScreenOptions opts) {
        mMesiboLaunched = true;
        MesiboUI.launchUserList(context, opts);
    }

    public static void launchUserSettings(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void launchEditProfile(Context context, int flag, long groupid, boolean launchMesibo) {
        Intent subActivity = new Intent(context, EditProfileActivity.class);
        if(flag > 0)
            subActivity.setFlags(flag);
        subActivity.putExtra("groupid", groupid);
        subActivity.putExtra("launchMesibo", launchMesibo);

        context.startActivity(subActivity);
    }

    public static void launchImageViewer(Activity context, String filePath) {
        MediaPicker.launchImageViewer(context, filePath);
    }

    public static void launchImageEditor(Context context, int type, int drawableid, String title, String filePath, boolean showEditControls, boolean showTitle, boolean showCropOverlay, boolean squareCrop, int maxDimension, MediaPicker.ImageEditorListener listener){
        MediaPicker.launchEditor((AppCompatActivity)context, type, drawableid, title, filePath, showEditControls, showTitle, showCropOverlay, squareCrop, maxDimension, listener);
    }

    public static boolean mProductTourShown = false;
    public static void initUiHelper() {
        MesiboUiHelperConfig config = new MesiboUiHelperConfig();

        List<WelcomeScreen> res = new ArrayList<WelcomeScreen>();

        res.add(new WelcomeScreen("Messaging, & Calls in your apps", "Add messaging, Video and Voice calls & conferencing in your apps in no time. Mesibo is built from ground-up to power this!", 0, R.drawable.welcome, 0xff00868b));
        res.add(new WelcomeScreen("Messaging, Voice, & Video", "Complete infrastructure with powerful APIs to get you started, rightaway!", 0, R.drawable.videocall, 0xff0f9d58));
        res.add(new WelcomeScreen("Open Source", "Quickly integrate mesibo in your own app using freely available source code", 0, R.drawable.opensource_ios, 0xff054a61));

        // dummy - requires
        res.add(new WelcomeScreen("", ":", 0, R.drawable.welcome, 0xff00868b));


        config.mScreens = res;
        config.mWelcomeBottomText = "Mesibo will never share your information";

        config.mWelcomeBackgroundColor = 0xff00868b; //TBD, not required, take from welcomescren[0]

        config.mBackgroundColor = 0xffffffff;
        config.mPrimaryTextColor = 0xff172727;
        config.mButttonColor = 0xff00868b;
        config.mButttonTextColor = 0xffffffff;
        config.mSecondaryTextColor = 0xff666666;

        config.mScreenAnimation = true;

        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.READ_CONTACTS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        config.mPermissions = permissions;
        config.mPermissionsRequestMessage = "mesibo requires Storage and Contacts permissions so that you can send messages and make calls to your contacts. Please grant to continue!";
        config.mPermissionsDeniedMessage = "mesibo will close now since the required permissions were not granted";

        //config.mPhoneVerificationText = "Get OTP from your mesibo console (In App settings), login https://mesibo.com/console";
        config.mPhoneVerificationBottomText = "IMPORTANT: We will NOT send OTP.  Instead, you can generate OTP for any number from the mesibo console. Sign up at https://mesibo.com/console";
        config.mLoginBottomDescColor = 0xAAFF0000;

        MesiboUiHelper.setConfig(config);
    }

    public static void launchWelcomeactivity(Activity context, boolean newtask, MesiboLoginUiHelperListener loginInterface, IProductTourListener tourListener){

        initUiHelper();


        // if mesibo was lauched in this session, we came here after logout, so
        // no need to show tour
        if(mMesiboLaunched) {
            launchLogin(context, MesiboListeners.getInstance());
            return;
        }

        mProductTourShown = true;
        MesiboUiHelper.launchTour(context, newtask, tourListener);
    }

    public static void launchLogin(Activity context, MesiboLoginUiHelperListener loginInterface){
        initUiHelper();
        MesiboUiHelper.launchLogin(context, true, 2, loginInterface);
    }

    public static void showAlert(Context context, String title, String message, DialogInterface.OnClickListener pl, DialogInterface.OnClickListener nl) {
        if(null == context) {
            return; //
        }
        androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(true);

        dialog.setPositiveButton(android.R.string.ok, pl);
        dialog.setNegativeButton(android.R.string.cancel, nl);

        try {
            dialog.show();
        } catch (Exception e) {
        }
    }

    public static void showAlert(Context context, String title, String message) {
        if(null == context) return;
        showAlert(context, title, message, null, null);
    }

}
