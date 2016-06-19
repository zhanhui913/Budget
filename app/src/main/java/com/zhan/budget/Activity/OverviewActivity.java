package com.zhan.budget.Activity;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.BarChartFragment;
import com.zhan.budget.Fragment.Chart.BaseChartFragment;
import com.zhan.budget.Fragment.Chart.PercentChartFragment;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Fragment.OverviewGenericFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.View.CustomViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OverviewActivity extends BaseActivity implements
    OverviewGenericFragment.OverviewInteractionListener{

    //Charts
    private PercentChartFragment percentChartFragment;
    private BarChartFragment barChartFragment;
    private PieChartFragment pieChartFragment;

    //Expense Income Fragment to list view
    private OverviewGenericFragment overviewExpenseFragment, overviewIncomeFragment;

    private Activity instance;
    private Toolbar toolbar;
    private Date currentMonth;
    private TextView totalCostForMonth;
    private CustomViewPager viewPager;
    private TabLayout tabLayout;

    private float totalExpenseCost = 0f;
    private float totalIncomeCost = 0f;
    private List<Category> expenseCategoryList;
    private List<Category> incomeCategoryList;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_overview;
    }

    @Override
    protected void init(){ Log.d("REALMZ1", "on init for overview activity");
        instance = this;

        currentMonth = (Date)(getIntent().getExtras()).get(Constants.REQUEST_NEW_OVERVIEW_MONTH);

        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setText(DateUtil.convertDateToStringFormat2(currentMonth));
        totalCostForMonth = (TextView) findViewById(R.id.totalCostTextView);

        createToolbar();
        addListeners();
        createTabs();
        createCharts();
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
            getSupportActionBar().setTitle("Monthly Overview");
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

    private void createTabs(){
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(BudgetType.EXPENSE.toString()));
        tabLayout.addTab(tabLayout.newTab().setText(BudgetType.INCOME.toString()));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        overviewExpenseFragment = OverviewGenericFragment.newInstance(BudgetType.EXPENSE, currentMonth);
        overviewIncomeFragment = OverviewGenericFragment.newInstance(BudgetType.INCOME, currentMonth);

        viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        viewPager.setPagingEnabled(false);

        TwoPageViewPager adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), overviewExpenseFragment, overviewIncomeFragment);
        viewPager.setAdapter(adapterViewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                changeTopPanelInfo(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void createCharts(){
        barChartFragment = BarChartFragment.newInstance(new ArrayList<Category>());
        percentChartFragment = PercentChartFragment.newInstance(new ArrayList<Category>());
        pieChartFragment = PieChartFragment.newInstance(new ArrayList<Category>());
        getSupportFragmentManager().beginTransaction().add(R.id.chartContentFrame, pieChartFragment).commit();
    }

    @Override
    public void onComplete(BudgetType type, List<Category> categoryList, float totalCost){
        if(type == BudgetType.EXPENSE){
            totalExpenseCost = totalCost;
            expenseCategoryList = categoryList;

            //set default tab to be the EXPENSE (ie position = 0)
            //Put this here so that it only gets called once.
            changeTopPanelInfo(0);
        }else{
            totalIncomeCost = totalCost;
            incomeCategoryList = categoryList;
        }
    }

    /**
     * Change chart and total cost information that is in the top panel
     * @param position The tab position
     */
    private void changeTopPanelInfo(int position){
        if(position == 0){
            //Set total cost for month
            totalCostForMonth.setText(CurrencyTextFormatter.formatFloat(totalExpenseCost, Constants.BUDGET_LOCALE));
            pieChartFragment.setData(expenseCategoryList);
        }else if(position == 1){
            //Set total cost for month
            totalCostForMonth.setText(CurrencyTextFormatter.formatFloat(totalIncomeCost, Constants.BUDGET_LOCALE));
            pieChartFragment.setData(incomeCategoryList);
        }
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
}


