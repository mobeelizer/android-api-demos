//
// FileSyncAdapter.java
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

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mobeelizer.demos.R;
import com.mobeelizer.demos.activities.BaseActivity.UserType;
import com.mobeelizer.demos.custom.EntityState;
import com.mobeelizer.demos.custom.MyArrayAdapter;
import com.mobeelizer.demos.model.FileSyncEntity;
import com.mobeelizer.java.api.MobeelizerFile;

/**
 * An adapter for a list view in "File Sync" example.
 * 
 * @see MyArrayAdapter
 * @see ArrayAdapter
 * @see FileSyncEntity
 */
public class FileSyncAdapter extends MyArrayAdapter<FileSyncEntity> {

    private LayoutInflater mInflater = null;

    private final Resources mRes;

    private final DisplayMetrics mDisplayMetrics;

    /**
     * Constructor
     * 
     * @param context
     * @param objects
     */
    public FileSyncAdapter(final Context context, final List<FileSyncEntity> objects) {
        super(context, R.layout.itemized_list_item, R.id.listItemLowerText, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRes = context.getResources();
        mDisplayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder vh = null;
        // check whether the view can be reused
        if (convertView == null) {
            // if not inflate the new one
            convertView = mInflater.inflate(R.layout.photos_list_item, null, false);

            // obtain references to child elements
            vh = new ViewHolder();
            vh.title = (TextView) convertView.findViewById(R.id.listItemText);
            vh.photo = (ImageView) convertView.findViewById(R.id.listItemPhoto);
            vh.user = (ImageView) convertView.findViewById(R.id.listItemUser);
            vh.overlay = convertView.findViewById(R.id.listItemOverlay);
            // and store them as a tag in the view for later reuse
            convertView.setTag(vh);
        } else {
            // if there is a view to reuse get child elements references
            vh = (ViewHolder) convertView.getTag();
        }
        // clear the ongoing animation so that on devices with Android prior to 3.0 overlay animation
        // wont be displayed on wrong elements after rapid items addition
        vh.overlay.clearAnimation();
        vh.overlay.setBackgroundColor(mRes.getColor(android.R.color.transparent));

        // get current entity
        FileSyncEntity fse = getItem(position);
        MobeelizerFile photo = fse.getPhoto();

        // in this case the title is not displayed
        vh.title.setText("");

        // decode stored bitmap and resize it to fit the list item ImageView
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(photo.getInputStream(), null, opt);
        opt.inJustDecodeBounds = false;
        opt.inSampleSize = (int) Math.max(Math.round(opt.outHeight / (35.0 * mDisplayMetrics.scaledDensity)),
                Math.round(opt.outHeight / (35.0 * mDisplayMetrics.scaledDensity)));

        vh.photo.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeStream(photo.getInputStream(), null, opt)));
        // and an owner
        UserType user = UserType.valueOf(getItem(position).getOwner());
        switch (user) {
            case A:
                vh.user.setImageResource(R.drawable.bt_user_a_small);
                break;
            case B:
                vh.user.setImageResource(R.drawable.bt_user_b_small);
                break;
        }

        // if the entity state has changed play the animation
        if (fse.getEntityState() != EntityState.NONE) {
            Animation anim = null;
            int delay = 0;
            switch (fse.getEntityState()) {
            // animation played when the item was added or removed by the user
                case NEW_A:
                case REMOVED_A:
                    anim = AnimationUtils.loadAnimation(getContext(), R.anim.list_item_overlay);
                    delay = 900;
                    break;
                // animation played when the item was synchronized
                case NEW_S:
                case REMOVED_S:
                    anim = AnimationUtils.loadAnimation(getContext(), R.anim.list_item_sync_overlay);
                    delay = 2000;
                    break;
            }
            int color = 0;
            switch (fse.getEntityState()) {
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
                    overlay.setBackgroundColor(mRes.getColor(android.R.color.transparent));
                }
            }, delay);

            fse.setEntityState(EntityState.NONE);
        }

        return convertView;
    }

    /**
     * Class that holds references for views contained by list item. It implements a ViewHolder pattern for efficient reuse of
     * views in {@link ListView}.
     */
    private class ViewHolder {

        public TextView title;

        public ImageView photo;

        public ImageView user;

        public View overlay;
    }
}
