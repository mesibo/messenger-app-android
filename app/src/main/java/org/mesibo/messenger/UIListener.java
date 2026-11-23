package org.mesibo.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboProfile;
import com.mesibo.calls.api.MesiboCall;
import com.mesibo.messaging.MesiboRecycleViewHolder;
import com.mesibo.messaging.MesiboUI;
import com.mesibo.messaging.MesiboUiAction;
import com.mesibo.messaging.MesiboUIListener;
import com.mesibo.messaging.MesiboUiConfig;

public class UIListener implements MesiboUIListener {

    /** each new configuration of the messaging UI (a theme applied): the app's own settings */
    @Override
    public void MesiboUI_onConfig(MesiboUiConfig config, boolean dark) {
        config.userList.emptyMessage = "No messages! Click on the message icon above to start messaging!";
        config.profileViewer.showAddress = true;
    }

    @Override
    public boolean MesiboUI_onInitScreen(MesiboUI.MesiboScreen screen) {
        if(MesiboUI.SCREEN_USERLIST == screen.type) {
            initUserListScreen((MesiboUI.MesiboUserListScreen) screen);
        } else if (MesiboUI.SCREEN_MESSAGES == screen.type) {
            initMessageListScreen((MesiboUI.MesiboMessageScreen) screen);
        } else if (MesiboUI.SCREEN_PROFILE == screen.type) {
            // the buttons under the name
            addCallItems(((MesiboUI.MesiboProfileScreen) screen).actions, screen);
        } else if (MesiboUI.SCREEN_PROFILE_POPUP == screen.type) {
            // the popup's buttons, between Message and Info
            addCallItems(screen.menu, screen);
        }
        return true;
    }

    // ids of the call items of the profile screen and popup
    private static final int MENU_PROFILE_VOICE = 9101;
    private static final int MENU_PROFILE_VIDEO = 9102;

    /**
     * Voice and video call buttons, for the profile screen and the profile popup (none for a group
     * the user left). During a call, they bring it back.
     */
    private void addCallItems(android.view.Menu menu, MesiboUI.MesiboScreen screen) {
        MesiboProfile profile = screen.profile;
        if (null == menu || null == profile || (profile.isGroup() && !profile.isActive()))
            return;

        for (int i = 0; i < 2; i++) {
            final boolean video = 1 == i;
            menu.add(android.view.Menu.NONE, video ? MENU_PROFILE_VIDEO : MENU_PROFILE_VOICE, 50 + i, video ? "Video" : "Voice")
                    .setIcon(video ? R.drawable.ic_videocam_white_24dp : R.drawable.ic_call_white_24dp)
                    .setOnMenuItemClickListener(item -> {
                        // during a call: back to it
                        if (MesiboCall.getInstance().isAnyCallInProgress())
                            MesiboCall.getInstance().showCallInProgress(screen.parent);
                        else
                            MesiboCall.getInstance().callUi(screen.parent, profile, video);
                        return true;
                    });
        }
    }

    // a call started or ended (MesiboCall.CallMonitorListener -> MesiboUI.updateScreens)
    @Override
    public void MesiboUI_onUpdateScreen(MesiboUI.MesiboScreen screen) {
        updateCallButtons(screen);
    }

    /** during a call: the call in progress button instead of the voice / video call buttons */
    void updateCallButtons(MesiboUI.MesiboScreen screen) {
        if (null == screen.menu)
            return;
        boolean inCall = MesiboCall.getInstance().isAnyCallInProgress();
        MenuItem progress = screen.menu.findItem(R.id.action_call_in_progress);
        if (null != progress)
            progress.setVisible(inCall);

        if (MesiboUI.SCREEN_MESSAGES == screen.type) {
            MesiboProfile profile = ((MesiboUI.MesiboMessageScreen) screen).profile;
            boolean canCall = !inCall && !(null != profile && profile.isGroup() && !profile.isActive());
            MenuItem call = screen.menu.findItem(R.id.action_call);
            MenuItem video = screen.menu.findItem(R.id.action_videocall);
            if (null != call) call.setVisible(canCall);
            if (null != video) video.setVisible(canCall);
        }
    }

    private void setCallInProgressHandler(MesiboUI.MesiboScreen screen) {
        MenuItem progress = screen.menu.findItem(R.id.action_call_in_progress);
        if (null != progress) {
            progress.setOnMenuItemClickListener(item -> {
                MesiboCall.getInstance().showCallInProgress(screen.parent);
                return true;
            });
        }
        updateCallButtons(screen);
    }

    @Override
    public MesiboRecycleViewHolder MesiboUI_onGetCustomRow(MesiboUI.MesiboScreen screen, MesiboUI.MesiboRow row) {
        return null;
    }

    @Override
    public void MesiboUI_onUpdateRow(MesiboUI.MesiboScreen screen, MesiboUI.MesiboRow row, boolean last) {
    }


    /**
     * The user did something (action.type, MesiboUI.ACTION_*): a row tapped, a profile to show,
     * an attachment, the camera, a voice message, a long press action, a message to send, ...
     * Return true to let the UI do it as usual, false when the app handled it. For example, to
     * check a message before it is sent (and change it, or return false to stop it):
     *     if (MesiboUI.ACTION_SEND == action.type) { ... action.message ... }
     */
    @Override
    public boolean MesiboUI_onAction(MesiboUI.MesiboScreen screen, MesiboUiAction action) {
        return true;
    }

    void userListScreenMenuHandler(Context context, int item) {
        if (item == R.id.action_settings) {
            UIManager.launchUserSettings(context);
        } else if (item == R.id.action_customize) {
            UIManager.launchCustomize(context);
        } else if(item == R.id.action_conf) {
            MesiboCall.getInstance().groupCallJoinRoomUi(context, "Mesibo Conferencing Demo");
        } else if(item == R.id.action_calllogs) {
            MesiboCall.getInstance().launchCallLogs(context);
        } else if(item == R.id.action_backup) {
            if (context instanceof Activity) BackupRestore.show((Activity) context);
        } else if(item == R.id.action_menu_e2ee) {
            MesiboUI.showEndToEndEncryptionConfiguration(context);
        } else if(item == R.id.mesibo_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            SampleAPI.Invite invite = AppConfig.getConfig().invite; // from the server; may be missing
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, null != invite ? invite.subject : null);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, null != invite ? invite.text : null);
            context.startActivity(Intent.createChooser(sharingIntent, null != invite ? invite.title : null));
        }
    }

    void initUserListScreen(MesiboUI.MesiboUserListScreen screen) {
        ((Activity)screen.parent).getMenuInflater().inflate(R.menu.messaging_activity_menu, screen.menu);

        MenuItem.OnMenuItemClickListener menuhandler = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                userListScreenMenuHandler(screen.parent, item.getItemId());
                return true;
            }
        };

        screen.menu.findItem(R.id.action_settings).setOnMenuItemClickListener(menuhandler);
        screen.menu.findItem(R.id.action_customize).setOnMenuItemClickListener(menuhandler);
        screen.menu.findItem(R.id.action_conf).setOnMenuItemClickListener(menuhandler);
        screen.menu.findItem(R.id.action_calllogs).setOnMenuItemClickListener(menuhandler);
        screen.menu.findItem(R.id.action_backup).setOnMenuItemClickListener(menuhandler);
        screen.menu.findItem(R.id.action_menu_e2ee).setOnMenuItemClickListener(menuhandler);
        screen.menu.findItem(R.id.mesibo_share).setOnMenuItemClickListener(menuhandler);
        setCallInProgressHandler(screen);

    }

    void messageListScreenMenuHandler(Context context, int item, MesiboProfile profile) {
        if(null == profile) {
            return;
        }

        // during a call, callUi brings back the call in progress instead
        if(R.id.action_call == item) {
            MesiboCall.getInstance().callUi(context, profile, false);
        }
        else if(R.id.action_videocall == item) {
            MesiboCall.getInstance().callUi(context, profile, true);
        }
        else if(R.id.action_e2e == item) {
            MesiboUI.showEndToEndEncryptionInfo(context, profile);
        }
        else if(R.id.action_disappearing == item) {
            MesiboUI.launchDisappearingMessages(context, profile);
        }
    }

    void initMessageListScreen(MesiboUI.MesiboMessageScreen screen) {
        ((Activity)screen.parent).getMenuInflater().inflate(R.menu.menu_messaging, screen.menu);

        // set group call icons
        if(null != screen.profile) {
            MenuItem menuItem = screen.menu.findItem(R.id.action_call);
            if(screen.profile.isGroup() && !screen.profile.isActive()) menuItem.setVisible(false);
            menuItem.setIcon(screen.profile.isGroup()?MesiboCall.MESIBO_DEFAULTICON_GROUPAUDIOCALL:MesiboCall.MESIBO_DEFAULTICON_AUDIOCALL);
            // MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);

            menuItem = screen.menu.findItem(R.id.action_videocall);
            menuItem.setIcon(screen.profile.isGroup()?MesiboCall.MESIBO_DEFAULTICON_GROUPVIDEOCALL:MesiboCall.MESIBO_DEFAULTICON_VIDEOCALL);
            if(screen.profile.isGroup() && !screen.profile.isActive()) menuItem.setVisible(false);
            //MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
        }

        MenuItem.OnMenuItemClickListener menuhandler = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                messageListScreenMenuHandler(screen.parent, item.getItemId(), screen.profile);
                return true;
            }
        };

        screen.menu.findItem(R.id.action_call).setOnMenuItemClickListener(menuhandler);
        screen.menu.findItem(R.id.action_videocall).setOnMenuItemClickListener(menuhandler);
        screen.menu.findItem(R.id.action_e2e).setOnMenuItemClickListener(menuhandler);

        // end-to-end encryption of this chat only while it is on (when off, the screen would
        // only say the chat isn't end-to-end encrypted), and never for groups
        screen.menu.findItem(R.id.action_e2e).setVisible(Mesibo.e2ee().isEnabled()
                && (null == screen.profile || !screen.profile.isGroup()));
        screen.menu.findItem(R.id.action_disappearing).setOnMenuItemClickListener(menuhandler);
        setCallInProgressHandler(screen);

        screen.titleArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MesiboUI.showProfile(screen.parent, screen.profile);
            }
        });
    }
}
