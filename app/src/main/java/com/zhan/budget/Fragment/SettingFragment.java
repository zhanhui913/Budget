package com.zhan.budget.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.zhan.budget.Adapter.TransactionListAdapter;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {

    private View view;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init(){

    }

}
