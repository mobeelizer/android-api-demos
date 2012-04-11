//
// PermissionsActivity.java
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
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.mobeelizer.demos.R;
import com.mobeelizer.demos.adapters.PermissionsSyncAdapter;
import com.mobeelizer.demos.custom.EntityState;
import com.mobeelizer.demos.model.PermissionsEntity;
import com.mobeelizer.demos.utils.DataUtil;
import com.mobeelizer.demos.utils.DataUtil.Movie;
import com.mobeelizer.demos.utils.UIUtils;
import com.mobeelizer.mobile.android.Mobeelizer;
import com.mobeelizer.mobile.android.api.MobeelizerLoginCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncStatus;

/**
 * Activity responsible for "Permissions" example. Both users adds their parts of data - movie title and director. When those data
 * are synchronized both users can see title but director is visible only to the owner of the data.
 * 
 * @see BaseActivity
 * @see MobeelizerLoginCallback
 */
public class PermissionsActivity extends BaseActivity<PermissionsEntity> implements MobeelizerSyncCallback {

    private Button mAddButton, mSyncButton;

    private ImageButton mInfoButton;

    private ListView mList;

    private PermissionsSyncAdapter mAdapter;

    private Dialog mSyncDialog = null;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_list_with_buttons);
        initTitleBarViews();
        setTitleBarTitle(R.string.titleBarPermissions);

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

        boolean initialized = mSharedPrefs.getBoolean(this.getClass().getSimpleName(), false);

        List<PermissionsEntity> data = Mobeelizer.getDatabase().list(PermissionsEntity.class);
        if (data.size() == 0 && !initialized) {
            showDialog(D_PERMISSIONS);
            mSharedPrefs.edit().putBoolean(this.getClass().getSimpleName(), true).commit();
        }

        mAdapter = new PermissionsSyncAdapter(this, data);
        mAdapter.sort(new PermissionsEntity());
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
                // change the current example to Conflicts one
                Intent i = new Intent(getApplicationContext(), ConflictsActivity.class);
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
        mAdapter.addAll(Mobeelizer.getDatabase().list(PermissionsEntity.class));
        mAdapter.sort(new PermissionsEntity());
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
                final List<PermissionsEntity> newList = Mobeelizer.getDatabase().list(PermissionsEntity.class);
                // get old items from list adapter
                final List<PermissionsEntity> oldList = mAdapter.getItems();

                // merge new items to old list and mark them as new,
                // find removed items in old list and mark them as such
                boolean showAnim = mergeLists(oldList, newList);
                mAdapter.sort(new PermissionsEntity());
                // refresh the list to display animation
                mAdapter.notifyDataSetChanged();

                // wait for animation to complete
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // and switch old list to new one
                        mAdapter.clear();
                        mAdapter.addAll(newList);
                        mAdapter.sort(new PermissionsEntity());
                        mAdapter.notifyDataSetChanged();
                        // then scroll the list to first position
                        mList.setSelection(0);
                    }
                }, showAnim ? 2000 : 0);

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

        if (b != null) {
            PermissionsActivity.this.showDialog(BaseActivity.D_CUSTOM, b);
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
                PermissionsEntity pe = new PermissionsEntity();
                pe.setTitle(m.title);
                pe.setDirector(m.director);

                // save it to database
                Mobeelizer.getDatabase().save(pe);
                // change entity state to show newly added item overlay animation
                pe.setEntityState(EntityState.NEW_A);
                mAdapter.add(pe);
                mAdapter.sort(new PermissionsEntity());
                // scroll the list to the last position
                mList.smoothScrollToPosition(mAdapter.getPosition(pe));
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
                mSyncDialog = new Dialog(PermissionsActivity.this, R.style.MobeelizerDialogTheme);
                mSyncDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mSyncDialog.setContentView(R.layout.progress_dialog);
                mSyncDialog.setCancelable(false);
                mSyncDialog.show();

                // start synchronization
                Mobeelizer.sync(PermissionsActivity.this);
            }
        };
    }

    /**
     * Returns {@link View.OnClickListener} for "Information" button. When the button is clicked the info dialog appears with
     * Permissions help text.
     */
    private View.OnClickListener getOnInfoClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                showDialog(D_PERMISSIONS);
            }
        };
    }
}
