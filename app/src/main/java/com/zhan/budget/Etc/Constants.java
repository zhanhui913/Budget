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
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE = 4; //to access orig realm data
    public static final int MY_PERMISSIONS_REQUEST_WRITE_AUTO_EXTERNAL_STORAGE = 5; //to create realm auto backup

    //Default account
    //public static final String DEFAULT_ACCOUNT = "Default Account";

    //Default currency Code and currency name
    //public static final String DEFAULT_CURRENCY_CODE = "USD";
    //public static final String DEFAULT_CURRENCY_NAME = "US Dollar";

    //Activity request
    //public static final String REQUEST_NEW_TRANSACTION = "New Transaction";
    //public static final String REQUEST_NEW_TRANSACTION_DATE = "New Transaction Date";
    //public static final String REQUEST_EDIT_TRANSACTION = "Edit Transaction";

    //public static final String REQUEST_NEW_CATEGORY = "New Category";
    //public static final String REQUEST_NEW_CATEGORY_TYPE = "New Category Type";
    //public static final String REQUEST_EDIT_CATEGORY = "Edit Category";
    //public static final String REQUEST_NEW_OVERVIEW_MONTH = "New Overview Month";
   // public static final String REQUEST_ALL_TRANSACTION_FOR_GENERIC_MONTH = "Request all transaction for generic month";
    //public static final String REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY = "Request all transaction for category category";
    //public static final String REQUEST_ALL_TRANSACTION_FOR_LOCATION_LOCATION = "Request all transaction for location location";
    //public static final String REQUEST_ALL_TRANSACTION_FOR_ACCOUNT_ACCOUNT = "Request all transaction for account account";
    //public static final String REQUEST_NEW_ACCOUNT = "New Account";
    //public static final String REQUEST_EDIT_ACCOUNT = "Edit Account";
    //public static final String REQUEST_NEW_LOCATION = "New Location";
    //public static final String REQUEST_EDIT_LOCATION = "Edit Location";
    //public static final String REQUEST_CURRENCY_IN_SETTINGS = "Currency In Settings";
    //public static final String REQUEST_DEFAULT_CURRENCY = "Default Currency";

    //Return result
    //public static final int RETURN_NEW_TRANSACTION = 1;
    //public static final int RETURN_EDIT_TRANSACTION = 2;
    //public static final int RETURN_NEW_CATEGORY = 3;
    //public static final int RETURN_EDIT_CATEGORY = 4;
    //public static final int RETURN_NEW_OVERVIEW = 5;
    //public static final int RETURN_CHANGE_LOCATION = 6;
    //public static final int RETURN_NEW_ACCOUNT = 7;
    //public static final int RETURN_EDIT_ACCOUNT = 8;
    //public static final int RETURN_NEW_LOCATION = 9;
    //public static final int RETURN_EDIT_LOCATION = 10;
    //public static final int RETURN_HAS_CHANGED = 11;//Used to determined if transaction list has changed.
    //public static final int RETURN_SELECTED_CURRENCY = 12;

    //public static final String RESULT_NEW_TRANSACTION = "New Transaction Result";
    //public static final String RESULT_EDIT_TRANSACTION = "Edit Transaction Result";

    //public static final String RESULT_NEW_CATEGORY = "New Category Result";
    //public static final String RESULT_EDIT_CATEGORY = "Edit Category Result";
    //public static final String RESULT_DELETE_CATEGORY = "Delete Category Result";

    //public static final String RESULT_SCHEDULE_TRANSACTION = "ScheduledTransaction";

    //public static final String RESULT_EDIT_ACCOUNT = "Edit Account Result";
    //public static final String RESULT_NEW_ACCOUNT = "New Account Result";
    //public static final String RESULT_DELETE_ACCOUNT = "Delete Account Result";

    //public static final String RESULT_EDIT_LOCATION = "Edit Location Result";
    //public static final String RESULT_NEW_LOCATION = "New Location Result";
    //public static final String RESULT_DELETE_LOCATION = "Delete Location Result";

    //public static final String RESULT_CURRENCY = "Return Currency Result";

    //public static final String CHANGED = "Has Been Changed?";

}
