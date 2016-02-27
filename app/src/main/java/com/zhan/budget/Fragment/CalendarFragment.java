package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.p_v.flexiblecalendar.view.BaseCellView;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Adapter.TransactionListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.PlusView;
import com.zhan.budget.View.RectangleCellView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFragment.OnCalendarInteractionListener} interface
 * to handle interaction events.
 */
public class CalendarFragment extends Fragment implements
        TransactionListAdapter.OnTransactionAdapterInteractionListener{

    private View view;
    private FlexibleCalendarView calendarView;

    private OnCalendarInteractionListener mListener;

    private TextView  totalCostForDay, dateTextView;

    private ListView transactionListView;
    private TransactionListAdapter transactionAdapter;
    private List<Transaction> transactionList;

    private Date selectedDate;

    private Realm myRealm;


    private RealmResults<Transaction> resultsTransactionForDay;


    private PtrFrameLayout frame;
    private PlusView header;

    private Boolean isPulldownToAddAllow = true;

    private ViewGroup emptyLayout;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calendar, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
        addListeners();
        createPullToAddTransaction();
        createCalendar();

        //List all transactions for today
        //populateTransactionsForDate(selectedDate);


        final Date beginDate = Util.refreshDate(selectedDate);
        final Date endDate = Util.getNextDate(selectedDate);
        resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", beginDate).lessThan("date", endDate).findAllAsync();
        resultsTransactionForDay.addChangeListener(resultsTransactionForDayChangeListener);
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        //By default it will be the current date;
        selectedDate = new Date();

        calendarView = (FlexibleCalendarView) view.findViewById(R.id.calendarView);
        totalCostForDay = (TextView) view.findViewById(R.id.totalCostForDay);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);

        transactionListView = (ListView) view.findViewById(R.id.transactionListView);
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionListAdapter(this, transactionList, false); //do not display date in each transaction item
        transactionListView.setAdapter(transactionAdapter);

        dateTextView.setText(Util.convertDateToStringFormat1(selectedDate));

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyTransactionLayout);
    }

    private void addListeners(){
        transactionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), "clicked on transaction :" + position, Toast.LENGTH_SHORT).show();
                editTransaction(position);
            }
        });
    }

    private void createPullToAddTransaction(){
        frame = (PtrFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);

        header = new PlusView(getContext());

        frame.setHeaderView(header);

        //frame.setPtrHandler(enablePullDown); //default
        frame.setPtrHandler(ptrHandler);

        frame.addPtrUIHandler(new PtrUIHandler() {

            @Override
            public void onUIReset(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "onUIReset");
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "onUIRefreshPrepare");
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "onUIRefreshBegin");
                header.playRotateAnimation();
            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "onUIRefreshComplete");
                addNewTransaction();
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

            }
        });
    }

    private void createCalendar(){
        updateMonthInToolbar(0);

        calendarView.setCalendarView(new FlexibleCalendarView.CalendarView() {
            @Override
            public BaseCellView getCellView(int position, View convertView, ViewGroup parent, @BaseCellView.CellType int cellType) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    cellView = (BaseCellView) inflater.inflate(R.layout.date_cell_view, parent, false);
                }

                if (cellType == BaseCellView.TODAY) {
                    cellView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                } else if (cellType == BaseCellView.SELECTED_TODAY) {
                    cellView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
                cellView.setTextSize(16);

                return cellView;
            }

            @Override
            public BaseCellView getWeekdayCellView(int position, View convertView, ViewGroup parent) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    cellView = (RectangleCellView) inflater.inflate(R.layout.week_cell_view, parent, false);
                }
                return cellView;
            }

            @Override
            public String getDayOfWeekDisplayValue(int dayOfWeek, String defaultValue) {
                return String.valueOf(defaultValue.toUpperCase());
            }
        });

        calendarView.setOnMonthChangeListener(new FlexibleCalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month, int direction) {
                //This is temporary for now because when we move to a new month, the 1st of that month is selected by default
                selectedDate = (new GregorianCalendar(year, month, 1)).getTime();
                dateTextView.setText(Util.convertDateToStringFormat1(selectedDate));

                populateTransactionsForDate(selectedDate);

                updateMonthInToolbar(0);
            }
        });

        calendarView.setOnDateClickListener(new FlexibleCalendarView.OnDateClickListener() {
            @Override
            public void onDateClick(int year, int month, int day) {
                selectedDate = (new GregorianCalendar(year, month, day)).getTime();

                dateTextView.setText(Util.convertDateToStringFormat1(selectedDate));

                populateTransactionsForDate(selectedDate);
            }
        });
    }

    private void populateTransactionsForDate(Date date) {
        final Date beginDate = Util.refreshDate(date);
        final Date endDate = Util.getNextDate(date);

        Log.d("CALENDAR_FRAGMENT", " populate transaction list (" + Util.convertDateToStringFormat5(beginDate) + " -> " + Util.convertDateToStringFormat5(endDate) + ")");

        transactionAdapter.clear();

        if(!myRealm.isClosed()) {
            resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", beginDate).lessThan("date", endDate).findAllAsync();
            Log.d("CALENDAR_FRAGMENT", "realm not CLOSE");

            /*resultsTransactionForDay.addChangeListener(new RealmChangeListener() {
                @Override
                public void onChange() {
                    Log.d("CALENDAR_FRAGMENT", "received " + resultsTransactionForDay.size() + " transactions");

                    float sumFloatValue = resultsTransactionForDay.sum("price").floatValue();
                    totalCostForDay.setText(CurrencyTextFormatter.formatFloat(sumFloatValue, Constants.BUDGET_LOCALE));

                    transactionList = myRealm.copyFromRealm(resultsTransactionForDay);
                    transactionAdapter.addAll(transactionList);

                    updateTransactionStatus();


                    //Removing on change listener
                    resultsTransactionForDay.removeChangeListener(this);

                }
            });*/
        }else{
            Log.d("CALENDAR_FRAGMENT", "myRealm was CLOSED");
        }
    }

    private void updateTransactionStatus(){
        if(transactionList.size() > 0){
            emptyLayout.setVisibility(View.GONE);
            transactionListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            transactionListView.setVisibility(View.GONE);
        }
    }

    private void addNewTransaction(){
        Intent newTransactionIntent = new Intent(getContext(), TransactionInfoActivity.class);

        //This is new transaction
        newTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION, true);
        newTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, Util.convertDateToString(selectedDate));
        startActivityForResult(newTransactionIntent, Constants.RETURN_NEW_TRANSACTION);
    }

    private void editTransaction(int position){
        Intent editTransactionIntent = new Intent(getContext(), TransactionInfoActivity.class);

        //This is edit mode, not a new transaction
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, Util.convertDateToString(selectedDate));

        Parcelable wrapped = Parcels.wrap(transactionList.get(position));
        editTransactionIntent.putExtra(Constants.REQUEST_EDIT_TRANSACTION, wrapped);

        startActivityForResult(editTransactionIntent, Constants.RETURN_EDIT_TRANSACTION);
    }

    private void updateMonthInToolbar(int direction){
        selectedDate = Util.getMonthWithDirection(selectedDate, direction);
        mListener.updateToolbar(selectedDate);
    }

    PtrHandler ptrHandler = new PtrHandler() {
        @Override
        public void onRefreshBegin(PtrFrameLayout insideFrame) {
            if(isPulldownToAddAllow){
                insideFrame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        frame.refreshComplete();
                    }
                }, 500);
            }
        }

        @Override
        public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
            return isPulldownToAddAllow && PtrDefaultHandler.checkContentCanBePulledDown(frame, transactionListView, header);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resumeRealm();

        if (resultCode == getActivity().RESULT_OK && data.getExtras() != null) {
            if(requestCode == Constants.RETURN_NEW_TRANSACTION){

                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_TRANSACTION));

                //Compare with today's date
                if(Util.getDaysFromDate(tt.getDate()) > Util.getDaysFromDate(new Date())){
                    tt.setDayType(DayType.SCHEDULED.toString());
                }else{
                    tt.setDayType(DayType.COMPLETED.toString());
                }

                addNewOrEditTransaction(tt);
            }else if(requestCode == Constants.RETURN_EDIT_TRANSACTION){
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_TRANSACTION));

                addNewOrEditTransaction(tt);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private RealmChangeListener resultsTransactionForDayChangeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            Log.d("ZHAN", "THeres a change; update adapter");

            Log.d("CALENDAR_FRAGMENT", "received " + resultsTransactionForDay.size() + " transactions");

            float sumFloatValue = resultsTransactionForDay.sum("price").floatValue();
            totalCostForDay.setText(CurrencyTextFormatter.formatFloat(sumFloatValue, Constants.BUDGET_LOCALE));

            transactionList = myRealm.copyFromRealm(resultsTransactionForDay);
            transactionAdapter.addAll(transactionList);

            updateTransactionStatus();

            transactionAdapter.notifyDataSetChanged();
        }
    };


    private void addNewOrEditTransaction(final Transaction newOrEditTransaction){
        Log.i("ZHAN", "----------- Parceler Result ----------");
        Log.d("ZHAN", "transaction id :"+newOrEditTransaction.getId());
        Log.d("ZHAN", "transaction note :" + newOrEditTransaction.getNote() + ", cost :" + newOrEditTransaction.getPrice());
        Log.d("ZHAN", "transaction daytype :" + newOrEditTransaction.getDayType() + ", date :" + newOrEditTransaction.getDate());
        Log.d("ZHAN", "category name :" + newOrEditTransaction.getCategory().getName() + ", id:" + newOrEditTransaction.getCategory().getId());
        Log.d("ZHAN", "account id : " + newOrEditTransaction.getAccount().getId());
        Log.d("ZHAN", "account name : " + newOrEditTransaction.getAccount().getName());
        Log.i("ZHAN", "----------- Parceler Result ----------");

        Realm realm = Realm.getDefaultInstance();
        try{
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(newOrEditTransaction);
            realm.commitTransaction();

            calendarView.selectDate(newOrEditTransaction.getDate());
            populateTransactionsForDate(newOrEditTransaction.getDate());
            updateTransactionStatus();
        }catch(Exception e){
            if(realm != null){
                try{
                    realm.cancelTransaction();
                }catch(Exception e1){
                    Log.e("REALM", "Failed to cancel transaction", e1);
                }
            }
        }finally{
            if(realm != null && !realm.isClosed()){
                realm.close();
            }
        }
    }

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
                updateMonthInToolbar(-1);
                calendarView.moveToPreviousMonth();
                return true;
            case R.id.rightChevron:
                updateMonthInToolbar(1);
                calendarView.moveToNextMonth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCalendarInteractionListener) {
            mListener = (OnCalendarInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCalendarInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("LIFECYCLE", "onResume");
        resumeRealm();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d("LIFECYCLE", "onPause");
        closeRealm();
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d("LIFECYCLE", "onStop");
        closeRealm();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        Log.d("LIFECYCLE", "onDestroyView");
        closeRealm();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("LIFECYCLE", "onDestroy");
        closeRealm();
    }

    private void resumeRealm(){
        if(myRealm == null || myRealm.isClosed()){
            myRealm = Realm.getDefaultInstance();
        }
    }

    private void closeRealm(){
        if(myRealm != null && !myRealm.isClosed()){
            myRealm.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteTransaction(int position){
        /*myRealm.beginTransaction();
        resultsAccount.remove(position);
        myRealm.commitTransaction();

        accountListAdapter.clear();
        accountListAdapter.addAll(accountList);*/


        Toast.makeText(getContext(), "calendar fragment delete transaction :"+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApproveTransaction(int position){
        Toast.makeText(getContext(), "calendar fragment approve transaction :"+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisablePtrPullDown(boolean value){
        isPulldownToAddAllow = !value;
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
    public interface OnCalendarInteractionListener {
        void updateToolbar(Date date);
    }

}
