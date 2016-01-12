package com.zhan.budget.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zhan.budget.Fragment.TransactionExpenseFragment;
import com.zhan.budget.Fragment.TransactionIncomeFragment;

/**
 * Created by Zhan on 16-01-11.
 */
public class TransactionViewPager extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;


    public TransactionViewPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = new Fragment();
        switch(position){
            case 0:
                f = TransactionExpenseFragment.newInstance();
                break;
            case 1:
                f = TransactionIncomeFragment.newInstance();
                break;
        }
        return f;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
