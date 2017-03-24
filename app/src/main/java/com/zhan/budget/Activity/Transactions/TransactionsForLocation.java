package com.zhan.budget.Activity.Transactions;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.Date;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForLocation extends BaseTransactions {

    private Location location;

    public static final String ALL_TRANSACTION_FOR_LOCATION = "All Transactions For Location";

    public static Intent createIntentToViewAllTransactionsForLocationForMonth(Context context, Location location, Date date){
        Intent intent = new Intent(context, TransactionsForLocation.class);
        intent.putExtra(ALL_TRANSACTION_FOR_DATE, date);

        Parcelable wrapped = Parcels.wrap(location);
        intent.putExtra(ALL_TRANSACTION_FOR_LOCATION, wrapped);

        return intent;
    }

    @Override
    protected void getDifferentData(){
        location = Parcels.unwrap((getIntent().getExtras()).getParcelable(ALL_TRANSACTION_FOR_LOCATION));
        updateTitleName(location.getName());
        updateEmptyListText(String.format(getString(R.string.empty_transaction_location_date), location.getName(), DateUtil.convertDateToStringFormat2(getApplicationContext(), beginMonth)));
    }

    @Override
    protected void getAllTransactionsForMonth(){
        Log.d("DEBUG", "getAllTransactionsWithAccountForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        transactionsForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("location.name", location.getName()).equalTo("dayType", DayType.COMPLETED.toString()).findAllSortedAsync("date");
        transactionsForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionList = myRealm.copyFromRealm(element);
                float total = CurrencyTextFormatter.findTotalCostForTransactions(transactionList);
                
                transactionAdapter = new TransactionRecyclerAdapter(instance, transactionList, true); //display date in each transaction item
                transactionListView.setAdapter(transactionAdapter);

                Log.d("ZHAN", "there are " + transactionList.size() + " transactions in this account " + location + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);

                //update balance
                updateTitleBalance(CurrencyTextFormatter.formatFloat(total));

                updateTransactionStatus();
            }
        });
    }

    @Override
    protected void updateMonthInToolbar(int direction){
        beginMonth = DateUtil.getMonthWithDirection(beginMonth, direction);

        //Need to go a day before as Realm's between date does inclusive on both end
        endMonth = DateUtil.getLastDateOfMonth(beginMonth);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(DateUtil.convertDateToStringFormat2(getApplicationContext(), beginMonth));
        }

        getDifferentData();
        getAllTransactionsForMonth();
    }
}
