package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.zhan.budget.Adapter.MonthReportGridAdapter;
import com.zhan.budget.Model.MonthReport;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonthReportFragment.OnOverviewInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MonthReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthReportFragment extends Fragment {

    private OnOverviewInteractionListener mListener;
    private View view;

    private List<MonthReport> monthReportList;
    private GridView monthReportGridView;
    private MonthReportGridAdapter monthReportGridAdapter;

    private Realm myRealm;
    private RealmResults<Transaction> transactionsResults;
    private List<Transaction> transactionList;

    private Date currentYear;

    private Date beginYear;
    private Date endYear;

    public MonthReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OverviewFragment.
     */
    public static MonthReportFragment newInstance() {
        MonthReportFragment fragment = new MonthReportFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_overview, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        monthReportList = new ArrayList<>();
        monthReportGridView = (GridView) view.findViewById(R.id.monthReportGridView);
        monthReportGridAdapter = new MonthReportGridAdapter(getActivity(), monthReportList);
        monthReportGridView.setAdapter(monthReportGridAdapter);

        currentYear = new Date();

        createMonthCard();
        updateYearInToolbar(0);
    }

    /**
     * Gets called once to initialize the card view for all months
     */
    private void createMonthCard(){
        for(int i = 0; i < 12; i++){
            MonthReport monthReport = new MonthReport();
            monthReport.setDoneCalculation(false); //default

            if(i == 0) {
                monthReport.setMonth(Util.refreshMonth(Util.refreshYear(currentYear)));
            }else{
                monthReport.setMonth(Util.getNextMonth(monthReportList.get(i - 1).getMonth()));
            }
            monthReportList.add(monthReport);
        }
    }

    /**
     * Gets called whenever you update the year
     */
    private void getMonthReport(){
        //Refresh these variables
        beginYear = Util.refreshYear(currentYear);

        //Need to go a day before as Realm's between date does inclusive on both end
        endYear = Util.getPreviousDate(Util.getNextYear(beginYear));

        Log.d("DEBUG", "get report from " + beginYear.toString() + " to " + endYear.toString());

        //Reset all values in list
        for(int i = 0 ; i < monthReportList.size(); i++){
            monthReportList.get(i).setDoneCalculation(false);
            monthReportList.get(i).setCostThisMonth(0);
            monthReportList.get(i).setChangeCost(0);
        }
        monthReportGridAdapter.notifyDataSetChanged();

        transactionsResults = myRealm.where(Transaction.class).between("date", beginYear, endYear).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                if (transactionList != null) {
                    transactionList.clear();
                }

                transactionList = myRealm.copyFromRealm(transactionsResults);

                performAsyncCalculation();
            }
        });
    }

    /**
     * Perform tedious calculation asynchronously to avoid blocking main thread
     */
    private void performAsyncCalculation(){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("MONTHLY_FRAGMENT", "preparing to aggregate results");
            }

            @Override
            protected Void doInBackground(Void... voids) {

                Log.d("MONTHLY_FRAGMENT", "Transaction size : "+transactionList.size());

                startTime = System.nanoTime();

                for (int i = 0; i < transactionList.size(); i++) {

                    int month = Util.getMonthFromDate(transactionList.get(i).getDate());

                    for(int a = 0; a < monthReportList.size(); a++){
                        if(month == Util.getMonthFromDate(monthReportList.get(a).getMonth())){
                            monthReportList.get(a).addCostThisMonth(transactionList.get(i).getPrice());
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);

                //Change the variable to true
                for(int i = 0; i < monthReportList.size(); i++){
                    monthReportList.get(i).setDoneCalculation(true);
                }

                monthReportGridAdapter.notifyDataSetChanged();

                endTime = System.nanoTime();
                duration = (endTime - startTime);
                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);
                Log.d("MONTHLY_FRAGMENT", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
    }

    private void updateYearInToolbar(int direction){
        /*Calendar cal = Calendar.getInstance();
        cal.setTime(currentYear);
        cal.add(Calendar.YEAR, direction);

        currentYear = cal.getTime();
        */

        currentYear = Util.getYearWithDirection(currentYear, direction);

        getMonthReport();

        if(((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Util.convertDateToStringFormat3(currentYear));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOverviewInteractionListener) {
            mListener = (OnOverviewInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(!myRealm.isClosed()) {
            myRealm.close();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!myRealm.isClosed()){
            myRealm.close();
        }
    }

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
    public interface OnOverviewInteractionListener {
        void onOverviewInteraction(String value);
    }
}
