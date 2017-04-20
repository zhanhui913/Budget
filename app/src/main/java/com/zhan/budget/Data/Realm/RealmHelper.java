package com.zhan.budget.Data.Realm;

import android.support.annotation.NonNull;

import com.zhan.budget.Model.Realm.Transaction;

import java.util.Date;

import io.realm.RealmResults;

/**
 * Created by zhanyap on 2017-04-19.
 */

public interface RealmHelper {

    interface LoadTransactionsForDayCallback{
        void onTransactionsLoaded(RealmResults<Transaction> list);

        void onDataNotAvailable();
    }

    void getTransactions(Date date, @NonNull LoadTransactionsForDayCallback callback);
}
