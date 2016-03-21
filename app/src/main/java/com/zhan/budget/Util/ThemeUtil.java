package com.zhan.budget.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.R;

/**
 * Created by Zhan on 16-03-17.
 */
public final class ThemeUtil {

    private ThemeUtil() {
    }//private constructor

    public final static int THEME_LIGHT = 0;
    public final static int THEME_DARK = 1;

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme) {
        setDefaultThemePreference(activity, theme);

        Log.d("THEME_COLOR_DEBUG", "changing theme to " + theme);
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity) {

        int sTheme = getCurrentThemePreference(activity);

        Log.d("THEME_COLOR_DEBUG", "current theme is " + sTheme);
        switch (sTheme) {
            default:
            case THEME_LIGHT:
                activity.setTheme(R.style.AppThemeLight_NoActionBar);
                break;
            case THEME_DARK:
                activity.setTheme(R.style.AppThemeDark_NoActionBar);
                break;
        }
    }

    private static void setDefaultThemePreference(Activity activity, int theme){
        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        Log.d("THEME_UTIL", "setDefaultThemePreference : "+theme);
        //set Constants.DEFAULT_THEME shared preferences to whatever the current theme is
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.DEFAULT_THEME, theme);
        editor.apply();
    }

    public static int getCurrentThemePreference(Activity activity){
        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        int retrievedTheme = sharedPreferences.getInt(Constants.DEFAULT_THEME, THEME_LIGHT);
        Log.d("THEME_UTIL", "getCurrentThemePreference : "+retrievedTheme);
        return retrievedTheme;
    }
}