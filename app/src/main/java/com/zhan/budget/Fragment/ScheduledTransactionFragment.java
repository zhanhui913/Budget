package com.zhan.budget.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.zhan.budget.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduledTransactionFragment extends BaseRealmFragment {

    private static final String TAG = "ScheduledTranFragment";


    public ScheduledTransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_scheduled_transaction;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();


    }

}
