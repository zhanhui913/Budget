package com.zhan.budget.Activity.Transactions;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.zhan.budget.Activity.BaseRealmActivity;
import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.Date;
import java.util.List;

import io.realm.RealmResults;

public abstract class BaseTransactions extends BaseRealmActivity implements
        TransactionRecyclerAdapter.OnTransactionAdapterInteractionListener{

    protected static final String TAG = "BaseTransactions";
    protected Activity instance;
    protected Date beginMonth, endMonth;

    protected RecyclerView transactionListView;
    protected TransactionRecyclerAdapter transactionAdapter;
    protected List<Transaction> transactionList;
    protected RealmResults<Transaction> transactionsForMonth;

    private Toolbar toolbar;
    private TextView titleNameTextView, titleBalanceTextView, emptyListTextView;

    /**
     * True if the user changes the status of at least 1 Transaction from COMPLETED
     * to SCHEDULED or deleted that Transaction.
     * False otherwise.
     */
    protected boolean isChanged = false;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_transactions_for_generic;
    }

    @Override
    protected void init(){
        super.init();

        instance = this;

        //Get intents from caller activity
        beginMonth = DateUtil.refreshMonth(DateUtil.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_ALL_TRANSACTION_FOR_GENERIC_MONTH)));

        //Need to go a day before as Realm's between date does inclusive on both end
        endMonth = DateUtil.getLastDateOfMonth(beginMonth);

        transactionListView = (RecyclerView) findViewById(R.id.transactionListView);
        transactionListView.setLayoutManager(new LinearLayoutManager(instance));

        titleNameTextView = (TextView) findViewById(R.id.genericName);
        titleBalanceTextView = (TextView) findViewById(R.id.transactionBalance);

        emptyListTextView = (TextView) findViewById(R.id.emptyTransactionTextView);

        createToolbar();
        getDifferentData();
        getAllTransactionsForMonth();
        addListeners();
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
                Intent intent = new Intent();
                intent.putExtra(Constants.CHANGED, isChanged);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    protected void updateTransactionList(){
        transactionList = myRealm.copyFromRealm(transactionsForMonth);
        transactionAdapter.setTransactionList(transactionList);

        updateTransactionStatus();
    }

    protected void updateTransactionStatus(){
        if(transactionList.size() > 0){
            transactionListView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }else{
            transactionListView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        }
    }

    protected void updateTitleName(String value){
        titleNameTextView.setText(value);
    }

    protected void updateTitleBalance(String value){
        titleBalanceTextView.setText(value);
    }

    protected void updateEmptyListText(String value){
        emptyListTextView.setText(value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Functions that subclass needs to implement
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void getDifferentData();

    protected abstract void getAllTransactionsForMonth();

    /**
     * Called whenever a transaction has been deleted or has its dayType changed.
     */
    protected abstract void changedInList();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.CHANGED, isChanged);
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public void onClickTransaction(int position){}

    @Override
    public void onDeleteTransaction(int position){
        myRealm.beginTransaction();
        transactionsForMonth.deleteFromRealm(position);
        myRealm.commitTransaction();

        changedInList();

        updateTransactionList();
    }

    @Override
    public void onApproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForMonth.get(position).setDayType(DayType.COMPLETED.toString());
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onUnapproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForMonth.get(position).setDayType(DayType.SCHEDULED.toString());
        myRealm.commitTransaction();

        changedInList();

        updateTransactionList();
    }

    @Override
    public void onPullDownAllow(boolean value){
        //no need to implement this as this activity has no pull down to refresh feature
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDestroy(){
        super.onDestroy();
        transactionsForMonth.removeChangeListeners();
    }
}
