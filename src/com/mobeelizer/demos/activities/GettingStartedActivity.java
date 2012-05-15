//
// GettingStartedActivity.java
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
import android.view.MenuItem;

import com.mobeelizer.demos.R;

/**
 * Activity with information about Mobeelizer platform, its capabilities.
 * 
 * @see BaseActivity
 */
@SuppressWarnings("rawtypes")
public class GettingStartedActivity extends BaseActivity {

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
        setContentView(R.layout.a_getting_started);
        super.initTitleBarViews();
        setTitleBarTitle(R.string.titleBarGettingStarted);
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
                Intent i = new Intent(getApplicationContext(), SimpleSyncActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUserChanged() {
    }
}
