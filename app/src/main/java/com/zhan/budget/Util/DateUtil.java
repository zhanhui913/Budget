package com.zhan.budget.Util;

import android.content.Context;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by zhanyap on 2016-03-02.
 */
public final class DateUtil {
    private DateUtil(){}

    public static Date convertStringToDate(Context context, String stringDate){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", context.getResources().getConfiguration().locale);
        Date date = null;
        try{
            date = formatter.parse(stringDate);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return date;
    }

    public static String convertDateToString(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", context.getResources().getConfiguration().locale);
        return formatter.format(date);
    }

    public static Date formatDate(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", context.getResources().getConfiguration().locale);

        String dateString = convertDateToString(context, date);
        Date newDate = null;

        try{
            newDate = formatter.parse(dateString);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return newDate;
    }

    public static String convertDateToStringFormat1(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d", context.getResources().getConfiguration().locale);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat2(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM yyyy", context.getResources().getConfiguration().locale);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat3(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy", context.getResources().getConfiguration().locale);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat4(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM", context.getResources().getConfiguration().locale);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat5(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd", context.getResources().getConfiguration().locale);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat6(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM_d_yyyy_hh_mm_a", context.getResources().getConfiguration().locale);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat7(Context context, Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy hh:mm a", context.getResources().getConfiguration().locale);
        return formatter.format(date);
    }

    public static String convertLongToStringFormat(Context context, long value){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy hh:mm a", context.getResources().getConfiguration().locale);
        return formatter.format(value);
    }

    /**
     * Refreshes the date to set the time component of date to 00:00:00
     * @param date Current Date
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
     * @param date Current Date
     * @return date + 1
     */
    public static Date getNextDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);

        return refreshDate(cal.getTime());
    }

    /**
     * Gives the previous date
     * @param date Current Date
     * @return date - 1
     */
    public static Date getPreviousDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);

        return refreshDate(cal.getTime());
    }

    /**
     * Refreshes the month to set the time component of date to 00:00:00
     * @param date Current Date
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
     * @param date Current Date
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
     * @param date Current Date
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
     * @param date Current Date
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
     * Get new date with directions
     * @param date Starting date point
     * @param direction -1, 0, 1 => (previous date, this date, next date)
     * @return date
     */
    public static Date getDateWithDirection(Date date, int direction){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, direction);

        return cal.getTime();
    }

    /**
     * Get new week with directions
     * @param date Starting date point
     * @param direction -1, 0, 1 => (previous week, this week, next week)
     * @return date
     */
    public static Date getWeekWithDirection(Date date, int direction){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.WEEK_OF_MONTH, direction);

        return cal.getTime();
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

    /**
     * Gets the last date of the current month
     * @param date Current Date
     * @return last date of month
     */
    public static Date getLastDateOfMonth(Date date){
        return getPreviousDate(getNextMonth(date));
    }

    /**
     * Gets the last date of the current year
     * @param date Current Date
     * @return last date of year
     */
    public static Date getLastDateOfYear(Date date){
        return getPreviousDate(getNextYear(date));
    }

    /**
     * Gets the string value of the day, useful for localization
     * @param value
     * @return
     */
    public static String getDayOfWeek(int value){
        switch(value){
            case 1:
                return DateFormatSymbols.getInstance().getShortWeekdays()[1];
            case 2:
                return DateFormatSymbols.getInstance().getShortWeekdays()[2];
            case 3:
                return DateFormatSymbols.getInstance().getShortWeekdays()[3];
            case 4:
                return DateFormatSymbols.getInstance().getShortWeekdays()[4];
            case 5:
                return DateFormatSymbols.getInstance().getShortWeekdays()[5];
            case 6:
                return DateFormatSymbols.getInstance().getShortWeekdays()[6];
            case 7:
                return DateFormatSymbols.getInstance().getShortWeekdays()[7];
            default:
                return DateFormatSymbols.getInstance().getShortWeekdays()[1];
        }
    }
}
