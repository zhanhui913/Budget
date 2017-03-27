package com.zhan.budget.Activity.Transactions;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.Date;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForAccount extends BaseTransactions {

    private Account account;

    public static final String ALL_TRANSACTION_FOR_ACCOUNT = "All Transactions For Account";

    public static Intent createIntentToViewAllTransactionsForAccountForMonth(Context context, Account account, Date date){
        Intent intent = new Intent(context, TransactionsForAccount.class);
        intent.putExtra(ALL_TRANSACTION_FOR_DATE, date);

        Parcelable wrapped = Parcels.wrap(account);
        intent.putExtra(ALL_TRANSACTION_FOR_ACCOUNT, wrapped);

        return intent;
    }

    @Override
    protected void getDifferentData(){
        account = Parcels.unwrap((getIntent().getExtras()).getParcelable(ALL_TRANSACTION_FOR_ACCOUNT));
        updateTitleName(account.getName());
        updateEmptyListText(String.format(getString(R.string.empty_transaction_account_date), account.getName(), DateUtil.convertDateToStringFormat2(getApplicationContext(), beginMonth)));
    }

    @Override
    protected void getAllTransactionsForMonth(){
        Log.d("DEBUG", "getAllTransactionsWithAccountForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        transactionsForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("account.id", account.getId()).equalTo("dayType", DayType.COMPLETED.toString()).findAllSortedAsync("date");
        transactionsForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionList = myRealm.copyFromRealm(element);
                double total = CurrencyTextFormatter.findTotalCostForTransactions(transactionList);

                transactionAdapter = new TransactionRecyclerAdapter(instance, transactionList, true); //display date in each transaction item
                transactionListView.setAdapter(transactionAdapter);

                Log.d("ZHAN", "there are " + transactionList.size() + " transactions in this account " + account.getName() + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);

                //update balance
                updateTitleBalance(CurrencyTextFormatter.formatDouble(total));

                if(total > 0){
                    titleBalanceTextView.setTextColor(ContextCompat.getColor(instance, R.color.green));
                }else if(total < 0){
                    titleBalanceTextView.setTextColor(ContextCompat.getColor(instance, R.color.red));
                }else{
                    titleBalanceTextView.setTextColor(Colors.getColorFromAttr(instance, R.attr.themeColorText));
                }

                updateTransactionStatus();
            }
        });
    }
}
