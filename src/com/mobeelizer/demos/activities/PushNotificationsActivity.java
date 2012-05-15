package com.mobeelizer.demos.activities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.mobeelizer.demos.R;
import com.mobeelizer.mobile.android.Mobeelizer;
import com.mobeelizer.mobile.android.api.MobeelizerSyncCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncStatus;

@SuppressWarnings("rawtypes")
public class PushNotificationsActivity extends BaseActivity implements MobeelizerSyncCallback {

    @Override
    protected Integer getHelpDialog() {
        return D_PUSH_NOTIFICATIONS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_push_notifications);
        super.initTitleBarViews();
        setTitleBarTitle(R.string.titleBarPushNotifications);

        ImageButton mInfoButton = (ImageButton) findViewById(R.id.footerInfo);

        mInfoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                showDialog(D_PUSH_NOTIFICATIONS);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.examples, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_next:
                Intent i = new Intent(getApplicationContext(), WhatNextActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    /**
     * Method called when 'send push notification to all' button pressed
     * 
     * @param v
     */
    public void performSendToAll(final View v) {
        Map<String, String> message = new HashMap<String, String>();
        message.put("alert", "Android device greets all users!");
        Mobeelizer.sendRemoteNotification(message);
    }

    /**
     * Method called when 'send push notification to user A' button pressed
     * 
     * @param v
     */
    public void performSendToA(final View v) {
        Map<String, String> message = new HashMap<String, String>();
        message.put("alert", "Android device greets user A!");
        Mobeelizer.sendRemoteNotificationToUsers(message, Arrays.asList(new String[] { "A" }));
    }

    /**
     * Method called when 'send push notification to user B' button pressed
     * 
     * @param v
     */
    public void performSendToB(final View v) {
        Map<String, String> message = new HashMap<String, String>();
        message.put("alert", "Android device greets user B!");
        Mobeelizer.sendRemoteNotificationToUsers(message, Arrays.asList(new String[] { "B" }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSyncFinished(final MobeelizerSyncStatus arg0) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUserChanged() {
        // do nothing
    }

}
