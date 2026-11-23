package org.mesibo.messenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;


import java.util.ArrayList;
import java.util.List;

public class NotifyUser {
    public static final int ID_GROUP_JOINED=3;
    public static final int ID_GROUP_LEFT=4;
    public static final int TYPE_MESSAGE=5;
    public static final int TYPE_OTHER=10;
    public static int mCount = 0;

    public static class NotificationContent {
        public String title = null;
        public String content = null;
        public String subContent = null;
        public RemoteViews v = null;
        public NotificationCompat.Style style = null;
    }

    private List<NotificationContent> mNotificationContentList = new ArrayList<NotificationContent>();
    private final Context mContxt;

    // Channel settings are immutable once created; the id was changed so that the corrected
    // sound attributes apply to existing installs as well. The old channel is deleted.
    public static final String NOTIFYMESSAGE_CHANNEL_ID = "MesiboMessages";
    private static final String OLD_CHANNEL_ID = "MesiboAppNotificationChannel";
    private static final String NOTIFYMESSAGE_CHANNEL_NAME = "New Messages";

    public NotifyUser(Context context) {
        mContxt = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26)
            return;

        NotificationChannel channel = new NotificationChannel(NOTIFYMESSAGE_CHANNEL_ID,
                NOTIFYMESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notifications for new messages");
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setShowBadge(true);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);

        NotificationManager nm = (NotificationManager) mContxt.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.deleteNotificationChannel(OLD_CHANNEL_ID);
        nm.createNotificationChannel(channel);
    }

    public void sendNotification(int id, PendingIntent intent, NotificationContent n) {

        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContxt, NOTIFYMESSAGE_CHANNEL_ID);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE); //TBD, configurable

        if(null != n.title)
            builder.setContentTitle(n.title);
        if(null != n.content)
            builder.setContentText(n.content);

        // The subtext, which appears under the text on newer devices.
        // This will show-up in the devices with Android 4.2 and above only
        if(!TextUtils.isEmpty(n.subContent))
            builder.setSubText(n.subContent);

        builder.setWhen(System.currentTimeMillis());

        // message notifications must be dismissible (was ongoing/sticky)
        builder.setOngoing(false);

        //icon appears in device notification bar and right hand corner of notification
        //https://clevertap.com/blog/fixing-notification-icon-for-android-lollipop-and-above/
        builder.setSmallIcon(R.drawable.ic_message_white_24dp);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(intent);
        //builder.setFullScreenIntent(intent, true);

        // Large icon appears on the left of the notification
        //builder.setLargeIcon(BitmapFactory.decodeResource(mContxt.getResources(), R.drawable.notify_large));

        if(null != n.v) {
            builder.setCustomContentView(n.v);
        }

        if(null != n.style)
            builder.setStyle(n.style);


        // clears on click
        builder.setAutoCancel(true);
        // sound/vibration on Android 6-7; on 8+ the channel decides
        builder.setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) mContxt.getSystemService(Context.NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(id, notification);
    }

    public synchronized void  clearNotification() {

        NotificationManager notificationManager = (NotificationManager) MainApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationContentList.clear();
        // Will display the notification in the notification bar
        notificationManager.cancel(TYPE_MESSAGE);
        mCount = 0;
        mNotificationContentList.clear();
        //notificationManager.cancel(TYPE_OTHER);
    }

    private PendingIntent getDefaultIntent(int code) {
        // This intent is fired when notification is clicked
        Intent intent = new Intent(mContxt, StartUpActivity.class);
        int immutable = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
        return PendingIntent.getActivity(mContxt, code, intent, PendingIntent.FLAG_UPDATE_CURRENT | immutable);
    }


    public void sendNotification(int id, String title, String content) {
        NotificationContent n = new NotificationContent();
        n.title = title;
        n.content = content;

        sendNotification(id, getDefaultIntent(0), n);
    }

    private synchronized void notifyMessages() {
        if(0 == mNotificationContentList.size())
            return;

        NotificationContent notify= mNotificationContentList.get(mNotificationContentList.size()-1);

        String title = "New Message from " + notify.title;
        NotificationCompat.InboxStyle inboxStyle = null;
        if(mNotificationContentList.size() > 1) {

            inboxStyle = new NotificationCompat.InboxStyle();
            for (NotificationContent n : mNotificationContentList)
                inboxStyle.addLine(n.title + " : " + n.content);

            inboxStyle.setBigContentTitle(title);
            inboxStyle.setSummaryText(mCount + " new messages");
            notify.style = inboxStyle;
            sendNotification(TYPE_MESSAGE, getDefaultIntent(0), notify);
            return;
        }

        // Don't use notify object else we it will overwrite title
        sendNotification(TYPE_MESSAGE, title, notify.content);
    }

    public synchronized void sendNotificationInList(String title, String message) {

        // it is also possible to limit only latest notification from a user by adding params and checking for duplicate
        NotificationContent notify = new NotificationContent();
        notify.title = title;
        notify.content = message;

        // inboxStyle can only have max 5 messages
        if(mNotificationContentList.size() >= 5)
            mNotificationContentList.remove(0);

        mNotificationContentList.add(notify);
        mCount++;

        notifyMessages();
        return;
    }
}
