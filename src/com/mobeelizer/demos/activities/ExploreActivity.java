//
// ExploreActivity.java
// 
// Copyright (C) 2012 Mobeelizer Ltd. All Rights Reserved.
// 
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License. You may obtain a copy 
// of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
// License for the specific language governing permissions and limitations under
// the License.
// 

package com.mobeelizer.demos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gcm.GCMRegistrar;
import com.mobeelizer.demos.R;
import com.mobeelizer.mobile.android.Mobeelizer;

/**
 * Activity responsible for displaying list of examples provided by the application and the getting started screen.
 * 
 * @see BaseActivity
 */
@SuppressWarnings("rawtypes")
public class ExploreActivity extends BaseActivity implements OnItemClickListener {

    private String[] mValues;

    private ListView mList;

    private ArrayAdapter<String> mAdapter;

    @Override
    protected Integer getHelpDialog() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_list);

        super.initTitleBarViews();
        mList = (ListView) findViewById(android.R.id.list);

        mValues = getResources().getStringArray(R.array.exploreListValues);
        mAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_list_item, R.id.listItemText, mValues);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);

        setTitleBarTitle(R.string.titleBarExplore);
        setSessionCodeVisibility(true);

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, "273557760831");
        } else {
            Mobeelizer.registerForRemoteNotifications(regId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.explore, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        Intent i = null;
        switch (position) {
            case 0:
                i = new Intent(getApplicationContext(), GettingStartedActivity.class);
                break;
            case 1:
                i = new Intent(getApplicationContext(), SimpleSyncActivity.class);
                break;
            case 2:
                i = new Intent(getApplicationContext(), PhotoSyncActivity.class);
                break;
            case 3:
                i = new Intent(getApplicationContext(), PermissionsActivity.class);
                break;
            case 4:
                i = new Intent(getApplicationContext(), ConflictsActivity.class);
                break;
            case 5:
                i = new Intent(getApplicationContext(), GraphsConflictActivity.class);
                break;
            case 6:
                i = new Intent(getApplicationContext(), PushNotificationsActivity.class);
                break;
            case 7:
                i = new Intent(getApplicationContext(), WhatNextActivity.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUserChanged() {
    }

}
