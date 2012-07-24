package com.mobeelizer.demos;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.mobeelizer.demos.activities.BaseActivity;
import com.mobeelizer.mobile.android.Mobeelizer;

public class GCMIntentService extends GCMBaseIntentService {

    private static int i = 0;

    @Override
    protected void onMessage(final Context context, final Intent intent) {

        String message = intent.getStringExtra("alert");
        String title = "Push received!";

        if (ApplicationStatus.isVisible()) {

            Intent i = new Intent(BaseActivity.DISPLAY_PUSH_MESSAGE_ACTION);
            i.putExtra(BaseActivity.DISPLAY_PUSH_MESSAGE_ACTION_EXTRA_MESSAGE, message);
            context.sendBroadcast(i);

        } else {
            Notification notification = new Notification(R.drawable.ic_launcher, message, System.currentTimeMillis());
            notification.setLatestEventInfo(context, title, message, null);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(i++, notification);
        }
    }

    @Override
    protected void onRegistered(final Context context, final String regId) {
        Mobeelizer.registerForRemoteNotifications(regId);
    }

    @Override
    protected void onUnregistered(final Context context, final String regId) {
        Mobeelizer.unregisterForRemoteNotifications();
    }

    @Override
    protected void onError(final Context context, final String errorId) {
        Log.e("GCMIntentService", errorId);
    }

}
