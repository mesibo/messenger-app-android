package org.mesibo.messenger;

import androidx.annotation.NonNull;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.mesibo.api.Mesibo;
import com.mesibo.calls.api.MesiboCall;
import com.mesibo.messaging.MesiboUI;

public class MainApplication extends Application implements Mesibo.RestartListener {
    public static final String TAG = "MesiboDemoApplication";
    private static Context mContext = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // light/dark: as chosen in Settings (default: follow the system), see UIManager.applyTheme
        UIManager.applyTheme(this);

        mContext = getApplicationContext();
        Mesibo.setRestartListener(this);
        new AppConfig(this);
        SampleAPI.init(mContext);

        MesiboCall.getInstance().init(mContext);

        // the messaging UI's settings: UIListener.MesiboUI_onConfig, each time a theme is applied
        MesiboCall.UiProperties up = MesiboCall.getInstance().getDefaultUiProperties();
        up.showScreenSharing = true;
    }

    public static String getRestartIntent() {
        return "org.mesibo.messenger.restart";
    }

    /** set first thing in onCreate */
    @NonNull
    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void Mesibo_onRestart() {
        Log.d(TAG, "Mesibo_onRestart");
        // keep the app's current visibility; do not push a visible app to the back
        StartUpActivity.newInstance(this, !Mesibo.isAppInForeground());
    }
}
