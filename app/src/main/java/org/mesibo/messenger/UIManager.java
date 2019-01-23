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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;

import com.mesibo.mediapicker.AlbumListData;
import com.mesibo.mediapicker.MediaPicker;

import org.mesibo.messenger.AppSettings.SettingsActivity;
import com.mesibo.uihelper.ILoginInterface;
import com.mesibo.uihelper.IProductTourListener;
import com.mesibo.uihelper.MesiboUiHelper;
import com.mesibo.uihelper.MesiboUiHelperConfig;
import com.mesibo.uihelper.WelcomeScreen;
import com.mesibo.messaging.MesiboUI;


import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class UIManager {

    public static void launchStartupActivity(Context context, boolean skipTour) {
        Intent intent = new Intent(context, StartUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(StartUpActivity.SKIPTOUR, skipTour);
        context.startActivity(intent);
    }

    public static boolean mMesiboLaunched = false;
    public static void launchMesibo(Context context, int flag, boolean startInBackground, boolean keepRunningOnBackPressed) {
        mMesiboLaunched = true;
        MesiboUI.launch(context, flag, startInBackground, keepRunningOnBackPressed);
    }

    public static void launchMesiboContacts(Context context, long forwardid, int selectionMode, int flag, Bundle bundle) {
        MesiboUI.launchContacts(context, forwardid, selectionMode, flag, bundle);
    }

    public static void launchUserProfile(Context context, long groupid, String peer) {
        Intent subActivity = new Intent(context, ShowProfileActivity.class);
        subActivity.
                putExtra("peer", peer).
                putExtra("groupid", groupid);
        context.startActivity(subActivity);
    }

    public static void launchUserSettings(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void launchUserRegistration(Context context, int flag) {
        Intent subActivity = new Intent(context, EditProfileActivity.class);
        if(flag > 0)
            subActivity.setFlags(flag);
        context.startActivity(subActivity);
    }

    public static void launchImageViewer(Activity context, String filePath) {
        MediaPicker.launchImageViewer(context, filePath);
    }
    public static void launchImageViewer(Activity context, ArrayList<String> files, int firstIndex) {
        MediaPicker.launchImageViewer(context, files, firstIndex);
    }

    public static void launchImageEditor(Context context, int type, int drawableid, String title, String filePath, boolean showEditControls, boolean showTitle, boolean showCropOverlay, boolean squareCrop, int maxDimension, MediaPicker.ImageEditorListener listener){
        MediaPicker.launchEditor((AppCompatActivity)context, type, drawableid, title, filePath, showEditControls, showTitle, showCropOverlay, squareCrop, maxDimension, listener);
    }

    public static void launchAlbum(Activity context, List<AlbumListData> albumList) {
        MediaPicker.launchAlbum(context, albumList);
    }

    public static boolean mProductTourShown = false;
    public static void launchWelcomeactivity(Activity context, boolean newtask, ILoginInterface loginInterface, IProductTourListener tourListener){



        MesiboUiHelperConfig config = new MesiboUiHelperConfig();

        List<WelcomeScreen> res = new ArrayList<WelcomeScreen>();

        res.add(new WelcomeScreen("Messaging in your apps", "Over 79% of all apps require some form of communications. Mesibo is built from ground-up to power this!", 0, R.drawable.welcome, 0xff00868b));
        res.add(new WelcomeScreen("Messaging, Voice, & Video", "Complete infrastructure with powerful APIs to get you started, rightaway!", 0, R.drawable.videocall, 0xff0f9d58));
        //res.add(new WelcomeScreen("Plug & Play", "Not just APIs, you can even use pluggable UI modules - Buid in just a few hours", 0, R.drawable.profile, 0xfff4b400));
        res.add(new WelcomeScreen("Open Source", "Quickly integrate Mesibo in your own app using freely available source code", 0, R.drawable.opensource_ios, 0xff054a61));

        //res.add(new WelcomeScreen("No Sweat Pricing", "Start free & only pay as you grow!", 0, R.drawable.users, 0xff0f9d58));

        // dummy - requires
        res.add(new WelcomeScreen("", ":", 0, R.drawable.welcome, 0xff00868b));


        config.mScreens = res;
        config.mWelcomeBottomText = "Mesibo will never share your information";
        config.mWelcomeBackgroundColor = 0xff00868b;

        config.mBackgroundColor = 0xffffffff;
        config.mPrimaryTextColor = 0xff172727;
        config.mButttonColor = 0xff00868b;
        config.mButttonTextColor = 0xffffffff;
        config.mSecondaryTextColor = 0xff666666;

        config.mScreenAnimation = true;
        config.mSmartLockUrl = "https://app.mesibo.com";

        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.READ_CONTACTS);
        config.mPermissions = permissions;
        config.mPermissionsRequestMessage = "Mesibo requires Storage and Contacts permissions so that you can send messages and make calls to your contacts. Please grant to continue!";
        config.mPermissionsDeniedMessage = "Mesibo will close now since the required permissions were not granted";


        MesiboUiHelper.setConfig(config);

        if(mMesiboLaunched) {
            launchLogin(context, MesiboListeners.getInstance());
            return;
        }

        mProductTourShown = true;
        MesiboUiHelper.launchTour(context, newtask, tourListener);
    }

    public static void launchLogin(Activity context, ILoginInterface loginInterface){
        MesiboUiHelper.launchAccountKit(context, true, loginInterface, null);
    }

    public static void showAlert(Context context, String title, String message, DialogInterface.OnClickListener pl, DialogInterface.OnClickListener nl) {
        if(null == context) {
            return; //
        }
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        // dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setCancelable(true);

        dialog.setPositiveButton(android.R.string.ok, pl);
        dialog.setNegativeButton(android.R.string.cancel, nl);

        try {
            dialog.show();
        } catch (Exception e) {

        }
    }

    public static void showAlert(Context context, String title, String message) {
        showAlert(context, title, message, null, null);
    }
}
