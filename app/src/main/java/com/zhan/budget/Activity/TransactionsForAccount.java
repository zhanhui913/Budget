package com.zhan.budget.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.ScheduledTransaction;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForAccount extends BaseRealmActivity implements
        TransactionRecyclerAdapter.OnTransactionAdapterInteractionListener{

    private static final String TAG = "TransactionsForAccount";
    private Activity instance;
    private Toolbar toolbar;
    private Date beginMonth, endMonth;
    private String account, accountId;
    private TextView accountTextView, costTextView;
    private RecyclerView accountListView;
    private TransactionRecyclerAdapter transactionAccountAdapter;
    private List<Transaction> transactionAccountList;
    private RealmResults<Transaction> transactionsForAccountForMonth;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_transactions_for_account;
    }

    @Override
    protected void init(){
        super.init();

        //Get intents from caller activity
        beginMonth = DateUtil.refreshMonth(DateUtil.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_ALL_TRANSACTION_FOR_ACCOUNT_MONTH)));
        account = getIntent().getExtras().getString(Constants.REQUEST_ALL_TRANSACTION_FOR_ACCOUNT_ACCOUNT);
        accountId = getIntent().getExtras().getString(Constants.REQUEST_ALL_TRANSACTION_FOR_ACCOUNT_ID);

        instance = this;

        endMonth = DateUtil.getLastDateOfMonth(beginMonth);

        accountTextView = (TextView)findViewById(R.id.accountName);
        costTextView = (TextView)findViewById(R.id.transactionAccountBalance);

        accountListView = (RecyclerView)findViewById(R.id.transactionAccountListView);
        accountListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        accountTextView.setText(account);

        createToolbar();
        addListeners();
        getAllTransactionsWithAccountForMonth();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(DateUtil.convertDateToStringFormat2(beginMonth));
        }
    }

    private void addListeners(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void  getAllTransactionsWithAccountForMonth(){
        Log.d("DEBUG", "getAllTransactionsWithAccountForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        transactionsForAccountForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("account.id", accountId).findAllSortedAsync("date");
        transactionsForAccountForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionAccountList = myRealm.copyFromRealm(element);
                float total = element.sum("price").floatValue();

                transactionAccountAdapter = new TransactionRecyclerAdapter(instance, transactionAccountList, true); //display date in each transaction item
                accountListView.setAdapter(transactionAccountAdapter);

                //Add divider
                accountListView.addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(instance)
                                .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                                .build());

                Log.d("ZHAN", "there are " + transactionAccountList.size() + " transactions in this account " + account + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);

                //update balance
                costTextView.setText(CurrencyTextFormatter.formatFloat(total, Constants.BUDGET_LOCALE));
            }
        });
    }

    private void updateTransactionList(){
        transactionAccountList = myRealm.copyFromRealm(transactionsForAccountForMonth);
        transactionAccountAdapter.setTransactionList(transactionAccountList);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.getExtras() != null) {
             if(requestCode == Constants.RETURN_EDIT_TRANSACTION){
                Toast.makeText(instance, "CAME BACK FROM EDIT", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onClickTransaction(int position){
        Intent editTransactionIntent = new Intent(instance, TransactionInfoActivity.class);

        //This is edit mode, not a new transaction
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, DateUtil.convertDateToString(transactionAccountList.get(position).getDate()));

        Parcelable wrapped = Parcels.wrap(transactionAccountList.get(position));
        editTransactionIntent.putExtra(Constants.REQUEST_EDIT_TRANSACTION, wrapped);

        startActivityForResult(editTransactionIntent, Constants.RETURN_EDIT_TRANSACTION);
    }

    @Override
    public void onDeleteTransaction(int position){
        myRealm.beginTransaction();
        transactionsForAccountForMonth.deleteFromRealm(position);
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onApproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForAccountForMonth.get(position).setDayType(DayType.COMPLETED.toString());
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onUnapproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForAccountForMonth.get(position).setDayType(DayType.SCHEDULED.toString());
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onPullDownAllow(boolean value){
        //no need to implement this as this activity has no pull down to refresh feature
    }
}
