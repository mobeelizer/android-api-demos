//
// ConflictsActivity.java
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.mobeelizer.demos.R;
import com.mobeelizer.demos.adapters.ConflictsSyncAdapter;
import com.mobeelizer.demos.custom.EntityState;
import com.mobeelizer.demos.model.ConflictsEntity;
import com.mobeelizer.demos.utils.DataUtil;
import com.mobeelizer.demos.utils.DataUtil.Movie;
import com.mobeelizer.demos.utils.UIUtils;
import com.mobeelizer.mobile.android.Mobeelizer;
import com.mobeelizer.mobile.android.api.MobeelizerLoginCallback;
import com.mobeelizer.mobile.android.api.MobeelizerRestrictions;
import com.mobeelizer.mobile.android.api.MobeelizerSyncCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncStatus;

/**
 * Activity responsible for "Conflicts" example. It allows the user to create a conflicted data when synchronizing with Mobeelizer
 * server. All data can be edited by each user. One of them might change the rating of the movie before synchronizing data. When
 * the other user also modified the same value the conflict appears.
 * 
 * @see BaseActivity
 * @see MobeelizerLoginCallback
 */
public class ConflictsActivity extends BaseActivity<ConflictsEntity> implements MobeelizerSyncCallback, OnItemClickListener {

    private static final int CHANGE_RATING = 0x400;

    private Button mAddButton, mSyncButton;

    private ImageButton mInfoButton;

    private TextView mWarningText;

    private ListView mList;

    private ConflictsSyncAdapter mAdapter;

    private Dialog mSyncDialog = null;

    @Override
    protected Integer getHelpDialog() {
        return D_CONFLICTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_list_with_buttons);
        initTitleBarViews();
        setTitleBarTitle(R.string.titleBarConflicts);

        mAddButton = (Button) findViewById(R.id.footerAdd);
        mSyncButton = (Button) findViewById(R.id.footerSync);
        mInfoButton = (ImageButton) findViewById(R.id.footerInfo);
        mWarningText = (TextView) findViewById(R.id.footerWarning);
        mList = (ListView) findViewById(android.R.id.list);

        mAddButton.setOnClickListener(getOnAddClickListener());
        mSyncButton.setOnClickListener(getOnSyncClickListener());
        mInfoButton.setOnClickListener(getOnInfoClickListener());

        // clip the add button drawable
        UIUtils.prepareClip(mAddButton);
        // clip the sync button drawable
        UIUtils.prepareClip(mSyncButton);

        List<ConflictsEntity> data = Mobeelizer.getDatabase().list(ConflictsEntity.class);

        long conflictsCount = Mobeelizer.getDatabase().find(ConflictsEntity.class).add(MobeelizerRestrictions.isConflicted())
                .count();
        showWarning(conflictsCount > 0);

        mAdapter = new ConflictsSyncAdapter(this, data);
        mAdapter.sort(new ConflictsEntity());
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
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
                // change current example to Graph's Conflicts one
                Intent i = new Intent(getApplicationContext(), GraphsConflictActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                // handle logout operation
                return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == CHANGE_RATING && resultCode == RESULT_OK) {
            int position = data.getIntExtra(ConflictsDetailsActivity.POSITION, -1);
            int rating = data.getIntExtra(ConflictsDetailsActivity.RATING, -1);

            if (position != -1 && rating != -1) {
                // edit entity object with the new rating and save it to database
                ConflictsEntity ce = mAdapter.getItem(position);
                ce.setScore(rating);
                Mobeelizer.getDatabase().save(ce);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Shows or hides conflicts warning text below the list view and above add/sync buttons. It should be visible only when at
     * least one field is in conflict.
     * 
     * @param isVisible
     *            Whether warning should be visible.
     */
    private void showWarning(final boolean isVisible) {
        mWarningText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

        String text = mWarningText.getText().toString();
        if (text.contains("|")) {
            // change '|' character to two red arrows
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            ssb.setSpan(boldSpan, 0, text.indexOf(':'), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            Bitmap syncIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_sync);
            int syncIndex = text.indexOf('|');
            ssb.setSpan(new ImageSpan(syncIcon), syncIndex, syncIndex + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            mWarningText.setText(ssb, BufferType.SPANNABLE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUserChanged() {
        mAdapter.clear();
        mAdapter.addAll(Mobeelizer.getDatabase().list(ConflictsEntity.class));
        mAdapter.sort(new ConflictsEntity());
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
                final List<ConflictsEntity> newList = Mobeelizer.getDatabase().list(ConflictsEntity.class);
                // get old items from list adapter
                final List<ConflictsEntity> oldList = mAdapter.getItems();

                // merge new items to old list and mark them as new,
                // find removed items in old list and mark them as such
                boolean showAnim = mergeLists(oldList, newList);
                mAdapter.sort(new ConflictsEntity());
                // search for conflicts
                long conflictsCount = Mobeelizer.getDatabase().find(ConflictsEntity.class)
                        .add(MobeelizerRestrictions.isConflicted()).count();
                showWarning(conflictsCount > 0);
                // refresh the list to display animation
                mAdapter.notifyDataSetChanged();
                mList.setSelection(0);

                final UserType loggedUserType = mUserType;

                // wait for animation to complete
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // skip, if user has changed
                        if (loggedUserType != mUserType) {
                            return;
                        }

                        // and switch old list to new one
                        mAdapter.clear();
                        mAdapter.addAll(newList);
                        mAdapter.sort(new ConflictsEntity());
                        mAdapter.notifyDataSetChanged();
                        // then scroll the list to first position
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
            ConflictsActivity.this.showDialog(BaseActivity.D_CUSTOM, b);
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
                ConflictsEntity ce = new ConflictsEntity();
                ce.setTitle(m.title);
                ce.setScore(m.rating);

                // save it to database
                Mobeelizer.getDatabase().save(ce);
                // change entity state to show newly added item overlay animation
                ce.setEntityState(EntityState.NEW_A);
                mAdapter.add(ce);
                mAdapter.sort(new ConflictsEntity());
                // scroll the list to the last position
                mList.smoothScrollToPosition(mAdapter.getPosition(ce));
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
                mSyncDialog = new Dialog(ConflictsActivity.this, R.style.MobeelizerDialogTheme);
                mSyncDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mSyncDialog.setContentView(R.layout.progress_dialog);
                mSyncDialog.setCancelable(false);
                mSyncDialog.show();

                // start synchronization
                Mobeelizer.sync(ConflictsActivity.this);
            }
        };
    }

    /**
     * Returns {@link View.OnClickListener} for "Information" button. When the button is clicked the info dialog appears with
     * Conflicts help text.
     */
    private View.OnClickListener getOnInfoClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                showDialog(D_CONFLICTS);
            }
        };
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        ConflictsEntity ce = mAdapter.getItem(position);

        Intent i = new Intent(ConflictsActivity.this, ConflictsDetailsActivity.class);
        i.putExtra(ConflictsDetailsActivity.POSITION, position);
        i.putExtra(ConflictsDetailsActivity.TITLE, ce.getTitle());
        i.putExtra(ConflictsDetailsActivity.RATING, ce.getScore());
        startActivityForResult(i, CHANGE_RATING);
    }
}
