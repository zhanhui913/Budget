package com.zhan.budget.Data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zhan.budget.Data.Prefs.PreferenceHelper;
import com.zhan.budget.Model.Realm.Transaction;

import java.util.Date;

import io.realm.RealmResults;

/**
 * Created by zhanyap on 2017-04-19.
 */

public class AppDataManager implements DataManager{
    private static final String TAG = "AppDataManager";

    private final Context mContext;
    private final PreferenceHelper mPreferenceHelper;

    public AppDataManager(@NonNull Context context, PreferenceHelper preferenceHelper) {
        mContext = context;
        mPreferenceHelper = preferenceHelper;
    }









    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm Helper
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void getTransactions(Date date, @NonNull LoadTransactionsForDayCallback callback){

    }


    @Override
    public void onTransactionsLoaded(RealmResults<Transaction> list){

    }

    @Override
    public void onDataNotAvailable(){

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Preference Helper
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void resetFirstTime(){
        //setPreferenceBoolean(context, FIRST_TIME, true);
    }

    @Override
    public boolean getFirstTime(){
        //return getPreferenceBoolean(context, FIRST_TIME, true);
        return true;
    }

    @Override
    public void setFirstTime(){
        //setPreferenceBoolean(context, FIRST_TIME, false);
    }
}
