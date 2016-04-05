package com.zhan.budget.Activity;

import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Adapter.CategoryPercentListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.BarChartFragment;
import com.zhan.budget.Fragment.Chart.BaseChartFragment;
import com.zhan.budget.Fragment.Chart.PercentChartFragment;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.CategoryPercent;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class OverviewActivity extends BaseRealmActivity {

    private PercentChartFragment percentChartFragment;
    private BarChartFragment barChartFragment;
    private PieChartFragment pieChartFragment;

    private Toolbar toolbar;
    private Date currentMonth;
    private CircularProgressBar circularProgressBar;
    private TextView totalCostForMonth;

    private RealmResults<Category> resultsCategory;
    private RealmResults<Transaction> transactionsResults;

    private List<Transaction> transactionList;
    private List<Category> categoryList;
    private List<CategoryPercent> categoryPercentList;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_overview;
    }

    @Override
    protected void init(){
        super.init();

        currentMonth = (Date)(getIntent().getExtras()).get(Constants.REQUEST_NEW_OVERVIEW_MONTH);

        categoryList = new ArrayList<>();
        categoryPercentList = new ArrayList<>();
        ListView categoryListView = (ListView) findViewById(R.id.percentCategoryListView);
        CategoryPercentListAdapter categoryPercentListAdapter = new CategoryPercentListAdapter(this, categoryPercentList);
        categoryListView.setAdapter(categoryPercentListAdapter);

        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setText(DateUtil.convertDateToStringFormat2(currentMonth));
        totalCostForMonth = (TextView) findViewById(R.id.totalCostTextView);

        circularProgressBar = (CircularProgressBar) findViewById(R.id.overviewProgressBar);

        createToolbar();
        addListeners();

        getCategoryList();
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
            getSupportActionBar().setTitle("Overview");
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
                resultsCategory.removeChangeListener(this);

                categoryList.clear();
                categoryList = myRealm.copyFromRealm(resultsCategory);
                getMonthReport(currentMonth);
            }
        });
    }

    private void getMonthReport(Date date){
        //Refresh these variables
        final Date month = DateUtil.refreshMonth(date);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = DateUtil.getPreviousDate(DateUtil.getNextMonth(month));

        Log.d("OVERVIEW_ACT", "("+DateUtil.convertDateToStringFormat1(month) + "-> "+DateUtil.convertDateToStringFormat1(endMonth)+")");

        transactionsResults = myRealm.where(Transaction.class).between("date", month, endMonth).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                transactionsResults.removeChangeListener(this);

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
        final AsyncTask<Void, Void, Float> loader = new AsyncTask<Void, Void, Float>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("OVERVIEW_ACT", "preparing to aggregate results");
            }

            @Override
            protected Float doInBackground(Void... voids) {
                float sumCost = 0;
                categoryPercentList.clear();

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
                    categoryList.remove(zeroSumList.get(i));
                }
                Log.d("PERCENT_VIEW", "AFTER REMOVING THERE ARE " + categoryList.size());

                //Go through list cost to get sumCost
                for(int i = 0; i < categoryList.size(); i++){
                    sumCost += categoryList.get(i).getCost();
                }

                //Sort from largest to smallest percentage
                Collections.sort(categoryList, new Comparator<Category>() {
                    @Override
                    public int compare(Category c1, Category c2) {
                        float cost1 = c1.getCost();
                        float cost2 = c2.getCost();

                        //ascending order
                        return ((int) cost1) - ((int) cost2);
                    }
                });

                //Now calculate percentage for each category
                for(int i = 0; i < categoryList.size(); i++){
                    CategoryPercent cp = new CategoryPercent();
                    cp.setCategory(categoryList.get(i));

                    BigDecimal current = BigDecimal.valueOf(categoryList.get(i).getCost());
                    BigDecimal total = BigDecimal.valueOf(sumCost);
                    BigDecimal hundred = new BigDecimal(100);
                    BigDecimal percent = current.divide(total, 4, BigDecimal.ROUND_HALF_EVEN);

                    cp.setPercent(percent.multiply(hundred).floatValue());
                    categoryPercentList.add(cp);
                }

                return sumCost;
            }

            @Override
            protected void onPostExecute(Float result) {
                super.onPostExecute(result);

                //Once the calculation is done, remove it
                circularProgressBar.setVisibility(View.GONE);

                //Set total cost for month
                totalCostForMonth.setText(CurrencyTextFormatter.formatFloat(result, Constants.BUDGET_LOCALE));

                barChartFragment = BarChartFragment.newInstance(categoryList);
                percentChartFragment = PercentChartFragment.newInstance(categoryList);
                pieChartFragment = PieChartFragment.newInstance(categoryList);
                getSupportFragmentManager().beginTransaction().add(R.id.chartContentFrame, barChartFragment).commit();

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Menu Options
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview_chart, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.percentChart:
                Toast.makeText(getApplicationContext(), "click here percent chart", Toast.LENGTH_SHORT).show();
                replaceFragment(percentChartFragment);
                return true;
            case R.id.barChart:
                Toast.makeText(getApplicationContext(), "click here bar chart", Toast.LENGTH_SHORT).show();
                replaceFragment(barChartFragment);
                return true;
            case R.id.pieChart:
                Toast.makeText(getApplicationContext(), "click here pie chart", Toast.LENGTH_SHORT).show();
                replaceFragment(pieChartFragment);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void replaceFragment(BaseChartFragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.chartContentFrame, fragment);
        ft.commit();
    }
}


