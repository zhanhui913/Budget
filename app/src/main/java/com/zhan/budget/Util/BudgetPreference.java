package com.zhan.budget.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Zhan on 16-03-25.
 */
public final class BudgetPreference {

    //First time
    public static final String FIRST_TIME = "First Time";

    //First time Currency
    public static final String FIRST_TIME_CURRENCY = "First Time Currency";

    //Default theme
    public static final String DEFAULT_THEME = "Default Theme";

    //Auto backup
    public static final String ALLOW_AUTO_BACKUP = "Allow auto backup";

    //Last backup
    public static final String LAST_BACKUP = "Last Backup";

    //Start day of calendar (Sunday or Monday)
    public static final String START_DAY_CALENDAR = "Start Day of Calendar";


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
        setPreferenceBoolean(context, FIRST_TIME, true);
    }

    public static boolean getFirstTime(Context context){
        return getPreferenceBoolean(context, FIRST_TIME, true);
    }

    public static void setFirstTime(Context context){
        setPreferenceBoolean(context, FIRST_TIME, false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // First time Currency functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void resetFirstTimeCurrency(Context context){
        setPreferenceBoolean(context, FIRST_TIME_CURRENCY, true);
    }

    public static boolean getFirstTimeCurrency(Context context){
        return getPreferenceBoolean(context, FIRST_TIME_CURRENCY, true);
    }

    public static void setFirstTimeCurrency(Context context){
        setPreferenceBoolean(context, FIRST_TIME_CURRENCY, false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Auto backup functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean getAllowAutoBackup(Context context){
        return getPreferenceBoolean(context, ALLOW_AUTO_BACKUP, false);
    }

    public static void setAllowAutoBackup(Context context, boolean val){
        setPreferenceBoolean(context, ALLOW_AUTO_BACKUP, val);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Last backup functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getLastBackup(Context context){
        return getPreferenceString(context, LAST_BACKUP, "None");
    }

    public static void setLastBackup(Context context, String value){
        setPreferenceString(context, LAST_BACKUP, value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Theme mode functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void setCurrentTheme(Context context, int theme){
        setPreferenceInt(context, DEFAULT_THEME, theme);
    }

    public static int getCurrentTheme(Context context){
        return getPreferenceInt(context, DEFAULT_THEME, ThemeUtil.THEME_LIGHT);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Starting day of week functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void setSundayStartDay(Context context){
        setPreferenceInt(context, START_DAY_CALENDAR, Calendar.SUNDAY);
    }

    public static void setMondayStartDay(Context context){
        setPreferenceInt(context, START_DAY_CALENDAR, Calendar.MONDAY);
    }

    public static int getStartDay(Context context){
        return getPreferenceInt(context, START_DAY_CALENDAR, Calendar.SUNDAY);
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
