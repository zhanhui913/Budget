package com.zhan.budget.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Zhan on 16-01-11.
 */
public class TwoPageViewPager extends FragmentPagerAdapter {
    private Fragment firstPage;
    private Fragment secondPage;

    public TwoPageViewPager(FragmentManager fm, Fragment firstPage, Fragment secondPage) {
        super(fm);
        this.firstPage = firstPage;
        this.secondPage = secondPage;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return this.firstPage;
            case 1:
                return this.secondPage;
        }
        return new Fragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
