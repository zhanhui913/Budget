package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.OverviewActivity;
import com.zhan.budget.Activity.SelectCurrencyActivity;
import com.zhan.budget.Adapter.MonthReportRecyclerAdapter;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.MonthReport;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
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

    private BudgetCurrency defaultCurrency;

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

        getDefaultCurrency();

        categoryList = new ArrayList<>();
        monthReportList = new ArrayList<>();
        monthReportListview = (RecyclerView) view.findViewById(R.id.monthReportListview);
        monthReportListview.setLayoutManager(new LinearLayoutManager(getActivity()));

        monthReportRecyclerAdapter = new MonthReportRecyclerAdapter(this, monthReportList, defaultCurrency);
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

    private void getDefaultCurrency(){
        final Realm myRealm = Realm.getDefaultInstance();

        defaultCurrency = myRealm.where(BudgetCurrency.class).equalTo("isDefault", true).findFirst();
        if(defaultCurrency == null){
            defaultCurrency = new BudgetCurrency();
            defaultCurrency.setCurrencyCode(SelectCurrencyActivity.DEFAULT_CURRENCY_CODE);
            defaultCurrency.setCurrencyName(SelectCurrencyActivity.DEFAULT_CURRENCY_NAME);
        }else {
            defaultCurrency = myRealm.copyFromRealm(defaultCurrency);
        }

        Toast.makeText(getContext(), "month report fragment; default currency : "+defaultCurrency.getCurrencyName(), Toast.LENGTH_LONG).show();
        myRealm.close();
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
                                    monthReportList.get(a).addCostThisMonth(CurrencyTextFormatter.convertCurrency(transactionList.get(i).getPrice(), transactionList.get(i).getRate()));
                                }
                            }
                        }else if(transactionList.get(i).getCategory().getType().equalsIgnoreCase(BudgetType.INCOME.toString())) {
                            int month = DateUtil.getMonthFromDate(transactionList.get(i).getDate());

                            for (int a = 0; a < monthReportList.size(); a++) {
                                if (month == DateUtil.getMonthFromDate(monthReportList.get(a).getMonth())) {
                                    monthReportList.get(a).addIncomeThisMonth(CurrencyTextFormatter.convertCurrency(transactionList.get(i).getPrice(), transactionList.get(i).getRate()));
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
                    monthReportList.get(i).setDoneCalculation(true);
                }

                monthReportRecyclerAdapter.setMonthReportList(monthReportList);

                endTime = System.nanoTime();
                duration = (endTime - startTime);
                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);
                Log.d("DEBUG_MONTH", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
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

    private void updateYearInToolbar(int direction){
        currentYear = DateUtil.getYearWithDirection(currentYear, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat3(getContext(), currentYear));
        getMonthReport();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listener
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickMonth(int position){
        startActivity(OverviewActivity.createIntentToViewOverviewOnMonth(getContext(), monthReportList.get(position).getMonth()));
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
