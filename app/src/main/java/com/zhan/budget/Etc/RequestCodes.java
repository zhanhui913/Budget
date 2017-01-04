package com.zhan.budget.Etc;

/**
 * Created by zhanyap on 2016-12-29.
 */

public final class RequestCodes {
    public static final int REQUEST_PERMISSION_WRITE = 1;

    public static final int REQUEST_PERMISSION_READ = 2;



    /////
    //
    // Transaction (100 - 200)
    //
    /////

    public static final int NEW_TRANSACTION = 100;
    public static final int EDIT_TRANSACTION = 101;
    public static final int HAS_TRANSACTION_CHANGED = 102;

    /////
    //
    // Category (200 - 300)
    //
    /////

    public static final int NEW_CATEGORY = 200;
    public static final int EDIT_CATEGORY = 201;

    /////
    //
    // Account (300 - 400)
    //
    /////

    public static final int NEW_ACCOUNT = 300;
    public static final int EDIT_ACCOUNT = 301;

    /////
    //
    // Location (400 - 500)
    //
    /////
    

    public static final int NEW_LOCATION = 400;
    public static final int EDIT_LOCATION = 401;

    /////
    //
    // Overview (500 - 600)
    //
    /////

    /////
    //
    // Currency (600 - 700)
    //
    /////

    public static final int SELECTED_CURRENCY = 600;

    /////
    //
    // Etc (700 - 800)
    //
    /////

    public static final int SETTINGS = 700;

}
