//
// CreateSessionCodeActivity.java
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobeelizer.demos.R;
import com.mobeelizer.demos.activities.BaseActivity.UserType;
import com.mobeelizer.demos.utils.UIUtils;
import com.mobeelizer.mobile.android.Mobeelizer;
import com.mobeelizer.mobile.android.api.MobeelizerLoginCallback;
import com.mobeelizer.mobile.android.api.MobeelizerLoginStatus;

/**
 * Activity responsible for obtaining session code from Mobeelizer server. When this operation finishes without an error the user
 * can start using demo functions.
 * 
 * @see MobeelizerLoginCallback
 */
public class CreateSessionCodeActivity extends Activity implements MobeelizerLoginCallback {

    private Button mExploreButton;

    private TextView mTitleBarTitle, mSessionCodeTextView;

    private LinearLayout mCreatingSessionLayout, mSessionCreatedLayout;

    private Dialog mLoginDialog = null;

    private String mSessionCode;

    private CreateSessionTask mCreateSessionTask = null;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_create_session);

        mTitleBarTitle = (TextView) findViewById(R.id.titleBarTitle);
        mSessionCodeTextView = (TextView) findViewById(R.id.createSessionCode);
        mExploreButton = (Button) findViewById(R.id.createSessionExplore);
        mCreatingSessionLayout = (LinearLayout) findViewById(R.id.creatingSession);
        mSessionCreatedLayout = (LinearLayout) findViewById(R.id.sessionCreated);

        mExploreButton.setOnClickListener(getOnExploreClickListenter());
        mTitleBarTitle.setText(R.string.titleBarSessionCode);

        UIUtils.prepareClip(mExploreButton);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        mCreateSessionTask = new CreateSessionTask();
        mCreateSessionTask.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        mCreateSessionTask.cancel(true);
        super.onPause();
    }

    /**
     * Allows to display information or error dialog with specific text to the user. Round brackets contains the {@code id} value
     * required to show each dialog.
     * 
     * <p>
     * The custom dialog ({@code BaseActivity.D_CUSTOM}) can be shown. In this case additional information needs to be provided. <br/>
     * Custom dialog requires reference to {@link Bundle} object with the following fields:<br/>
     * - type of the dialog (information or error) - {@code BaseActivity.IS_INFO} as {@code boolean}<br/>
     * - text to display as resource id - {@code BaseActivity.TEXT_RES_ID} as {@code int}<br/>
     * - text to display as String object - {@code BaseActivity.CUSTOM_TEXT} as {@code String} <br/>
     * <br/>
     * The last two can be used interchangeably but when both are used resource id is preferred.
     */
    @Override
    protected Dialog onCreateDialog(final int id, final Bundle args) {
        Dialog dialog = null;
        TextView text = null;
        Button closeButton = null;

        if (id == BaseActivity.D_CUSTOM && args != null) {
            boolean isInfoDialog = args.getBoolean(BaseActivity.IS_INFO, true);
            int resId = args.getInt(BaseActivity.TEXT_RES_ID, -1);
            String customText = args.getString(BaseActivity.CUSTOM_TEXT);

            dialog = new Dialog(this, R.style.MobeelizerDialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(isInfoDialog ? R.layout.info_dialog : R.layout.error_dialog);
            text = (TextView) dialog.findViewById(R.id.dialogText);
            closeButton = (Button) dialog.findViewById(R.id.dialogButton);

            if (resId != -1) {
                text.setText(resId);
            } else if (customText != null) {
                text.setText(customText);
            } else {
                dialog = null;
            }
        }

        if (dialog != null) {
            final Dialog tmp = dialog;
            closeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View paramView) {
                    tmp.dismiss();
                }
            });
        }

        return dialog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoginFinished(final MobeelizerLoginStatus status) {
        Bundle err;
        // If logging in succeeded show examples list. Otherwise show an error dialog
        switch (status) {
            case OK:
                // start explore activity
                Intent i = new Intent(getApplicationContext(), ExploreActivity.class);
                startActivity(i);
                // and close current one
                finish();
                break;
            case MISSING_CONNECTION_FAILURE:
                err = new Bundle();
                err.putBoolean(BaseActivity.IS_INFO, false);
                err.putInt(BaseActivity.TEXT_RES_ID, R.string.e_missingConnection);
                showDialog(BaseActivity.D_CUSTOM, err);
                break;
            case CONNECTION_FAILURE:
            case AUTHENTICATION_FAILURE:
            case OTHER_FAILURE:
                err = new Bundle();
                err.putBoolean(BaseActivity.IS_INFO, false);
                err.putInt(BaseActivity.TEXT_RES_ID, R.string.e_cannotConnectToSession);
                showDialog(BaseActivity.D_CUSTOM, err);
                break;
        }

        if (mLoginDialog != null) {
            mLoginDialog.dismiss();
        }
    }

    // =====================================================================================
    // ================================= ONCLICKS ==========================================
    // =====================================================================================

    /**
     * Returns {@link View.OnClickListener} for "Explore" button. When the button has been clicked {@link ExploreActivity} is
     * displayed.
     * 
     * @see ExploreActivity
     */
    private View.OnClickListener getOnExploreClickListenter() {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                mLoginDialog = new Dialog(CreateSessionCodeActivity.this, R.style.MobeelizerDialogTheme);
                mLoginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mLoginDialog.setContentView(R.layout.progress_dialog);
                ((TextView) mLoginDialog.findViewById(R.id.dialogText)).setText(R.string.loggingIn);
                mLoginDialog.setCancelable(false);
                mLoginDialog.show();

                Mobeelizer.login(mSessionCode, // session code
                        getString(R.string.c_userALogin), // the user - A
                        getString(R.string.c_userAPassword), // user password
                        CreateSessionCodeActivity.this); // MobeelizerLoginCallback
            }
        };
    }

    // =====================================================================================
    // =============================== ASYNC TASKS =========================================
    // =====================================================================================

    /**
     * Creates an {@link AsyncTask} for obtaining session code from Mobeelizer server.
     */
    private class CreateSessionTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            mCreatingSessionLayout.setVisibility(View.VISIBLE);
            mSessionCreatedLayout.setVisibility(View.GONE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String doInBackground(final Void... params) {
            try {
                URL url = new URL(getString(R.string.c_apiURL));
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(getResources().getInteger(R.integer.c_connectionTimeout));
                BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
                byte[] buffer = new byte[128];
                int read = bis.read(buffer);
                return new String(buffer, 0, read);
            } catch (SocketTimeoutException e) {
                // if timeout occurs show error dialog
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Bundle b = new Bundle();
                        b.putBoolean(BaseActivity.IS_INFO, false);
                        b.putInt(BaseActivity.TEXT_RES_ID, R.string.e_cannotCreateSession);
                        showDialog(BaseActivity.D_CUSTOM, b);
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(final String result) {
            if (result != null) {
                mSessionCode = result;
                mSessionCodeTextView.setText(result);

                // save obtained session code and user A login to automatically log him in
                // on the next start of the application
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sp.edit().putString(BaseActivity.SESSION_CODE, result).putString(BaseActivity.USER_TYPE, UserType.A.name())
                        .commit();

                // change the visible view elements to those displaying session code
                mCreatingSessionLayout.setVisibility(View.GONE);
                mSessionCreatedLayout.setVisibility(View.VISIBLE);
            }
        }

    }
}
