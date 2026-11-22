package org.mesibo.messenger;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.mesibo.api.Mesibo;
import com.mesibo.messaging.MesiboUI;

/**
 * Trampoline activity: routes to login or the user list and then finishes.
 *
 * It must never be left alive in the task, otherwise bringing the task to the
 * front (launcher icon, notification tap) shows its empty layout - the
 * "blank startup" screen. Hence:
 *  - every launch, including re-delivery via onNewIntent(), routes to the next screen
 *  - the activity always finishes, whether or not the Play update check succeeds
 * It is not noHistory: the permission and update dialogs cover it and must deliver results.
 */
public class StartUpActivity extends AppCompatActivity {

    private static final String TAG = "MesiboStartupActivity";
    public final static String INTENTEXIT = "exit";
    public final static String SKIPTOUR = "skipTour";
    public final static String STARTINBACKGROUND = "startinbackground";

    private boolean mRunInBackground = false;
    private boolean mUpdateFlowStarted = false;

    private final ActivityResultLauncher<IntentSenderRequest> mUpdateLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> finish());

    public static void newInstance(Context context, boolean startInBackground) {
        Intent i = new Intent(context, StartUpActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(STARTINBACKGROUND, startInBackground);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    // singleTop: a launch while this activity is on top arrives here instead of onCreate.
    // Previously such launches were ignored, leaving the blank screen visible.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getBooleanExtra(INTENTEXIT, false)) {
            finish();
            return;
        }

        // Only honour a background start when the app is genuinely not visible; a user
        // tapping the launcher must never get the task pushed to the back.
        mRunInBackground = intent.getBooleanExtra(STARTINBACKGROUND, false)
                && !Mesibo.isAppInForeground()
                && (Mesibo.getTimestamp() - Mesibo.getStartTimestamp()) <= 30000;

        Log.d(TAG, "Starting " + (mRunInBackground ? "in background" : "in foreground"));

        if (mRunInBackground)
            moveTaskToBack(true);

        startNextActivity();
    }

    private void startNextActivity() {
        if (TextUtils.isEmpty(SampleAPI.getToken())) {
            if (getIntent().getBooleanExtra(SKIPTOUR, false))
                UIManager.launchLogin(this);
            else
                UIManager.launchWelcome(this);
            fadeTransition();
            onNextActivityStarted();
            return;
        }

        MesiboUI.MesiboUserListScreenOptions opts = new MesiboUI.MesiboUserListScreenOptions();
        opts.keepRunning = true;
        opts.startInBackground = mRunInBackground;

        // no permission prompts when starting in background
        if (mRunInBackground) {
            UIManager.launchMesibo(this, opts);
            onNextActivityStarted();
            return;
        }

        UIManager.requestPermissions(this, () -> {
            UIManager.launchMesibo(this, opts);
            onNextActivityStarted();
        });
    }

    @SuppressWarnings("deprecation") // pre-34 branch
    private void fadeTransition() {
        if (Build.VERSION.SDK_INT >= 34)
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out);
        else
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void onNextActivityStarted() {
        if (mRunInBackground || mUpdateFlowStarted)
            finish();
        else
            checkUpdates();
    }

    /* Testing tips:
       https://stackoverflow.com/questions/57534545/appupdatemanager-startupdateflowforresult-not-showing-dialog
     */
    private void checkUpdates() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        // Previously only a success listener was attached: on sideloaded/debug builds, devices
        // without Play, or network errors, the task failed and finish() was never called.
        appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener(info -> {
                    if (!startUpdateFlow(appUpdateManager, info))
                        finish();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Update check failed: " + e);
                    finish();
                });
    }

    private boolean startUpdateFlow(AppUpdateManager appUpdateManager, AppUpdateInfo info) {
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE)
            return false;

        int type;
        if (info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
            type = AppUpdateType.IMMEDIATE;
        else if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))
            type = AppUpdateType.FLEXIBLE;
        else
            return false;

        try {
            mUpdateFlowStarted = appUpdateManager.startUpdateFlowForResult(info, mUpdateLauncher,
                    AppUpdateOptions.newBuilder(type).build());
        } catch (Exception e) {
            Log.d(TAG, "Unable to start update flow: " + e);
            mUpdateFlowStarted = false;
        }
        return mUpdateFlowStarted;
    }
}
