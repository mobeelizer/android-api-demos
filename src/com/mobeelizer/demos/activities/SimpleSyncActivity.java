//
// SimpleSyncActivity.java
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

import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.mobeelizer.demos.R;
import com.mobeelizer.demos.adapters.SimpleSyncAdapter;
import com.mobeelizer.demos.custom.EntityState;
import com.mobeelizer.demos.model.SimpleSyncEntity;
import com.mobeelizer.demos.utils.DataUtil;
import com.mobeelizer.demos.utils.DataUtil.Movie;
import com.mobeelizer.demos.utils.UIUtils;
import com.mobeelizer.mobile.android.Mobeelizer;
import com.mobeelizer.mobile.android.api.MobeelizerLoginCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncStatus;

/**
 * Activity responsible for "Simple Sync" example. It allows the user to add sample data, sync them to Mobeelizer server where
 * they will be merged with the one from the other user and sync back to the application.
 * 
 * 
 * @see BaseActivity
 * @see MobeelizerLoginCallback
 */
public class SimpleSyncActivity extends BaseActivity<SimpleSyncEntity> implements MobeelizerSyncCallback {

    private Button mAddButton, mSyncButton;

    private ImageButton mInfoButton;

    private ListView mList;

    private SimpleSyncAdapter mAdapter;

    private Dialog mSyncDialog = null;

    @Override
    protected Integer getHelpDialog() {
        return D_SIMPLE_SYNC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_list_with_buttons);
        initTitleBarViews();
        setTitleBarTitle(R.string.titleBarSimpleSync);

        mAddButton = (Button) findViewById(R.id.footerAdd);
        mSyncButton = (Button) findViewById(R.id.footerSync);
        mInfoButton = (ImageButton) findViewById(R.id.footerInfo);
        mList = (ListView) findViewById(android.R.id.list);

        mAddButton.setOnClickListener(getOnAddClickListener());
        mSyncButton.setOnClickListener(getOnSyncClickListener());
        mInfoButton.setOnClickListener(getOnInfoClickListener());

        // clip the add button drawable
        UIUtils.prepareClip(mAddButton);
        // clip the sync button drawable
        UIUtils.prepareClip(mSyncButton);

        List<SimpleSyncEntity> data = Mobeelizer.getDatabase().list(SimpleSyncEntity.class);

        mAdapter = new SimpleSyncAdapter(this, data);
        mAdapter.sort(new SimpleSyncEntity());
        mList.setAdapter(mAdapter);
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
                // changes current example to Photo sync one
                Intent i = new Intent(getApplicationContext(), PhotoSyncActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                // handle logout operation
                return super.onMenuItemSelected(featureId, item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUserChanged() {
        mAdapter.clear();
        mAdapter.addAll(Mobeelizer.getDatabase().list(SimpleSyncEntity.class));
        mAdapter.sort(new SimpleSyncEntity());
        mAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSyncFinished(final MobeelizerSyncStatus status) {
        Bundle b = null;
        // If synchronization succeeded show examples list. Otherwise show an error dialog
        switch (status) {
            case FINISHED_WITH_SUCCESS:
                // get newly synchronized items from database
                final List<SimpleSyncEntity> newList = Mobeelizer.getDatabase().list(SimpleSyncEntity.class);
                // get old items from list adapter
                final List<SimpleSyncEntity> oldList = mAdapter.getItems();
                // merge new items to old list and mark them as new,
                // find removed items in old list and mark them as such
                mergeLists(oldList, newList);
                mAdapter.sort(new SimpleSyncEntity());
                // refresh the list to display animation
                Log.i("AAA", "=================== SYNC FINISHED");
                mAdapter.notifyDataSetChanged();
                mList.setSelection(0);

                break;
            case FINISHED_WITH_FAILURE:
                b = new Bundle();
                b.putBoolean(BaseActivity.IS_INFO, false);
                b.putInt(BaseActivity.TEXT_RES_ID, R.string.e_syncFailed);
                break;
            case NONE:
                b = new Bundle();
                b.putBoolean(BaseActivity.IS_INFO, true);
                b.putInt(BaseActivity.TEXT_RES_ID, R.string.e_syncDisabled);
                break;
        }

        if (mSyncDialog != null) {
            mSyncDialog.dismiss();
        }

        // show dialog with synchronization status
        if (b != null) {
            SimpleSyncActivity.this.showDialog(BaseActivity.D_CUSTOM, b);
        }
    }

    // =====================================================================================
    // ================================= ONCLICKS ==========================================
    // =====================================================================================

    /**
     * Returns {@link View.OnClickListener} for "Add" button. When the button is clicked application draws a random movie and adds
     * it to database and list view.
     */
    private View.OnClickListener getOnAddClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // choose a random movie and create a database entity object
                Movie m = DataUtil.getRandomMovie(getResources());
                SimpleSyncEntity sse = new SimpleSyncEntity();
                sse.setTitle(m.title);

                // save it to database
                Mobeelizer.getDatabase().save(sse);
                // change entity state to show newly added item overlay animation
                sse.setEntityState(EntityState.NEW_A);
                mAdapter.add(sse);
                mAdapter.sort(new SimpleSyncEntity());
                // scroll the list to the last position
                mList.smoothScrollToPosition(mAdapter.getPosition(sse));
            }
        };
    }

    /**
     * Returns {@link View.OnClickListener} for "Sync" button. When the button is clicked application tries to sync it's data with
     * Mobeelizer server. After synchronization completes the {@link #onSyncFinished(MobeelizerSyncStatus)} method is called with
     * a result object.
     */
    private View.OnClickListener getOnSyncClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // show synchronization progress dialog
                mSyncDialog = new Dialog(SimpleSyncActivity.this, R.style.MobeelizerDialogTheme);
                mSyncDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mSyncDialog.setContentView(R.layout.progress_dialog);
                mSyncDialog.setCancelable(false);
                mSyncDialog.show();

                // start synchronization
                Mobeelizer.sync(SimpleSyncActivity.this);
            }
        };
    }

    /**
     * Returns {@link View.OnClickListener} for "Information" button. When the button is clicked the info dialog appears with
     * Simple Sync help text.
     */
    private View.OnClickListener getOnInfoClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                showDialog(D_SIMPLE_SYNC);
            }
        };
    }
}
