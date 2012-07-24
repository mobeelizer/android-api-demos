//
// PhotoSyncActivity.java
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.mobeelizer.demos.R;
import com.mobeelizer.demos.adapters.FileSyncAdapter;
import com.mobeelizer.demos.custom.EntityState;
import com.mobeelizer.demos.model.FileSyncEntity;
import com.mobeelizer.demos.utils.DataUtil;
import com.mobeelizer.demos.utils.UIUtils;
import com.mobeelizer.java.api.MobeelizerFile;
import com.mobeelizer.mobile.android.Mobeelizer;
import com.mobeelizer.mobile.android.api.MobeelizerLoginCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncCallback;
import com.mobeelizer.mobile.android.api.MobeelizerSyncStatus;

/**
 * Activity responsible for "File Sync" example. It allows the user to add a photo from built in camera, save it to local database
 * and synchronize with Mobeelizer server. This picture can be then downloaded by oder users connected to the same session.
 * 
 * @see BaseActivity
 * @see MobeelizerLoginCallback
 */
public class PhotoSyncActivity extends BaseActivity<FileSyncEntity> implements OnItemClickListener, MobeelizerSyncCallback {

    private static final int TAKE_PHOTO = 0x100;

    private static final int CHOOSE_PHOTO = 0x101;

    private Button mAddButton, mSyncButton;

    private ImageButton mInfoButton;

    private ListView mList;

    private FileSyncAdapter mAdapter;

    private Dialog mSyncDialog = null;

    @Override
    protected Integer getHelpDialog() {
        return D_PHOTO_SYNC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_list_with_buttons);
        initTitleBarViews();
        setTitleBarTitle(R.string.titleBarPhotoSync);

        mAddButton = (Button) findViewById(R.id.footerAdd);
        mSyncButton = (Button) findViewById(R.id.footerSync);
        mInfoButton = (ImageButton) findViewById(R.id.footerInfo);
        mList = (ListView) findViewById(android.R.id.list);

        mAddButton.setText(R.string.addPhoto);
        mAddButton.setOnClickListener(getOnAddClickListener());
        mSyncButton.setOnClickListener(getOnSyncClickListener());
        mInfoButton.setOnClickListener(getOnInfoClickListener());

        // clip the add button drawable
        UIUtils.prepareClip(mAddButton);
        // clip the sync button drawable
        UIUtils.prepareClip(mSyncButton);

        List<FileSyncEntity> data = Mobeelizer.getDatabase().list(FileSyncEntity.class);

        mAdapter = new FileSyncAdapter(this, data);
        mAdapter.sort(new FileSyncEntity());
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if ((requestCode == TAKE_PHOTO || requestCode == CHOOSE_PHOTO) && resultCode == RESULT_OK) {
            MobeelizerFile photo;

            File photoFile = null;
            try {
                // get the photo file from external storage
                photoFile = new File(getExternalFilesDir(null), "tmp_photo");

                String filePath = null;
                if (requestCode == TAKE_PHOTO) {
                    filePath = photoFile.getAbsolutePath();
                } else {
                    Uri chosenImageUri = data.getData();
                    filePath = getRealPathFromURI(chosenImageUri);
                }

                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, opt);
                opt.inJustDecodeBounds = false;
                opt.inSampleSize = (int) Math.ceil(opt.outWidth / 600.0);
                Bitmap scaled = BitmapFactory.decodeFile(filePath, opt);

                Log.d("TEST", "Width: " + scaled.getWidth() + "\tHeight: " + scaled.getHeight());
                photoFile.delete();
                photoFile.createNewFile();
                if (scaled.compress(CompressFormat.JPEG, 92, new BufferedOutputStream(new FileOutputStream(photoFile)))) {
                    scaled.recycle();
                }

                // create database object from scaled bitmap
                photo = Mobeelizer.createFile("photo", new BufferedInputStream(new FileInputStream(photoFile)));
                FileSyncEntity fse = new FileSyncEntity();
                fse.setPhoto(photo);

                // save it to database
                Mobeelizer.getDatabase().save(fse);
                fse.setEntityState(EntityState.NEW_A);
                // and add to listview
                mAdapter.add(fse);
                mAdapter.notifyDataSetChanged();
                mList.smoothScrollToPosition(mList.getCount() - 1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    photoFile.delete();
                } catch (Exception e) {
                }
            }
        }
    }

    private String getRealPathFromURI(final Uri contentUri) {
        // can post image
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
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
                // change current example to Permissions one
                Intent i = new Intent(getApplicationContext(), PermissionsActivity.class);
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
        mAdapter.addAll(Mobeelizer.getDatabase().list(FileSyncEntity.class));
        mAdapter.sort(new FileSyncEntity());
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
                // get newly synchronized items from database
                final List<FileSyncEntity> newList = Mobeelizer.getDatabase().list(FileSyncEntity.class);
                // get old items from list adapter
                final List<FileSyncEntity> oldList = mAdapter.getItems();

                // merge new items to old list and mark them as new,
                // find removed items in old list and mark them as such
                mergeLists(oldList, newList);
                mAdapter.sort(new FileSyncEntity());
                // refresh the list to display animation
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

        if (b != null) {
            PhotoSyncActivity.this.showDialog(BaseActivity.D_CUSTOM, b);
        }
    }

    // =====================================================================================
    // ================================= ONCLICKS ==========================================
    // =====================================================================================

    /**
     * Returns {@link View.OnClickListener} for "Add" button. When the button is clicked application determines whether it runs on
     * the emulator or the actual device and based on that knowledge takes a photo from its resources or using phones camera.
     */
    private View.OnClickListener getOnAddClickListener() {
        return new View.OnClickListener() {

            public void onClick(final View v) {

                CharSequence[] items = { "Camera", "Photo gallery", "Random image" };
                final boolean isEmulator = "google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.PRODUCT);

                if (isEmulator) {
                    items = new CharSequence[] { "Photo gallery", "Random image" };
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose source:");
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, final int item) {
                        if (isEmulator) {
                            if (item == 0) {
                                getImageFromGallery();
                            } else {
                                getRandomImage();
                            }
                        } else {
                            if (item == 0) {
                                getImageFromCamera();
                            } else if (item == 1) {
                                getImageFromGallery();
                            } else {
                                getRandomImage();
                            }
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        };
    }

    private void getImageFromCamera() {
        try {
            // take a photo using built in camera
            File photo = new File(getExternalFilesDir(null), "tmp_photo");
            if (photo.exists()) {
                photo.delete();
            }
            photo.createNewFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            startActivityForResult(intent, TAKE_PHOTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private void getRandomImage() {
        // get a random photo from application resources
        FileSyncEntity fse = new FileSyncEntity();
        MobeelizerFile photo = Mobeelizer.createFile("photo", getResources().openRawResource(DataUtil.getRandomImage()));
        fse.setPhoto(photo);

        // add it to database and display on a list view
        Mobeelizer.getDatabase().save(fse);
        fse.setEntityState(EntityState.NEW_A);
        mAdapter.add(fse);
        mAdapter.sort(new FileSyncEntity());
        mAdapter.notifyDataSetChanged();
        // scroll the list to the last position
        mList.smoothScrollToPosition(mAdapter.getPosition(fse));
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
                mSyncDialog = new Dialog(PhotoSyncActivity.this, R.style.MobeelizerDialogTheme);
                mSyncDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mSyncDialog.setContentView(R.layout.progress_dialog);
                mSyncDialog.setCancelable(false);
                mSyncDialog.show();

                // start synchronization
                Mobeelizer.sync(PhotoSyncActivity.this);
            }
        };
    }

    /**
     * Returns {@link View.OnClickListener} for "Information" button. When the button is clicked the info dialog appears with File
     * Sync help text.
     */
    private View.OnClickListener getOnInfoClickListener() {
        return new View.OnClickListener() {

            public void onClick(final View v) {
                showDialog(D_PHOTO_SYNC);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final MobeelizerFile photo = ((FileSyncEntity) parent.getItemAtPosition(position)).getPhoto();

        // display a dialog with zoomed photo
        Dialog dialog = new Dialog(this, R.style.MobeelizerDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.photo_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        ImageView photoView = (ImageView) dialog.findViewById(R.id.dialogPhoto);
        photoView.setImageDrawable(BitmapDrawable.createFromStream(photo.getInputStream(), photo.getName()));

        dialog.show();
    }
}
