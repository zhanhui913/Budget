package com.zhan.budget.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Adapter.TransactionListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForCategory extends AppCompatActivity implements
        TransactionListAdapter.OnTransactionAdapterInteractionListener{

    private Toolbar toolbar;
    private Date currentMonth;
    private Date beginMonth;
    private Date endMonth;
    private Realm myRealm;
    private Category selectedCategory;

    private ImageView transactionCategoryIcon;
    private TextView transactionCategoryName, transactionCategoryBalance;

    private ListView transactionCategoryListView;
    private TransactionListAdapter transactionCategoryAdapter;
    private List<Transaction> transactionCategoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_for_category);

        //Get intents from caller activity
        currentMonth = DateUtil.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_MONTH));
        selectedCategory = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY));

        init();
        addListeners();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        createToolbar();

        transactionCategoryListView = (ListView) findViewById(R.id.transactionCategoryListView);
        transactionCategoryIcon = (ImageView) findViewById(R.id.transactionCategoryIcon);
        transactionCategoryName = (TextView) findViewById(R.id.transactionCategoryName);
        transactionCategoryBalance = (TextView) findViewById(R.id.transactionCategoryBalance);

        transactionCategoryList = new ArrayList<>();
        transactionCategoryAdapter = new TransactionListAdapter(this, transactionCategoryList, true); //display date in each transaction item
        transactionCategoryListView.setAdapter(transactionCategoryAdapter);

        beginMonth = DateUtil.refreshMonth(currentMonth);

        //Need to go a day before as Realm's between date does inclusive on both end
        endMonth = DateUtil.getPreviousDate(DateUtil.getNextMonth(currentMonth));

        transactionCategoryIcon.setImageResource(CategoryUtil.getIconID(getApplicationContext(), selectedCategory.getIcon()));

        transactionCategoryName.setText(selectedCategory.getName());

        Log.d("ZHAN", "selected category => " + selectedCategory.getName() + " -> " + selectedCategory.getId());

        getAllTransactionsWithCategoryForMonth();
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
            getSupportActionBar().setTitle(DateUtil.convertDateToStringFormat2(currentMonth));
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

        final RealmResults<Transaction> resultsInMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).findAllAsync();
        resultsInMonth.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                resultsInMonth.removeChangeListener(this);

                //sort by date
                resultsInMonth.sort("date");

                float total = 0f;

                //filter by category
                for(int i = 0; i < resultsInMonth.size(); i++){
                    if(resultsInMonth.get(i).getCategory().getId().equalsIgnoreCase(selectedCategory.getId())){
                        transactionCategoryList.add(resultsInMonth.get(i));
                        total += resultsInMonth.get(i).getPrice();
                    }
                }

                Log.d("ZHAN", "there are " + transactionCategoryList.size() + " transactions in this category " + selectedCategory.getName() + " for this month " + beginMonth + " -> " + endMonth);

                //update balance
                transactionCategoryBalance.setText(CurrencyTextFormatter.formatFloat(total, Constants.BUDGET_LOCALE));

                transactionCategoryAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(!myRealm.isClosed()){
            myRealm.close();
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        myRealm = Realm.getDefaultInstance();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteTransaction(int position){
        /*myRealm.beginTransaction();
        resultsAccount.remove(position);
        myRealm.commitTransaction();

        accountListAdapter.clear();
        accountListAdapter.addAll(accountList);*/
        Toast.makeText(getApplicationContext(), "transactionsforcategory delete transaction :" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApproveTransaction(int position){
        Toast.makeText(getApplicationContext(), "transactionsforcategory approve transaction :"+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisablePtrPullDown(boolean value){
        //no need to implement this as this activity has no pull down to refresh feature
    }
}
