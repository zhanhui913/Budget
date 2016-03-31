package com.zhan.budget.Util;

import com.zhan.budget.Etc.Constants;

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

    public static Date convertStringToDate(String stringDate){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Constants.BUDGET_LOCALE);
        Date date = null;
        try{
            date = formatter.parse(stringDate);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return date;
    }

    public static String convertDateToString(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Constants.BUDGET_LOCALE);
        return formatter.format(date);
    }

    public static Date formatDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Constants.BUDGET_LOCALE);

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
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d", Constants.BUDGET_LOCALE);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat2(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM yyyy", Constants.BUDGET_LOCALE);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat3(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy", Constants.BUDGET_LOCALE);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat4(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM", Constants.BUDGET_LOCALE);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat5(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd", Constants.BUDGET_LOCALE);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat6(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM_d_yyyy_hh_mm_a", Constants.BUDGET_LOCALE);
        return formatter.format(date);
    }

    public static String convertDateToStringFormat7(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy hh:mm a", Constants.BUDGET_LOCALE);
        return formatter.format(date);
    }

    public static String convertLongToStringFormat(long value){
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy hh:mm a", Constants.BUDGET_LOCALE);
        return formatter.format(value);
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
}
