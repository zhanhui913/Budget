package com.zhan.budget.Fragment;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.View.CustomViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class CategoryFragment extends BaseFragment {

    private static final String TAG = "CategoryFragment";
    private OnCategoryInteractionListener mListener;
    private Date currentMonth;
    private CategoryGenericFragment categoryIncomeFragment, categoryExpenseFragment;

    private TextView leftTextView, rightTextView;

    private PieChartFragment pieChartFragment;

    private BudgetCurrency currentCurrency;

    private boolean isCategoryIncomeCalculationComplete = false;
    private boolean isCategoryExpenseCalculationComplete = false;
    private float totalExpenseCost = 0f;
    private float totalIncomeCost = 0f;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_category;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        setHasOptionsMenu(true);

        currentMonth = new Date();

        leftTextView = (TextView) view.findViewById(R.id.leftTextView);
        rightTextView = (TextView) view.findViewById(R.id.rightTextView);
        leftTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.alizarin));
        rightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.nephritis));

        getDefaultCurrency();

        createTabs();

        //0 represents no change in month relative to currentMonth variable.
        //false because we dont need to get all transactions yet.
        //This may conflict with populateCategoryWithNoInfo async where its trying to get the initial
        //categories
        updateMonthInToolbar(0, false);
    }

    private void createTabs(){
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(BudgetType.EXPENSE.toString()));
        tabLayout.addTab(tabLayout.newTab().setText(BudgetType.INCOME.toString()));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        categoryExpenseFragment = CategoryGenericFragment.newInstance(BudgetType.EXPENSE, CategoryGenericRecyclerAdapter.ARRANGEMENT.BUDGET, false);
        categoryIncomeFragment = CategoryGenericFragment.newInstance(BudgetType.INCOME, CategoryGenericRecyclerAdapter.ARRANGEMENT.BUDGET, false);

        categoryExpenseFragment.setInteraction(new CategoryGenericFragment.OnCategoryGenericListener() {
            @Override
            public void onComplete(float totalCost) {
                isCategoryExpenseCalculationComplete = true;
                totalExpenseCost = totalCost;
                leftTextView.setText(CurrencyTextFormatter.formatFloat(totalCost, currentCurrency));
                updatePieChart();
            }
        });

        categoryIncomeFragment.setInteraction(new CategoryGenericFragment.OnCategoryGenericListener() {
            @Override
            public void onComplete(float totalCost) {
                isCategoryIncomeCalculationComplete = true;
                totalIncomeCost = totalCost;
                rightTextView.setText(CurrencyTextFormatter.formatFloat(totalCost, currentCurrency));
                updatePieChart();
            }
        });

        final CustomViewPager viewPager = (CustomViewPager) view.findViewById(R.id.viewPager);
        viewPager.setPagingEnabled(false);

        TwoPageViewPager adapterViewPager = new TwoPageViewPager(getChildFragmentManager(), categoryExpenseFragment, categoryIncomeFragment);
        viewPager.setAdapter(adapterViewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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

        Toast.makeText(getContext(), "category fragment: default currency : "+currentCurrency.getCurrencyName(), Toast.LENGTH_LONG).show();
        myRealm.close();
    }

    /**
     * Updates pie chart once both Expense and Income Category has been calculated
     */
    private void updatePieChart() {
        //If both EXPENSE and INCOME calculation are completed, do this
        if(isCategoryIncomeCalculationComplete && isCategoryExpenseCalculationComplete){
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

            pieChartFragment = PieChartFragment.newInstance(catList, true, true);
            getFragmentManager().beginTransaction().replace(R.id.chartContentFrame, pieChartFragment).commit();
            pieChartFragment.displayLegend();

        }
    }

    private void updateMonthInToolbar(int direction, boolean updateCategoryInfo){
        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(currentMonth));

        if(updateCategoryInfo) {
            categoryIncomeFragment.updateMonthCategoryInfo(currentMonth);
            categoryExpenseFragment.updateMonthCategoryInfo(currentMonth);
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
