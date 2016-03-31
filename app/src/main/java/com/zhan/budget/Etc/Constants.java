package com.zhan.budget.Etc;

import java.util.Locale;

/**
 * Created by zhanyap on 15-12-21.
 */
public class Constants {
    public static final String NAME = "Budget";
    public static final String REALM_NAME = NAME + ".realm";
    public static final String CSV_NAME = NAME + ".csv";

    //Locale
    public static final Locale BUDGET_LOCALE = Locale.CANADA;

    //Permissions for Android M
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    //First time
    public static final String FIRST_TIME = "First Time";

    //Default theme
    public static final String DEFAULT_THEME = "Default Theme";

    //Last backup
    public static final String LAST_BACKUP = "Last Backup";

    //Starting fragment in MainActivity
    public static final String START_FRAGMENT = "Starting Fragment";

    //Start day of calendar (Sunday or Monday)
    public static final String START_DAY_CALENDAR = "Start Day of Calendar";

    //Activity request
    public static final String REQUEST_NEW_TRANSACTION = "New Transaction";
    public static final String REQUEST_NEW_TRANSACTION_DATE = "New Transaction Date";
    public static final String REQUEST_EDIT_TRANSACTION = "Edit Transaction";

    public static final String REQUEST_NEW_CATEGORY = "New Category";
    public static final String REQUEST_NEW_CATEGORY_TYPE = "New Category Type";
    public static final String REQUEST_EDIT_CATEGORY = "Edit Category";
    public static final String REQUEST_ALL_TRANSACTION_FOR_CATEGORY_MONTH = "Request all transaction for category month";
    public static final String REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY = "Request all transaction for category category";
    public static final String REQUEST_NEW_OVERVIEW_MONTH = "New Overview Month";

    //Return result
    public static final int RETURN_NEW_TRANSACTION = 1;
    public static final int RETURN_EDIT_TRANSACTION = 2;
    public static final int RETURN_NEW_CATEGORY = 3;
    public static final int RETURN_EDIT_CATEGORY = 4;
    public static final int RETURN_NEW_OVERVIEW = 5;

    public static final String RESULT_NEW_TRANSACTION = "New Transaction Result";
    public static final String RESULT_EDIT_TRANSACTION = "Edit Transaction Result";
    public static final String RESULT_NEW_CATEGORY = "New Category Result";
    public static final String RESULT_EDIT_CATEGORY = "Edit Category Result";
    public static final String RESULT_SCHEDULE_TRANSACTION = "ScheduledTransaction";
}
