package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Adapter.CategorySection;
import com.zhan.budget.Adapter.CategorySectionAdapter;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;


public class CategoryFragment1 extends BaseRealmFragment {

    private static final String TAG = "CategoryFragment";
    private OnCategoryInteractionListener mListener;
    private Date currentMonth;
    private CategoryGenericFragment categoryIncomeFragment, categoryExpenseFragment;

    private CategorySectionAdapter categorySectionAdapter;
    private RecyclerView categoryListView;

    private LinearLayoutManager linearLayoutManager;
    private SwipeLayout currentSwipeLayoutTarget;

    private TextView leftTextView, rightTextView;

    private PieChartFragment pieChartFragment;

    private boolean isCategoryIncomeCalculationComplete = false;
    private boolean isCategoryExpenseCalculationComplete = false;
    private float totalExpenseCost = 0f;
    private float totalIncomeCost = 0f;

    public CategoryFragment1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_category1;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();
        setHasOptionsMenu(true);

        currentMonth = new Date();

        leftTextView = (TextView) view.findViewById(R.id.leftTextView);
        rightTextView = (TextView) view.findViewById(R.id.rightTextView);

        pieChartFragment = PieChartFragment.newInstance(new ArrayList<Category>(), true, true, getString(R.string.category));
        getFragmentManager().beginTransaction().replace(R.id.chartContentFrame, pieChartFragment).commit();
        pieChartFragment.displayLegend();

        bothCategoryList = new ArrayList<>();
        expenseCategoryList = new ArrayList<>();
        incomeCategoryList = new ArrayList<>();
        transactionMonthList = new ArrayList<>();

        //0 represents no change in month relative to currentMonth variable.
        //false because we dont need to get all transactions yet.
        //This may conflict with populateCategoryWithNoInfo async where its trying to get the initial
        //categories
        //updateMonthInToolbar(0, false);

        createSectionCategoryListView();
    }

    private void createSectionCategoryListView(){
        categorySectionAdapter = new CategorySectionAdapter(this, CategorySection.ARRANGEMENT.BUDGET);
        categorySectionAdapter.setExpenseCategoryList(new ArrayList<Category>());
        categorySectionAdapter.setIncomeCategoryList(new ArrayList<Category>());

        linearLayoutManager = new LinearLayoutManager(getActivity());

        categoryListView = (RecyclerView) view.findViewById(R.id.categoryListView);
        categoryListView.setLayoutManager(linearLayoutManager);
        categoryListView.setAdapter(categorySectionAdapter);

        //Add divider
        /*categoryListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());
*/
        populateCategoryWithNoInfo();
    }

    private List<Category> bothCategoryList;
    private List<Category> expenseCategoryList;
    private List<Category> incomeCategoryList;
    private List<Transaction> transactionMonthList;

    //Should be called only the first time when the fragment is created
    private void populateCategoryWithNoInfo(){

        myRealm.where(Category.class).findAllAsync().addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListeners();

                bothCategoryList = myRealm.copyFromRealm(element);

                for(int i = 0; i < bothCategoryList.size(); i++){
                    if(bothCategoryList.get(i).getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                        expenseCategoryList.add(bothCategoryList.get(i));
                    }else if(bothCategoryList.get(i).getType().equalsIgnoreCase(BudgetType.INCOME.toString())){
                        incomeCategoryList.add(bothCategoryList.get(i));
                    }
                }

                //Sort based on 'index'
                Collections.sort(expenseCategoryList, new Comparator<Category>() {
                    @Override
                    public int compare(Category c1, Category c2) {
                        //ascending order
                        return (c1.getIndex() - c2.getIndex());
                    }
                });

                Collections.sort(incomeCategoryList, new Comparator<Category>() {
                    @Override
                    public int compare(Category c1, Category c2) {
                        //ascending order
                        return (c1.getIndex() - c2.getIndex());
                    }
                });

                categorySectionAdapter.setExpenseCategoryList(expenseCategoryList);
                categorySectionAdapter.setIncomeCategoryList(incomeCategoryList);

                populateCategoryWithInfo();
            }
        });
    }

    private void populateCategoryWithInfo(){
        final Date startMonth = DateUtil.refreshMonth(currentMonth);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = DateUtil.getLastDateOfMonth(currentMonth);

        Log.d("DEBUG","Get all transactions from month is "+startMonth.toString()+", to next month is "+endMonth.toString());

        myRealm.where(Transaction.class).between("date", startMonth, endMonth).equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync().addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListeners();

                Log.d("REALM", "got this month transaction, " + element.size());

                transactionMonthList = myRealm.copyFromRealm(element);

                aggregateCategoryInfo();
            }
        });
    }

    private void aggregateCategoryInfo(){
        Log.d("DEBUG", "1) There are " + expenseCategoryList.size() + " expense categories");
        Log.d("DEBUG", "1) There are " + incomeCategoryList.size() + " income categories");
        Log.d("DEBUG", "1) There are " + transactionMonthList.size() + " transactions for this month");

        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("DEBUG", "preparing to aggregate results");
            }

            @Override
            protected Void doInBackground(Void... voids) {

                startTime = System.nanoTime();



                //Go through each COMPLETED transaction and put them into the correct category
                for(int t = 0; t < transactionMonthList.size(); t++){
                    for(int c = 0 ; c < bothCategoryList.size(); c++){
                        if(transactionMonthList.get(t).getCategory() != null){
                            if(transactionMonthList.get(t).getCategory().getId().equalsIgnoreCase(bothCategoryList.get(c).getId())){
                                double transactionPrice = transactionMonthList.get(t).getPrice();
                                double currentCategoryPrice = bothCategoryList.get(c).getCost();
                                bothCategoryList.get(c).setCost(transactionPrice + currentCategoryPrice);
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                for(int i = 0; i < bothCategoryList.size(); i++){
                    Log.d("ZHAN1", "category : "+bothCategoryList.get(i).getName()+" -> "+bothCategoryList.get(i).getCost());
                }




                for(int b = 0; b < bothCategoryList.size(); b++){
                    if(bothCategoryList.get(b).getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                        for(int e = 0 ; e < expenseCategoryList.size(); e++){
                            if(bothCategoryList.get(b).getId().equalsIgnoreCase(expenseCategoryList.get(e).getId())){
                                expenseCategoryList.set(e, bothCategoryList.get(b));
                            }
                        }
                    }else if(bothCategoryList.get(b).getType().equalsIgnoreCase(BudgetType.INCOME.toString())){
                        for(int i = 0 ; i < incomeCategoryList.size(); i++){
                            if(bothCategoryList.get(b).getId().equalsIgnoreCase(incomeCategoryList.get(i).getId())){
                                incomeCategoryList.set(i, bothCategoryList.get(b));
                            }
                        }
                    }
                }

                //Sort based on 'index'
                Collections.sort(expenseCategoryList, new Comparator<Category>() {
                    @Override
                    public int compare(Category c1, Category c2) {
                        //ascending order
                        return (c1.getIndex() - c2.getIndex());
                    }
                });

                Collections.sort(incomeCategoryList, new Comparator<Category>() {
                    @Override
                    public int compare(Category c1, Category c2) {
                        //ascending order
                        return (c1.getIndex() - c2.getIndex());
                    }
                });

                categorySectionAdapter.setExpenseCategoryList(expenseCategoryList);
                categorySectionAdapter.setIncomeCategoryList(incomeCategoryList);

                //Calculate total Expense and Income cost
                totalExpenseCost = 0f;
                totalIncomeCost = 0f;
                for(int i = 0; i < expenseCategoryList.size(); i++){
                    totalExpenseCost += expenseCategoryList.get(i).getCost();
                }

                for(int i = 0; i < incomeCategoryList.size(); i++){
                    totalIncomeCost += incomeCategoryList.get(i).getCost();
                }

                updateExpensePriceStatus(totalExpenseCost);
                updateIncomePriceStatus(totalIncomeCost);

                //update piechart
                updatePieChart();

                endTime = System.nanoTime();
                duration = (endTime - startTime);

                long milli = (duration/1000000);
                long second = (milli/1000);
                float minutes = (second / 60.0f);
                Log.d("DEBUG_CAT", " aggregating took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
    }

    /**
     * Updates pie chart once both Expense and Income Category has been calculated
     */
    private void updatePieChart() {
        //If both EXPENSE and INCOME calculation are completed, do this
        //if(isCategoryIncomeCalculationComplete && isCategoryExpenseCalculationComplete){
            List<Category> catList = new ArrayList<>();

            int[] colorList = new int[]{R.color.alizarin, R.color.nephritis};

            Category catExpense = new Category();
            catExpense.setName("Expense");
            catExpense.setCost(Math.abs(totalExpenseCost));
            catExpense.setColor(getResources().getString(colorList[0]));

            Category catIncome = new Category();
            catIncome.setName("Income");
            catIncome.setCost(Math.abs(totalIncomeCost));
            catIncome.setColor(getResources().getString(colorList[1]));

            catList.add(catIncome);
            catList.add(catExpense);

            pieChartFragment.setData(catList, true);
        //}
    }

    private void updateMonthInToolbar(int direction, boolean updateCategoryInfo){
        //reset pie chart data & total cost text view for both EXPENSE & INCOME
        pieChartFragment.resetPieChart();
        updateBothPriceStatus(0); //reset it back to 0

        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(getContext(), currentMonth));

        if(updateCategoryInfo) {
            resetCategoryInfo();

            populateCategoryWithInfo();
        }
    }

    private void resetCategoryInfo(){
        //Reset EXPENSE list
        for(int i = 0; i < categorySectionAdapter.getExpenseCategoryList().size(); i++){
            categorySectionAdapter.getExpenseCategoryList().get(i).setCost(0);
        }

        //Reset INCOME list
        for(int i = 0; i < categorySectionAdapter.getIncomeCategoryList().size(); i++){
            categorySectionAdapter.getIncomeCategoryList().get(i).setCost(0);
        }

        categorySectionAdapter.notifyDataSetChanged();
    }

    private void updateBothPriceStatus(double price){
        updateExpensePriceStatus(price);
        updateIncomePriceStatus(price);
    }

    private void updateExpensePriceStatus(double price){
        leftTextView.setText(CurrencyTextFormatter.formatDouble(price));

        if(price < 0){
            leftTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }else if(price == 0){
            leftTextView.setTextColor(Colors.getColorFromAttr(getContext(), R.attr.themeColorText));
        }
    }

    private void updateIncomePriceStatus(double price){
        rightTextView.setText(CurrencyTextFormatter.formatDouble(price));

        if(price > 0){
            rightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }else if(price == 0){
            rightTextView.setTextColor(Colors.getColorFromAttr(getContext(), R.attr.themeColorText));
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
        if (context instanceof OnCategoryInteractionListener) {
            mListener = (OnCategoryInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCategoryInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                updateMonthInToolbar(-1, true);
                return true;
            case R.id.rightChevron:
                updateMonthInToolbar(1, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public interface OnCategoryInteractionListener {
        void updateToolbar(String date);
    }
}
