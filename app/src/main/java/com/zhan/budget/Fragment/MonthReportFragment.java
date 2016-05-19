package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.OverviewActivity;
import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Adapter.FourPageViewPager;
import com.zhan.budget.Adapter.MonthReportRecyclerAdapter;
import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.CategoryCalculator;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.MonthReport;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.View.CustomViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonthReportFragment extends Fragment {

    private static final String TAG = "MonthlyFragment";

    private OnMonthlyInteractionListener mListener;

    private List<MonthReport> monthReportList;
    private RecyclerView monthReportListview;
    private MonthReportRecyclerAdapter monthReportRecyclerAdapter;

    private RealmResults<Transaction> transactionsResults;
    private List<Transaction> transactionList;

    private Date currentYear;

    private Date beginYear;
    private Date endYear;

    private List<Category> categoryList;
    private RealmResults<Category> resultsCategory;


    private MonthReportGenericFragment quarter1, quarter2, quarter3, quarter4;
    private View view;

    public MonthReportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        view = inflater.inflate(getFragmentLayout(), container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        init();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private int getFragmentLayout() {
        return R.layout.fragment_month_report;
    }


    protected void init(){ Log.d(TAG, "init");
        //super.init();

        Toast.makeText(getContext(), "im here", Toast.LENGTH_SHORT).show();

        currentYear = DateUtil.refreshYear(new Date());

        createTabs();
        updateYearInToolbar(0);
    }

    private void createTabs(){
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Q1"));
        tabLayout.addTab(tabLayout.newTab().setText("Q2"));
        tabLayout.addTab(tabLayout.newTab().setText("Q3"));
        tabLayout.addTab(tabLayout.newTab().setText("Q4"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        quarter1 = MonthReportGenericFragment.newInstance(MonthReportGenericFragment.Quarter.Q1);
        quarter2 = MonthReportGenericFragment.newInstance(MonthReportGenericFragment.Quarter.Q2);
        quarter3 = MonthReportGenericFragment.newInstance(MonthReportGenericFragment.Quarter.Q3);
        quarter4 = MonthReportGenericFragment.newInstance(MonthReportGenericFragment.Quarter.Q4);

        final CustomViewPager viewPager = (CustomViewPager) view.findViewById(R.id.viewPager);
        viewPager.setPagingEnabled(true);

        FourPageViewPager adapterViewPager = new FourPageViewPager(getChildFragmentManager(), quarter1, quarter2, quarter3, quarter4);
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

    private void updateYearInToolbar(int direction){
        currentYear = DateUtil.getYearWithDirection(currentYear, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat3(currentYear));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listener
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
/*
    @Override
    public void onClickMonth(int position){
        Intent overviewActivity = new Intent(getContext(), OverviewActivity.class);
        overviewActivity.putExtra(Constants.REQUEST_NEW_OVERVIEW_MONTH, monthReportList.get(position).getMonth());
        startActivityForResult(overviewActivity, Constants.RETURN_NEW_OVERVIEW);

}*/
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
                updateYearInToolbar(-1);
                return true;
            case R.id.rightChevron:
                updateYearInToolbar(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        if (context instanceof OnMonthlyInteractionListener) {
            mListener = (OnMonthlyInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMonthlyInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnMonthlyInteractionListener {
        void updateToolbar(String date);
    }
}
