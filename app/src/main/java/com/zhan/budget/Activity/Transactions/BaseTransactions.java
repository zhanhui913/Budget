package com.zhan.budget.Activity.Transactions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.BaseRealmActivity;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public abstract class BaseTransactions extends BaseRealmActivity implements
        TransactionRecyclerAdapter.OnTransactionAdapterInteractionListener{

    protected static final String TAG = "BaseTransactions";

    protected static final String ALL_TRANSACTION_FOR_DATE = "All Transactions For Date";

    protected Activity instance;
    protected Date beginMonth, endMonth;

    protected RecyclerView transactionListView;
    protected TransactionRecyclerAdapter transactionAdapter;
    protected List<Transaction> transactionList;
    protected RealmResults<Transaction> transactionsForMonth;
    protected BudgetCurrency defaultCurrency;

    private Toolbar toolbar;
    private TextView titleNameTextView, emptyListTextView;
    protected TextView titleBalanceTextView;

    /**
     * True if the user changes the status of at least 1 Transaction from COMPLETED
     * to SCHEDULED or deleted that Transaction.
     * False otherwise.
     */
    protected boolean isChanged = false;

    protected LinearLayoutManager linearLayoutManager;
    protected SwipeLayout currentSwipeLayoutTarget;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_transactions_for_generic;
    }

    @Override
    protected void init(){
        super.init();

        instance = this;

        //Get intents from caller activity
        beginMonth = DateUtil.refreshMonth((Date)(getIntent().getSerializableExtra(ALL_TRANSACTION_FOR_DATE)));

        //Need to go a day before as Realm's between date does inclusive on both end
        endMonth = DateUtil.getLastDateOfMonth(beginMonth);

        linearLayoutManager = new LinearLayoutManager(instance);

        transactionListView = (RecyclerView) findViewById(R.id.transactionListView);
        transactionListView.setLayoutManager(linearLayoutManager);

        //Add divider
        transactionListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(instance)
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        titleNameTextView = (TextView) findViewById(R.id.genericName);
        titleBalanceTextView = (TextView) findViewById(R.id.transactionBalance);

        emptyListTextView = (TextView) findViewById(R.id.emptyTransactionTextView);

        createToolbar();
        getDifferentData();
        getAllTransactionsForMonth();
        addListeners();
        getDefaultCurrency();
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
            getSupportActionBar().setTitle(DateUtil.convertDateToStringFormat2(getApplicationContext(), beginMonth));
        }
    }

    private void addListeners(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(TransactionInfoActivity.HAS_CHANGED, isChanged);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void getDefaultCurrency(){
        final Realm myRealm = Realm.getDefaultInstance();

        defaultCurrency = myRealm.where(BudgetCurrency.class).equalTo("isDefault", true).findFirst();
        if(defaultCurrency == null){
            defaultCurrency = new BudgetCurrency();
            defaultCurrency.setCurrencyCode(Constants.DEFAULT_CURRENCY_CODE);
            defaultCurrency.setCurrencyName(Constants.DEFAULT_CURRENCY_NAME);
        }else{
            defaultCurrency = myRealm.copyFromRealm(defaultCurrency);
        }

        Toast.makeText(getApplicationContext(), "default currency : "+defaultCurrency.getCurrencyName(), Toast.LENGTH_LONG).show();
        myRealm.close();
    }

    private void confirmDeleteTransaction(final int position){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(R.string.warning_delete_transaction);

        new AlertDialog.Builder(instance)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteTransaction(position);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        closeSwipeItem(position);
                    }
                })
                .create()
                .show();
    }

    private void deleteTransaction(int position){
        myRealm.beginTransaction();
        transactionsForMonth.deleteFromRealm(position);
        myRealm.commitTransaction();
        isChanged = true;
        updateTransactionList();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Protected functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This gets called when a Transaction has been deleted or its dayType changed.
     */
    protected void updateTransactionList(){
        //Filter out Transactions with SCHEDULED dayType
        transactionsForMonth.where().equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync();

        transactionsForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionList = myRealm.copyFromRealm(element);
                transactionAdapter.setTransactionList(transactionList);
                updateTransactionStatus();

                float total = CurrencyTextFormatter.findTotalCostForTransactions(transactionList);

                updateTitleBalance(CurrencyTextFormatter.formatFloat(total, defaultCurrency));
            }
        });
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

    protected void openSwipeItem(int position){
        currentSwipeLayoutTarget = (SwipeLayout) linearLayoutManager.findViewByPosition(position);
        currentSwipeLayoutTarget.open();
    }

    protected void closeSwipeItem(int position){
        currentSwipeLayoutTarget = (SwipeLayout) linearLayoutManager.findViewByPosition(position);
        currentSwipeLayoutTarget.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Functions that subclass needs to implement
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void getDifferentData();

    protected abstract void getAllTransactionsForMonth();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Life cycle methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.getExtras() != null) {
             if(requestCode == RequestCodes.EDIT_TRANSACTION){
                 boolean hasChanged = data.getExtras().getBoolean(TransactionInfoActivity.HAS_CHANGED);

                 //Toast.makeText(getBaseContext(), "has changed : "+hasChanged, Toast.LENGTH_SHORT).show();

                 if(hasChanged){
                     //If something has been changed, update the list
                     getDifferentData();
                     getAllTransactionsForMonth();
                     isChanged = true;
                 }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(TransactionInfoActivity.HAS_CHANGED, isChanged);
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
        startActivityForResult(TransactionInfoActivity.createIntentToEditTransaction(getBaseContext(), transactionList.get(position)), RequestCodes.EDIT_TRANSACTION);
    }

    @Override
    public void onDeleteTransaction(int position){
        /*myRealm.beginTransaction();
        transactionsForMonth.deleteFromRealm(position);
        myRealm.commitTransaction();

        isChanged = true;

        updateTransactionList();
*/

        confirmDeleteTransaction(position);
    }

    @Override
    public void onApproveTransaction(int position){
        //Not possible to approve a Transaction (ie: going from SCHEDULED to COMPLETED as
        //we only display COMPLETED Transactions);
    }

    @Override
    public void onUnapproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForMonth.get(position).setDayType(DayType.SCHEDULED.toString());
        myRealm.commitTransaction();

        isChanged = true;

        updateTransactionList();
    }

    @Override
    public void onPullDownAllow(boolean value){
        //no need to implement this as this activity has no pull down to refresh feature
    }

}
