package com.zhan.budget.Util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.zhan.budget.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
            StringBuilder output = new StringBuilder();
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
     * If string is null, then return empty string. Otherwise return the string value
     * @param value The string to check if null or not
     * @return String value
     */
    public static String checkNull(String value){
        return (Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(value)) ? value : "" ;
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

    public static int getScreenHeight(Activity activity){
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getScreenWidth(Activity activity){
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static char getFirstCharacterFromString(String value){
        return value.toCharArray()[0];
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Snackbar
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void createSnackbar(Context context, View v, String value){
        Snackbar snackbar = Snackbar.make(v, value, Snackbar.LENGTH_SHORT);

        View sbView = snackbar.getView();

        // Change background color
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));

        // Changing message text color
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.white));
        snackbar.show();
    }
}
