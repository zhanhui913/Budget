package com.zhan.budget.Activity.Transactions;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.Date;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForCategory extends BaseTransactions {

    private Category selectedCategory;

    public static final String ALL_TRANSACTION_FOR_CATEGORY = "All Transactions For Category";

    public static Intent createIntentToViewAllTransactionsForCategoryForMonth(Context context, Category category, Date date){
        Intent intent = new Intent(context, TransactionsForCategory.class);
        intent.putExtra(ALL_TRANSACTION_FOR_DATE, date);

        Parcelable wrapped = Parcels.wrap(category);
        intent.putExtra(ALL_TRANSACTION_FOR_CATEGORY, wrapped);

        return intent;
    }

    @Override
    protected void getDifferentData(){
        selectedCategory = Parcels.unwrap((getIntent().getExtras()).getParcelable(ALL_TRANSACTION_FOR_CATEGORY));
        updateTitleName(selectedCategory.getName());
        updateEmptyListText(String.format(getString(R.string.empty_transaction_category_date), selectedCategory.getName(), DateUtil.convertDateToStringFormat2(getApplicationContext(), beginMonth)));
    }

    @Override
    protected void getAllTransactionsForMonth(){
        Log.d("TransactionsForCategory", "getAllTransactionsWithCategoryForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        transactionsForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("category.id", selectedCategory.getId()).equalTo("dayType", DayType.COMPLETED.toString()).findAllSortedAsync("date");
        transactionsForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionList = myRealm.copyFromRealm(element);
                double total = CurrencyTextFormatter.findTotalCostForTransactions(transactionList);

                transactionAdapter = new TransactionRecyclerAdapter(instance, transactionList, true); //display date in each transaction item
                transactionListView.setAdapter(transactionAdapter);

                Log.d("TransactionsForCategory", "there are " + transactionList.size() + " transactions in this category " + selectedCategory.getName() + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("TransactionsForCategory", "total sum is "+total);

                //update balance
                updateTitleBalance(total);
                updateTransactionStatus();
            }
        });
    }
}
