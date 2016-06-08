package com.zhan.budget.Activity;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.Date;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForLocation extends BaseRealmActivity implements
        TransactionRecyclerAdapter.OnTransactionAdapterInteractionListener{

    private static final String TAG = "TransactionsForLocation";
    private Activity instance;
    private Toolbar toolbar;
    private Date beginMonth, endMonth;
    private String location;
    private TextView locationTextView, costTextView, emptyListTextView;
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

        //Need to go a day before as Realm's between date does inclusive on both end
        endMonth = DateUtil.getLastDateOfMonth(beginMonth);

        locationTextView = (TextView)findViewById(R.id.genericName);
        costTextView = (TextView)findViewById(R.id.transactionBalance);

        emptyListTextView = (TextView)findViewById(R.id.emptyTransactionTextView);
        emptyListTextView.setText("There is no transaction for '"+location+"' during "+DateUtil.convertDateToStringFormat2(beginMonth));

        locationListView = (RecyclerView)findViewById(R.id.transactionListView);
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

                Log.d("ZHAN", "there are " + transactionLocationList.size() + " transactions in this location " + location + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);

                //update balance
                costTextView.setText(CurrencyTextFormatter.formatFloat(total, Constants.BUDGET_LOCALE));

                updateTransactionStatus();
            }
        });
    }

    private void updateTransactionList(){
        transactionLocationList = myRealm.copyFromRealm(transactionsForLocationForMonth);
        transactionLocationAdapter.setTransactionList(transactionLocationList);
        updateTransactionStatus();
    }

    private void updateTransactionStatus(){
        if(transactionLocationList.size() > 0){
            locationListView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }else{
            locationListView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        }
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
    public void onClickTransaction(int position){
        /*Intent editTransactionIntent = new Intent(this, TransactionInfoActivity.class);

        //This is edit mode, not a new transaction
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, DateUtil.convertDateToString(transactionsForLocationForMonth.get(position).getDate()));

        Parcelable wrapped = Parcels.wrap(transactionsForLocationForMonth.get(position));
        editTransactionIntent.putExtra(Constants.REQUEST_EDIT_TRANSACTION, wrapped);

        startActivityForResult(editTransactionIntent, Constants.RETURN_EDIT_TRANSACTION);*/
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
