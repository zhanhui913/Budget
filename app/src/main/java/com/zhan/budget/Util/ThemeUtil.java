package com.zhan.budget.Util;

import android.app.Activity;
import android.content.Intent;
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
        BudgetPreference.setCurrentTheme(activity, theme);

        Log.d("THEME_COLOR_DEBUG", "changing theme to " + theme);

        Intent intent = new Intent(activity, activity.getClass());
        intent.putExtra(Constants.REQUEST_CHANGE_THEME, true);

        activity.finish();
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity) {

        int sTheme = BudgetPreference.getCurrentTheme(activity);

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
}