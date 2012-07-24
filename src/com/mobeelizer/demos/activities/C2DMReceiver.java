package com.mobeelizer.demos.activities;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.mobeelizer.demos.ApplicationStatus;
import com.mobeelizer.demos.R;
import com.mobeelizer.mobile.android.Mobeelizer;

public class C2DMReceiver extends BroadcastReceiver {

    private static int i = 0;

    private static String registrationId;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
            handleRegistration(context, intent);
        } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            handleMessage(context, intent);
        }
    }

    private void handleMessage(final Context context, final Intent intent) {
        String message = intent.getStringExtra("alert");
        String title = "Push received!";

        if (ApplicationStatus.isVisible()) {
            final Dialog dialog = new Dialog(ApplicationStatus.getCurrentActivity(), R.style.MobeelizerDialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_dialog);
            TextView titleView = (TextView) dialog.findViewById(R.id.dialogTitle);
            titleView.setText(title);
            TextView textView = (TextView) dialog.findViewById(R.id.dialogText);
            textView.setText(message);
            Button closeButton = (Button) dialog.findViewById(R.id.dialogButton);
            closeButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(final View paramView) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        } else {
            Notification notification = new Notification(R.drawable.ic_launcher, message, System.currentTimeMillis());
            notification.setLatestEventInfo(context, title, message, null);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(i++, notification);
        }
    }

    private void handleRegistration(final Context context, final Intent intent) {
        registrationId = intent.getStringExtra("registration_id");
        if (intent.getStringExtra("error") != null) {
            Log.e("C2DM", "REGISTRATION FAILED");
        } else if (intent.getStringExtra("unregistered") != null) {
            Mobeelizer.unregisterForRemoteNotifications();
        } else if (registrationId != null) {
            performPushRegistration();
        }
    }

    public static void performPushRegistration() {
        if (registrationId != null) {
            Mobeelizer.registerForRemoteNotifications(registrationId);
        }
    }
}
