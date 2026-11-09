package org.mesibo.messenger;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.ComponentActivity;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboPhoneContact;
import com.mesibo.api.MesiboProfile;
import com.mesibo.calls.api.MesiboCall;
import org.mesibo.messenger.AppSettings.SettingsActivity;
import com.mesibo.messaging.MesiboUI;
import com.mesibo.messaging.MesiboDialogs;
import com.mesibo.uihelper.MesiboLoginOptions;
import com.mesibo.uihelper.MesiboPermissions;
import com.mesibo.uihelper.MesiboUiHelper;
import com.mesibo.uihelper.MesiboWelcomeOptions;
import com.mesibo.uihelper.NotificationPermission;

public class UIManager {

    private static final int BRAND_COLOR = 0xff00868b;

    public static void launchStartupActivity(@NonNull Context context, boolean skipWelcome) {
        Intent intent = new Intent(context, StartUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(StartUpActivity.SKIPTOUR, skipWelcome);
        context.startActivity(intent);
    }

    public static void launchMesibo(Context context, MesiboUI.MesiboUserListScreenOptions opts) {
        MesiboUI.launchUserList(context, opts);
    }

    /**
     * Requests the permissions the mesibo UI needs, then notification permission, then (once)
     * full-screen incoming calls, then calls onReady. If the required permissions are denied, the
     * app closes (as before).
     */
    public static void requestPermissions(ComponentActivity activity, Runnable onReady) {
        MesiboPermissions.request(activity, MesiboPermissions.requiredPermissions(),
                "mesibo requires Storage and Contacts permissions so that you can send messages and make calls to your contacts. Please grant to continue!",
                granted -> {
                    if (!granted) {
                        MesiboDialogs.Options o = new MesiboDialogs.Options();
                        o.message = "mesibo will close now since the required permissions were not granted";
                        o.cancelable = false;
                        o.onPositive = activity::finishAffinity;
                        MesiboDialogs.show(activity, o);
                        return;
                    }
                    // notifications are optional; continue either way
                    NotificationPermission.request(activity,
                            "Allow notifications so that you are informed about new messages and calls.",
                            notify -> requestFullScreenCalls(activity, onReady));
                });
    }

    /**
     * Android 14+: incoming calls show full screen on a locked phone only if allowed (Google Play
     * grants it to apps declared as calling apps). Asked once; the app continues either way.
     */
    private static void requestFullScreenCalls(Activity activity, Runnable onReady) {
        android.content.SharedPreferences prefs = activity.getSharedPreferences("mesibo_prompts", Context.MODE_PRIVATE);
        if (activity.isFinishing() || MesiboCall.canShowFullScreenCall(activity)
                || prefs.getBoolean("fullscreen_calls_asked", false)) {
            onReady.run();
            return;
        }
        prefs.edit().putBoolean("fullscreen_calls_asked", true).apply();

        MesiboDialogs.Options o = new MesiboDialogs.Options();
        o.title = "Show incoming calls";
        o.message = "Allow mesibo to show incoming calls on the full screen, even when your phone is locked. Otherwise, calls only show as a notification.";
        o.positiveLabel = "Allow";
        o.negativeLabel = "Not now";
        o.cancelable = false;
        o.onPositive = () -> MesiboCall.requestFullScreenCallPermission(activity);
        o.onDismiss = onReady;
        MesiboDialogs.show(activity, o);
    }

    // -- theme (Settings): stored in preferences as it is applied at startup, before mesibo starts --

    private static final String[] THEME_NAMES = {"System default", "Light", "Dark"};

    /** MesiboUI.THEME_SYSTEM (default), THEME_LIGHT or THEME_DARK */
    public static int getTheme(Context context) {
        return context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).getInt("theme", MesiboUI.THEME_SYSTEM);
    }

    public static String getThemeName(Context context) {
        int t = getTheme(context);
        return (t >= 0 && t < THEME_NAMES.length) ? THEME_NAMES[t] : THEME_NAMES[0];
    }

    public static void applyTheme(Context context) {
        MesiboUI.setTheme(getTheme(context));
    }

    /** choose System default / Light / Dark; applied the next time the app starts */
    public static void chooseTheme(Activity activity, Runnable onChanged) {
        MesiboDialogs.Options o = new MesiboDialogs.Options();
        o.title = "Theme";
        o.items = THEME_NAMES;
        o.sheet = true;
        o.onSelect = which -> {
            int theme = (0 == which) ? MesiboUI.THEME_SYSTEM : (1 == which ? MesiboUI.THEME_LIGHT : MesiboUI.THEME_DARK);
            if (theme == getTheme(activity))
                return;
            activity.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putInt("theme", theme).apply();
            if (null != onChanged) onChanged.run();
            MesiboDialogs.alert(activity, "Theme", "The new theme will be applied when you restart the app.");
        };
        MesiboDialogs.show(activity, o);
    }

    public static void launchUserSettings(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void launchEditProfile(@NonNull Context context, boolean newUser) {
        MesiboUI.setEditProfileListener(new MesiboUI.MesiboEditProfileListener() {
            @Override
            public void MesiboEditProfile_onSaved(Activity activity, MesiboProfile profile, boolean newUser) {
                if (newUser)   // first sign-in: continue to the app via startup (permissions)
                    launchStartupActivity(activity, true);
            }
        });

        Mesibo.getSelfProfile().setSearchable(true);

        MesiboUI.MesiboEditProfileOptions opts = new MesiboUI.MesiboEditProfileOptions();
        opts.newUser = newUser;
        // not from a screen (e.g. the app context): the screen needs a new task
        if (!(context instanceof Activity))
            opts.activityFlags = Intent.FLAG_ACTIVITY_NEW_TASK;
        MesiboUI.launchEditProfile(context, opts);
    }

    public static void launchWelcome(Context context) {
        MesiboWelcomeOptions w = new MesiboWelcomeOptions();
        w.logoResId = R.drawable.mesibo_logo_white;
        w.title = "Messaging & Calls in your apps";
        w.description = "Add messaging, video and voice calls & conferencing in your apps in no time.";
        w.githubUrl = "https://github.com/mesibo";
        w.buttonText = "Get Started";
        // colors: uihelper defaults (black background, white text and button)

        MesiboUiHelper.launchWelcome(context, w, loginOptions(), MesiboListeners.getInstance());
    }

    public static void launchLogin(Context context) {
        MesiboUiHelper.launchLogin(context, loginOptions(), MesiboListeners.getInstance());
    }

    private static MesiboLoginOptions loginOptions() {
        MesiboLoginOptions o = new MesiboLoginOptions();

        MesiboPhoneContact country = Mesibo.getPhoneContactsManager().getCountryCode();
        o.defaultCountryCode = (null != country && country.countryPhoneCode > 0) ? country.countryPhoneCode : 1;

        // we do not send verification codes; testers generate them from the mesibo console
        o.note = "This demo app will NOT send verification code. You can generate yourself from the mesibo console https://console.mesibo.com";
        o.noteIconResId = R.drawable.ic_touch_app;
        o.noteColor = 0xffb45309;
        o.noteBorderColor = 0x55ffb900;
        o.noteBackgroundColor = 0x14ffb900;
        o.codeMessage = "Enter the verification code for %PHONE% generated from the mesibo console";

        o.backgroundColor = 0xffffffff;
        o.titleColor = 0xff172727;
        o.messageColor = 0xff666666;
        o.inputColor = 0xff172727;
        o.accentColor = BRAND_COLOR;
        o.buttonColor = BRAND_COLOR;
        o.buttonTextColor = 0xffffffff;
        return o;
    }
}
