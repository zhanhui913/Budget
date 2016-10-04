package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.OverviewActivity;
import com.zhan.budget.Adapter.MonthReportRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.MonthReport;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonthReportFragment extends BaseRealmFragment implements
        MonthReportRecyclerAdapter.OnMonthReportAdapterInteractionListener{

    private static final String TAG = "MonthlyFragment";

    private OnMonthlyInteractionListener mListener;

    private List<MonthReport> monthReportList;
    private RecyclerView monthReportListview;
    private MonthReportRecyclerAdapter monthReportRecyclerAdapter;

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
        categoryList = new ArrayList<>();
        monthReportList = new ArrayList<>();
        monthReportListview = (RecyclerView) view.findViewById(R.id.monthReportListview);
        monthReportListview.setLayoutManager(new LinearLayoutManager(getActivity()));

        monthReportRecyclerAdapter = new MonthReportRecyclerAdapter(this, monthReportList);
        monthReportListview.setAdapter(monthReportRecyclerAdapter);

        //Add divider
        monthReportListview.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.right_padding_divider, R.dimen.right_padding_divider)
                        .build());

        currentYear = DateUtil.refreshYear(new Date());

        createMonthCard();
        getCategoryList();
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
        endYear = DateUtil.getLastDateOfYear(beginYear);

        Log.d(TAG, "get report from " + beginYear.toString() + " to " + endYear.toString());

        //Reset all values in list
        for(int i = 0 ; i < monthReportList.size(); i++){
            monthReportList.get(i).setMonth(DateUtil.getMonthWithDirection(beginYear, i));
            monthReportList.get(i).setDoneCalculation(false);
            monthReportList.get(i).setCostThisMonth(0);
            monthReportList.get(i).setIncomeThisMonth(0);
            monthReportList.get(i).setFirstCategory(null);
            monthReportList.get(i).setSecondCategory(null);
            monthReportList.get(i).setThirdCategory(null);
        }
        monthReportRecyclerAdapter.setMonthReportList(monthReportList);

        transactionsResults = myRealm.where(Transaction.class).between("date", beginYear, endYear).equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                if (transactionList != null) {
                    transactionList.clear();
                }

                transactionList = myRealm.copyFromRealm(element);

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
                    if(transactionList.get(i).getCategory() != null){
                        if(transactionList.get(i).getCategory().getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())) {
                            int month = DateUtil.getMonthFromDate(transactionList.get(i).getDate());

                            for (int a = 0; a < monthReportList.size(); a++) {
                                if (month == DateUtil.getMonthFromDate(monthReportList.get(a).getMonth())) {
                                    monthReportList.get(a).addCostThisMonth(transactionList.get(i).getPrice());
                                }
                            }
                        }else if(transactionList.get(i).getCategory().getType().equalsIgnoreCase(BudgetType.INCOME.toString())) {
                            int month = DateUtil.getMonthFromDate(transactionList.get(i).getDate());

                            for (int a = 0; a < monthReportList.size(); a++) {
                                if (month == DateUtil.getMonthFromDate(monthReportList.get(a).getMonth())) {
                                    monthReportList.get(a).addIncomeThisMonth(transactionList.get(i).getPrice());
                                }
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
                    //performTediousCalculation(i);
                    monthReportList.get(i).setDoneCalculation(true);
                }

                monthReportRecyclerAdapter.setMonthReportList(monthReportList);

                endTime = System.nanoTime();
                duration = (endTime - startTime);
                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);
                Log.d("DEBUG_MONTH", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");

                //performTediousCalculation(0);
            }
        };
        loader.execute();
    }

    //Should be called only the first time when the activity is created
    private void getCategoryList(){
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListener(this);

                categoryList = myRealm.copyFromRealm(element);
            }
        });
    }
/*
    private void performTediousCalculation(final int indexOfMonth){
        final Date month = DateUtil.refreshMonth(monthReportList.get(0).getMonth());
        final Date endMonth = DateUtil.getLastDateOfMonth(monthReportList.get(11).getMonth()) ;

        final RealmResults<Transaction> newTransactionsResults = myRealm.where(Transaction.class).between("date", month, endMonth).findAllSortedAsync("date");
        newTransactionsResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                Log.d("WHO", indexOfMonth+" ("+month+", "+endMonth+") ->"+element.size());

                s1(myRealm.copyFromRealm(element));
            }
        });
    }

    private void s1(List<Transaction> ttList){
        List<Transaction> janList = new ArrayList<>();
        List<Transaction> febList = new ArrayList<>();
        List<Transaction> marList = new ArrayList<>();
        List<Transaction> aprList = new ArrayList<>();
        List<Transaction> mayList = new ArrayList<>();
        List<Transaction> junList = new ArrayList<>();
        List<Transaction> julList = new ArrayList<>();
        List<Transaction> augList = new ArrayList<>();
        List<Transaction> sepList = new ArrayList<>();
        List<Transaction> octList = new ArrayList<>();
        List<Transaction> novList = new ArrayList<>();
        List<Transaction> decList = new ArrayList<>();

        //group all transactions in the year into months
        for(int i = 0; i < ttList.size(); i++){
            if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 0){
                //January
                janList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 1){
                //February
                febList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 2){
                //March
                marList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 3){
                //April
                aprList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 4){
                //May
                mayList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 5){
                //June
                junList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 6){
                //July
                julList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 7){
                //August
                augList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 8){
                //September
                sepList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 9){
                //October
                octList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 10){
                //November
                novList.add(ttList.get(i));
            }else if(DateUtil.getMonthFromDate(ttList.get(i).getDate()) == 11){
                //December
                decList.add(ttList.get(i));
            }
        }

        ss(0, janList);
        ss(1, febList);
        ss(2, marList);
        ss(3, aprList);
        ss(4, mayList);
        ss(5, junList);
        ss(6, julList);
        ss(7, augList);
        ss(8, sepList);
        ss(9, octList);
        ss(10, novList);
        ss(11, decList);

    }

    private void ss(final int month, final List<Transaction> ttList){
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListener(this);

                List<Category> categoryList1 = myRealm.copyFromRealm(element);

                Log.d("CAT_CAL", "before calling categoryCalculator : there are "+categoryList1.size());
                CategoryCalculator cc = new CategoryCalculator(ttList, categoryList1, monthReportList.get(month).getMonth(), new CategoryCalculator.OnCategoryCalculatorInteractionListener() {
                    @Override
                    public void onCompleteCalculation(List<Category> catList) {
                        Log.d("CAT_CAL", monthReportList.get(month).getMonth()+" -> COMPLETED, size: "+catList.size());
                        //Log.d("WHO", "top cat : "+catList.get(0).getName()+" with cost : "+catList.get(0).getCost());

                        if(catList.size() >= 1){
                            monthReportList.get(month).setFirstCategory(catList.get(0));
                        }

                        if(catList.size() >= 2){
                            monthReportList.get(month).setSecondCategory(catList.get(1));
                        }

                        if(catList.size() >= 3){
                            monthReportList.get(month).setThirdCategory(catList.get(2));
                        }

                        monthReportRecyclerAdapter.setMonthReportList(monthReportList);
                    }
                });
                cc.execute();
            }
        });
    }*/

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
        //startActivityForResult(overviewActivity, Constants.RETURN_NEW_OVERVIEW);
        startActivity(overviewActivity);
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
