package com.zhan.budget.Activity.Transactions;

import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForCategory extends BaseTransactions {

    private Category selectedCategory;

    @Override
    protected void getDifferentData(){
        selectedCategory = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY));
        updateTitleName(selectedCategory.getName());
        updateEmptyListText(String.format(getString(R.string.empty_transaction_custom_date), selectedCategory.getName(), DateUtil.convertDateToStringFormat2(getApplicationContext(), beginMonth)));
    }

    @Override
    protected void getAllTransactionsForMonth(){
        Log.d("DEBUG", "getAllTransactionsWithCategoryForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        transactionsForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("category.id", selectedCategory.getId()).equalTo("dayType", DayType.COMPLETED.toString()).findAllSortedAsync("date");
        transactionsForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionList = myRealm.copyFromRealm(element);
                float total = element.sum("price").floatValue();

                transactionAdapter = new TransactionRecyclerAdapter(instance, transactionList, currentCurrency, true); //display date in each transaction item
                transactionListView.setAdapter(transactionAdapter);

                Log.d("ZHAN", "there are " + transactionList.size() + " transactions in this category " + selectedCategory.getName() + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);

                //update balance
                updateTitleBalance(CurrencyTextFormatter.formatFloat(total, currentCurrency));

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
