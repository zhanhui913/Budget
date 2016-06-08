package com.zhan.budget.Activity.Transactions;

import android.util.Log;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForLocation extends BaseTransactions {

    private String location;

    @Override
    protected void getDifferentData(){
        location = getIntent().getExtras().getString(Constants.REQUEST_ALL_TRANSACTION_FOR_LOCATION_LOCATION);
        updateTitleName(location);
        updateEmptyListText("There is no transaction for '"+location+"' during "+DateUtil.convertDateToStringFormat2(beginMonth));
    }

    @Override
    protected void getAllTransactionsForMonth(){
        Log.d("DEBUG", "getAllTransactionsWithAccountForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        transactionsForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("location", location).findAllSortedAsync("date");
        transactionsForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionList = myRealm.copyFromRealm(element);
                float total = element.sum("price").floatValue();

                transactionAdapter = new TransactionRecyclerAdapter(instance, transactionList, true); //display date in each transaction item
                transactionListView.setAdapter(transactionAdapter);

                //Add divider
                transactionListView.addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(instance)
                                .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                                .build());

                Log.d("ZHAN", "there are " + transactionList.size() + " transactions in this account " + location + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);

                //update balance
                updateTitleBalance(CurrencyTextFormatter.formatFloat(total, Constants.BUDGET_LOCALE));

                updateTransactionStatus();
            }
        });
    }

}
