package com.zhan.budget.Activity.Settings;

import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zhan.budget.Activity.BaseActivity;
import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Fragment.CategoryGenericFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.R;
import com.zhan.budget.View.CustomViewPager;

public class SettingsCategory extends BaseActivity {

    private  Toolbar toolbar;
    private CategoryGenericFragment categoryIncomeFragment, categoryExpenseFragment;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_settings_category;
    }

    @Override
    protected void init(){
        createToolbar();
        createTabs();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createTabs(){
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(BudgetType.EXPENSE.toString()));
        tabLayout.addTab(tabLayout.newTab().setText(BudgetType.INCOME.toString()));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        categoryExpenseFragment = CategoryGenericFragment.newInstance(BudgetType.EXPENSE, CategoryGenericRecyclerAdapter.ARRANGEMENT.MOVE);
        categoryIncomeFragment = CategoryGenericFragment.newInstance(BudgetType.INCOME, CategoryGenericRecyclerAdapter.ARRANGEMENT.MOVE);

        final CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        viewPager.setPagingEnabled(false);

        TwoPageViewPager adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), categoryExpenseFragment, categoryIncomeFragment);
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
}
