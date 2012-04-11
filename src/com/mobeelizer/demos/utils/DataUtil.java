//
// DataUtil.java
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

package com.mobeelizer.demos.utils;

import java.util.Random;

import android.content.res.Resources;

import com.mobeelizer.demos.R;

/**
 * Helper class used for generating random data for a number of lists in the application. <br/>
 * All possible values are stored in {@code res/values/movies.xml} file.
 */
public class DataUtil {

    private static Random mRand;

    private static String[] mTitles = null, mDirectors = null;

    private static int[] mImages = new int[] { R.raw.landscape_01, R.raw.landscape_02, R.raw.landscape_03, R.raw.landscape_04,
            R.raw.landscape_05, R.raw.landscape_06, R.raw.landscape_07, R.raw.landscape_08, R.raw.landscape_09,
            R.raw.landscape_10 };

    static {
        mRand = new Random(System.nanoTime());
    }

    /**
     * Generates the title, related director of the movie and its rating. Data is then stored in {@link Movie} class. <br/>
     * {@link android.content.res.Resources Resource} reference is required for first usage to access
     * {@code res/values/movies.xml} file and its data.
     * 
     * @param res
     *            Reference to Android {@link android.content.res.Resources Resources} class.
     * @return Random {@link Movie} object.
     */
    public static Movie getRandomMovie(final Resources res) {
        if (mTitles == null) {
            mTitles = res.getStringArray(R.array.movieTitles);
            mDirectors = res.getStringArray(R.array.movieDirectors);
        }

        int index = mRand.nextInt(mTitles.length);

        Movie m = new Movie();
        m.title = mTitles[index];
        m.director = mDirectors[index];
        m.rating = mRand.nextInt(5) + 1;

        return m;
    }

    /**
     * Returns a resource id for randomly chosen image.
     * 
     * @return Random image resource id.
     */
    public static int getRandomImage() {
        int index = mRand.nextInt(mImages.length);
        return mImages[index];
    }

    // ==========================================================================================
    // ==========================================================================================

    /**
     * Movie class holding generated data (title, director and
     */
    public static class Movie {

        /** Movie title */
        public String title;

        /** Movie director */
        public String director;

        /** The rating from 1 to 5 */
        public int rating;
    }
}
