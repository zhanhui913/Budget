package com.zhan.budget.Util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
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

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * @param listView to be resized
     * @return true if the listView is successfully resized, false otherwise
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        if (listView.getAdapter() != null) {
            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = getListViewHeight(listView);
            listView.setLayoutParams(params);
            listView.requestLayout();
            Write("set) HEIGHT OF LIST VIEW IS " + params.height);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets ListView height dynamically based on the height of all items.
     *
     * @param listView to be resized
     * @return int the total height of the list view
     */
    public static int getListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                //Individual items such as image_item.xml, marker_item.xml, and item_transactiontion.xml needs to be relative layout
                if (item instanceof ViewGroup) {
                    item.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                }
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

            int totalHeight = totalItemsHeight + totalDividersHeight;

            Write("get) HEIGHT OF LIST VIEW IS " + totalHeight);
            return totalHeight;
        }
        return 0;
    }

    /**
     * Converting DP to PX
     *
     * @param context Context
     * @param dp      dp to be converted to px
     * @return px
     */
    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
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

    public static boolean checkInternetConnection(Activity activity) {
        final ConnectivityManager conMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Date functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static Date convertStringToDate(String stringDate){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        Date date = null;
        try{
            date = formatter.parse(stringDate);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return date;
    }

    public static String convertDateToString(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        return formatter.format(date);
    }

    public static Date formatDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);

        String dateString = convertDateToString(date);
        Date newDate = null;

        try{
            newDate = formatter.parse(dateString);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return newDate;
    }

    public static String convertDateToStringFormat1(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d", Locale.CANADA);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat2(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM yyyy", Locale.CANADA);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat3(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy", Locale.CANADA);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat4(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM", Locale.CANADA);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat5(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd", Locale.CANADA);
        return formatter.format(date);
    }

    /**
     * Refreshes the date to set the time component of date to 00:00:00
     * @param date
     * @return date with 00:00:00 time component
     */
    public static Date refreshDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DATE);

        return new GregorianCalendar(year, month, day).getTime();
    }

    /**
     * Gives the following date.
     * @param date
     * @return date + 1
     */
    public static Date getNextDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);

        return refreshDate(cal.getTime());
    }

    public static Date getPreviousDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);

        return refreshDate(cal.getTime());
    }

    /**
     * Refreshes the month to set the time component of date to 00:00:00
     * @param date
     * @return month with 00:00:00 time component and date = 1
     */
    public static Date refreshMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        return new GregorianCalendar(year, month, 1).getTime();
    }

    /**
     * Gives the following month.
     * @param date
     * @return date + 1
     */
    public static Date getNextMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(Calendar.MONTH, 1);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        return new GregorianCalendar(year, month, 1).getTime();
    }

    /**
     * Refreshes the year to set the time component of date to 00:00:00
     * @param date
     * @return year with 00:00:00 time component and date = 1, month = 1
     */
    public static Date refreshYear(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);

        return new GregorianCalendar(year, 0, 1).getTime();
    }

    /**
     * Gives the following year.
     * @param date
     * @return date + 1
     */
    public static Date getNextYear(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(Calendar.YEAR, 1);

        int year = cal.get(Calendar.YEAR);

        return new GregorianCalendar(year, 0, 1).getTime();
    }

    public static int getYearFromDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.YEAR);
    }

    public static int getMonthFromDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.MONTH);
    }

    public static int getDateFromDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.DATE);
    }

    /**
     * Get the number of days since the Jan 1
     * @return int
     */
    public static int getDaysFromDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Get new month with directions
     * @param date Starting date point
     * @param direction -1, 0, 1 => (previous month, this month, next month)
     * @return date
     */
    public static Date getMonthWithDirection(Date date, int direction){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, direction);

        return cal.getTime();
    }

    /**
     * Get new year with directions
     * @param date Starting date point
     * @param direction -1, 0, 1 => (previous year, this year, next year)
     * @return date
     */
    public static Date getYearWithDirection(Date date, int direction){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, direction);

        return cal.getTime();
    }
}