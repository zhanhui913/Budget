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

import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.BaseChartFragment;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Fragment.OverviewGenericFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.View.CustomViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OverviewActivity extends BaseActivity {

    public static final String MONTH = "View Overview Month";

    //Charts
    private PieChartFragment pieChartFragment;

    //Expense Income Fragment to list view
    private OverviewGenericFragment overviewExpenseFragment, overviewIncomeFragment;

    private Activity instance;
    private Toolbar toolbar;
    private Date currentMonth;
    private TextView dateTextView, totalCostForMonth;
    private CustomViewPager viewPager;
    private TabLayout tabLayout;

    private float totalExpenseCost = 0f;
    private float totalIncomeCost = 0f;
    private List<Category> expenseCategoryList;
    private List<Category> incomeCategoryList;

    private int currentTabPosition = 0; //EXPENSE tab (ie: position = 0) is default

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

        dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setText(DateUtil.convertDateToStringFormat2(getApplicationContext(), currentMonth));
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

        overviewExpenseFragment.setListener(new OverviewGenericFragment.OverviewInteractionListener() {
            @Override
            public void onComplete(BudgetType type, List<Category> categoryList, float totalCost, boolean animate) {
                if(type == BudgetType.EXPENSE){
                    totalExpenseCost = totalCost;
                    expenseCategoryList = categoryList;
                }

                //Only if the current tab position is set to this do we update the pie chart and
                //other info visually
                if(currentTabPosition == 0){
                    changeTopPanelInfo(0, animate);
                }
            }
        });

        overviewIncomeFragment.setListener(new OverviewGenericFragment.OverviewInteractionListener() {
            @Override
            public void onComplete(BudgetType type, List<Category> categoryList, float totalCost, boolean animate) {
                if(type == BudgetType.INCOME){
                    totalIncomeCost = totalCost;
                    incomeCategoryList = categoryList;
                }

                //Only if the current tab position is set to this do we update the pie chart and
                //other info visually
                if(currentTabPosition == 1){
                    changeTopPanelInfo(1, animate);
                }
            }
        });

        viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        viewPager.setPagingEnabled(false);

        TwoPageViewPager adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), overviewExpenseFragment, overviewIncomeFragment);
        viewPager.setAdapter(adapterViewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
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
        pieChartFragment = PieChartFragment.newInstance(new ArrayList<Category>(), false, false, getString(R.string.category));
        getSupportFragmentManager().beginTransaction().add(R.id.chartContentFrame, pieChartFragment).commit();
    }

    /**
     * Change chart and total cost information that is in the top panel
     * @param position The tab position
     * @param animate To animate the pie chart or not
     */
    private void changeTopPanelInfo(int position, boolean animate){
        if(position == 0){
            //Set total cost for month
            totalCostForMonth.setText(CurrencyTextFormatter.formatDouble(totalExpenseCost));
            pieChartFragment.setData(expenseCategoryList, animate);

            if(totalExpenseCost < 0){
                totalCostForMonth.setTextColor(ContextCompat.getColor(instance, R.color.red));
            }else{
                totalCostForMonth.setTextColor(Colors.getColorFromAttr(instance, R.attr.themeColorText));
            }
        }else if(position == 1){
            //Set total cost for month
            totalCostForMonth.setText(CurrencyTextFormatter.formatDouble(totalIncomeCost));
            pieChartFragment.setData(incomeCategoryList, animate);

            if(totalIncomeCost > 0){
                totalCostForMonth.setTextColor(ContextCompat.getColor(instance, R.color.green));
            }else{
                totalCostForMonth.setTextColor(Colors.getColorFromAttr(instance, R.attr.themeColorText));
            }
        }
    }

    /**
     * Updates the month, this will update the text in the toolbar and the results.
     */
    private void updateMonth(int direction){ Log.d("PIE","move, current pos "+currentTabPosition);
        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);

        dateTextView.setText(DateUtil.convertDateToStringFormat2(getApplicationContext(), currentMonth));

        //Update month
        overviewExpenseFragment.setCurrentMonth(currentMonth);
        overviewIncomeFragment.setCurrentMonth(currentMonth);

        //Re-calculate
        overviewExpenseFragment.getCategoryList();
        overviewIncomeFragment.getCategoryList();
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
        getMenuInflater().inflate(R.menu.change_month_year, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.pdfMaker:
                Toast.makeText(getApplicationContext(), "click here pdf maker", Toast.LENGTH_SHORT).show();
                Intent pdfIntent = new Intent(getApplicationContext(), PdfActivity.class);
                startActivity(pdfIntent);
                return true;
            case R.id.pieChart:
                //Toast.makeText(getApplicationContext(), "click here pie chart", Toast.LENGTH_SHORT).show();
                replaceFragment(pieChartFragment);
                return true;*/
            case R.id.leftChevron:
                updateMonth(-1);
                return true;
            case R.id.rightChevron:
                updateMonth(1);
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


