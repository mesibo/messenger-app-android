package org.mesibo.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.mesibo.api.Mesibo;
import com.mesibo.mediapicker.FilePicker;
import com.mesibo.messaging.MesiboDialogs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Message backups in the app's media folder, Android/media/<package>/Backups, like WhatsApp. It
 * needs no permission and users can see and copy the files with a file manager or a PC. Android
 * removes the folder on uninstall unless the user keeps the app data (hasFragileUserData in the
 * manifest), so restore also accepts a file picked from anywhere (FilePicker in mediapicker).
 */
public class BackupRestore {

    private static final String PREFIX = "mesibo-backup-";
    private static final String SUFFIX = ".db";

    /** Back up now / Restore */
    public static void show(Activity activity) {
        MesiboDialogs.Options o = new MesiboDialogs.Options();
        o.title = "Backup & Restore";
        o.items = new String[]{"Back up now", "Restore"};
        o.sheet = true;
        o.onSelect = i -> {
            if (0 == i) backup(activity);
            else chooseRestore(activity);
        };
        MesiboDialogs.show(activity, o);
    }

    public static void backup(Activity activity) {
        File dir = backupDir(activity);
        if (null == dir) {
            toast(activity, "Storage not available");
            return;
        }
        String name = PREFIX + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + SUFFIX;
        String path = new File(dir, name).getAbsolutePath();
        Context app = activity.getApplicationContext();

        toast(app, "Backing up messages...");
        new Thread(() -> {
            String result = (null != Mesibo.backupDatabase(path))
                    ? "Messages backed up to Android/media/" + app.getPackageName() + "/Backups/" + name
                    : "Backup failed";
            new Handler(Looper.getMainLooper()).post(() -> toast(app, result));
        }).start();
    }

    /** backups in the backup folder (newest first), and Choose file... */
    private static void chooseRestore(Activity activity) {
        final List<File> backups = listBackups(activity);
        String[] items = new String[backups.size() + 1];
        for (int i = 0; i < backups.size(); i++)
            items[i] = describe(backups.get(i));
        items[backups.size()] = "Choose file...";

        MesiboDialogs.Options o = new MesiboDialogs.Options();
        o.title = backups.isEmpty() ? "No backups found on this phone" : "Restore from";
        o.items = items;
        o.sheet = true;
        o.onSelect = i -> {
            if (i < backups.size())
                confirmRestore(activity, backups.get(i), false);
            else
                pickFile(activity);
        };
        MesiboDialogs.show(activity, o);
    }

    private static void pickFile(Activity activity) {
        if (!(activity instanceof ComponentActivity))
            return;
        FilePicker.Options o = new FilePicker.Options();
        o.extensions = new String[]{"db"};
        o.copyToCache = true; // restore needs a file path
        FilePicker.pick((ComponentActivity) activity, o, new FilePicker.Listener() {
            @Override
            public void onFilePicked(Uri uri, String name, File file) {
                if (null == uri)
                    return; // cancelled
                if (null == file)
                    toast(activity, "Could not read the file");
                else
                    confirmRestore(activity, file, true);
            }

            @Override
            public void onFileRejected(Uri uri, String name) {
                MesiboDialogs.alert(activity, "Not a backup", name + " is not a mesibo backup (.db) file");
            }
        });
    }

    private static void confirmRestore(Activity activity, File file, boolean deleteAfter) {
        MesiboDialogs.Options o = new MesiboDialogs.Options();
        o.title = "Restore messages?";
        o.message = "Your current messages will be replaced by the backup. mesibo will restart after restoring.";
        o.positiveLabel = "Restore";
        o.negativeLabel = "Cancel";
        o.destructive = true;
        o.onPositive = () -> restore(activity, file, deleteAfter);
        if (deleteAfter)
            o.onNegative = file::delete;
        MesiboDialogs.show(activity, o);
    }

    private static void restore(Activity activity, File file, boolean deleteAfter) {
        int rv = Mesibo.restoreDatabase(file.getAbsolutePath());
        if (deleteAfter)
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        if (0 != rv) {
            MesiboDialogs.alert(activity, "Restore failed", "The file is not a valid mesibo backup (" + rv + ")");
            return;
        }

        MesiboDialogs.Options o = new MesiboDialogs.Options();
        o.title = "Messages restored";
        o.message = "mesibo will now restart.";
        o.cancelable = false;
        o.onPositive = () -> restart(activity);
        MesiboDialogs.show(activity, o);
    }

    /** messages and profiles are loaded in memory, so start a fresh process */
    private static void restart(Activity activity) {
        Intent launch = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        if (null != launch && null != launch.getComponent())
            activity.startActivity(Intent.makeRestartActivityTask(launch.getComponent()));
        activity.finishAffinity();
        Runtime.getRuntime().exit(0);
    }

    private static File backupDir(Context context) {
        File base = null;
        File[] media = context.getExternalMediaDirs();
        if (null != media && media.length > 0)
            base = media[0];
        if (null == base)
            base = context.getExternalFilesDir(null);
        if (null == base)
            return null;
        File dir = new File(base, "Backups");
        if (!dir.isDirectory() && !dir.mkdirs())
            return null;
        return dir;
    }

    private static List<File> listBackups(Context context) {
        List<File> list = new ArrayList<>();
        File dir = backupDir(context);
        File[] files = (null != dir) ? dir.listFiles((d, n) -> n.startsWith(PREFIX) && n.endsWith(SUFFIX)) : null;
        if (null != files) {
            list.addAll(Arrays.asList(files));
            list.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        }
        return list;
    }

    private static String describe(File f) {
        String when = new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(f.lastModified()));
        long kb = Math.max(1, f.length() / 1024);
        return kb >= 1024 ? when + "  ·  " + (kb / 1024) + " MB" : when + "  ·  " + kb + " KB";
    }

    private static void toast(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
    }
}
