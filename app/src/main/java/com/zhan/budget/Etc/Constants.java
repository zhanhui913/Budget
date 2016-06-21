package com.zhan.budget.Etc;

import java.util.Locale;

/**
 * Created by zhanyap on 15-12-21.
 */
public class Constants {
    public static final String NAME = "Budget";
    public static final String REALM_END = ".realm";
    public static final String REALM_NAME = NAME + REALM_END;
    public static final String CSV_END = ".csv";
    public static final String CSV_NAME = NAME + CSV_END;

    //Locale
    public static final Locale BUDGET_LOCALE = Locale.CANADA;

    //Permissions for Android M
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1; //to create realm backup
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2; //to read realm backup
    public static final int MY_PERMISSIONS_REQUEST_WRITE_CSV = 3; //to create csv

    //First time
    public static final String FIRST_TIME = "First Time";

    //Default theme
    public static final String DEFAULT_THEME = "Default Theme";

    //Last backup
    public static final String LAST_BACKUP = "Last Backup";

    //Start day of calendar (Sunday or Monday)
    public static final String START_DAY_CALENDAR = "Start Day of Calendar";

    //Default account
    public static final String DEFAULT_ACCOUNT = "Default Account";

    //Activity request
    public static final String REQUEST_NEW_TRANSACTION = "New Transaction";
    public static final String REQUEST_NEW_TRANSACTION_DATE = "New Transaction Date";
    public static final String REQUEST_EDIT_TRANSACTION = "Edit Transaction";

    public static final String REQUEST_NEW_CATEGORY = "New Category";
    public static final String REQUEST_NEW_CATEGORY_TYPE = "New Category Type";
    public static final String REQUEST_EDIT_CATEGORY = "Edit Category";
    public static final String REQUEST_NEW_OVERVIEW_MONTH = "New Overview Month";
    public static final String REQUEST_ALL_TRANSACTION_FOR_GENERIC_MONTH = "Request all transaction for generic month";
    public static final String REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY = "Request all transaction for category category";
    public static final String REQUEST_ALL_TRANSACTION_FOR_LOCATION_LOCATION = "Request all transaction for location location";
    public static final String REQUEST_ALL_TRANSACTION_FOR_ACCOUNT_ACCOUNT = "Request all transaction for account account";
    public static final String REQUEST_NEW_ACCOUNT = "New Account";
    public static final String REQUEST_EDIT_ACCOUNT = "Edit Account";

    //Return result
    public static final int RETURN_NEW_TRANSACTION = 1;
    public static final int RETURN_EDIT_TRANSACTION = 2;
    public static final int RETURN_NEW_CATEGORY = 3;
    public static final int RETURN_EDIT_CATEGORY = 4;
    public static final int RETURN_NEW_OVERVIEW = 5;
    public static final int RETURN_CHANGE_LOCATION = 6;
    public static final int RETURN_NEW_ACCOUNT = 7;
    public static final int RETURN_EDIT_ACCOUNT = 8;


    public static final String RESULT_NEW_TRANSACTION = "New Transaction Result";
    public static final String RESULT_EDIT_TRANSACTION = "Edit Transaction Result";
    public static final String RESULT_NEW_CATEGORY = "New Category Result";
    public static final String RESULT_EDIT_CATEGORY = "Edit Category Result";
    public static final String RESULT_SCHEDULE_TRANSACTION = "ScheduledTransaction";
    public static final String RESULT_EDIT_ACCOUNT = "Edit Account Result";
    public static final String RESULT_NEW_ACCOUNT = "New Account Result";
}
