package com.zhan.budget.Fragment;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.View.CustomViewPager;

import java.util.Date;

public class CategoryFragment extends BaseFragment {

    private static final String TAG = "CategoryFragment";
    private OnCategoryInteractionListener mListener;
    private Date currentMonth;
    private CategoryGenericFragment categoryIncomeFragment, categoryExpenseFragment;

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

        categoryExpenseFragment = CategoryGenericFragment.newInstance(BudgetType.EXPENSE, CategoryGenericRecyclerAdapter.ARRANGEMENT.BUDGET);
        categoryIncomeFragment = CategoryGenericFragment.newInstance(BudgetType.INCOME, CategoryGenericRecyclerAdapter.ARRANGEMENT.BUDGET);

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
