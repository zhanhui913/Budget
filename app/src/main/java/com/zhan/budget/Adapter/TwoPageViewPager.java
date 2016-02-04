package com.zhan.budget.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zhan.budget.Fragment.BaseFragment;
import com.zhan.budget.Fragment.TransactionExpenseFragment;
import com.zhan.budget.Fragment.TransactionIncomeFragment;

/**
 * Created by Zhan on 16-01-11.
 */
public class TwoPageViewPager extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;
    private BaseFragment firstPage;
    private BaseFragment secondPage;

    public TwoPageViewPager(FragmentManager fm, BaseFragment firstPage, BaseFragment secondPage) {
        super(fm);
        this.firstPage = firstPage;
        this.secondPage = secondPage;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = new Fragment();
        switch(position){
            case 0:
                f = this.firstPage.newInstance();
                break;
            case 1:
                f = this.secondPage.newInstance();
                break;
        }
        return f;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
