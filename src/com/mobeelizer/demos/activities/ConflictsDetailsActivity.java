//
// ConflictsDetailsActivity.java
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
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.mobeelizer.demos.ApplicationStatus;
import com.mobeelizer.demos.R;

/**
 * Creates the activity used for modifying movie review.
 * 
 * @see ConflictsActivity
 */
public class ConflictsDetailsActivity extends Activity implements View.OnClickListener {

    /**
     * Parameter name for {@link Bundle} object used to pass information about current rating and pass back the new one.
     */
    public static final String RATING = "rating";

    /**
     * Parameter name for {@link Bundle} object used to pass information about position of the item in the list view to request it
     * when the result is returned.
     */
    public static final String POSITION = "position";

    /**
     * Parameter name for {@link Bundle} object used to pass the title of the movie to display in the title bar.
     */
    public static final String TITLE = "title";

    private TextView mTitle;

    private RadioGroup mRadioGroup;

    private TableRow[] mStarChooser;

    private int[] mRadioIds;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_conflicts_details);

        mTitle = (TextView) findViewById(R.id.titleBarTitle);
        mRadioGroup = (RadioGroup) findViewById(R.id.detailsRadioGroup);
        mStarChooser = new TableRow[] { (TableRow) findViewById(R.id.detailsStar1), (TableRow) findViewById(R.id.detailsStar2),
                (TableRow) findViewById(R.id.detailsStar3), (TableRow) findViewById(R.id.detailsStar4),
                (TableRow) findViewById(R.id.detailsStar5) };
        mRadioIds = new int[] { R.id.detailsRStar1, R.id.detailsRStar2, R.id.detailsRStar3, R.id.detailsRStar4,
                R.id.detailsRStar5 };

        findViewById(R.id.titleBarButton).setVisibility(View.GONE);
        for (TableRow tr : mStarChooser) {
            tr.setOnClickListener(this);
        }

        String title = getIntent().getStringExtra(TITLE);
        int rating = getIntent().getIntExtra(RATING, -1);
        if (rating == -1) {
            rating = 1;
        }
        mTitle.setText(title);
        mRadioGroup.check(mRadioIds[rating - 1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        ApplicationStatus.activityResumed(this);
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        ApplicationStatus.activityPaused();
        super.onPause();
    }

    /**
     * {@inheritDoc} <br/>
     * <br/>
     * When the user clicks the desired rating {@link ConflictsDetailsActivity} is finishes and the rating is returned as a
     * result.
     */
    public void onClick(final View v) {
        int newRating = -1;
        switch (v.getId()) {
            case R.id.detailsStar1:
                mRadioGroup.check(R.id.detailsRStar1);
                newRating = 1;
                break;
            case R.id.detailsStar2:
                mRadioGroup.check(R.id.detailsRStar2);
                newRating = 2;
                break;
            case R.id.detailsStar3:
                mRadioGroup.check(R.id.detailsRStar3);
                newRating = 3;
                break;
            case R.id.detailsStar4:
                mRadioGroup.check(R.id.detailsRStar4);
                newRating = 4;
                break;
            case R.id.detailsStar5:
                mRadioGroup.check(R.id.detailsRStar5);
                newRating = 5;
                break;
        }

        if (newRating != -1) {
            Intent i = getIntent();
            i.putExtra(RATING, newRating);
            setResult(Activity.RESULT_OK, i);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }
}
