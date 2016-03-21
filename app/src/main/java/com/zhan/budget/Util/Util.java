package com.zhan.budget.Util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.zhan.budget.Etc.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Created by zhanyap on 15-08-24.
 * Util class that handles Serialization and deserialization of models, file IOs, SharedPreferences,
 * and etc.
 */
public final class Util {

    private Util() {
    }//private constructor

    public static String generateUUID(){
        return UUID.randomUUID().toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Saving / reading file persistent data
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void SaveToFile(String fileName, String content) {
        try {
            //Create directory
            File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Surveyor");
            directory.mkdirs();

            //Save the path as a string value
            String externalStorageDirectory = directory.toString();

            //Create new file if it doesn't exist
            File file = new File(externalStorageDirectory, fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException();
                }
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String ReadFromFile(String fileName) {
        BufferedReader br;
        String response = "";

        try {
            StringBuffer output = new StringBuffer();
            String path = Environment.getExternalStorageDirectory().toString() + "/Surveyor/" + fileName;

            br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            response = output.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public static boolean DoesFileExist(String fileName) {
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/Surveyor", fileName);
        return (file.exists());
    }

    public static String getDataPath() {
        return Environment.getExternalStorageDirectory().toString() + "/Surveyor/";
    }

    public static boolean removeDirectory(File directory) {
        if (directory == null)
            return false;
        if (!directory.exists())
            return true;
        if (!directory.isDirectory())
            return false;

        String[] list = directory.list();

        // Some JVMs return null for File.list() when the
        // directory is empty.
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File entry = new File(directory, list[i]);

                if (entry.isDirectory()) {
                    if (!removeDirectory(entry))
                        return false;
                } else {
                    if (!entry.delete())
                        return false;
                }
            }
        }

        return directory.delete();
    }


    public static void Write(String content) {
        Log.d("ZHAN", content);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Demonstrate checking for String that is not null, not empty, and not white
     * space only using standard Java classes.
     *
     * @param string String to be checked for not null, not empty, and not white
     *               space only.
     * @return {@code true} if provided String is not null, is not empty, and
     * has at least one character that is not considered white space.
     */
    public static boolean isNotNullNotEmptyNotWhiteSpaceOnlyByJava(final String string) {
        return string != null && !string.isEmpty() && !string.trim().isEmpty();
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            Write("Hiding KEYBOARD");
        } else {
            Write("fail at Hiding KEYBOARD");
        }
    }

    public static String setPriceToCorrectDecimalInString(float price){
        BigDecimal d = new BigDecimal(price).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return d.toPlainString();

        //return String.format("%.2f", price);
    }

    public static String setPriceToCorrectDecimalInString(Number price){
        BigDecimal d = new BigDecimal((double)price).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return d.toPlainString();

        //return String.format("%.2f", price);
    }

    public static int getScreenHeight(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getScreenWidth(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static void setStartDayOfWeekPreference(Activity activity, int startDay){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

        if(startDay == 1){
            Log.d("THEME_UTIL", "setStartDayOfWeekPreference : SUNDAY");
        }else if(startDay == 2){
            Log.d("THEME_UTIL", "setStartDayOfWeekPreference : MONDAY");
        }

        //set Constants.START_DAY_CALENDAR shared preferences to whatever the selected start day is
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.START_DAY_CALENDAR, startDay);
        editor.apply();
    }

    public static int getStartDayOfWeekPreference(Activity activity){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        int startDay = sharedPreferences.getInt(Constants.START_DAY_CALENDAR, 1);

        if(startDay == 1){
            Log.d("THEME_UTIL", "getStartDayOfWeekPreference : SUNDAY" );
        }else if(startDay == 2){
            Log.d("THEME_UTIL", "getStartDayOfWeekPreference : MONDAY" );
        }

        return startDay;
    }
}
