package com.zhan.budget.Activity;

import android.app.Activity;
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
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForLocation extends BaseRealmActivity implements
        TransactionRecyclerAdapter.OnTransactionAdapterInteractionListener{

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
                finish();
            }
        });
    }

    private void  getAllTransactionsWithLocationForMonth(){
        Log.d("DEBUG", "getAllTransactionsWithLocationForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        //final Realm myRealm = Realm.getDefaultInstance();

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

                //myRealm.close();
            }
        });
    }

    private void updateTransactionList(){
        transactionLocationList = myRealm.copyFromRealm(transactionsForLocationForMonth);
        transactionLocationAdapter.setTransactionList(transactionLocationList);
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

    }

    @Override
    public void onDeleteTransaction(int position){
        myRealm.beginTransaction();
        transactionsForLocationForMonth.deleteFromRealm(position);
        myRealm.commitTransaction();

        updateTransactionList();

        Toast.makeText(getApplicationContext(), "transactionsforlocation delete transaction :" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForLocationForMonth.get(position).setDayType(DayType.COMPLETED.toString());
        myRealm.commitTransaction();

        updateTransactionList();

        Toast.makeText(getApplicationContext(), "transactionsforlocation approve transaction :"+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUnapproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForLocationForMonth.get(position).setDayType(DayType.SCHEDULED.toString());
        myRealm.commitTransaction();

        updateTransactionList();

        Toast.makeText(getApplicationContext(), "transactionsforlocation unapprove transaction :"+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPullDownAllow(boolean value){
        //no need to implement this as this activity has no pull down to refresh feature
    }
}
