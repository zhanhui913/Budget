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
import com.zhan.budget.Model.RepeatType;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForLocation extends BaseRealmActivity implements
        TransactionRecyclerAdapter.OnTransactionAdapterInteractionListener{

    private static final String TAG = "TransactionsForLocation";
    private Activity instance;
    private Toolbar toolbar;
    private Date beginMonth, endMonth;
    private String location;
    private TextView locationTextView, costTextView;
    private RecyclerView locationListView;
    private TransactionRecyclerAdapter transactionLocationAdapter;
    private List<Transaction> transactionLocationList;
    private RealmResults<Transaction> transactionsForLocationForMonth;


    @Override
    protected int getActivityLayout(){
        return R.layout.activity_transactions_for_location;
    }

    @Override
    protected void init(){
        super.init();

        //Get intents from caller activity
        beginMonth = DateUtil.refreshMonth(DateUtil.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_ALL_TRANSACTION_FOR_LOCATION_MONTH)));
        location = getIntent().getExtras().getString(Constants.REQUEST_ALL_TRANSACTION_FOR_LOCATION_LOCATION);

        instance = this;

        endMonth = DateUtil.getLastDateOfMonth(beginMonth);

        locationTextView = (TextView)findViewById(R.id.locationName);
        costTextView = (TextView)findViewById(R.id.transactionLocationBalance);

        locationListView = (RecyclerView)findViewById(R.id.transactionLocationListView);
        locationListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        locationTextView.setText(location);

        createToolbar();
        addListeners();
        getAllTransactionsWithLocationForMonth();
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
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void  getAllTransactionsWithLocationForMonth(){
        Log.d("DEBUG", "getAllTransactionsWithLocationForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        transactionsForLocationForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("location", location).findAllSortedAsync("date");
        transactionsForLocationForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionLocationList = myRealm.copyFromRealm(element);
                float total = element.sum("price").floatValue();

                transactionLocationAdapter = new TransactionRecyclerAdapter(instance, transactionLocationList, true); //display date in each transaction item
                locationListView.setAdapter(transactionLocationAdapter);

                //Add divider
                locationListView.addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(instance)
                                .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                                .build());

                Log.d("ZHAN", "there are " + transactionLocationList.size() + " transactions in this category " + location + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);


                //update balance
                costTextView.setText(CurrencyTextFormatter.formatFloat(total, Constants.BUDGET_LOCALE));
            }
        });
    }

    private void updateTransactionList(){
        transactionLocationList = myRealm.copyFromRealm(transactionsForLocationForMonth);
        transactionLocationAdapter.setTransactionList(transactionLocationList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resumeRealm();
        if (resultCode == RESULT_OK && data.getExtras() != null) {
            if(requestCode == Constants.RETURN_EDIT_TRANSACTION){
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_TRANSACTION));
                ScheduledTransaction scheduledTransaction = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_SCHEDULE_TRANSACTION));

                addNewOrEditTransaction(tt);

                if(scheduledTransaction != null) {
                    addScheduleTransaction(scheduledTransaction, tt);
                }
            }
        }
    }

    /**
     * The function that will be called after user either adds or edit a scheduled transaction.
     * @param scheduledTransaction The new scheduled transaction information.
     * @param transaction The transaction that the scheduled transaction is based on.
     */
    private void addScheduleTransaction(ScheduledTransaction scheduledTransaction, Transaction transaction){
        if(scheduledTransaction != null && scheduledTransaction.getRepeatUnit() != 0){
            myRealm.beginTransaction();
            scheduledTransaction.setTransaction(transaction);
            myRealm.copyToRealmOrUpdate(scheduledTransaction);
            myRealm.commitTransaction();

            Log.d(TAG, "----------- Parceler Result ----------");
            Log.d(TAG, "scheduled transaction id :" + scheduledTransaction.getId());
            Log.d(TAG, "scheduled transaction unit :" + scheduledTransaction.getRepeatUnit() + ", type :" + scheduledTransaction.getRepeatType());
            Log.d(TAG, "transaction note :" + scheduledTransaction.getTransaction().getNote() + ", cost :" + scheduledTransaction.getTransaction().getPrice());
            Log.i(TAG, "----------- Parceler Result ----------");

            transaction.setDayType(DayType.SCHEDULED.toString());
            Date nextDate = transaction.getDate();

            for(int i = 0; i < 10; i++){
                myRealm.beginTransaction();

                if(scheduledTransaction.getRepeatType().equalsIgnoreCase(RepeatType.DAYS.toString())){
                    nextDate = DateUtil.getDateWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                }else if(scheduledTransaction.getRepeatType().equalsIgnoreCase(RepeatType.WEEKS.toString())){
                    nextDate = DateUtil.getWeekWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                }else{
                    nextDate = DateUtil.getMonthWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                }

                Log.d(TAG, i + "-> " + DateUtil.convertDateToStringFormat5(nextDate));
                myRealm.copyToRealmOrUpdate(transaction);
                myRealm.commitTransaction();
            }
        }
    }

    /**
     * The function that will be called after user either adds or edit a transaction.
     * @param edittedTransaction The editted transaction information.
     */
    private void addNewOrEditTransaction(Transaction edittedTransaction){
        Log.d(TAG, "----------- Parceler Result ----------");
        Log.d(TAG, "transaction id :"+edittedTransaction.getId());
        Log.d(TAG, "transaction note :" + edittedTransaction.getNote() + ", cost :" + edittedTransaction.getPrice());
        Log.d(TAG, "transaction daytype :" + edittedTransaction.getDayType() + ", date :" + edittedTransaction.getDate());
        Log.d(TAG, "category name :" + edittedTransaction.getCategory().getName() + ", id:" + edittedTransaction.getCategory().getId());
        Log.d(TAG, "category type :" + edittedTransaction.getCategory().getType());
        Log.d(TAG, "account id : " + edittedTransaction.getAccount().getId());
        Log.d(TAG, "account name : " + edittedTransaction.getAccount().getName());
        Log.i(TAG, "----------- Parceler Result ----------");

        myRealm.beginTransaction();
        myRealm.copyToRealmOrUpdate(edittedTransaction);
        myRealm.commitTransaction();

        checkIfLocationIsSame(edittedTransaction);
    }

    /**
     * If not the same, remove from list
     */
    private void checkIfLocationIsSame(Transaction tt){
        if(!this.location.equalsIgnoreCase(tt.getLocation())){
            //Update the list
            getAllTransactionsWithLocationForMonth();
        }else{
            updateTransactionList();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();

        setResult(RESULT_OK, intent);
        finish();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickTransaction(int position){
        Intent editTransactionIntent = new Intent(this, TransactionInfoActivity.class);

        //This is edit mode, not a new transaction
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, DateUtil.convertDateToString(transactionsForLocationForMonth.get(position).getDate()));

        Parcelable wrapped = Parcels.wrap(transactionsForLocationForMonth.get(position));
        editTransactionIntent.putExtra(Constants.REQUEST_EDIT_TRANSACTION, wrapped);

        startActivityForResult(editTransactionIntent, Constants.RETURN_EDIT_TRANSACTION);
    }

    @Override
    public void onDeleteTransaction(int position){
        myRealm.beginTransaction();
        transactionsForLocationForMonth.deleteFromRealm(position);
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onApproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForLocationForMonth.get(position).setDayType(DayType.COMPLETED.toString());
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onUnapproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForLocationForMonth.get(position).setDayType(DayType.SCHEDULED.toString());
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onPullDownAllow(boolean value){
        //no need to implement this as this activity has no pull down to refresh feature
    }
}
