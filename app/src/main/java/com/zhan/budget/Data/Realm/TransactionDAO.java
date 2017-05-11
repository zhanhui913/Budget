package com.zhan.budget.Data.Realm;

import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


/**
 * Created by zhanyap on 2017-05-10.
 */

public class TransactionDAO {



    public void getTransactions(Date date, final RealmHelper.LoadTransactionsCallback callback){
        Util.checkNotNull(callback);

        Date startDate = DateUtil.refreshDate(date);
        Date endDate = DateUtil.getNextDate(date);


        final Realm myRealm = Realm.getDefaultInstance();
        if(myRealm != null){
            //Log.d(TAG, "Preparing to get transactions for "+startDate.toString());

            final RealmResults<Transaction> resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", startDate).lessThan("date", endDate).findAllAsync();
            resultsTransactionForDay.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
                @Override
                public void onChange(RealmResults<Transaction> element) {
                    resultsTransactionForDay.removeChangeListener(this);
                    element.removeChangeListener(this);

                   // Log.d(TAG, "received " + element.size() + " transactions");

                    if(element.size() > 0){
                        callback.onTransactionsLoaded(myRealm.copyFromRealm(element));
                    }else{
                        callback.onDataNotAvailable();
                    }
                    myRealm.close();
                }
            });
        }else{
            //Log.d(TAG, "realm failed");
            callback.onFail();
            myRealm.close();
        }
    }

    public List<Transaction> getTransactions(Date date){
        Date startDate = DateUtil.refreshDate(date);
        Date endDate = DateUtil.getNextDate(date);

        Realm myRealm = Realm.getDefaultInstance();

        List<Transaction> resultsTransactionForDay = myRealm.copyFromRealm(myRealm.where(Transaction.class).greaterThanOrEqualTo("date", startDate).lessThan("date", endDate).findAllAsync());

        myRealm.close();
        return resultsTransactionForDay;

    }
}
