package com.zhan.budget.Fragment;

import android.os.Bundle;

/**
 * Created by zhanyap on 2017-04-19.
 */

public abstract class BaseMVPFragment extends BaseFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPresenter();
    }

    /**
     * Used to call the Presenter's start method
     */
    protected abstract void initPresenter();
}
