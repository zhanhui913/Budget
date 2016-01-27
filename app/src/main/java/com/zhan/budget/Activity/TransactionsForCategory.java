package com.zhan.budget.Activity;

import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.zhan.budget.Adapter.TransactionListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Util;
import com.zhan.circularview.CircularView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class TransactionsForCategory extends AppCompatActivity {

    private Toolbar toolbar;
    private Date currentMonth;
    private Date beginMonth;
    private Date endMonth;
    private Realm myRealm;
    //private ParcelableCategory selectedParcelableCategory;
    private Category selectedCategory;

    private ImageView transactionCategoryIcon;
    private TextView transactionCategoryName, transactionCategoryBalance;

    private SwipeMenuListView transactionCategoryListView;
    private TransactionListAdapter transactionCategoryAdapter;
    private List<Transaction> transactionCategoryList;

    private CircularView circularView;
    private ViewGroup emptyStateContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_for_category);

        //Get intents from caller activity
        currentMonth = Util.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_MONTH));
        //selectedParcelableCategory = (getIntent().getExtras()).getParcelable(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY);
        selectedCategory = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY));

        init();
        createSwipeMenu();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        transactionCategoryListView = (SwipeMenuListView) findViewById(R.id.transactionCategoryListView);
        transactionCategoryIcon = (ImageView) findViewById(R.id.transactionCategoryIcon);
        transactionCategoryName = (TextView) findViewById(R.id.transactionCategoryName);
        transactionCategoryBalance = (TextView) findViewById(R.id.transactionCategoryBalance);
        circularView = (CircularView) findViewById(R.id.emptyCategoryView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Util.convertDateToStringFormat2(currentMonth));
        }

        transactionCategoryList = new ArrayList<>();
        transactionCategoryAdapter = new TransactionListAdapter(this, transactionCategoryList);
        transactionCategoryListView.setAdapter(transactionCategoryAdapter);

        beginMonth = Util.refreshMonth(currentMonth);
        endMonth = Util.getNextMonth(currentMonth);

        transactionCategoryIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                CategoryUtil.getIconResourceId(selectedCategory.getIcon()), getTheme()));

        transactionCategoryName.setText(selectedCategory.getName());

        emptyStateContainer = (ViewGroup) findViewById(R.id.emptyStateContainer);


        Log.d("ZHAN", "selected parcelable category => " + selectedCategory.getName() + " -> " + selectedCategory.getId());

        getAllTransactionsWithCategoryForMonth();
/*
        final RealmResults<Category> results = myRealm.where(Category.class).equalTo("id", selectedParcelableCategory.getId()).findAllAsync();
        results.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                selectedCategory = results.get(0);

                Log.d("ZHAN", "results for selected category : " + results.size());
                Log.d("ZHAN", "converted category => " + selectedCategory.getName());

                circularView.setIconDrawable(ResourcesCompat.getDrawable(getResources(),
                        CategoryUtil.getIconResourceId(selectedCategory.getIcon()), getTheme()));
                circularView.setCircleColor(Color.parseColor(selectedCategory.getColor()));

                getAllTransactionsWithCategoryForMonth();
            }
        });*/
    }

    private void getAllTransactionsWithCategoryForMonth(){
        final RealmResults<Transaction> resultsInMonth = myRealm.where(Transaction.class).between("date", beginMonth, endMonth).findAllAsync();
        resultsInMonth.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {

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
                transactionCategoryBalance.setText(Util.setPriceToCorrectDecimalInString(total));

                transactionCategoryAdapter.notifyDataSetChanged();

                updateStatus();
            }
        });
    }

    /**
     * Add swipe capability on list view to delete that item.
     * From 3rd party library.
     */
    private void createSwipeMenu(){
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(R.color.red);// set item background
                deleteItem.setWidth(Util.dp2px(getApplicationContext(), 90));// set item width
                deleteItem.setIcon(R.drawable.ic_delete);// set a icon
                menu.addMenuItem(deleteItem);// add to menu
            }
        };
        //set creator
        transactionCategoryListView.setMenuCreator(creator);

        // step 2. listener item click event
        transactionCategoryListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //deleting a transaction
                        Transaction transactionToBeDeleted = transactionCategoryList.get(position);

                        myRealm.beginTransaction();
                        //resultsTransactionForDay.get(position).removeFromRealm();
                        myRealm.commitTransaction();

                        break;
                }
                //False: Close the menu
                //True: Did not close the menu
                return false;
            }
        });

        // set SwipeListener
        transactionCategoryListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });
    }

    private void updateStatus(){
        if(transactionCategoryList.size() > 0){
            transactionCategoryListView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }else{
            transactionCategoryListView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        myRealm.close();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        myRealm = Realm.getDefaultInstance();
    }


}
