package com.mobeelizer.demos;

import android.app.Activity;

public class ApplicationStatus {

    private static Activity currentActivity;

    public static boolean isVisible() {
        return currentActivity != null;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void activityPaused() {
        currentActivity = null;
    }

    public static void activityResumed(final Activity activity) {
        currentActivity = activity;
    }

}
