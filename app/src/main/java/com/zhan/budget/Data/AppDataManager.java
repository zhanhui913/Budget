package com.zhan.budget.Data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zhan.budget.Data.Prefs.PreferenceHelper;
import com.zhan.budget.Data.Realm.RealmHelper;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by zhanyap on 2017-04-19.
 */

public class AppDataManager implements DataManager{
    private static final String TAG = "AppDataManager";

    private static AppDataManager INSTANCE = null;

    private final Context mContext;


    // Prevent direct instantiation.
    private AppDataManager(@NonNull Context context) {
        mContext = context;
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
    public void getTransactions(Date date, @NonNull final LoadTransactionsForDayCallback callback){
        Util.checkNotNull(callback);

        final Date startDate = DateUtil.refreshDate(date);
        final Date endDate = DateUtil.getNextDate(date);

        final Realm myRealm = Realm.getDefaultInstance();
        final RealmResults<Transaction> resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", startDate).lessThan("date", endDate).findAllAsync();
        resultsTransactionForDay.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                resultsTransactionForDay.removeChangeListener(this);

                element.removeChangeListener(this);

                Log.d(TAG, "received " + element.size() + " transactions");

                //double sumValue = CurrencyTextFormatter.findTotalCostForTransactions(resultsTransactionForDay);

                if(resultsTransactionForDay.size() > 0){
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
