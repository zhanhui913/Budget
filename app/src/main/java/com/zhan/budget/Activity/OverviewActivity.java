package com.zhan.budget.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.View.CustomViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class OverviewActivity extends BaseActivity implements
    OverviewGenericFragment.OverviewInteractionListener{

    public static final String MONTH = "View Overview Month";

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

    private BudgetCurrency currentCurrency;

    public static Intent createIntentToViewOverviewOnMonth(Context context, Date date){
        Intent intent = new Intent(context, OverviewActivity.class);
        intent.putExtra(MONTH, date);
        return intent;
    }

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_overview;
    }

    @Override
    protected void init(){ Log.d("REALMZ1", "on init for overview activity");
        instance = this;

        currentMonth = DateUtil.refreshDate((Date)(getIntent().getSerializableExtra(MONTH)));

        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setText(DateUtil.convertDateToStringFormat2(getApplicationContext(), currentMonth));
        totalCostForMonth = (TextView) findViewById(R.id.totalCostTextView);

        getDefaultCurrency();

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
            getSupportActionBar().setTitle(R.string.monthly_overview);
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
        tabLayout.addTab(tabLayout.newTab().setText(R.string.category_expense));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.category_income));
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
                changeTopPanelInfo(tab.getPosition(), true);
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
        //barChartFragment = BarChartFragment.newInstance(new ArrayList<Category>());
        //percentChartFragment = PercentChartFragment.newInstance(new ArrayList<Category>());
        pieChartFragment = PieChartFragment.newInstance(new ArrayList<Category>(), false, false, getString(R.string.category));
        getSupportFragmentManager().beginTransaction().add(R.id.chartContentFrame, pieChartFragment).commit();
    }

    @Override
    public void onComplete(BudgetType type, List<Category> categoryList, float totalCost, boolean animate){
        if(type == BudgetType.EXPENSE){
            totalExpenseCost = totalCost;
            expenseCategoryList = categoryList;

            //set default tab to be the EXPENSE (ie position = 0)
            //Put this here so that it only gets called once.
            changeTopPanelInfo(0, animate);
        }else{
            totalIncomeCost = totalCost;
            incomeCategoryList = categoryList;
        }
    }

    /**
     * Change chart and total cost information that is in the top panel
     * @param position The tab position
     * @param animate To animate the pie chart or not
     */
    private void changeTopPanelInfo(int position, boolean animate){
        if(position == 0){
            //Set total cost for month
            totalCostForMonth.setText(CurrencyTextFormatter.formatFloat(totalExpenseCost, currentCurrency));
            pieChartFragment.setData(expenseCategoryList, animate);

            if(totalExpenseCost < 0){
                totalCostForMonth.setTextColor(ContextCompat.getColor(instance, R.color.red));
            }else{
                totalCostForMonth.setTextColor(Colors.getColorFromAttr(instance, R.attr.themeColorText));
            }
        }else if(position == 1){
            //Set total cost for month
            totalCostForMonth.setText(CurrencyTextFormatter.formatFloat(totalIncomeCost, currentCurrency));
            pieChartFragment.setData(incomeCategoryList, animate);

            if(totalIncomeCost > 0){
                totalCostForMonth.setTextColor(ContextCompat.getColor(instance, R.color.green));
            }else{
                totalCostForMonth.setTextColor(Colors.getColorFromAttr(instance, R.attr.themeColorText));
            }
        }
    }

    private void getDefaultCurrency(){
        final Realm myRealm = Realm.getDefaultInstance();

        currentCurrency = myRealm.where(BudgetCurrency.class).equalTo("isDefault", true).findFirst();
        if(currentCurrency == null){
            currentCurrency = new BudgetCurrency();
            currentCurrency.setCurrencyCode(Constants.DEFAULT_CURRENCY_CODE);
            currentCurrency.setCurrencyName(Constants.DEFAULT_CURRENCY_NAME);
        }else{
            currentCurrency = myRealm.copyFromRealm(currentCurrency);
        }

        myRealm.close();
        Toast.makeText(getApplicationContext(), "default currency : "+currentCurrency.getCurrencyName(), Toast.LENGTH_LONG).show();
        //updateCost(category.getBudget(), currentCurrency);
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

    //Remove menu for now
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview_chart, menu);
        //return true;
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.pdfMaker:
                Toast.makeText(getApplicationContext(), "click here pdf maker", Toast.LENGTH_SHORT).show();
                Intent pdfIntent = new Intent(getApplicationContext(), PdfActivity.class);
                startActivity(pdfIntent);
                return true;*/
            case R.id.percentChart:
                //Toast.makeText(getApplicationContext(), "click here percent chart", Toast.LENGTH_SHORT).show();
                replaceFragment(percentChartFragment);
                return true;
            case R.id.barChart:
                //Toast.makeText(getApplicationContext(), "click here bar chart", Toast.LENGTH_SHORT).show();
                replaceFragment(barChartFragment);
                return true;
            case R.id.pieChart:
                //Toast.makeText(getApplicationContext(), "click here pie chart", Toast.LENGTH_SHORT).show();
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


