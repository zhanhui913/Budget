package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import com.zhan.budget.Activity.OverviewActivity;
import com.zhan.budget.Adapter.MonthReportGridAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.BarChartFragment;
import com.zhan.budget.Fragment.Chart.PercentChartFragment;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.MonthReport;
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

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonthReportFragment extends BaseRealmFragment implements
        MonthReportGridAdapter.OnMonthReportAdapterInteractionListener{

    private static final String TAG = "MonthlyFragment";

    private OnMonthlyInteractionListener mListener;

    private List<MonthReport> monthReportList;
    private GridView monthReportGridView;
    private MonthReportGridAdapter monthReportGridAdapter;

    private RealmResults<Transaction> transactionsResults;
    private List<Transaction> transactionList;

    private Date currentYear;

    private Date beginYear;
    private Date endYear;

    private List<Category> categoryList;
    private RealmResults<Category> resultsCategory;


    public MonthReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_month_report;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();

        monthReportList = new ArrayList<>();
        monthReportGridView = (GridView) view.findViewById(R.id.monthReportGridView);
        monthReportGridAdapter = new MonthReportGridAdapter(this, monthReportList);
        monthReportGridView.setAdapter(monthReportGridAdapter);

        currentYear = DateUtil.refreshYear(new Date());

        createMonthCard();
        updateYearInToolbar(0);
    }

    /**
     * Gets called once to initialize the card view for all months
     */
    private void createMonthCard(){
        for(int i = 0; i < 12; i++){
            MonthReport monthReport = new MonthReport();
            monthReport.setDoneCalculation(false); //default
            monthReport.setMonth(DateUtil.getMonthWithDirection(currentYear, i));
            monthReportList.add(monthReport);
        }
    }

    /**
     * Gets called whenever you update the year
     */
    private void getMonthReport(){
        //Refresh these variables
        beginYear = DateUtil.refreshYear(currentYear);

        //Need to go a day before as Realm's between date does inclusive on both end
        endYear = DateUtil.getPreviousDate(DateUtil.getNextYear(beginYear));

        Log.d(TAG, "get report from " + beginYear.toString() + " to " + endYear.toString());

        //Reset all values in list
        for(int i = 0 ; i < monthReportList.size(); i++){
            monthReportList.get(i).setMonth(DateUtil.getMonthWithDirection(beginYear, i));
            monthReportList.get(i).setDoneCalculation(false);
            monthReportList.get(i).setCostThisMonth(0);
            monthReportList.get(i).setChangeCost(0);
        }
        monthReportGridAdapter.notifyDataSetChanged();

        transactionsResults = myRealm.where(Transaction.class).between("date", beginYear, endYear).findAllAsync();
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
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("MONTHLY_FRAGMENT", "preparing to aggregate results");
            }

            @Override
            protected Void doInBackground(Void... voids) {

                Log.d("MONTHLY_FRAGMENT", "Transaction size : "+transactionList.size());

                startTime = System.nanoTime();

                for (int i = 0; i < transactionList.size(); i++) {
                    //Only add Category of type EXPENSE
                    if(transactionList.get(i).getCategory().getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())) {
                        int month = DateUtil.getMonthFromDate(transactionList.get(i).getDate());

                        for (int a = 0; a < monthReportList.size(); a++) {
                            if (month == DateUtil.getMonthFromDate(monthReportList.get(a).getMonth())) {
                                monthReportList.get(a).addCostThisMonth(transactionList.get(i).getPrice());
                            }
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);

                //Change the variable to true
                for(int i = 0; i < monthReportList.size(); i++){
                    monthReportList.get(i).setDoneCalculation(true);
                }

                monthReportGridAdapter.notifyDataSetChanged();

                endTime = System.nanoTime();
                duration = (endTime - startTime);
                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);
                Log.d("MONTHLY_FRAGMENT", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
    }

    //Should be called only the first time when the activity is created
    private void getCategoryList(){
        final Realm myRealm = Realm.getDefaultInstance();
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                resultsCategory.removeChangeListener(this);

                categoryList.clear();
                categoryList = myRealm.copyFromRealm(resultsCategory);
                myRealm.close();

                //getMonthReport(currentMonth);
                performAsyncCalculation1();
            }
        });
    }

    /**
     * Perform tedious calculation asynchronously to avoid blocking main thread
     */
    private void performAsyncCalculation1(){
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

    private void updateYearInToolbar(int direction){
        currentYear = DateUtil.getYearWithDirection(currentYear, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat3(currentYear));
        getMonthReport();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listener
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickMonth(int position){
        Intent overviewActivity = new Intent(getContext(), OverviewActivity.class);
        overviewActivity.putExtra(Constants.REQUEST_NEW_OVERVIEW_MONTH, monthReportList.get(position).getMonth());
        startActivityForResult(overviewActivity, Constants.RETURN_NEW_OVERVIEW);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Etc
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.change_month_year, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.leftChevron:
                updateYearInToolbar(-1);
                return true;
            case R.id.rightChevron:
                updateYearInToolbar(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMonthlyInteractionListener) {
            mListener = (OnMonthlyInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMonthlyInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMonthlyInteractionListener {
        void updateToolbar(String date);
    }
}
