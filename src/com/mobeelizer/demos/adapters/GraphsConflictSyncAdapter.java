//
// GraphsConflictSyncAdapter.java
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

package com.mobeelizer.demos.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mobeelizer.demos.R;
import com.mobeelizer.demos.activities.BaseActivity.UserType;
import com.mobeelizer.demos.custom.EntityState;
import com.mobeelizer.demos.model.GraphsConflictsItemEntity;
import com.mobeelizer.demos.model.GraphsConflictsOrderEntity;
import com.mobeelizer.mobile.android.Mobeelizer;

/**
 * An adapter for an expandable list view in "Graph's Conflict" example.
 * 
 * @see BaseExpandableListAdapter
 * @see GraphsConflictsOrderEntity
 * @see GraphsConflictsItemEntity
 */
public class GraphsConflictSyncAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater mInflater;

    private final Resources mRes;

    private final Context mContext;

    private final ExpandableListView mList;

    private final List<GraphsConflictsOrderEntity> mOrders;

    private final Map<String, List<GraphsConflictsItemEntity>> mItems;

    /**
     * Constructor
     * 
     * @param context
     */
    public GraphsConflictSyncAdapter(final Context context, final ExpandableListView list) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRes = context.getResources();
        mOrders = new ArrayList<GraphsConflictsOrderEntity>();
        mItems = new HashMap<String, List<GraphsConflictsItemEntity>>();
        mList = list;
    }

    /**
     * Adds an order to the adapter and creates an empty list for order items if necessary.
     * 
     * @param order
     *            An order to add
     */
    public void addOrder(final GraphsConflictsOrderEntity order) {
        if (!mOrders.contains(order)) {
            mOrders.add(order);
            mItems.put(order.getGuid(), new ArrayList<GraphsConflictsItemEntity>());
        }
    }

    /**
     * Adds an item to the adapter only when there is a matching order.
     * 
     * @param item
     *            An item to add
     */
    public void addItem(final GraphsConflictsItemEntity item) {
        if (mItems.containsKey(item.getOrderGuid())) {
            mItems.get(item.getOrderGuid()).add(item);
        }
    }

    /**
     * Adds a list of orders to an adapter. This method also creates an empty lists for order items if it doesn't exist yet.
     * 
     * @param orders
     *            List of orders that needs to be added
     */
    public void addAllOrders(final List<GraphsConflictsOrderEntity> orders) {
        for (GraphsConflictsOrderEntity order : orders) {
            if (!mOrders.contains(order)) {
                mOrders.add(order);
                mItems.put(order.getGuid(), new ArrayList<GraphsConflictsItemEntity>());
            }
        }
    }

    /**
     * Adds a list of items to an adapter. When items are added they are also sorted by the order to which they belong to. When
     * there is no order that matches currently added item that item is skipped.<br/>
     * <b>Before adding</b> any items the order list should be added using {@link #addAllOrders(List)} method.
     * 
     * @param items
     *            List of items that needs to be added
     */
    public void addAllItems(final List<GraphsConflictsItemEntity> items) {
        for (GraphsConflictsItemEntity item : items) {
            if (mItems.containsKey(item.getOrderGuid())) {
                mItems.get(item.getOrderGuid()).add(item);
            }
        }
    }

    /**
     * Returns the list of items held by the adapter which belongs to the order provided.
     * 
     * @param order
     *            An order which items should be returned
     * @return A list of orders items
     */
    public List<GraphsConflictsItemEntity> getChildList(final GraphsConflictsOrderEntity order) {
        return mItems.get(order.getGuid());
    }

    /**
     * Returns the list of groups held by the adapter. It is required to show overlay animation after data synchronization.
     */
    public List<GraphsConflictsOrderEntity> getGroupList() {
        return mOrders;
    }

    /**
     * Removes a specific order from adapter as well as all items that has been linked with the order.
     * 
     * @param order
     *            An order to remove
     */
    public void removeOrder(final GraphsConflictsOrderEntity order) {
        List<GraphsConflictsItemEntity> items = mItems.get(order.getGuid());
        if (items != null) {
            for (GraphsConflictsItemEntity gItem : items) {
                Mobeelizer.getDatabase().delete(gItem);
            }
            Mobeelizer.getDatabase().delete(order);
            mItems.remove(order.getGuid());
            mOrders.remove(order);
        }
    }

    /**
     * Removes a specific item from the order
     * 
     * @param item
     *            An item to remove
     */
    public void removeOrderItem(final GraphsConflictsItemEntity item) {
        Mobeelizer.getDatabase().delete(item);
        List<GraphsConflictsItemEntity> items = mItems.get(item.getOrderGuid());
        if (items != null) {
            items.remove(item);
        }
    }

    /**
     * Removes all orders from the adapter. This method also removes all items.
     */
    public void clearAllOrders() {
        mOrders.clear();
        mItems.clear();
    }

    /**
     * Removes all items from the adapter
     */
    public void clearAllItems() {
        mItems.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChild(final int groupPosition, final int childPosition) {
        return mItems.get(mOrders.get(groupPosition).getGuid()).get(childPosition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return childPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, View convertView,
            final ViewGroup parent) {
        ViewHolder vh;
        // check whether the view can be reused
        if (convertView == null) {
            // if not inflate the new one
            convertView = mInflater.inflate(R.layout.itemized_child_list_item, null, false);

            // obtain references to child elements
            vh = new ViewHolder();
            vh.text = (TextView) convertView.findViewById(R.id.listItemText);
            vh.user = (ImageView) convertView.findViewById(R.id.listItemUser);
            vh.conflict = (ImageView) convertView.findViewById(R.id.listItemConflict);
            vh.overlay = convertView.findViewById(R.id.listItemOverlay);
            vh.add = (ImageView) convertView.findViewById(R.id.listItemAdd);
            // and store them as a tag in the view for later reuse
            convertView.setTag(R.attr.ViewHolder, vh);
        } else {
            // if there is a view to reuse get child elements references
            vh = (ViewHolder) convertView.getTag(R.attr.ViewHolder);
        }
        // clear the ongoing animation so that on devices with Android prior to 3.0 overlay animation
        // wont be displayed on wrong elements after rapid items addition
        vh.overlay.clearAnimation();
        vh.overlay.setBackgroundColor(mRes.getColor(android.R.color.transparent));

        vh.add.setTag(groupPosition);
        convertView.setTag(R.attr.IsFooterView, isLastChild);

        if (!isLastChild) {
            // if not the last child hide footer
            showChildFooter(vh, false);

            // get current entity
            GraphsConflictsItemEntity gItem = (GraphsConflictsItemEntity) getChild(groupPosition, childPosition);

            vh.text.setText(gItem.getTitle());
            // show conflict indicator if necessary
            vh.conflict.setVisibility(gItem.isConflicted() ? View.VISIBLE : View.GONE);

            // and an owner
            UserType user = UserType.valueOf(gItem.getOwner());
            switch (user) {
                case A:
                    vh.user.setImageResource(R.drawable.bt_user_a_small);
                    break;
                case B:
                    vh.user.setImageResource(R.drawable.bt_user_b_small);
                    break;
            }

            // if the entity state has changed play the animation
            if (gItem.getEntityState() != EntityState.NONE) {
                Animation anim = null;
                int delay = 0;
                switch (gItem.getEntityState()) {
                // animation played when the item was added or removed by the user
                    case NEW_A:
                    case REMOVED_A:
                        anim = AnimationUtils.loadAnimation(mContext, R.anim.list_item_overlay);
                        delay = 900;
                        break;
                    // animation played when the item was synchronized
                    case NEW_S:
                    case REMOVED_S:
                        anim = AnimationUtils.loadAnimation(mContext, R.anim.list_item_sync_overlay);
                        delay = 2000;
                        break;
                }
                int color = 0;
                switch (gItem.getEntityState()) {
                // green overlay
                    case NEW_A:
                    case NEW_S:
                        color = mRes.getColor(R.color.listItemAddedOverlay);
                        break;
                    // red overlay
                    case REMOVED_A:
                    case REMOVED_S:
                        color = mRes.getColor(R.color.listItemRemovedOverlay);
                        break;
                }

                // set the background
                vh.overlay.setBackgroundColor(color);
                // and animate it
                vh.overlay.startAnimation(anim);

                // when the animation finishes change the background back to transparent
                final View overlay = vh.overlay;
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        overlay.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                    }
                }, delay);

                gItem.setEntityState(EntityState.NONE);
            }
        } else {
            showChildFooter(vh, true);
            vh.conflict.setVisibility(View.GONE);
        }

        return convertView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildrenCount(final int groupPosition) {
        String guid = ((GraphsConflictsOrderEntity) getGroup(groupPosition)).getGuid();
        return mItems.get(guid).size() + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getGroup(final int groupPosition) {
        return mOrders.get(groupPosition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupCount() {
        return mOrders.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        ViewHolder vh;
        // check whether the view can be reused
        if (convertView == null) {
            // if not inflate the new one
            convertView = mInflater.inflate(R.layout.itemized_list_item, null, false);

            // obtain references to child elements
            vh = new ViewHolder();
            vh.text = (TextView) convertView.findViewById(R.id.listItemText);
            vh.status = (ImageView) convertView.findViewById(R.id.listItemStatus);
            vh.user = (ImageView) convertView.findViewById(R.id.listItemUser);
            vh.conflict = (ImageView) convertView.findViewById(R.id.listItemConflict);
            vh.overlay = convertView.findViewById(R.id.listItemOverlay);
            // and store them as a tag in the view for later reuse
            convertView.setTag(vh);

            vh.status.setVisibility(View.VISIBLE);
        } else {
            // if there is a view to reuse get child elements references
            vh = (ViewHolder) convertView.getTag();
        }
        // clear the ongoing animation so that on devices with Android prior to 3.0 overlay animation
        // wont be displayed on wrong elements after rapid items addition
        vh.overlay.clearAnimation();
        vh.overlay.setBackgroundColor(mRes.getColor(android.R.color.transparent));

        // get current group entity
        GraphsConflictsOrderEntity gOrder = (GraphsConflictsOrderEntity) getGroup(groupPosition);

        vh.text.setText(mRes.getString(R.string.graphsConflictOrder, gOrder.getName()));
        // show conflict indicator if necessary
        vh.conflict.setVisibility(gOrder.isConflicted() ? View.VISIBLE : View.GONE);

        // set a correct image for current status
        switch (gOrder.getStatus()) {
            case 1:
                vh.status.setImageResource(R.drawable.ic_status_new);
                break;
            case 2:
                vh.status.setImageResource(R.drawable.ic_status_pending);
                break;
            case 3:
                vh.status.setImageResource(R.drawable.ic_status_readytoship);
                break;
            case 4:
                vh.status.setImageResource(R.drawable.ic_status_shipped);
                break;
            case 5:
                vh.status.setImageResource(R.drawable.ic_status_received);
                break;
        }

        // and an owner
        UserType user = UserType.valueOf(gOrder.getOwner());
        switch (user) {
            case A:
                vh.user.setImageResource(R.drawable.bt_user_a_small);
                break;
            case B:
                vh.user.setImageResource(R.drawable.bt_user_b_small);
                break;
        }

        // if the entity state has changed play the animation
        if (gOrder.getEntityState() != EntityState.NONE) {
            Animation anim = null;
            int delay = 0;
            switch (gOrder.getEntityState()) {
            // animation played when the item was added or removed by the user
                case NEW_A:
                case REMOVED_A:

                    anim = AnimationUtils.loadAnimation(mContext, R.anim.list_item_overlay);
                    delay = 900;
                    break;
                // animation played when the item was synchronized
                case NEW_S:
                case REMOVED_S:
                    anim = AnimationUtils.loadAnimation(mContext, R.anim.list_item_sync_overlay);
                    delay = 2500;
                    break;
            }
            int color = 0;
            switch (gOrder.getEntityState()) {
            // green overlay
                case NEW_A:
                case NEW_S:
                    color = mRes.getColor(R.color.listItemAddedOverlay);
                    break;
                // red overlay
                case REMOVED_A:
                case REMOVED_S:
                    color = mRes.getColor(R.color.listItemRemovedOverlay);
                    break;
            }

            // set the background
            vh.overlay.setBackgroundColor(color);
            // and animate it
            vh.overlay.startAnimation(anim);

            // when the animation finishes change the background back to transparent
            final View overlay = vh.overlay;
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    overlay.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                }
            }, delay);

            gOrder.setEntityState(EntityState.NONE);
        }

        return convertView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        // all groups should be expanded and without ability to collapse
        for (int i = 0; i < getGroupCount(); i++) {
            mList.expandGroup(i);
        }
        super.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }

    /**
     * Gives the ability to show or hide the child element footer with add button.
     * 
     * @param vh
     *            Reference to {@link ViewHolder} class
     * @param isFooterVisible
     *            Whether the footer should be visible or not
     */
    private void showChildFooter(final ViewHolder vh, final boolean isFooterVisible) {
        vh.add.setVisibility(isFooterVisible ? View.VISIBLE : View.GONE);
        vh.text.setVisibility(isFooterVisible ? View.GONE : View.VISIBLE);
        vh.user.setVisibility(isFooterVisible ? View.GONE : View.VISIBLE);
    }

    /**
     * Gives the ability to sort the {@link ExpandableListView}. At first the group are sorted, then groups items. In the default
     * implementation sorting take into account only the name or title filed.
     * 
     * @param groupComparator
     *            Comparator used to sort group entities
     * @param itemComparator
     *            Comparator used to sort item entities
     */
    public void sort(final Comparator<GraphsConflictsOrderEntity> groupComparator,
            final Comparator<GraphsConflictsItemEntity> itemComparator) {
        Collections.sort(mOrders, groupComparator);
        for (GraphsConflictsOrderEntity gOrder : mOrders) {
            Collections.sort(mItems.get(gOrder.getGuid()), itemComparator);
        }
    }

    /**
     * Class that holds references for views contained by list item. It implements a ViewHolder pattern for efficient reuse of
     * views in {@link ListView}.
     */
    private class ViewHolder {

        public TextView text;

        public ImageView status;

        public ImageView user;

        public ImageView conflict;

        public View overlay;

        public ImageView add;
    }
}
