//
// GraphsConflictActivity.java
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

import java.util.ArrayList;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.mobeelizer.demos.R;
import com.mobeelizer.demos.adapters.GraphsConflictSyncAdapter;
import com.mobeelizer.demos.custom.EntityState;
import com.mobeelizer.demos.model.GraphsConflictsItemEntity;
import com.mobeelizer.demos.model.GraphsConflictsOrderEntity;
import com.mobeelizer.demos.utils.DataUtil;
import com.mobeelizer.demos.utils.DataUtil.Movie;
import com.mobeelizer.demos.utils.UIUtils;
import com.mobeelizer.mobile.android.Mobeelizer;
import com.mobeelizer.mobile.android.api.MobeelizerLoginCallback;
import com.mobeelizer.mobile.android.api.MobeelizerRestrictions;
import com.mobeelizer.mobile.android.api.MobeelizerSyncCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncStatus;

/**
 * Activity responsible for "Graph's Conflict" example. This example demonstrates tree data structure and conflicts which can be
 * created by users modifying data at the same time.
 * 
 * @see BaseActivity
 * @see MobeelizerLoginCallback
 */
public class GraphsConflictActivity extends BaseActivity<GraphsConflictsItemEntity> implements MobeelizerSyncCallback,
        OnGroupClickListener, OnChildClickListener {

    private static final int CHANGE_STATUS = 0x500;

    private Button mAddButton, mSyncButton;

    private ImageButton mInfoButton;

    private TextView mWarningText;

    private ExpandableListView mList;

    private GraphsConflictSyncAdapter mAdapter;

    private Dialog mSyncDialog = null;

    @Override
    protected Integer getHelpDialog() {
        return D_GRAPHS_CONFLICT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_list_expendable_with_buttons);
        initTitleBarViews();
        setTitleBarTitle(R.string.titleBarGraphsConflicts);

        mAddButton = (Button) findViewById(R.id.footerAdd);
        mSyncButton = (Button) findViewById(R.id.footerSync);
        mInfoButton = (ImageButton) findViewById(R.id.footerInfo);
        mWarningText = (TextView) findViewById(R.id.footerWarning);
        mList = (ExpandableListView) findViewById(android.R.id.list);

        mAddButton.setOnClickListener(getOnAddClickListener());
        mSyncButton.setOnClickListener(getOnSyncClickListener());
        mInfoButton.setOnClickListener(getOnInfoClickListener());

        // clip the add button drawable
        UIUtils.prepareClip(mAddButton);
        // clip the sync button drawable
        UIUtils.prepareClip(mSyncButton);

        List<GraphsConflictsOrderEntity> orders = Mobeelizer.getDatabase().list(GraphsConflictsOrderEntity.class);
        List<GraphsConflictsItemEntity> items = Mobeelizer.getDatabase().list(GraphsConflictsItemEntity.class);

        long conflictsCount = Mobeelizer.getDatabase().find(GraphsConflictsOrderEntity.class)
                .add(MobeelizerRestrictions.isConflicted()).count();
        conflictsCount += Mobeelizer.getDatabase().find(GraphsConflictsItemEntity.class)
                .add(MobeelizerRestrictions.isConflicted()).count();
        showWarning(conflictsCount > 0);

        mAdapter = new GraphsConflictSyncAdapter(getApplicationContext(), mList);
        mAdapter.addAllOrders(orders);
        mAdapter.addAllItems(items);
        mAdapter.sort(new GraphsConflictsOrderEntity(), new GraphsConflictsItemEntity());
        mList.setAdapter(mAdapter);
        mList.setOnCreateContextMenuListener(this);
        mList.setOnGroupClickListener(this);
        mList.setOnChildClickListener(this);
        mAdapter.notifyDataSetChanged();
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
                Intent i = new Intent(getApplicationContext(), PushNotificationsActivity.class);
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
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
        // check whether an element is an order item
        if (ExpandableListView.getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            boolean isFooterView = true;
            try {
                isFooterView = (Boolean) info.targetView.getTag(R.attr.IsFooterView);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            // if it's not the footer view show context menu
            if (!isFooterView) {
                getMenuInflater().inflate(R.menu.list_context_menu, menu);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.cm_delete) {
            ExpandableListContextMenuInfo menuInfo = (ExpandableListContextMenuInfo) item.getMenuInfo();

            int groupPosition = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);

            // get the order item which will be deleted
            final GraphsConflictsItemEntity gItem = (GraphsConflictsItemEntity) mAdapter.getChild(groupPosition, childPosition);
            Mobeelizer.getDatabase().delete(gItem);
            gItem.setEntityState(EntityState.REMOVED_A);
            mAdapter.notifyDataSetChanged();

            final UserType loggedUserType = mUserType;

            new Handler().postDelayed(new Runnable() {

                public void run() {
                    // skip, if user has changed
                    if (loggedUserType != mUserType) {
                        return;
                    }

                    mAdapter.removeOrderItem(gItem);
                    mAdapter.notifyDataSetChanged();
                }
            }, 900);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUserChanged() {
        mAdapter = new GraphsConflictSyncAdapter(getApplicationContext(), mList);
        mList.setAdapter(mAdapter);
        mAdapter.addAllOrders(Mobeelizer.getDatabase().list(GraphsConflictsOrderEntity.class));
        mAdapter.addAllItems(Mobeelizer.getDatabase().list(GraphsConflictsItemEntity.class));
        mAdapter.sort(new GraphsConflictsOrderEntity(), new GraphsConflictsItemEntity());
        mAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void onSyncFinished(final MobeelizerSyncStatus status) {
        Bundle b = null;
        // If synchronization succeeded show examples list. Otherwise show an error dialog
        switch (status) {
            case FINISHED_WITH_SUCCESS:
                final List<GraphsConflictsOrderEntity> newOrders = Mobeelizer.getDatabase()
                        .list(GraphsConflictsOrderEntity.class);
                final List<GraphsConflictsOrderEntity> oldOrders = mAdapter.getGroupList();
                final List<GraphsConflictsItemEntity> newItems = Mobeelizer.getDatabase().list(GraphsConflictsItemEntity.class);

                boolean showAnim = false;

                // mark new orders to show green overlay animation
                List<GraphsConflictsOrderEntity> addedOrders = new ArrayList<GraphsConflictsOrderEntity>();
                for (GraphsConflictsOrderEntity gOrder : newOrders) {
                    if (!oldOrders.contains(gOrder)) {
                        gOrder.setEntityState(EntityState.NEW_S);
                        addedOrders.add(gOrder);
                        showAnim = true;
                    }
                }

                // mark removed orders to show red overlay animation
                for (GraphsConflictsOrderEntity gOrder : oldOrders) {
                    if (!newOrders.contains(gOrder)) {
                        gOrder.setEntityState(EntityState.REMOVED_S);
                        showAnim = true;
                    }
                }

                // mark new items to show green overlay animation
                List<GraphsConflictsItemEntity> addedItems = new ArrayList<GraphsConflictsItemEntity>(newItems);
                for (GraphsConflictsOrderEntity gOrder : oldOrders) {
                    List<GraphsConflictsItemEntity> gItems = mAdapter.getChildList(gOrder);
                    for (GraphsConflictsItemEntity gItem : gItems) {
                        if (addedItems.contains(gItem)) {
                            addedItems.remove(gItem);
                            showAnim = true;
                        }
                    }
                }
                for (int i = 0; i < addedItems.size(); i++) {
                    addedItems.get(i).setEntityState(EntityState.NEW_S);
                }

                // mark removed items to show red overlay animation
                for (GraphsConflictsOrderEntity gOrder : oldOrders) {
                    List<GraphsConflictsItemEntity> gItems = mAdapter.getChildList(gOrder);
                    for (GraphsConflictsItemEntity gItem : gItems) {
                        if (!newItems.contains(gItem)) {
                            gItem.setEntityState(EntityState.REMOVED_S);
                            showAnim = true;
                        }
                    }
                }

                // add new orders to old list
                if (addedOrders.size() > 0) {
                    mAdapter.addAllOrders(addedOrders);
                }

                // add new items to old list
                if (addedItems.size() > 0) {
                    mAdapter.addAllItems(addedItems);
                }

                mAdapter.sort(new GraphsConflictsOrderEntity(), new GraphsConflictsItemEntity());
                mAdapter.notifyDataSetChanged();
                long conflictsCount = Mobeelizer.getDatabase().find(GraphsConflictsOrderEntity.class)
                        .add(MobeelizerRestrictions.isConflicted()).count();
                conflictsCount += Mobeelizer.getDatabase().find(GraphsConflictsItemEntity.class)
                        .add(MobeelizerRestrictions.isConflicted()).count();
                showWarning(conflictsCount > 0);

                final UserType loggedUserType = mUserType;

                new Handler().postDelayed(new Runnable() {

                    public void run() {
                        // skip, if user has changed
                        if (loggedUserType != mUserType) {
                            return;
                        }

                        mAdapter.clearAllOrders();
                        mAdapter.addAllOrders(newOrders);
                        mAdapter.addAllItems(newItems);
                        mAdapter.sort(new GraphsConflictsOrderEntity(), new GraphsConflictsItemEntity());
                        mAdapter.notifyDataSetChanged();
                        // scroll the list to first position
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
            GraphsConflictActivity.this.showDialog(BaseActivity.D_CUSTOM, b);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK && requestCode == CHANGE_STATUS) {
            int id = data.getIntExtra(GraphsConflictDetailsActivity.ID, -1);
            int status = data.getIntExtra(GraphsConflictDetailsActivity.STATUS, -1);

            if (id != -1 && status != -1) {
                // edit entity object with the new status and save it to database
                GraphsConflictsOrderEntity gOrder = (GraphsConflictsOrderEntity) mAdapter.getGroup(id);

                gOrder.setStatus(status);
                Mobeelizer.getDatabase().save(gOrder);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    // =====================================================================================
    // =====================================================================================
    // =====================================================================================

    /**
     * Creates an order number based on currently logged in user and the number of orders created before.
     */
    private String getOrderNumber() {
        String owner = mUserType.name();
        long count = Mobeelizer.getDatabase().find(GraphsConflictsOrderEntity.class).add(MobeelizerRestrictions.ownerEq(owner))
                .count();
        return String.format("%s/%04d", owner, count + 1);
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
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            ssb.setSpan(boldSpan, 0, text.indexOf(':'), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            Bitmap syncIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_sync);
            int syncIndex = text.indexOf('|');
            ssb.setSpan(new ImageSpan(syncIcon), syncIndex, syncIndex + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            mWarningText.setText(ssb, BufferType.SPANNABLE);
        }
    }

    // =====================================================================================
    // ================================= ONCLICKS ==========================================
    // =====================================================================================

    /**
     * Returns {@link View.OnClickListener} for "Add" button.
     * 
     */
    private View.OnClickListener getOnAddClickListener() {
        return new View.OnClickListener() {

            public void onClick(final View v) {
                GraphsConflictsOrderEntity gOrder = new GraphsConflictsOrderEntity();
                gOrder.setName(getOrderNumber());
                gOrder.setStatus(1);

                Mobeelizer.getDatabase().save(gOrder);
                gOrder.setEntityState(EntityState.NEW_A);
                mAdapter.addOrder(gOrder);
                mAdapter.sort(new GraphsConflictsOrderEntity(), new GraphsConflictsItemEntity());
                mAdapter.notifyDataSetChanged();

                int position = 0;
                for (GraphsConflictsOrderEntity order : mAdapter.getGroupList()) {
                    if (!order.equals(gOrder)) {
                        position += mAdapter.getChildList(order).size() + 2;
                    } else {
                        position += 2;
                        break;
                    }
                }
                mList.smoothScrollToPosition(position);
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

            public void onClick(final View v) {
                // show synchronization progress dialog
                mSyncDialog = new Dialog(GraphsConflictActivity.this, R.style.MobeelizerDialogTheme);
                mSyncDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mSyncDialog.setContentView(R.layout.progress_dialog);
                mSyncDialog.setCancelable(false);
                mSyncDialog.show();

                // start synchronization
                Mobeelizer.sync(GraphsConflictActivity.this);
            }
        };
    }

    /**
     * Returns {@link View.OnClickListener} for "Information" button. When the button is clicked the info dialog appears with
     * Graph's Conflicts help text.
     */
    private View.OnClickListener getOnInfoClickListener() {
        return new View.OnClickListener() {

            public void onClick(final View v) {
                showDialog(D_GRAPHS_CONFLICT);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public boolean onGroupClick(final ExpandableListView paramExpandableListView, final View paramView, final int paramInt,
            final long paramLong) {
        GraphsConflictsOrderEntity gOrder = (GraphsConflictsOrderEntity) mAdapter.getGroup(paramInt);

        Intent i = new Intent(GraphsConflictActivity.this, GraphsConflictDetailsActivity.class);
        i.putExtra(GraphsConflictDetailsActivity.ID, paramInt);
        i.putExtra(GraphsConflictDetailsActivity.TITLE, gOrder.getName());
        i.putExtra(GraphsConflictDetailsActivity.STATUS, gOrder.getStatus());
        startActivityForResult(i, CHANGE_STATUS);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onChildClick(final ExpandableListView parent, final View v, final int groupPosition, final int childPosition,
            final long id) {
        Boolean isFooter = (Boolean) v.getTag(R.attr.IsFooterView);
        if (isFooter) {
            // when the clicked item is the footer one add an item to the order
            GraphsConflictsOrderEntity gOrder = (GraphsConflictsOrderEntity) mAdapter.getGroup(groupPosition);

            Movie m = DataUtil.getRandomMovie(getResources());
            GraphsConflictsItemEntity gItem = new GraphsConflictsItemEntity();
            gItem.setTitle(m.title);
            gItem.setOrderGuid(gOrder.getGuid());

            Mobeelizer.getDatabase().save(gItem);
            gItem.setEntityState(EntityState.NEW_A);
            mAdapter.addItem(gItem);
            mAdapter.sort(new GraphsConflictsOrderEntity(), new GraphsConflictsItemEntity());
            mAdapter.notifyDataSetChanged();

            int position = 0;
            for (GraphsConflictsOrderEntity order : mAdapter.getGroupList()) {
                if (!order.equals(gOrder)) {
                    position += mAdapter.getChildList(order).size() + 2;
                } else {
                    position += mAdapter.getChildList(order).indexOf(gItem) + 1;
                    break;
                }
            }
            mList.smoothScrollToPosition(position);
        }
        return true;
    }
}
