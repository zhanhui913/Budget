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

}
