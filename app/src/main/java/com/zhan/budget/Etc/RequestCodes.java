package com.zhan.budget.Etc;

/**
 * Created by zhanyap on 2016-12-29.
 */

public final class RequestCodes {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_AUTO_EXTERNAL_STORAGE = 1; //to create realm auto backup

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2; //to read realm backup

    public static final int MY_PERMISSIONS_REQUEST_WRITE_CSV = 3; //to create csv

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE = 4; //to access orig realm data

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
    // Currency (500 - 600)
    //
    /////

    public static final int SELECTED_CURRENCY = 500;

    /////
    //
    // Etc (600 - 700)
    //
    /////

    public static final int SETTINGS = 600;

}
