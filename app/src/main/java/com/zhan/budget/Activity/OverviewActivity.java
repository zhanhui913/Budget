package com.zhan.budget.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.DateUtil;


import org.parceler.Parcels;

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
    protected void init(){ Log.d("REALMZ1", "on init for overview activity");
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
            getSupportActionBar().setTitle("Monthly Expenses");
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
        final Realm myRealm = Realm.getDefaultInstance();  BudgetPreference.addRealmCache(this);
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListener(this);

                categoryList.clear();
                categoryList = myRealm.copyFromRealm(element);
                myRealm.close();  BudgetPreference.removeRealmCache(getBaseContext());
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

        final Realm myRealm = Realm.getDefaultInstance();  BudgetPreference.addRealmCache(this);
        transactionsResults = myRealm.where(Transaction.class).between("date", month, endMonth).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                if (transactionList != null) {
                    transactionList.clear();
                }

                transactionList = myRealm.copyFromRealm(element);
                myRealm.close();  BudgetPreference.removeRealmCache(getBaseContext());
                performAsyncCalculation();
            }
        });
    }

    /**
     * Perform tedious calculation asynchronously to avoid blocking main thread
     */
    private void performAsyncCalculation(){
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
                pieChartFragment = PieChartFragment.newInstance(categoryList, true);
                getSupportFragmentManager().beginTransaction().add(R.id.chartContentFrame, barChartFragment).commit();

            }
        });
        cc.execute();
    }

    private void confirmDelete(final int position){
        // get alertdialog_generic_message.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);


        title.setText("Confirm Delete");
        message.setText("Are you sure you want to delete this category?");

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getBaseContext(), "DELETE...", Toast.LENGTH_SHORT).show();
                        deleteCategory(position);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void deleteCategory(int position){

    }

    private void editCategory(int position){
        Intent editCategoryActivity = new Intent(this, CategoryInfoActivity.class);

        Parcelable wrapped = Parcels.wrap(categoryList.get(position));

        editCategoryActivity.putExtra(Constants.REQUEST_EDIT_CATEGORY, wrapped);
        editCategoryActivity.putExtra(Constants.REQUEST_NEW_CATEGORY, false);

        startActivityForResult(editCategoryActivity, Constants.RETURN_EDIT_CATEGORY);
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
    public void onDeleteCategory(int position){
        confirmDelete(position);
    }

    @Override
    public void onEditCategory(int position){
        editCategory(position);
    }

    @Override
    public void onPullDownAllow(boolean value){
        //not being used
    }

    @Override
    public void onDoneDrag(){
        //not being used
    }

    @Override
    public void onClick(int position){
        Toast.makeText(this, "click on category :" + categoryList.get(position).getName(), Toast.LENGTH_SHORT).show();

        Intent viewAllTransactionsForCategory = new Intent(this, TransactionsForCategory.class);
        viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_MONTH, DateUtil.convertDateToString(currentMonth));

        Parcelable wrapped = Parcels.wrap(categoryList.get(position));

        viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY, wrapped);
        startActivity(viewAllTransactionsForCategory);
    }
}


