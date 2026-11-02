package org.mesibo.messenger.AppSettings;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.mesibo.messaging.MesiboUI;
import com.mesibo.messaging.MesiboUiDefaults;

/**
 * Applies the mesibo UI colors to the app's own screens so that Settings/About look the same as
 * the mesibo messaging screens: MesiboUiDefaults when set, else the mesibo theme colors (which
 * follow the system light/dark mode). Also lays the screen out edge-to-edge, as Android 15
 * enforces: the app bar extends under the status bar and the content stays clear of the
 * navigation bar.
 */
final class MesiboToolbarStyle {

    private MesiboToolbarStyle() {}

    static void apply(AppCompatActivity activity, Toolbar toolbar, View appBar, View content) {
        MesiboUiDefaults opt = MesiboUI.getUiDefaults();
        int bar = (0 != opt.toolbarColor) ? opt.toolbarColor
                : ContextCompat.getColor(activity, com.mesibo.messaging.R.color.mesibo_bar);
        int text;
        if (0 != opt.toolbarTextColor)
            text = opt.toolbarTextColor;
        else if (0 != opt.toolbarColor)
            text = isLight(opt.toolbarColor) ? 0xDE000000 : 0xFFFFFFFF;
        else
            text = ContextCompat.getColor(activity, com.mesibo.messaging.R.color.mesibo_on_bar);
        int background = ContextCompat.getColor(activity, com.mesibo.messaging.R.color.mesibo_background);

        appBar.setBackgroundColor(bar);
        toolbar.setBackgroundColor(bar);
        toolbar.setTitleTextColor(text);
        toolbar.setSubtitleTextColor(text);

        Drawable nav = toolbar.getNavigationIcon();
        if (null != nav)
            nav.mutate().setTint(text);
        Drawable overflow = toolbar.getOverflowIcon();
        if (null != overflow)
            overflow.mutate().setTint(text);

        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        if (Build.VERSION.SDK_INT < 35)
            transparentSystemBars(window);

        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(window, window.getDecorView());
        c.setAppearanceLightStatusBars(isLight(bar));
        c.setAppearanceLightNavigationBars(isLight(background));

        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets i = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(i.left, i.top, i.right, 0);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets i = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(i.left, 0, i.right, i.bottom);
            return insets;
        });
    }

    // Android 15+ draws the bars transparent by itself (and deprecates these calls)
    @SuppressWarnings("deprecation")
    private static void transparentSystemBars(Window window) {
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    static boolean isLight(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) > 0.5;
    }
}
