package com.zhan.budget.Fragment;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.View.CustomViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryFragment extends BaseFragment {

    private static final String TAG = "CategoryFragment";
    private OnCategoryInteractionListener mListener;
    private Date currentMonth;
    private CategoryGenericFragment categoryIncomeFragment, categoryExpenseFragment;

    private TextView leftTextView, rightTextView;

    private PieChartFragment pieChartFragment;

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

        pieChartFragment = PieChartFragment.newInstance(new ArrayList<Category>(), true, true, true, getString(R.string.category));
        getFragmentManager().beginTransaction().replace(R.id.chartContentFrame, pieChartFragment).commit();

        createTabs();

        //0 represents no change in month relative to currentMonth variable.
        //false because we dont need to get all transactions yet.
        //This may conflict with populateCategoryWithNoInfo async where its trying to get the initial
        //categories
        updateMonthInToolbar(0, false);
    }

    private void createTabs(){
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.category_expense)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.category_income)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        categoryExpenseFragment = CategoryGenericFragment.newInstance(BudgetType.EXPENSE, CategoryGenericRecyclerAdapter.ARRANGEMENT.BUDGET, false);
        categoryIncomeFragment = CategoryGenericFragment.newInstance(BudgetType.INCOME, CategoryGenericRecyclerAdapter.ARRANGEMENT.BUDGET, false);

        categoryExpenseFragment.setInteraction(new CategoryGenericFragment.OnCategoryGenericListener() {
            @Override
            public void onComplete(float totalCost) {
                isCategoryExpenseCalculationComplete = true;
                totalExpenseCost = totalCost;

                updateExpensePriceStatus(totalCost);
                updatePieChart();
            }
        });

        categoryIncomeFragment.setInteraction(new CategoryGenericFragment.OnCategoryGenericListener() {
            @Override
            public void onComplete(float totalCost) {
                isCategoryIncomeCalculationComplete = true;
                totalIncomeCost = totalCost;

                updateIncomePriceStatus(totalCost);
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

            pieChartFragment.setData(catList, true);
        }
    }

    private void updateMonthInToolbar(int direction, boolean updateCategoryInfo){
        //reset pie chart data & total cost text view for both EXPENSE & INCOME
        pieChartFragment.resetPieChart();
        updateBothPriceStatus(0); //reset it back to 0

        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(getContext(), currentMonth));

        if(updateCategoryInfo) {
            categoryIncomeFragment.updateMonthCategoryInfo(currentMonth);
            categoryExpenseFragment.updateMonthCategoryInfo(currentMonth);
        }
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
