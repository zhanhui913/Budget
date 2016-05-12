package com.zhan.budget.Activity;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Etc.CategoryCalculator;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.BarChartFragment;
import com.zhan.budget.Fragment.Chart.BaseChartFragment;
import com.zhan.budget.Fragment.Chart.PercentChartFragment;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class OverviewActivity extends BaseActivity implements
        CategoryGenericRecyclerAdapter.OnCategoryGenericAdapterInteractionListener{

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

    private CategoryGenericRecyclerAdapter categoryPercentListAdapter;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_overview;
    }

    @Override
    protected void init(){
        currentMonth = (Date)(getIntent().getExtras()).get(Constants.REQUEST_NEW_OVERVIEW_MONTH);

        categoryList = new ArrayList<>();
        RecyclerView categoryListView = (RecyclerView) findViewById(R.id.percentCategoryListView);
        categoryPercentListAdapter = new CategoryGenericRecyclerAdapter(this, categoryList, CategoryGenericRecyclerAdapter.ARRANGEMENT.PERCENT, null);
        categoryListView.setLayoutManager(new LinearLayoutManager(this));

        categoryListView.setAdapter(categoryPercentListAdapter);

        //Add divider
        categoryListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

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
        final Realm myRealm = Realm.getDefaultInstance();
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListener(this);

                categoryList.clear();
                categoryList = myRealm.copyFromRealm(element);
                myRealm.close();
                getMonthReport(currentMonth);
            }
        });
    }

    private void getMonthReport(Date date){
        //Refresh these variables
        final Date month = DateUtil.refreshMonth(date);

        //Need to go a day before as Realm's between date does inclusive on both end
        //final Date endMonth = DateUtil.getPreviousDate(DateUtil.getNextMonth(month));
        final Date endMonth = DateUtil.getLastDateOfMonth(month);

        Log.d("OVERVIEW_ACT", "("+DateUtil.convertDateToStringFormat1(month) + "-> "+DateUtil.convertDateToStringFormat1(endMonth)+")");

        final Realm myRealm = Realm.getDefaultInstance();
        transactionsResults = myRealm.where(Transaction.class).between("date", month, endMonth).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                if (transactionList != null) {
                    transactionList.clear();
                }

                transactionList = myRealm.copyFromRealm(element);
                myRealm.close();
                performAsyncCalculation();
            }
        });
    }

    /**
     * Perform tedious calculation asynchronously to avoid blocking main thread
     */
    private void performAsyncCalculation(){
        /*
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
                    BigDecimal current = BigDecimal.valueOf(categoryList.get(i).getCost());
                    BigDecimal total = BigDecimal.valueOf(sumCost);
                    BigDecimal hundred = new BigDecimal(100);
                    BigDecimal percent = current.divide(total, 4, BigDecimal.ROUND_HALF_EVEN);

                    categoryList.get(i).setPercent(percent.multiply(hundred).floatValue());
                }

                return sumCost;
            }

            @Override
            protected void onPostExecute(Float result) {
                super.onPostExecute(result);

                categoryPercentListAdapter.setCategoryList(categoryList);

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
        */

        Log.d("CAT_CAL", "starting category calculator");
        CategoryCalculator cc = new CategoryCalculator(this, transactionList, categoryList, new Date(),new CategoryCalculator.OnCategoryCalculatorInteractionListener() {
            @Override
            public void onCompleteCalculation(List<Category> catList) {
                Toast.makeText(getApplicationContext(), "DONE CATEGORY CALCULATION", Toast.LENGTH_LONG).show();

                categoryList = catList;

                //Calculate total cost
                float sumCost=0f;
                for(int i = 0; i < categoryList.size(); i++){
                    sumCost += categoryList.get(i).getCost();
                }

                //Now calculate percentage for each category
                for(int i = 0; i < categoryList.size(); i++){
                    BigDecimal current = BigDecimal.valueOf(categoryList.get(i).getCost());
                    BigDecimal total = BigDecimal.valueOf(sumCost);
                    BigDecimal hundred = new BigDecimal(100);
                    BigDecimal percent = current.divide(total, 4, BigDecimal.ROUND_HALF_EVEN);

                    categoryList.get(i).setPercent(percent.multiply(hundred).floatValue());
                }

                categoryPercentListAdapter.setCategoryList(categoryList);

                //Once the calculation is done, remove it
                circularProgressBar.setVisibility(View.GONE);

                //Set total cost for month
                totalCostForMonth.setText(CurrencyTextFormatter.formatFloat(sumCost, Constants.BUDGET_LOCALE));

                barChartFragment = BarChartFragment.newInstance(categoryList);
                percentChartFragment = PercentChartFragment.newInstance(categoryList);
                pieChartFragment = PieChartFragment.newInstance(categoryList);
                pieChartFragment.setData(categoryList);
                getSupportFragmentManager().beginTransaction().add(R.id.chartContentFrame, barChartFragment).commit();

            }
        });
        cc.execute();

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
            /*case R.id.pdfMaker:
                Toast.makeText(getApplicationContext(), "click here pdf maker", Toast.LENGTH_SHORT).show();
                Intent pdfIntent = new Intent(getApplicationContext(), PdfActivity.class);
                startActivity(pdfIntent);
                return true;*/
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteCategory(int position){}

    @Override
    public void onEditCategory(int position){}

    @Override
    public void onPullDownAllow(boolean value){}

    @Override
    public void onDoneDrag(){
        //not being used
    }

    @Override
    public void onClick(int position){}
}


