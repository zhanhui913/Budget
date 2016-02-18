package com.zhan.budget.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.zhan.budget.Adapter.CategoryListAdapter;
import com.zhan.budget.Adapter.CategoryPercentListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.CategoryPercent;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.percentview.Model.Slice;
import com.zhan.percentview.PercentView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class OverviewActivity extends AppCompatActivity implements
        CategoryListAdapter.OnCategoryAdapterInteractionListener{

    private Toolbar toolbar;
    private Date currentMonth;
    private TextView dateTextView, totalCostForMonthTextView;
    private PercentView percentView;

    private Realm myRealm;
    private RealmResults<Category> resultsCategory;
    private RealmResults<Transaction> transactionsResults;

    private List<Transaction> transactionList;
    private List<Category> categoryList;
    private List<CategoryPercent> categoryPercentList;
    private List<Slice> sliceList;

    private ListView categoryListView;
    private CategoryPercentListAdapter categoryPercentListAdapter;

    //private HashMap<String, Float> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        currentMonth = (Date)(getIntent().getExtras()).get(Constants.REQUEST_NEW_OVERVIEW_MONTH);

        init();
        createToolbar();
        addListeners();

        getCategoryList();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        categoryList = new ArrayList<>();
        categoryPercentList = new ArrayList<>();
        categoryListView = (ListView) findViewById(R.id.percentCategoryListView);
        categoryPercentListAdapter = new CategoryPercentListAdapter(this, categoryPercentList);
        categoryListView.setAdapter(categoryPercentListAdapter);

        totalCostForMonthTextView = (TextView) findViewById(R.id.totalCostForMonth);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setText(Util.convertDateToStringFormat2(currentMonth));

        percentView = (PercentView) findViewById(R.id.percentView);
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Overview");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    //Should be called only the first time when the activity is created
    private void getCategoryList(){
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                categoryList.clear();
                categoryList = myRealm.copyFromRealm(resultsCategory);

                getMonthReport(currentMonth);
            }
        });
    }

    private void getMonthReport(Date date){
        //Refresh these variables
        final Date month = Util.refreshMonth(date);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = Util.getPreviousDate(Util.getNextMonth(month));

        sliceList = new ArrayList<>();

        //map = new HashMap<String, Float>();

        Log.d("OVERVIEW_ACT", "("+Util.convertDateToStringFormat1(month) + "-> "+Util.convertDateToStringFormat1(endMonth)+")");

        transactionsResults = myRealm.where(Transaction.class).between("date", month, endMonth).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                if (transactionList != null) {
                    transactionList.clear();
                }

                transactionList = myRealm.copyFromRealm(transactionsResults);

                performAsyncCalculation();
            }
        });
    }

    /**
     * Perform tedious calculation asynchronously to avoid blocking main thread
     */
    private void performAsyncCalculation(){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("OVERVIEW_ACT", "preparing to aggregate results");
            }

            @Override
            protected Void doInBackground(Void... voids) {

                Log.d("OVERVIEW_ACT", "Transaction size : "+transactionList.size());

                startTime = System.nanoTime();

                //Go through each transaction and put them into the correct category
                for(int t = 0; t < transactionList.size(); t++){
                    for(int c = 0; c < categoryList.size(); c++){
                        if(transactionList.get(t).getCategory().getId().equalsIgnoreCase(categoryList.get(c).getId())){
                            float transactionPrice = transactionList.get(t).getPrice();
                            float currentCategoryPrice = categoryList.get(c).getCost();
                            categoryList.get(c).setCost(transactionPrice + currentCategoryPrice);
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);

                float sumCost = 0;

                //List of string that is the ID of category in categoryList who's sum for cost is 0
                // or INCOME type
                List<Category> zeroSumList = new ArrayList<>();

                //Get position of Category who's sum cost is 0 or INCOME type
                for(int i = 0; i < categoryList.size(); i++){
                    if(categoryList.get(i).getCost() == 0f || categoryList.get(i).getType().equalsIgnoreCase(BudgetType.INCOME.toString())){
                        Log.d("PERCENT_VIEW", "Category : " + categoryList.get(i).getName() + " -> with cost " + categoryList.get(i).getCost());
                        zeroSumList.add(categoryList.get(i));
                    }
                }
                Log.d("PERCENT_VIEW", "BEFORE REMOVING THERE ARE "+categoryList.size());

                for(int i = 0; i < zeroSumList.size(); i++){
                    Log.d("PERCENT_VIEW", "ZERO SUM LIST : "+zeroSumList.get(i).getName());
                }

                //Remove those category who's sum for cost is 0 or INCOME type
                for(int i = 0; i < zeroSumList.size(); i++){
                    //categoryList.remove(zeroSumList.get(i));
                    categoryList.remove(zeroSumList.get(i));
                }
                Log.d("PERCENT_VIEW", "AFTER REMOVING THERE ARE " + categoryList.size());



                //Go through list cost to get sumCost
                for(int i = 0; i < categoryList.size(); i++){
                    sumCost += categoryList.get(i).getCost();
                }

                //Now calculate percentage for each category
                for(int i = 0; i < categoryList.size(); i++){
                    CategoryPercent cp = new CategoryPercent();
                    cp.setCategory(categoryList.get(i));
                    cp.setPercent((categoryList.get(i).getCost() / sumCost) * 100);

                    Log.d("PERCENT_VIEW", i+", "+cp.getCategory().getName()+"->"+cp.getPercent());

                    categoryPercentList.add(cp);

                    Slice slice = new Slice();
                    slice.setColor(categoryList.get(i).getColor());
                    slice.setWeight(categoryList.get(i).getCost());
                    sliceList.add(slice);
                }



                categoryPercentListAdapter.clear();
                categoryPercentListAdapter.addAll(categoryPercentList);
                categoryPercentListAdapter.notifyDataSetChanged();

                /*
                Log.d("PERCENT_VIEW", "BEFORE, There are " + sliceList.size() + " items in the list");

                for(int i = 0; i < categoryList.size(); i++){
                    Log.d("ZHAN1", "category : " + categoryList.get(i).getName() + " -> " + categoryList.get(i).getCost());

                    //Add only EXPENSE category type
                    if(categoryList.get(i).getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                        //Don't add those Category who's sum cost is 0
                        if(categoryList.get(i).getCost() != 0f){
                            Slice slice = new Slice();
                            slice.setColor(categoryList.get(i).getColor());
                            slice.setWeight(categoryList.get(i).getCost());
                            Log.d("PERCENT_VIEW", i + ") DURING, weigh sum :" + categoryList.get(i).getCost());
                            sliceList.add(slice);
                            sumCost += categoryList.get(i).getCost();
                        }
                    }
                }*/

                totalCostForMonthTextView.setText(CurrencyTextFormatter.formatFloat(sumCost, Constants.BUDGET_LOCALE));

                percentView.setSliceList(sliceList);
                Log.d("PERCENT_VIEW", "AFTER, There are " + sliceList.size() + " items in the list");

                endTime = System.nanoTime();
                duration = (endTime - startTime);
                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);
                Log.d("PERCENT_VIEW", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
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
    // Interfaces from CategoryListAdapter
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteCategory(int position){

    }

    @Override
    public void onEditCategory(int position){

    }

    @Override
    public void onDisablePtrPullDown(boolean value){

    }
}


