package com.zhan.budget.Data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by zhanyap on 2017-04-19.
 */

public class AppDataManager implements DataManager{
    private static final String TAG = "AppDataManager";

    private static AppDataManager INSTANCE = null;

    private final Context mContext;

    private final Realm myRealm;

    // Prevent direct instantiation.
    private AppDataManager(@NonNull Context context) {
        mContext = context;
        myRealm = Realm.getDefaultInstance();
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param context the context
     * @return the {@link AppDataManager} instance
     */
    public static AppDataManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppDataManager(context);
        }
        return INSTANCE;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm Helper
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void getTransactions(Date date, @NonNull final LoadTransactionsCallback callback){
        Util.checkNotNull(callback);

        Date startDate = DateUtil.refreshDate(date);
        Date endDate = DateUtil.getNextDate(date);

        final RealmResults<Transaction> resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", startDate).lessThan("date", endDate).findAllAsync();
        resultsTransactionForDay.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                resultsTransactionForDay.removeChangeListener(this);
                element.removeChangeListener(this);

                Log.d(TAG, "received " + element.size() + " transactions");

                if(element.size() > 0){
                    callback.onTransactionsLoaded(myRealm.copyFromRealm(element));
                }else{
                    callback.onDataNotAvailable();
                }
            }
        });
    }

    @Override
    public void approveTransaction(String id, @NonNull final LoadTransactionCallback callback){
        Util.checkNotNull(callback);

        final Transaction transaction = myRealm.where(Transaction.class).equalTo("id", id).findFirstAsync();
        transaction.addChangeListener(new RealmChangeListener<RealmObject>() {
            @Override
            public void onChange(RealmObject element) {
                transaction.removeChangeListener(this);

                //According to the documentation of findFirstAsync()
                //If isLoaded() is true and isValid() is false => 0 results
                if(element.isLoaded() && !element.isValid()){
                    callback.onDataNotAvailable();
                }else{
                    myRealm.beginTransaction();
                    ((Transaction) element).setDayType(DayType.COMPLETED.toString());
                    myRealm.commitTransaction();

                    callback.onTransactionLoaded(element);
                }
            }
        });

    }

    @Override
    public void unapproveTransaction(String id, @NonNull final LoadTransactionCallback callback){
        Util.checkNotNull(callback);

        final Transaction transaction = myRealm.where(Transaction.class).equalTo("id", id).findFirstAsync();
        transaction.addChangeListener(new RealmChangeListener<RealmObject>() {
            @Override
            public void onChange(RealmObject element) {
                transaction.removeChangeListener(this);

                //According to the documentation of findFirstAsync()
                //If isLoaded() is true and isValid() is false => 0 results
                if(element.isLoaded() && !element.isValid()){
                    callback.onDataNotAvailable();
                }else{
                    myRealm.beginTransaction();
                    ((Transaction) element).setDayType(DayType.SCHEDULED.toString());
                    myRealm.commitTransaction();

                    callback.onTransactionLoaded(element);
                }
            }
        });
    }

    @Override
    public void getScheduledTransactions(@NonNull final LoadTransactionsCallback callback){
        Util.checkNotNull(callback);

        final RealmResults<Transaction> scheduledTransactions = myRealm.where(Transaction.class).equalTo("dayType", DayType.SCHEDULED.toString()).findAllAsync();
        scheduledTransactions.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                scheduledTransactions.removeChangeListener(this);
                element.removeChangeListener(this);

                Log.d(TAG, "received " + element.size() + " scheduled transactions");

                if(element.size() > 0){
                    callback.onTransactionsLoaded(myRealm.copyFromRealm(element));
                }else{
                    callback.onDataNotAvailable();
                }
            }
        });
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
