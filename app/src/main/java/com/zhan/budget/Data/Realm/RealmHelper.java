package com.zhan.budget.Data.Realm;

import android.support.annotation.NonNull;

import com.zhan.budget.Model.Realm.Transaction;

import java.util.Date;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by zhanyap on 2017-04-19.
 */

public interface RealmHelper {

    interface LoadTransactionsForDayCallback{
        void onTransactionsLoaded(List<Transaction> list);

        void onDataNotAvailable();
    }

    void getTransactions(Date date, @NonNull LoadTransactionsForDayCallback callback);
}
