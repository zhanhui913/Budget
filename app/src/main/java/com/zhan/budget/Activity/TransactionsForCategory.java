package com.zhan.budget.Activity;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForCategory extends BaseRealmActivity implements
        TransactionRecyclerAdapter.OnTransactionAdapterInteractionListener{

    private Activity instance;
    private Toolbar toolbar;
    private Date beginMonth, endMonth;
    private Category selectedCategory;

    private ImageView transactionCategoryIcon;
    private TextView transactionCategoryName, transactionCategoryBalance, emptyListTextView;

    private RecyclerView transactionCategoryListView;
    private TransactionRecyclerAdapter transactionCategoryAdapter;
    private List<Transaction> transactionCategoryList;


    private RealmResults<Transaction> transactionsForCategoryForMonth;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_transactions_for_category;
    }

    @Override
    protected void init(){
        super.init();

        //Get intents from caller activity
        beginMonth = DateUtil.refreshMonth(DateUtil.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_MONTH)));
        selectedCategory = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY));

        instance = this;

        createToolbar();

        transactionCategoryListView = (RecyclerView) findViewById(R.id.transactionCategoryListView);
        transactionCategoryListView.setLayoutManager(new LinearLayoutManager(instance.getBaseContext()));

        transactionCategoryIcon = (ImageView) findViewById(R.id.transactionCategoryIcon);
        transactionCategoryName = (TextView) findViewById(R.id.transactionCategoryName);
        transactionCategoryBalance = (TextView) findViewById(R.id.transactionCategoryBalance);


        //Need to go a day before as Realm's between date does inclusive on both end
        endMonth = DateUtil.getLastDateOfMonth(beginMonth);

        transactionCategoryIcon.setImageResource(CategoryUtil.getIconID(this, selectedCategory.getIcon()));

        transactionCategoryName.setText(selectedCategory.getName());

        emptyListTextView = (TextView) findViewById(R.id.emptyTransactionTextView);
        emptyListTextView.setText("There is no transaction for "+selectedCategory.getName()+" during "+DateUtil.convertDateToStringFormat2(beginMonth));


        Log.d("ZHAN", "selected category => " + selectedCategory.getName() + " -> " + selectedCategory.getId());

        getAllTransactionsWithCategoryForMonth();
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
                finish();
            }
        });
    }

    private void getAllTransactionsWithCategoryForMonth(){
        Log.d("DEBUG", "getAllTransactionsWithCategoryForMonth from " + beginMonth.toString() + " to " + endMonth.toString());

        transactionsForCategoryForMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).equalTo("category.id", selectedCategory.getId()).findAllSortedAsync("date");
        transactionsForCategoryForMonth.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionCategoryList = myRealm.copyFromRealm(element);
                float total = element.sum("price").floatValue();

                transactionCategoryAdapter = new TransactionRecyclerAdapter(instance, transactionCategoryList, true); //display date in each transaction item
                transactionCategoryListView.setAdapter(transactionCategoryAdapter);

                //Add divider
                transactionCategoryListView.addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(instance)
                                .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                                .build());

                Log.d("ZHAN", "there are " + transactionCategoryList.size() + " transactions in this category " + selectedCategory.getName() + " for this month " + beginMonth + " -> " + endMonth);
                Log.d("ZHAN", "total sum is "+total);

                //update balance
                transactionCategoryBalance.setText(CurrencyTextFormatter.formatFloat(total, Constants.BUDGET_LOCALE));

                updateTransactionStatus();
            }
        });
    }

    private void updateTransactionList(){
        transactionCategoryList = myRealm.copyFromRealm(transactionsForCategoryForMonth);
        transactionCategoryAdapter.setTransactionList(transactionCategoryList);

        updateTransactionStatus();
    }

    private void updateTransactionStatus(){
        if(transactionCategoryList.size() > 0){
            transactionCategoryListView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }else{
            transactionCategoryListView.setVisibility(View.GONE);
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
       /* Intent editTransactionIntent = new Intent(this, TransactionInfoActivity.class);

        //This is edit mode, not a new transaction
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, DateUtil.convertDateToString(transactionsForCategoryForMonth.get(position).getDate()));

        Parcelable wrapped = Parcels.wrap(transactionsForCategoryForMonth.get(position));
        editTransactionIntent.putExtra(Constants.REQUEST_EDIT_TRANSACTION, wrapped);

        startActivityForResult(editTransactionIntent, Constants.RETURN_EDIT_TRANSACTION);*/
    }

    @Override
    public void onDeleteTransaction(int position){
        myRealm.beginTransaction();
        transactionsForCategoryForMonth.deleteFromRealm(position);
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onApproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForCategoryForMonth.get(position).setDayType(DayType.COMPLETED.toString());
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onUnapproveTransaction(int position){
        myRealm.beginTransaction();
        transactionsForCategoryForMonth.get(position).setDayType(DayType.SCHEDULED.toString());
        myRealm.commitTransaction();

        updateTransactionList();
    }

    @Override
    public void onPullDownAllow(boolean value){
        //no need to implement this as this activity has no pull down to refresh feature
    }
}
