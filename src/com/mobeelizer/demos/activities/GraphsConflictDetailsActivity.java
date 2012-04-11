//
// GraphsConflictDetailsActivity.java
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.mobeelizer.demos.R;

/**
 * Creates the activity used for modifying order status.
 */
public class GraphsConflictDetailsActivity extends Activity implements OnClickListener {

    /**
     * Parameter name for {@link Bundle} object used to pass information about id of the item in the expandable list view to
     * request it when the result is returned.
     */
    public static final String ID = "id";

    /**
     * Parameter name for {@link Bundle} object used to pass the title of the movie to display in the title bar.
     */
    public static final String TITLE = "title";

    /**
     * Parameter name for {@link Bundle} object used to pass information about current status and pass back the new one.
     */
    public static final String STATUS = "state";

    private TextView mTitle;

    private RadioGroup mRadioGroup;

    private TableRow[] mStatusChooser;

    private int[] mRadioIds;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_graphs_conflict_details);

        mTitle = (TextView) findViewById(R.id.titleBarTitle);
        mRadioGroup = (RadioGroup) findViewById(R.id.detailsRadioGroup);
        mStatusChooser = new TableRow[] { (TableRow) findViewById(R.id.detailsStatus1),
                (TableRow) findViewById(R.id.detailsStatus2), (TableRow) findViewById(R.id.detailsStatus3),
                (TableRow) findViewById(R.id.detailsStatus4), (TableRow) findViewById(R.id.detailsStatus5) };
        mRadioIds = new int[] { R.id.detailsRStatus1, R.id.detailsRStatus2, R.id.detailsRStatus3, R.id.detailsRStatus4,
                R.id.detailsRStatus5 };

        findViewById(R.id.titleBarButton).setVisibility(View.GONE);
        for (TableRow tr : mStatusChooser) {
            tr.setOnClickListener(this);
        }

        String title = getIntent().getStringExtra(TITLE);
        int rating = getIntent().getIntExtra(STATUS, -1);
        if (rating == -1) {
            rating = 1;
        }
        mTitle.setText(title);
        mRadioGroup.check(mRadioIds[rating - 1]);
    }

    /**
     * {@inheritDoc} <br/>
     * <br/>
     * When the user clicks the desired status {@link GraphsConflictDetailsActivity} finishes and the rating is returned as a
     * result.
     */
    @Override
    public void onClick(final View v) {
        int newStatus = -1;
        switch (v.getId()) {
            case R.id.detailsStatus1:
                mRadioGroup.check(R.id.detailsRStatus1);
                newStatus = 1;
                break;
            case R.id.detailsStatus2:
                mRadioGroup.check(R.id.detailsRStatus2);
                newStatus = 2;
                break;
            case R.id.detailsStatus3:
                mRadioGroup.check(R.id.detailsRStatus3);
                newStatus = 3;
                break;
            case R.id.detailsStatus4:
                mRadioGroup.check(R.id.detailsRStatus4);
                newStatus = 4;
                break;
            case R.id.detailsStatus5:
                mRadioGroup.check(R.id.detailsRStatus5);
                newStatus = 5;
                break;
        }

        if (newStatus != -1) {
            Intent i = getIntent();
            i.putExtra(STATUS, newStatus);
            setResult(Activity.RESULT_OK, i);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }
}
