package com.zhan.budget.Etc;

/**
 * Created by zhanyap on 15-12-21.
 */
public class Constants {
    public static final String REALM_NAME = "budget.realm";

    //First time
    public static final String FIRST_TIME = "First Time";

    //Activity request
    public static final String REQUEST_NEW_TRANSACTION = "New Transaction";
    public static final String REQUEST_NEW_TRANSACTION_DATE = "New Transaction Date";
    public static final String REQUEST_NEW_CATEGORY = "New Category";
    public static final String REQUEST_EDIT_CATEGORY = "Edit Category";
    public static final String REQUEST_ALL_TRANSACTION_FOR_CATEGORY_MONTH = "Request all transaction for category month";
    public static final String REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY = "Request all transaction for category category";

    //Return result
    public static final int RETURN_NEW_TRANSACTION = 1;
    public static final int RETURN_EDIT_TRANSACTION = 2;
    public static final int RETURN_NEW_CATEGORY = 3;
    public static final int RETURN_EDIT_CATEGORY = 4;

    public static final String RESULT_NEW_TRANSACTION = "New Transaction Result";
    public static final String RESULT_EDIT_TRANSACTION = "Edit Transaction Result";
    public static final String RESULT_NEW_CATEGORY = "New Category Result";
    public static final String RESULT_EDIT_CATEGORY = "Edit Category Result";
}
