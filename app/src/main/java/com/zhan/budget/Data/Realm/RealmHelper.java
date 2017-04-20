package com.zhan.budget.Data.Realm;

import android.support.annotation.NonNull;

import com.zhan.budget.Model.Realm.Transaction;

import java.util.Date;
import java.util.List;

import io.realm.RealmObject;

/**
 * Interface that needs to be implemented for Realm db purposes.
 */
public interface RealmHelper {

    interface LoadTransactionsCallback{
        void onTransactionsLoaded(List<Transaction> list);

        void onDataNotAvailable();
    }

    interface LoadTransactionCallback{
        <T extends RealmObject> void onTransactionLoaded(T realmObject);

        void onDataNotAvailable();
    }

    interface DeleteTransactionCallback{
        void onSuccess();

        void onFailed();
    }

    void getTransactions(Date date, @NonNull LoadTransactionsCallback callback);

    void approveTransaction(String id, @NonNull LoadTransactionCallback callback);

    void unapproveTransaction(String id, @NonNull LoadTransactionCallback callback);

    void deleteTransaction(String id, @NonNull DeleteTransactionCallback callback);

    void getScheduledTransactions(@NonNull LoadTransactionsCallback callback);
}
