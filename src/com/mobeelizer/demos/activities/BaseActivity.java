//
// BaseActivity.java
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

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mobeelizer.demos.ApplicationStatus;
import com.mobeelizer.demos.R;
import com.mobeelizer.demos.custom.EntityState;
import com.mobeelizer.demos.model.OverlayedEntity;
import com.mobeelizer.java.api.MobeelizerOperationError;
import com.mobeelizer.mobile.android.Mobeelizer;
import com.mobeelizer.mobile.android.api.MobeelizerOperationCallback;

/**
 * This is a base activity for all other activities except {@link LoginActivity} and {@link CreateSessionCodeActivity}. It is
 * responsible for title bar, contains some common methods and fields like merging lists or handling user login and logout.
 * 
 * @param <T>
 *            This parameter extends {@link OverlayedEntity} and it is required in {@link #mergeLists(List, List)} method to allow
 *            {@link OverlayedEntity#setEntityState(EntityState)} usage.
 * 
 * @see OverlayedEntity
 * @see MobeelizerOperationCallback
 */
public abstract class BaseActivity<T extends OverlayedEntity> extends Activity implements OnClickListener {

    public static final String DISPLAY_PUSH_MESSAGE_ACTION = "DISPLAY_PUSH_MESSAGE";

    public static final String DISPLAY_PUSH_MESSAGE_ACTION_EXTRA_MESSAGE = "message";

    /** Parameter name for {@link Bundle} object used to pass information about dialog type (info or error) */
    public static final String IS_INFO = "IS_INFO_DIALOG";

    /** Parameter name for {@link Bundle} object used to pass {@link Resources} id of displayed text. */
    public static final String TEXT_RES_ID = "RES_ID";

    /** Parameter name for {@link Bundle} object used to pass {@link String} object with the text to display inside dialog. */
    public static final String CUSTOM_TEXT = "TEXT";

    /** Name of the value in {@link SharedPreferences} which contains name of the current user. */
    public static final String USER_TYPE = "USER_TYPE";

    /** Name of the value in {@link SharedPreferences} which contains currently used session. */
    public static final String SESSION_CODE = "SESSION_CODE";

    /** Id of the help dialog that can be created without providing any additional data. */
    public static final int D_SIMPLE_SYNC = 0x100, D_PHOTO_SYNC = 0x101, D_PERMISSIONS = 0x102, D_CONFLICTS = 0x103,
            D_GRAPHS_CONFLICT = 0x104, D_PUSH_NOTIFICATIONS = 0x105;

    /**
     * Id of the dialog that can be customized to display any information or error stored in Android {@link Resources} or
     * {@link String} object.
     */
    public static final int D_CUSTOM = 0x106;

    private ImageButton mUserButton;

    private TextView mSessionCodeTextView, mTitleBarTitle;

    private Dialog mLoginDialog = null;

    /** Reference to {@link SharedPreferences} object. */
    protected SharedPreferences mSharedPrefs;

    /** Current session code */
    private String mSessionCode;

    /** Currently logged in user */
    protected UserType mUserType;

    protected abstract Integer getHelpDialog();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSessionCode = mSharedPrefs.getString(SESSION_CODE, null);
        mUserType = UserType.valueOf(mSharedPrefs.getString(USER_TYPE, "A"));

        boolean initialized = mSharedPrefs.getBoolean(getClass().getSimpleName(), false);
        if (!initialized) {
            if (getHelpDialog() != null) {
                showDialog(getHelpDialog());
            }
            mSharedPrefs.edit().putBoolean(this.getClass().getSimpleName(), true).commit();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        ApplicationStatus.activityResumed(this);
        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_PUSH_MESSAGE_ACTION));
        super.onResume();
        mUserType = UserType.valueOf(mSharedPrefs.getString(USER_TYPE, "A"));
        if (mUserButton != null) {
            setUserType();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        ApplicationStatus.activityPaused();
        unregisterReceiver(mHandleMessageReceiver);
        super.onPause();
    }

    /**
     * Allows to display information or error dialog with specific text to the user. Round brackets contains the {@code id} value
     * required to show each dialog.
     * 
     * <p>
     * <b>Possible information dialogs:</b><br/>
     * - Simple Sync Activity Help ({@code D_SIMPLE_SYNC})<br/>
     * - Photo Sync Activity Help ({@code D_PHOTO_SYNC})<br/>
     * - Permissions Activity Help ({@code D_PERMISSIONS})<br/>
     * - Conflicts Activity Help ({@code D_CONFLICTS})<br/>
     * - Graph's Conflict Activity Help ({@code D_GRAPHS_CONFLICT})
     * 
     * <p>
     * The custom dialog ({@code D_CUSTOM}) can also be shown. In this case additional information needs to be provided. <br/>
     * Custom dialog requires reference to {@link Bundle} object with the following fields:<br/>
     * - type of the dialog (information or error) - {@code IS_INFO} as {@code boolean}<br/>
     * - text to display as resource id - {@code TEXT_RES_ID} as {@code int}<br/>
     * - text to display as String object - {@code CUSTOM_TEXT} as {@code String} <br/>
     * <br/>
     * The last two can be used interchangeably but when both are used resource id is preferred.
     */
    @Override
    protected Dialog onCreateDialog(final int id, final Bundle args) {
        Dialog dialog = null;
        switch (id) {
        // if custom dialog should be displayed additional information are required
            case D_CUSTOM:
                if (args == null || (args.getInt(TEXT_RES_ID, -1) == -1 && args.getString(CUSTOM_TEXT) == null)) {
                    return null;
                }
                // otherwise we have all information required
            case D_SIMPLE_SYNC:
            case D_PHOTO_SYNC:
            case D_PERMISSIONS:
            case D_CONFLICTS:
            case D_GRAPHS_CONFLICT:
            case D_PUSH_NOTIFICATIONS:
                break;
            // if the value is out of range no dialog should be shown
            default:
                return null;
        }

        dialog = new Dialog(this, R.style.MobeelizerDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    /**
     * {@inheritDoc} <br/>
     * <br/>
     * See: {@link BaseActivity#onCreateDialog(int, Bundle)}
     */
    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog, final Bundle args) {
        TextView text = null;
        Button closeButton = null;

        if (id == D_CUSTOM) {
            // if custom dialog should be displayed additional information are required
            if (args != null) {
                // whether to display info dialog or error dialog
                boolean isInfoDialog = args.getBoolean(IS_INFO, true);
                // information/error text from resources
                int resId = args.getInt(TEXT_RES_ID, -1);
                // infroamtion/error text from String object
                String customText = args.getString(CUSTOM_TEXT);

                dialog.setContentView(isInfoDialog ? R.layout.info_dialog : R.layout.error_dialog);
                text = (TextView) dialog.findViewById(R.id.dialogText);
                closeButton = (Button) dialog.findViewById(R.id.dialogButton);

                // determines which text should be chosen to be displayed
                if (resId != -1) {
                    text.setText(resId);
                } else if (customText != null) {
                    text.setText(customText);
                }
            }
        } else {
            // list of help informations for each example
            switch (id) {
                case D_SIMPLE_SYNC:
                    dialog.setContentView(R.layout.info_dialog_simple_sync);
                    break;
                case D_PHOTO_SYNC:
                    dialog.setContentView(R.layout.info_dialog_files_sync);
                    break;
                case D_PERMISSIONS:
                    dialog.setContentView(R.layout.info_dialog_permissions_sync);
                    break;
                case D_CONFLICTS:
                    dialog.setContentView(R.layout.info_dialog_conflicts_sync);
                    break;
                case D_GRAPHS_CONFLICT:
                    dialog.setContentView(R.layout.info_dialog_relation_conflicts_sync);
                    break;
                case D_PUSH_NOTIFICATIONS:
                    dialog.setContentView(R.layout.info_dialog_push_notifications);
                    break;
            }
            closeButton = (Button) dialog.findViewById(R.id.dialogButton);
        }

        final Dialog tmp = dialog;
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                tmp.dismiss();
            }

        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_logout:
                // check if there is synchronization in progress
                if (Mobeelizer.checkSyncStatus().isRunning()) {
                    // if true show error dialog and cancel user logout operation
                    Bundle err = new Bundle();
                    err.putBoolean(IS_INFO, false);
                    err.putInt(TEXT_RES_ID, R.string.e_cannotLogout);
                    showDialog(D_CUSTOM, err);
                    return true;
                }

                Mobeelizer.unregisterForRemoteNotifications();
                Mobeelizer.logout();

                // remove both the user and session information from shared preferences
                mSharedPrefs.edit().remove(SESSION_CODE).remove(USER_TYPE).commit();

                // show login activity
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ========================================================================================
    // ========================================================================================
    // ========================================================================================

    /**
     * Initializes references to title bar views and sets user indicator on click listener. This method is used by derived classes
     * after setting content view.
     */
    protected void initTitleBarViews() {
        mUserButton = (ImageButton) findViewById(R.id.titleBarButton);
        mSessionCodeTextView = (TextView) findViewById(R.id.titleBarSessionCode);
        mTitleBarTitle = (TextView) findViewById(R.id.titleBarTitle);

        if (mUserButton != null) {
            mUserButton.setOnClickListener(this);
        }
    }

    /**
     * Updates title bar user indicator to match currently logged in user.
     */
    protected void setUserType() {
        switch (mUserType) {
            case A:
                mUserButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_user_a_big));
                break;
            case B:
                mUserButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_user_b_big));
                break;
        }
    }

    /**
     * Shows or hides "SESSION CODE: ..." text in title bar.
     * 
     * @param isVisible
     *            Whether the text should be visible.
     */
    protected void setSessionCodeVisibility(final boolean isVisible) {
        if (isVisible) {
            mSessionCodeTextView.setText(getResources().getString(R.string.titleBarSessionCodeN, mSessionCode));
        }
        mSessionCodeTextView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Sets the text displayed below Mobeelizer logo.
     * 
     * @param title
     *            Title stored as a {@link String}
     * 
     * @see {@link #setTitleBarTitle(int)}
     */
    protected void setTitleBarTitle(final String title) {
        mTitleBarTitle.setText(title);
    }

    /**
     * Sets the text displayed below Mobeelizer logo. This version uses Android {@link Resources} id instead of String object
     * 
     * @param titleResource
     *            {@link Resources} id pointing to desired title text
     * 
     * @see {@link #setTitleBarTitle(String)}
     */
    protected void setTitleBarTitle(final int titleResource) {
        mTitleBarTitle.setText(titleResource);
    }

    /**
     * Merges two lists of items and determines which items are added and which will be removed. This information is used for
     * starting proper overlay animation.
     * 
     * <p>
     * Merging works so that all items from new list that are not in the old one sets the
     * {@link OverlayedEntity#setEntityState(EntityState)} with {@link EntityState#NEW_S} value. <br/>
     * Additionally all items from old list not contained by new one are marked with {@link EntityState#REMOVED_S} value. Result
     * of the merging is stored in old list and the new one is not modified.
     * 
     * <p>
     * Before swapping old list with the new one in the adapter one needs to call {@code adapter.notifyDataSetChanged()} to start
     * the animation. After it completes adapters data can be swapped to the new list.
     * 
     * @param oldList
     *            List of items currently displayed in {@link ListView}
     * @param newList
     *            List of items synchronized with Mobeelizer servers.
     * @return Whether to show an addition/removal overlay animation.
     */
    protected boolean mergeLists(final List<T> oldList, final List<T> newList) {
        boolean showAnim = false;

        // mark new items to show green overlay animation
        List<T> added = new ArrayList<T>();
        for (T item : newList) {
            if (!oldList.contains(item)) {
                item.setEntityState(EntityState.NEW_S);
                added.add(item);
                showAnim = true;
            }
        }

        // mark removed items to show red overlay animation
        for (T item : oldList) {
            if (!newList.contains(item)) {
                item.setEntityState(EntityState.REMOVED_S);
                showAnim = true;
            }
        }

        // add all new items to old list
        if (added.size() > 0) {
            oldList.addAll(added);
        }

        return showAnim;
    }

    // ========================================================================================
    // ================================== ON CLICKS ===========================================
    // ========================================================================================

    /**
     * Called when the title bar user indicator has been clicked. It's responsible for logging out the current user and trying to
     * login as a new one.
     */
    @Override
    public void onClick(final View paramView) {
        // check if there is synchronization in progress
        if (Mobeelizer.checkSyncStatus().isRunning()) {
            // if true show error dialog and cancel user switching
            Bundle err = new Bundle();
            err.putBoolean(IS_INFO, false);
            err.putInt(TEXT_RES_ID, R.string.e_cannotChangeUser);
            showDialog(D_CUSTOM, err);
            return;
        }

        // show logging in dialog
        mLoginDialog = new Dialog(this, R.style.MobeelizerDialogTheme);
        mLoginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoginDialog.setContentView(R.layout.progress_dialog);
        mLoginDialog.setCancelable(false);
        // this is the synchronizing dialog so the text displayed needs to be changed
        ((TextView) mLoginDialog.findViewById(R.id.dialogText)).setText(R.string.loggingIn);
        mLoginDialog.show();

        // logout current user
        Mobeelizer.unregisterForRemoteNotifications();
        Mobeelizer.logout();

        // remove user login data so it won't be automatically logged in if the application will be closed
        Editor editor = mSharedPrefs.edit();
        editor.remove(USER_TYPE).commit();

        switch (mUserType) {
            case A:
                // try to login as a user B when A was recently logged out
                Mobeelizer.login(mSessionCode, getString(R.string.c_userBLogin), getString(R.string.c_userBPassword),
                        new LoginListener());
                mUserType = UserType.B;
                break;
            case B:
                // try to login as a user A when B was recently logged out
                Mobeelizer.login(mSessionCode, getString(R.string.c_userALogin), getString(R.string.c_userAPassword),
                        new LoginListener());
                mUserType = UserType.A;
                break;
        }
    }

    // ========================================================================================
    // ========================================================================================
    // ========================================================================================

    /**
     * This method should contain the code that needs to be called after the user changes. In most cases this will refresh the
     * list with the data of the newly logged in user.
     */
    protected abstract void onUserChanged();

    /**
     * Enum with the user types (logins) for ease of user changes.
     */
    public enum UserType {
        /** User A */
        A,
        /** User B */
        B
    }

    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String title = "Push received!";
            String message = intent.getExtras().getString(DISPLAY_PUSH_MESSAGE_ACTION_EXTRA_MESSAGE);

            final Dialog dialog = new Dialog(ApplicationStatus.getCurrentActivity(), R.style.MobeelizerDialogTheme);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_dialog);
            TextView titleView = (TextView) dialog.findViewById(R.id.dialogTitle);
            titleView.setText(title);
            TextView textView = (TextView) dialog.findViewById(R.id.dialogText);
            textView.setText(message);
            Button closeButton = (Button) dialog.findViewById(R.id.dialogButton);
            closeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View paramView) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    };

    private class LoginListener implements MobeelizerOperationCallback {

        @Override
        public void onSuccess() {
            C2DMReceiver.performPushRegistration();

            // change the title bar user indicator
            setUserType();
            // save current user
            Editor editor = mSharedPrefs.edit();
            editor.putString(USER_TYPE, mUserType.name());
            editor.commit();

            onUserChanged();
            if (mLoginDialog != null) {
                mLoginDialog.dismiss();
            }
        }

        @Override
        public void onFailure(final MobeelizerOperationError error) {
            Editor editor = mSharedPrefs.edit();
            editor.remove(SESSION_CODE);

            Bundle err = new Bundle();
            err.putBoolean(IS_INFO, false);
            if (error.getCode().equals("connectionFailure")) {
                err.putInt(TEXT_RES_ID, R.string.e_missingConnection);
            } else {
                err.putInt(TEXT_RES_ID, R.string.e_cannotConnectToSession);
            }
            showDialog(D_CUSTOM, err);

            // if login operation does not succeed show login activity
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            // and close the current one
            finish();
            if (mLoginDialog != null) {
                mLoginDialog.dismiss();
            }
        }

    }

}
