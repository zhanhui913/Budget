package com.zhan.budget.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Zhan on 16-05-19.
 */
public class FourPageViewPager extends FragmentPagerAdapter {
    private Fragment page1, page2, page3, page4;

    public FourPageViewPager(FragmentManager fm, Fragment page1, Fragment page2, Fragment page3, Fragment page4) {
        super(fm);
        this.page1 = page1;
        this.page2 = page2;
        this.page3 = page3;
        this.page4 = page4;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return this.page1;
            case 1:
                return this.page2;
            case 2:
                return this.page3;
            case 3:
                return this.page4;
        }
        return new Fragment();
    }

    @Override
    public int getCount() {
        return 4;
    }
}
