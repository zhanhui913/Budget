package com.zhan.budget.Data.Realm;

import com.zhan.budget.Model.Realm.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhanyap on 2017-05-10.
 */

public class TransactionCaller implements RealmHelper.LoadTransactionsCallback {

    private final TransactionDAO transactionDAO;
    private List<Transaction> transactionList = new ArrayList<Transaction>();

    public TransactionCaller(TransactionDAO transactionDOA){
        this.transactionDAO = transactionDOA;
    }

    public void getTransactions(Date date){
        transactionDAO.getTransactions(date, this);
    }

    public List<Transaction> getTransactionResults(){
        return this.transactionList;
    }

    //interfaces

    @Override
    public void onTransactionsLoaded(List<Transaction> list){
        this.transactionList = list;
    }

    @Override
    public void onDataNotAvailable(){

    }

    @Override
    public void onFail(){}
}
