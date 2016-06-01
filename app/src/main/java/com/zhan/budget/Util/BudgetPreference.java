package com.zhan.budget.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zhan.budget.Etc.Constants;

import java.util.Calendar;

/**
 * Created by Zhan on 16-03-25.
 */
public final class BudgetPreference {

    private BudgetPreference(){}

//testing for realm cache closing

    public static void resetRealmCache(Context context){
        setPreferenceInt(context, "RealmCache", 0);
    }

    public static int getRealmCache(Context context){
        return getPreferenceInt(context, "RealmCache", 0);
    }

    public static void addRealmCache(Context context){
        setPreferenceInt(context, "RealmCache", getRealmCache(context) + 1);
        Log.d("REALMZ1", "Adding realmCachez to "+getRealmCache(context));
    }

    public static void removeRealmCache(Context context){
        setPreferenceInt(context, "RealmCache", getRealmCache(context) - 1);
        Log.d("REALMZ1", "removing realmCachez to "+getRealmCache(context));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // First time functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void resetFirstTime(Context context){
        setPreferenceBoolean(context, Constants.FIRST_TIME, true);
    }

    public static boolean getFirstTime(Context context){
        return getPreferenceBoolean(context, Constants.FIRST_TIME, true);
    }

    public static void setFirstTime(Context context){
        setPreferenceBoolean(context, Constants.FIRST_TIME, false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Last backup functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLastBackup(Context context){
        return getPreferenceString(context, Constants.LAST_BACKUP, "None");
    }

    public static void setLastBackup(Context context, String value){
        setPreferenceString(context, Constants.LAST_BACKUP, value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Theme mode functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void setCurrentTheme(Context context, int theme){
        setPreferenceInt(context, Constants.DEFAULT_THEME, theme);
    }

    public static int getCurrentTheme(Context context){
        return getPreferenceInt(context, Constants.DEFAULT_THEME, ThemeUtil.THEME_LIGHT);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Starting day of week functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void setSundayStartDay(Context context){
        setPreferenceInt(context, Constants.START_DAY_CALENDAR, Calendar.SUNDAY);
    }

    public static void setMondayStartDay(Context context){
        setPreferenceInt(context, Constants.START_DAY_CALENDAR, Calendar.MONDAY);
    }

    public static int getStartDay(Context context){
        return getPreferenceInt(context, Constants.START_DAY_CALENDAR, Calendar.SUNDAY);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Helper functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void setPreferenceBoolean(Context context, String key, boolean value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getPreferenceBoolean(Context context, String key, boolean defVal){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defVal);
    }

    public static void setPreferenceInt(Context context, String key, int value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getPreferenceInt(Context context, String key, int defVal){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(key, defVal);
    }

    public static void setPreferenceString(Context context, String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getPreferenceString(Context context, String key, String defVal){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, defVal);
    }
}
