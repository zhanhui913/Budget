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
import com.zhan.budget.Model.Realm.ScheduledTransaction;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Model.RepeatType;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
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
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFragment.OnCalendarInteractionListener} interface
 * to handle interaction events.
 */
public class CalendarFragment extends BaseFragment implements
        TransactionListAdapter.OnTransactionAdapterInteractionListener{

    private static final String TAG = "CalendarFragment";

    private ViewGroup emptyLayout;
    private OnCalendarInteractionListener mListener;

    //Calendar
    private FlexibleCalendarView calendarView;

    private TextView  totalCostForDay, dateTextView;

    //Transaction
    private ListView transactionListView;
    private TransactionListAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private RealmResults<Transaction> resultsTransactionForDay;

    private Date selectedDate;

    //Pull down
    private PtrFrameLayout frame;
    private PlusView header;
    private Boolean isPulldownToAddAllow = true;


    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_calendar;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();
        //By default it will be the current date;
        selectedDate = new Date();

        calendarView = (FlexibleCalendarView) view.findViewById(R.id.calendarView);
        totalCostForDay = (TextView) view.findViewById(R.id.totalCostForDay);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);

        transactionListView = (ListView) view.findViewById(R.id.transactionListView);
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionListAdapter(this, transactionList, false); //do not display date in each transaction item
        transactionListView.setAdapter(transactionAdapter);

        emptyLayout = (ViewGroup) view.findViewById(R.id.emptyTransactionLayout);

        //List all transactions for today
        populateTransactionsForDate(selectedDate);

        addListeners();
        createPullToAddTransaction();
        createCalendar();
    }

    private void addListeners(){
        transactionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Transaction debugTransaction = transactionList.get(position);

                Log.d(TAG, "----------- Click Result ----------");
                Log.d(TAG, "transaction id :" + debugTransaction.getId());
                Log.d(TAG, "transaction note :" + debugTransaction.getNote() + ", cost :" + debugTransaction.getPrice());
                Log.d(TAG, "transaction daytype :" + debugTransaction.getDayType() + ", date :" + debugTransaction.getDate());
                Log.d(TAG, "category name :" + debugTransaction.getCategory().getName() + ", id:" + debugTransaction.getCategory().getId());
                Log.d(TAG, "category type :" + debugTransaction.getCategory().getType());
                Log.d(TAG, "account id : " + debugTransaction.getAccount().getId());
                Log.d(TAG, "account name : " + debugTransaction.getAccount().getName());
                Log.i(TAG, "----------- Click Result ----------");


                editTransaction(position);
            }
        });
    }

    /**
     * Create the pull down effect.
     */
    private void createPullToAddTransaction(){
        frame = (PtrFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);

        header = new PlusView(getContext());

        frame.setHeaderView(header);

        frame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout insideFrame) {
                if (isPulldownToAddAllow) {
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
        });

        frame.addPtrUIHandler(new PtrUIHandler() {

            @Override
            public void onUIReset(PtrFrameLayout frame) {
                Log.d(TAG, "onUIReset");
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout frame) {
                Log.d(TAG, "onUIRefreshPrepare");
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout frame) {
                Log.d(TAG, "onUIRefreshBegin");
                header.playRotateAnimation();
            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout frame) {
                Log.d(TAG, "onUIRefreshComplete");
                addNewTransaction();
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

            }
        });
    }

    /**
     * Create calendar
     */
    private void createCalendar(){
        updateMonthInToolbar(0);

        calendarView.setCalendarView(new FlexibleCalendarView.CalendarView() {
            @Override
            public BaseCellView getCellView(int position, View convertView, ViewGroup parent, @BaseCellView.CellType int cellType) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    cellView = (BaseCellView) inflater.inflate(R.layout.calendar_date_cell_view, parent, false);
                }

                if (cellType == BaseCellView.TODAY) {
                    cellView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                } else if (cellType == BaseCellView.SELECTED_TODAY) {
                    cellView.setTextColor(ContextCompat.getColor(getContext(), R.color.day_text));
                }

                return cellView;
            }

            @Override
            public BaseCellView getWeekdayCellView(int position, View convertView, ViewGroup parent) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    cellView = (RectangleCellView) inflater.inflate(R.layout.calendar_week_cell_view, parent, false);
                }
                return cellView;
            }

            @Override
            public String getDayOfWeekDisplayValue(int dayOfWeek, String defaultValue) {
                return String.valueOf(defaultValue.toUpperCase());
            }
        });

        calendarView.setStartDayOfTheWeek(Util.getStartDayOfWeekPreference(getActivity()));

        calendarView.setOnMonthChangeListener(new FlexibleCalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month, int direction) {
                //This is temporary for now because when we move to a new month, the 1st of that month is selected by default
                selectedDate = (new GregorianCalendar(year, month, 1)).getTime();
                populateTransactionsForDate(selectedDate);
                updateMonthInToolbar(0);
            }
        });

        calendarView.setOnDateClickListener(new FlexibleCalendarView.OnDateClickListener() {
            @Override
            public void onDateClick(int year, int month, int day) {
                selectedDate = (new GregorianCalendar(year, month, day)).getTime();
                populateTransactionsForDate(selectedDate);
            }
        });
    }

    /**
     * Populate the list of transactions for the specific date.
     * @param date The date to search in db.
     */
    private void populateTransactionsForDate(Date date) {
        transactionListView.smoothScrollToPosition(0);

        final Date beginDate = DateUtil.refreshDate(date);
        final Date endDate = DateUtil.getNextDate(date);

        //Update date text view in center panel
        dateTextView.setText(DateUtil.convertDateToStringFormat1(beginDate));

        Log.d(TAG, " populate transaction list (" + DateUtil.convertDateToStringFormat5(beginDate) + " -> " + DateUtil.convertDateToStringFormat5(endDate) + ")");

        resumeRealm();

        resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", beginDate).lessThan("date", endDate).findAllAsync();
        resultsTransactionForDay.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                resultsTransactionForDay.removeChangeListener(this);

                Log.d(TAG, "received " + resultsTransactionForDay.size() + " transactions");

                float sumFloatValue = resultsTransactionForDay.sum("price").floatValue();
                totalCostForDay.setText(CurrencyTextFormatter.formatFloat(sumFloatValue, Constants.BUDGET_LOCALE));

                updateTransactionList();
            }
        });
    }

    /**
     * Whenever there is a change in the list resultsTransactionForDay, this function updates
     * the UI.
     */
    private void updateTransactionList(){
        transactionAdapter.clear();
        transactionList = myRealm.copyFromRealm(resultsTransactionForDay);
        transactionAdapter.addAll(transactionList);

        updateTransactionStatus();

        transactionAdapter.notifyDataSetChanged();
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

    /**
     * Create an intent to add transaction.
     */
    private void addNewTransaction(){
        Intent newTransactionIntent = new Intent(getContext(), TransactionInfoActivity.class);

        //This is new transaction
        newTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION, true);
        newTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, DateUtil.convertDateToString(selectedDate));
        startActivityForResult(newTransactionIntent, Constants.RETURN_NEW_TRANSACTION);
    }

    /**
     * Create an intent to edit a transaction.
     */
    private void editTransaction(int position){
        Intent editTransactionIntent = new Intent(getContext(), TransactionInfoActivity.class);

        //This is edit mode, not a new transaction
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
        editTransactionIntent.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, DateUtil.convertDateToString(selectedDate));

        Parcelable wrapped = Parcels.wrap(transactionList.get(position));
        editTransactionIntent.putExtra(Constants.REQUEST_EDIT_TRANSACTION, wrapped);

        startActivityForResult(editTransactionIntent, Constants.RETURN_EDIT_TRANSACTION);
    }

    /**
     * Update the the toolbar's title with specific date.
     * @param direction how many to add to the current month.
     */
    private void updateMonthInToolbar(int direction){
        selectedDate = DateUtil.getMonthWithDirection(selectedDate, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(selectedDate));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resumeRealm();
        if (resultCode == getActivity().RESULT_OK && data.getExtras() != null) {
            if(requestCode == Constants.RETURN_NEW_TRANSACTION){
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_TRANSACTION));
                ScheduledTransaction scheduledTransaction = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_SCHEDULE_TRANSACTION));
                Log.d(TAG, "scheduledTransaction from new :"+scheduledTransaction.getId());

                //Compare with today's date
                if(DateUtil.getDaysFromDate(tt.getDate()) > DateUtil.getDaysFromDate(new Date())){
                    tt.setDayType(DayType.SCHEDULED.toString());
                }else{
                    tt.setDayType(DayType.COMPLETED.toString());
                }

                addNewOrEditTransaction(tt);
                addScheduleTransaction(scheduledTransaction, tt);
            }else if(requestCode == Constants.RETURN_EDIT_TRANSACTION){
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_TRANSACTION));
                ScheduledTransaction scheduledTransaction = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_SCHEDULE_TRANSACTION));
                Log.d(TAG, "scheduledTransaction from edit :" + scheduledTransaction.getId());

                addNewOrEditTransaction(tt);
                addScheduleTransaction(scheduledTransaction, tt);
            }
        }
    }

    /**
     * The function that will be called after user either adds or edit a scheduled transaction.
     * @param scheduledTransaction The new scheduled transaction information.
     * @param transaction The transaction that the scheduled transaction is based on.
     */
    private void addScheduleTransaction(ScheduledTransaction scheduledTransaction, Transaction transaction){
        if(scheduledTransaction != null){
            myRealm.beginTransaction();
            scheduledTransaction.setTransaction(transaction);
            myRealm.copyToRealmOrUpdate(scheduledTransaction);
            myRealm.commitTransaction();

            Log.d(TAG, "----------- Parceler Result ----------");
            Log.d(TAG, "scheduled transaction id :" + scheduledTransaction.getId());
            Log.d(TAG, "scheduled transaction unit :" + scheduledTransaction.getRepeatUnit() + ", type :" + scheduledTransaction.getRepeatType());
            Log.d(TAG, "transaction note :" + scheduledTransaction.getTransaction().getNote() + ", cost :" + scheduledTransaction.getTransaction().getPrice());
            Log.i(TAG, "----------- Parceler Result ----------");

            transaction.setDayType(DayType.SCHEDULED.toString());
            Date nextDate = transaction.getDate();
            /*if(scheduledTransaction.getRepeatType().equalsIgnoreCase(RepeatType.DAYS.toString())){
                //Repeat 10 times
                for(int i = 0; i < 10; i++){
                    myRealm.beginTransaction();
                    nextDate = DateUtil.getDateWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    Log.d(TAG, i + "-> " + DateUtil.convertDateToStringFormat5(nextDate));
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                    myRealm.copyToRealmOrUpdate(transaction);
                    myRealm.commitTransaction();
                }
            }else if(scheduledTransaction.getRepeatType().equalsIgnoreCase(RepeatType.WEEKS.toString())){
                //Repeat 10 times
                for(int i = 0; i < 10; i++){
                    myRealm.beginTransaction();
                    nextDate = DateUtil.getWeekWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    Log.d(TAG, i + "-> " + DateUtil.convertDateToStringFormat5(nextDate));
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                    myRealm.copyToRealmOrUpdate(transaction);
                    myRealm.commitTransaction();
                }
            }else{
                //Repeat 10 times
                for(int i = 0; i < 10; i++){
                    myRealm.beginTransaction();
                    nextDate = DateUtil.getMonthWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    Log.d(TAG, i + "-> " + DateUtil.convertDateToStringFormat5(nextDate));
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                    myRealm.copyToRealmOrUpdate(transaction);
                    myRealm.commitTransaction();
                }
            }*/




            for(int i = 0; i < 10; i++){
                myRealm.beginTransaction();

                if(scheduledTransaction.getRepeatType().equalsIgnoreCase(RepeatType.DAYS.toString())){
                    nextDate = DateUtil.getDateWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                }else if(scheduledTransaction.getRepeatType().equalsIgnoreCase(RepeatType.WEEKS.toString())){
                    nextDate = DateUtil.getWeekWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                }else{
                    nextDate = DateUtil.getMonthWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                    transaction.setId(Util.generateUUID());
                    transaction.setDate(nextDate);
                }

                Log.d(TAG, i + "-> " + DateUtil.convertDateToStringFormat5(nextDate));
                myRealm.copyToRealmOrUpdate(transaction);
                myRealm.commitTransaction();
            }





        }
    }

    /**
     * The function that will be called after user either adds or edit a transaction.
     * @param newOrEditTransaction The new transaction information.
     */
    private void addNewOrEditTransaction(final Transaction newOrEditTransaction){
        Log.d(TAG, "----------- Parceler Result ----------");
        Log.d(TAG, "transaction id :"+newOrEditTransaction.getId());
        Log.d(TAG, "transaction note :" + newOrEditTransaction.getNote() + ", cost :" + newOrEditTransaction.getPrice());
        Log.d(TAG, "transaction daytype :" + newOrEditTransaction.getDayType() + ", date :" + newOrEditTransaction.getDate());
        Log.d(TAG, "category name :" + newOrEditTransaction.getCategory().getName() + ", id:" + newOrEditTransaction.getCategory().getId());
        Log.d(TAG, "category type :" + newOrEditTransaction.getCategory().getType());
        Log.d(TAG, "account id : " + newOrEditTransaction.getAccount().getId());
        Log.d(TAG, "account name : " + newOrEditTransaction.getAccount().getName());
        Log.i(TAG, "----------- Parceler Result ----------");

        myRealm.beginTransaction();
        myRealm.copyToRealmOrUpdate(newOrEditTransaction);
        myRealm.commitTransaction();

        calendarView.selectDate(newOrEditTransaction.getDate());
        populateTransactionsForDate(newOrEditTransaction.getDate());
        updateTransactionStatus();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCalendarInteractionListener) {
            mListener = (OnCalendarInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCalendarInteractionListener");
        }

        Log.d(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Log.d(TAG, "onDetach");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteTransaction(int position){
        myRealm.beginTransaction();
        Log.d(TAG, "remove " + position + "-> from result");
        Log.d(TAG, "b4 There are "+resultsTransactionForDay.size()+" transactions today");
        resultsTransactionForDay.get(position).removeFromRealm();
        myRealm.commitTransaction();
        Log.d(TAG, "After There are "+resultsTransactionForDay.size()+" transactions today");
        updateTransactionList();
        Toast.makeText(getContext(), "calendar fragment delete transaction :"+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApproveTransaction(int position){
        Toast.makeText(getContext(), "calendar fragment approve transaction :"+position, Toast.LENGTH_SHORT).show();
        myRealm.beginTransaction();
        resultsTransactionForDay.get(position).setDayType(DayType.COMPLETED.toString());
        myRealm.commitTransaction();
        updateTransactionList();
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
        void updateToolbar(String date);
    }

}
