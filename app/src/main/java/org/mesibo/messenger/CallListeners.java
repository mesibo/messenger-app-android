package org.mesibo.messenger;

import static org.mesibo.messenger.MainApplication.getAppContext;

import com.mesibo.api.Mesibo;
import com.mesibo.api.MesiboDateTime;
import com.mesibo.api.MesiboProfile;
import com.mesibo.calls.api.MesiboCall;
import com.mesibo.messaging.MesiboUI;

/**
 * All call listeners of the app:
 *
 * IncomingListener: incoming calls (call properties), missed call notifications.
 * CallMonitorListener: calls starting and ending - updates the chat list and chats, which show
 * the call in progress button during a call (UIListener.updateCallButtons).
 */
public class CallListeners implements MesiboCall.IncomingListener, MesiboCall.CallMonitorListener {

    private static CallListeners sInstance = null;

    public static synchronized CallListeners getInstance() {
        if (null == sInstance)
            sInstance = new CallListeners();
        return sInstance;
    }

    /** registers all call listeners; once MesiboCall is initialized */
    public static void register() {
        MesiboCall.getInstance().setListener(getInstance());
        MesiboCall.getInstance().addCallMonitorListener(getInstance());
    }

    // -- IncomingListener --

    @Override
    public MesiboCall.CallProperties MesiboCall_OnIncoming(MesiboProfile userProfile, boolean video, boolean waiting) {
        MesiboCall.CallProperties cc = MesiboCall.getInstance().createCallProperties(video);
        cc.parent = getAppContext();
        cc.user = userProfile;
        return cc;
    }

    @Override
    public boolean MesiboCall_OnShowUserInterface(MesiboCall.Call call, MesiboCall.CallProperties callProperties) {
        return false;
    }

    @Override
    public void MesiboCall_OnError(MesiboCall.CallProperties callProperties, int error) {
    }

    @Override
    public boolean MesiboCall_onNotify(int type, MesiboProfile profile, boolean video, MesiboDateTime ts) {
        if (MesiboCall.MESIBOCALL_NOTIFY_MISSED == type) {
            String subject = "Mesibo Missed Call";
            String message = "You missed a mesibo " + (video ? "video " : "") + "call from " + profile.getNameOrAddress();
            SampleAPI.notify((int) Mesibo.getTimestamp(), subject, message);
        }
        return true;
    }

    // -- CallMonitorListener --

    @Override
    public void MesiboCall_onCallStarted(MesiboProfile profile, boolean incoming, boolean video) {
        MesiboUI.updateScreens();
    }

    @Override
    public void MesiboCall_onCallEnded(MesiboProfile profile, boolean incoming, boolean video) {
        MesiboUI.updateScreens();
    }
}
