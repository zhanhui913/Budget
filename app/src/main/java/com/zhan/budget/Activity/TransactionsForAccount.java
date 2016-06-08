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
import com.zhan.budget.Model.Realm.Account;
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
    private Account account;
    private TextView accountTextView, costTextView, emptyListTextView;
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

        account = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_ALL_TRANSACTION_FOR_ACCOUNT_ACCOUNT));

        instance = this;

        //Need to go a day before as Realm's between date does inclusive on both end
        endMonth = DateUtil.getLastDateOfMonth(beginMonth);

        accountTextView = (TextView)findViewById(R.id.genericName);
        costTextView = (TextView)findViewById(R.id.transactionBalance);

        emptyListTextView = (TextView)findViewById(R.id.emptyTransactionTextView);
        emptyListTextView.setText("There is no transaction for '"+account.getName()+"' during "+DateUtil.convertDateToStringFormat2(beginMonth));

        accountListView = (RecyclerView)findViewById(R.id.transactionListView);
        accountListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        accountTextView.setText(account.getName());

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

        transactionsForAccountForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("account.id", account.getId()).findAllSortedAsync("date");
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

                Log.d("ZHAN", "there are " + transactionAccountList.size() + " transactions in this account " + account.getName() + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);

                //update balance
                costTextView.setText(CurrencyTextFormatter.formatFloat(total, Constants.BUDGET_LOCALE));

                updateTransactionStatus();
            }
        });
    }

    private void updateTransactionList(){
        transactionAccountList = myRealm.copyFromRealm(transactionsForAccountForMonth);
        transactionAccountAdapter.setTransactionList(transactionAccountList);
        updateTransactionStatus();
    }

    private void updateTransactionStatus(){
        if(transactionAccountList.size() > 0){
            accountListView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }else{
            accountListView.setVisibility(View.GONE);
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
    public void onClickTransaction(int position){}

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
