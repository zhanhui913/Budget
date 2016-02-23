package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.p_v.flexiblecalendar.entity.Event;
import com.p_v.flexiblecalendar.view.BaseCellView;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Adapter.TransactionListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.Account;
import com.zhan.budget.Model.Calendar.CustomEvent;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.PlusView;
import com.zhan.budget.View.RectangleCellView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFragment.OnCalendarListener} interface
 * to handle interaction events.
 */
public class CalendarFragment extends Fragment implements
        TransactionListAdapter.OnTransactionAdapterInteractionListener{

    private OnCalendarListener mListener;
    private View view;
    private FlexibleCalendarView calendarView;

    private TextView  totalCostForDay, dateTextView;

    private ListView transactionListView;
    private TransactionListAdapter transactionAdapter;
    private List<Transaction> transactionList;

    private Date selectedDate;

    private Realm myRealm;

    private Map<Integer, List<CustomEvent>> eventMap;

    private RealmResults<Transaction> resultsTransactionForDay;
    private RealmResults<Transaction> resultsTransactionForMonth;
    private List<Date> dateListDecorators;

    private PtrFrameLayout frame;
    private PlusView header;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calendar, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
        initEvents();
        addListeners();
        createPullToAddTransaction();
        createCalendar();
        //createSwipeMenu();

        //List all transactions for today
        populateTransactionsForDate(selectedDate);
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

        frame.setPtrHandler(enablePullDown); //default

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
                    cellView = (BaseCellView) inflater.inflate(R.layout.date_cell_view, null);
                }

                if (cellType == BaseCellView.TODAY) {
                    cellView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                } else if(cellType == BaseCellView.SELECTED_TODAY){
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
                    cellView = (RectangleCellView) inflater.inflate(R.layout.week_cell_view, null);
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
                populateTransactionsForDate(selectedDate);

                dateTextView.setText(Util.convertDateToStringFormat1(selectedDate));

                updateMonthInToolbar(0);
                //GetTransactionsForThese3Month(year, month);
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

        calendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<? extends Event> getEventsForTheDay(int year, int month, int day) {
                return getEvents(year, month, day);
            }
        });
    }

    private List<CustomEvent> getCustomEventList(){
        List<CustomEvent> customEventList = new ArrayList<>();
        customEventList.add(new CustomEvent(R.color.colorPrimaryDark));
        return customEventList;
    }

    private void initEvents(){
        eventMap = new HashMap<>();

        for(int i = 0; i < 365; i++){
            eventMap.put(i, getCustomEventList());
        }
    }

    private List<CustomEvent> getEvents(int year, int month, int day){
        int daysSinceYear = Util.getDaysFromDate(new GregorianCalendar(year, month, day).getTime()); //Log.d("DECORATOR", "days since year: "+daysSinceYear);
        return eventMap.get(daysSinceYear);
    }

    private List<Transaction> dateListWithTransactions;
    private void GetTransactionsForThese3Month(int year, int month){
        Date current = new GregorianCalendar(year, (month), 1).getTime();
        Date before = Util.getMonthWithDirection(current, -1);
        Date after = Util.getMonthWithDirection(current, 2); //Gets 2 month after
        after = Util.getPreviousDate(after); //gets 1 day before

        Log.d("DECORATORS", "before:" + before + ", current:" + current + ", after:"+after);

        resultsTransactionForMonth = myRealm.where(Transaction.class).between("date", before, after).findAllAsync();
        resultsTransactionForMonth.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d("DECORATORS", "there are " + resultsTransactionForMonth.size() + " transactions");
                dateListWithTransactions = myRealm.copyFromRealm(resultsTransactionForMonth);
                compareDecorators();
            }
        });
    }

    private void compareDecorators(){
        final List<CustomEvent> colorList = new ArrayList<CustomEvent>(){{
            add(new CustomEvent(R.color.green));
        }};

        calendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<? extends Event> getEventsForTheDay(int year, int month, int day) {
                Log.d("DECORATORS", "get events for day (" + year + "-" + (month + 1) + "-" + day + ")");
                if (year == 2016 && month == 1 && day == 5) {
                    return colorList;
                }


/*
                for(int i = 0; i < dateListWithTransactions.size(); i++){
                    Date dateToCheck = dateListWithTransactions.get(i).getDate();

                    if(year == Util.getYearFromDate(dateToCheck) &&
                            month == Util.getMonthFromDate(dateToCheck) &&
                            day == Util.getDateFromDate(dateToCheck)){
                        return colorList;
                    }
                }*/
                //();


                return null;
            }
        });
/*
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {


                startTime = System.nanoTime();


                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);



                endTime = System.nanoTime();
                duration = (endTime - startTime);
                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);
                Log.d("MONTHLY_FRAGMENT", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();*/
    }

    private void populateTransactionsForDate(Date date) {
        final Date beginDate = Util.refreshDate(date);
        Date endDate = Util.getNextDate(date);

        Log.d("CALENDAR_FRAGMENT", " populate transaction list for date between " + beginDate.toString() + " and " + endDate.toString());

        transactionAdapter.clear();

        resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", beginDate).lessThan("date", endDate).findAllAsync();
        //resultsTransactionForDay = myRealm.where(Transaction.class).equalTo("date", beginDate).findAllAsync();
        resultsTransactionForDay.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d("CALENDAR_FRAGMENT", "received " + resultsTransactionForDay.size() + " transactions");

                float sumFloatValue = resultsTransactionForDay.sum("price").floatValue();
                Toast.makeText(getContext(), "total is " + sumFloatValue, Toast.LENGTH_LONG).show();

                totalCostForDay.setText(CurrencyTextFormatter.formatFloat(sumFloatValue, Constants.BUDGET_LOCALE));

                transactionList = myRealm.copyFromRealm(resultsTransactionForDay);
                updateTransactionStatus();

                transactionAdapter.clear();
                transactionAdapter.addAll(transactionList);
                //transactionAdapter.addAll(resultsTransactionForDay);

                //Removing on change listener
                resultsTransactionForDay.removeChangeListener(this);
            }
        });
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
        Intent newTransaction = new Intent(getContext(), TransactionInfoActivity.class);

        //This is new transaction
        newTransaction.putExtra(Constants.REQUEST_NEW_TRANSACTION, true);
        newTransaction.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, Util.convertDateToString(selectedDate));
        startActivityForResult(newTransaction, Constants.RETURN_NEW_TRANSACTION);
    }

    private void editTransaction(int position){
        Intent editTransaction = new Intent(getContext(), TransactionInfoActivity.class);

        //This is edit mode, not a new transaction
        editTransaction.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
        editTransaction.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, Util.convertDateToString(selectedDate));

        Parcelable wrapped = Parcels.wrap(transactionList.get(position));
        editTransaction.putExtra(Constants.REQUEST_EDIT_TRANSACTION, wrapped);

        startActivityForResult(editTransaction, Constants.RETURN_EDIT_TRANSACTION);
    }

    private void updateMonthInToolbar(int direction){
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.add(Calendar.MONTH, direction);

        selectedDate = cal.getTime();

        if(((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Util.convertDateToStringFormat2(selectedDate));
        }
    }

    PtrHandler enablePullDown = new PtrHandler() {
        @Override
        public void onRefreshBegin(PtrFrameLayout insideFrame) {
            Log.d("CALENDAR_FRAGMENT", "-- on refresh begin");
            insideFrame.postDelayed(new Runnable() {
                @Override
                public void run() {
                    frame.refreshComplete();
                }
            }, 500);
        }

        @Override
        public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
            return PtrDefaultHandler.checkContentCanBePulledDown(frame, transactionListView, header);
        }
    };

    PtrHandler disablePullDown = new PtrHandler() {
        @Override
        public void onRefreshBegin(PtrFrameLayout insideFrame) {
        }

        @Override
        public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
            return false;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_NEW_TRANSACTION){

                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_TRANSACTION));

                addNewOrEditTransaction(tt);
                /*
                Log.i("ZHAN", "----------- Parceler Result ----------");
                Log.d("ZHAN", "transaction id "+tt.getId());
                Log.d("ZHAN", "transaction name is " + tt.getNote() + " cost is " + tt.getPrice());
                Log.d("ZHAN", "category is " + tt.getCategory().getName() + ", " + tt.getCategory().getId());
                Log.d("ZHAN", "account id is : " + tt.getAccount().getId());
                Log.d("ZHAN", "account name is : " + tt.getAccount().getName());
                Log.i("ZHAN", "----------- Parceler Result ----------");

                //Attaching account
                final RealmResults<Account> accountRealmResults = myRealm.where(Account.class).equalTo("id", tt.getAccount().getId()).findAll();
                final Account account = myRealm.copyToRealm(accountRealmResults.get(0));

                //Attaching category
                final RealmResults<Category> cateList = myRealm.where(Category.class).equalTo("id", tt.getCategory().getId()).findAllAsync();
                cateList.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (cateList.size() != 0) {
                            Category cat = myRealm.copyToRealm(cateList.get(0));

                            myRealm.beginTransaction();
                            Transaction transactionReturnedFromTransaction = myRealm.createObject(Transaction.class);
                            transactionReturnedFromTransaction.setId(tt.getId());
                            transactionReturnedFromTransaction.setPrice(tt.getPrice());
                            transactionReturnedFromTransaction.setDate(tt.getDate());
                            transactionReturnedFromTransaction.setNote(tt.getNote());
                            transactionReturnedFromTransaction.setCategory(cat);
                            transactionReturnedFromTransaction.setAccount(account);

                            if(Util.getDaysFromDate(tt.getDate()) > Util.getDaysFromDate(new Date())){
                                transactionReturnedFromTransaction.setDayType(DayType.SCHEDULED.toString());
                            }else{
                                transactionReturnedFromTransaction.setDayType(DayType.COMPLETED.toString());
                            }

                            myRealm.commitTransaction();

                            Log.d("ZHAN", "successfully added transaction : " + transactionReturnedFromTransaction.getNote() + " for cat : " + transactionReturnedFromTransaction.getCategory().getName());

                            //option 1
                            //transactionList.add(transactionReturnedFromTransaction);
                            //transactionAdapter.notifyDataSetChanged();

                            //option 2
                            //dont update anything

                            updateTransactionStatus();
                        }
                    }
                });
                */
            }else if(requestCode == Constants.RETURN_EDIT_TRANSACTION){
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_TRANSACTION));

                addNewOrEditTransaction(tt);
            }
        }
    }

    private void addNewOrEditTransaction(final Transaction newOrEditTransaction){
        Log.i("ZHAN", "----------- Parceler Result ----------");
        Log.d("ZHAN", "transaction id "+newOrEditTransaction.getId());
        Log.d("ZHAN", "transaction name is " + newOrEditTransaction.getNote() + " cost is " + newOrEditTransaction.getPrice());
        Log.d("ZHAN", "category is " + newOrEditTransaction.getCategory().getName() + ", " + newOrEditTransaction.getCategory().getId());
        Log.d("ZHAN", "account id is : " + newOrEditTransaction.getAccount().getId());
        Log.d("ZHAN", "account name is : " + newOrEditTransaction.getAccount().getName());
        Log.i("ZHAN", "----------- Parceler Result ----------");

        //Attaching account
        final RealmResults<Account> accountRealmResults = myRealm.where(Account.class).equalTo("id", newOrEditTransaction.getAccount().getId()).findAll();
        final Account account = myRealm.copyToRealm(accountRealmResults.get(0));

        //Attaching category
        final RealmResults<Category> cateList = myRealm.where(Category.class).equalTo("id", newOrEditTransaction.getCategory().getId()).findAllAsync();
        cateList.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                if (cateList.size() != 0) {
                    Category cat = myRealm.copyToRealm(cateList.get(0));

                    myRealm.beginTransaction();
                    Transaction transactionReturnedFromTransaction = myRealm.createObject(Transaction.class);
                    transactionReturnedFromTransaction.setId(newOrEditTransaction.getId());
                    transactionReturnedFromTransaction.setPrice(newOrEditTransaction.getPrice());
                    transactionReturnedFromTransaction.setDate(newOrEditTransaction.getDate());
                    transactionReturnedFromTransaction.setNote(newOrEditTransaction.getNote());
                    transactionReturnedFromTransaction.setCategory(cat);
                    transactionReturnedFromTransaction.setAccount(account);

                    if(Util.getDaysFromDate(newOrEditTransaction.getDate()) > Util.getDaysFromDate(new Date())){
                        transactionReturnedFromTransaction.setDayType(DayType.SCHEDULED.toString());
                    }else{
                        transactionReturnedFromTransaction.setDayType(DayType.COMPLETED.toString());
                    }

                    myRealm.commitTransaction();

                    Log.d("ZHAN", "successfully added transaction : " + transactionReturnedFromTransaction.getNote() + " for cat : " + transactionReturnedFromTransaction.getCategory().getName());

                    populateTransactionsForDate(newOrEditTransaction.getDate());
                    updateTransactionStatus();
                }
            }
        });
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
        if (context instanceof OnCalendarListener) {
            mListener = (OnCalendarListener) context;
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
    public interface OnCalendarListener {
        void onCalendarInteraction(String value);
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
        if(value){ //disable
            frame.setPtrHandler(disablePullDown);
        }else{ //enable
            frame.setPtrHandler(enablePullDown);
        }
    }
}
