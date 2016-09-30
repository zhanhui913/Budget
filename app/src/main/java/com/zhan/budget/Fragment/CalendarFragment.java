package com.zhan.budget.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.p_v.flexiblecalendar.entity.Event;
import com.p_v.flexiblecalendar.view.BaseCellView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Calendar.BudgetEvent;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.PlusView;
import com.zhan.budget.View.RectangleCellView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
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
public class CalendarFragment extends BaseRealmFragment implements
        TransactionRecyclerAdapter.OnTransactionAdapterInteractionListener{

    private static final String TAG = "CalendarFragment";

    private ViewGroup emptyLayout;
    private OnCalendarInteractionListener mListener;

    //Calendar
    private FlexibleCalendarView calendarView;

    //Transaction
    private RecyclerView transactionListView;
    private TransactionRecyclerAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private RealmResults<Transaction> resultsTransactionForDay;

    //Pull down
    private PtrFrameLayout frame;
    private PlusView header;
    private Boolean isPulldownAllow = true;

    private TextView  totalCostTextView, dateTextView;
    private CircularProgressBar progressBar;

    private Date selectedDate;
    private Map<Date,List<BudgetEvent>> eventMap;

    private LinearLayoutManager linearLayoutManager;
    private SwipeLayout currentSwipeLayoutTarget;

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
        isFirstTime();

        //By default it will be the current date;
        selectedDate = new Date();

        calendarView = (FlexibleCalendarView) view.findViewById(R.id.calendarView);
        totalCostTextView = (TextView) view.findViewById(R.id.totalCostTextView);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);

        linearLayoutManager = new LinearLayoutManager(getActivity());

        transactionListView = (RecyclerView) view.findViewById(R.id.transactionListView);
        transactionListView.setLayoutManager(linearLayoutManager);

        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionRecyclerAdapter(this, transactionList, false); //do not display date in each transaction item
        transactionListView.setAdapter(transactionAdapter);

        //Add divider
        transactionListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());


        emptyLayout = (ViewGroup) view.findViewById(R.id.emptyTransactionLayout);

        progressBar = (CircularProgressBar) view.findViewById(R.id.transactionProgressbar);

        //List all transactions for today
        populateTransactionsForDate(selectedDate);

        createPullToAddTransaction();
        createCalendar();

        updateScheduledTransactionsForDecoration();
    }

    private void isFirstTime(){
        boolean isFirstTime = BudgetPreference.getFirstTime(getContext());

        if(isFirstTime){
            createFakeTransactions();
            BudgetPreference.setFirstTime(getContext());
        }
    }

    private void createFakeTransactions(){
        long startTime,endTime,duration;
        myRealm.beginTransaction();

        startTime = System.nanoTime();


        //First time usage
        ArrayList<Category> categoryList = new ArrayList<>();
        ArrayList<Account> accountList = new ArrayList<>();
        ArrayList<Location> locationList = new ArrayList<>();

        String[] tempCategoryNameList = new String[]{"Breakfast", "Lunch", "Dinner", "Snacks", "Drink", "Rent", "Travel", "Car", "Shopping", "Necessity", "Utilities", "Bill", "Groceries"};
        int[] tempCategoryColorList = new int[]{R.color.lemon, R.color.orange, R.color.pumpkin, R.color.alizarin, R.color.cream_can, R.color.midnight_blue, R.color.peter_river, R.color.turquoise, R.color.wisteria, R.color.jordy_blue, R.color.concrete, R.color.emerald, R.color.gossip};
        int[] tempCategoryIconList = new int[]{R.drawable.c_food, R.drawable.c_food, R.drawable.c_food, R.drawable.c_food, R.drawable.c_cafe, R.drawable.c_house, R.drawable.c_airplane, R.drawable.c_car, R.drawable.c_shirt, R.drawable.c_etc, R.drawable.c_utilities, R.drawable.c_bill, R.drawable.c_groceries};

        //create expense category
        for (int i = 0; i < tempCategoryNameList.length; i++) {
            Category c = myRealm.createObject(Category.class);
            c.setId(Util.generateUUID());
            c.setName(tempCategoryNameList[i]);
            c.setColor(getResources().getString(tempCategoryColorList[i]));
            c.setIcon(getResources().getResourceEntryName(tempCategoryIconList[i]));
            c.setBudget(100.0f + (i/5));
            c.setType(BudgetType.EXPENSE.toString());
            c.setCost(0);
            c.setIndex(i);

            categoryList.add(c);
        }

        String[] tempCategoryIncomeNameList = new String[]{"Salary", "Other"};
        int[] tempCategoryIncomeColorList = new int[]{R.color.light_wisteria, R.color.harbor_rat};
        int[] tempCategoryIncomeIconList = new int[]{R.drawable.c_bill, R.drawable.c_etc};

        //create income category
        for (int i = 0; i < tempCategoryIncomeNameList.length; i++) {
            Category c = myRealm.createObject(Category.class);
            c.setId(Util.generateUUID());
            c.setName(tempCategoryIncomeNameList[i]);
            c.setColor(getResources().getString(tempCategoryIncomeColorList[i]));
            c.setIcon(getResources().getResourceEntryName(tempCategoryIncomeIconList[i]));
            c.setBudget(0);
            c.setType(BudgetType.INCOME.toString());
            c.setCost(0);
            c.setIndex(i);

            categoryList.add(c);
        }

        //Create default accounts
        String[] tempAccountList = new String[]{"Credit Card","Debit Card", "Cash"};
        int[] tempAccountColorList = new int[]{R.color.amethyst, R.color.asbestos, R.color.belize_hole};
        for(int i = 0 ; i < tempAccountList.length; i++){
            Account account = myRealm.createObject(Account.class);
            account.setId(Util.generateUUID());
            account.setName(tempAccountList[i]);
            account.setIsDefault((i == 0));
            account.setColor(getResources().getString(tempAccountColorList[i]));
            accountList.add(account);
        }
/*
        //Create fake locations
        String[] locationTempList = new String[] {"Belgium", "France", "Italy", "Germany", "Spain", "USA", "Canada", "Brazil", "Norway", "England"};
        for(int i = 0; i < locationTempList.length; i++){
            Location location = myRealm.createObject(Location.class);
            location.setName(locationTempList[i]);
            location.setAmount(0);
            location.setColor(Colors.getRandomColorString(getContext()));
            locationList.add(location);
        }

        //Create fake transactions
        Date startDate = DateUtil.convertStringToDate("2016-01-01");
        Date endDate = DateUtil.convertStringToDate("2016-07-01");

        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);


        String dayType;

        for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
            Random random = new Random();
            int rd = random.nextInt(categoryList.size());
            int rda = random.nextInt(accountList.size());
            int ll = random.nextInt(locationList.size());

            if(date.before(new Date())){
                dayType = DayType.COMPLETED.toString();
            }else{
                dayType = DayType.SCHEDULED.toString();
            }

            //Create random transactions per day
            for (int j = 0; j < rd; j++) {
                Transaction transaction = myRealm.createObject(Transaction.class);
                transaction.setId(Util.generateUUID());
                transaction.setDate(date);
                transaction.setDayType(dayType);
                transaction.setLocation(locationList.get(ll));

                Account account = accountList.get(rda);

                Category category = categoryList.get(rd);
                if(category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                    transaction.setPrice(-120.0f + (rd * 0.5f));
                }else{
                    transaction.setPrice(Math.abs(-120.0f + (rd * 0.5f)));
                }

                transaction.setAccount(account);
                transaction.setCategory(category);
                transaction.setNote("Note " + j + " for " + DateUtil.convertDateToString(date));
            }
        }
*/
        myRealm.commitTransaction();
        endTime = System.nanoTime();

        duration = (endTime - startTime);

        long milli = (duration / 1000000);
        long second = (milli / 1000);
        float minutes = (second / 60.0f);

        Log.d("REALM", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");


        final RealmResults<Transaction> testResults = myRealm.where(Transaction.class).findAllAsync();
        testResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                Log.d(TAG, "total transactions  created for testing : "+element.size() );
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
                if (isPulldownAllow) {
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
                return isPulldownAllow && PtrDefaultHandler.checkContentCanBePulledDown(frame, transactionListView, header);
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
                Log.d("PLUS_VIEW", "onUIRefreshComplete");
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addNewTransaction();
                    }
                }, 250);
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
        updateMonthInToolbar();

        calendarView.setCalendarView(new FlexibleCalendarView.CalendarView() {
            @Override
            public BaseCellView getCellView(int position, View convertView, ViewGroup parent, @BaseCellView.CellType int cellType) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    cellView = (BaseCellView) View.inflate(getContext(), R.layout.calendar_date_cell_view, null);
                }

                if (cellType == BaseCellView.TODAY) {
                    cellView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                } else if (cellType == BaseCellView.SELECTED_TODAY) {
                    cellView.setTextColor(Colors.getColorFromAttr(getContext(), R.attr.themeColor));
                }

                return cellView;
            }

            @Override
            public BaseCellView getWeekdayCellView(int position, View convertView, ViewGroup parent) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    cellView = (RectangleCellView) View.inflate(getContext(), R.layout.calendar_week_cell_view, null);
                }
                return cellView;
            }

            @Override
            public String getDayOfWeekDisplayValue(int dayOfWeek, String defaultValue) {
                return String.valueOf(defaultValue.toUpperCase());
            }
        });

        calendarView.setStartDayOfTheWeek(BudgetPreference.getStartDay(getContext()));

        calendarView.selectDate(new Date());

        calendarView.setOnMonthChangeListener(new FlexibleCalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month, int direction) {
                updateMonthInToolbar();
            }
        });

        calendarView.setOnDateClickListener(new FlexibleCalendarView.OnDateClickListener() {
            @Override
            public void onDateClick(int year, int month, int day) {
                selectedDate = (new GregorianCalendar(year, month, day)).getTime();
                populateTransactionsForDate(selectedDate);
            }
        });

        calendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<? extends Event> getEventsForTheDay(int year, int month, int day) {
                return getEvents((new GregorianCalendar(year, month, day)).getTime());
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

        progressBar.setVisibility(View.VISIBLE);

        resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", beginDate).lessThan("date", endDate).findAllAsync();
        resultsTransactionForDay.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                Log.d(TAG, "received " + element.size() + " transactions");

                float sumFloatValue = element.sum("price").floatValue();
                totalCostTextView.setText(CurrencyTextFormatter.formatFloat(sumFloatValue, Constants.BUDGET_LOCALE));

                updateTransactionList();
            }
        });
    }

    /**
     * Fetch new data from realm for the list of transactions for the day
     */
    private void updateTransactionList(){
        transactionList = myRealm.copyFromRealm(resultsTransactionForDay);
        transactionAdapter.setTransactionList(transactionList);



        updateTransactionStatus();
    }


    private void updateTransactionStatusAtPosition(int position, Transaction transaction){
        transactionList.set(position, transaction);

        updateTransactionStatus();
    }

    /***
     * Update the UI based on the count of items in the transaction list
     */
    private void updateTransactionStatus(){
        if(transactionList.size() > 0){
            emptyLayout.setVisibility(View.GONE);
            transactionListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            transactionListView.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
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
     * Update the the toolbar's title with current CalendarView's date
     */
    private void updateMonthInToolbar(){
        Date tempDate = new GregorianCalendar(calendarView.getCurrentYear(), calendarView.getCurrentMonth(), 1).getTime();
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(tempDate));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Decorators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private List<BudgetEvent> getEvents(Date date){
        return eventMap.get(date);
    }

    private void updateScheduledTransactionsForDecoration(){
        eventMap = new HashMap<>();
        /*Log.d("EVENT", "there are " + eventMap.size() + " items in map");

        final RealmResults<Transaction> scheduledTransactions = myRealm.where(Transaction.class).equalTo("dayType", DayType.SCHEDULED.toString()).findAllAsync();
        scheduledTransactions.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                for (int i = 0; i < element.size(); i++) {
                    List<BudgetEvent> colorList = new ArrayList<>();
                    try {
                        //Put as many indication per transactions that is SCHEDULED
                        //if (eventMap.containsKey(element.get(i).getDate())) {
                        //    eventMap.get(element.get(i).getDate()).add(new BudgetEvent(CategoryUtil.getColorID(getContext(), scheduledTransactions.get(i).getCategory().getColor())));
                        //} else {
                        //    colorList.add(new BudgetEvent(CategoryUtil.getColorID(getContext(), scheduledTransactions.get(i).getCategory().getColor())));
                        //    eventMap.put(element.get(i).getDate(), colorList);
                        //}
                        //Only put 1 indication for the event per day
                        if(!eventMap.containsKey(element.get(i).getDate())){
                            colorList.add(new BudgetEvent(CategoryUtil.getColorID(getContext(), scheduledTransactions.get(i).getCategory().getColor())));
                            eventMap.put(element.get(i).getDate(), colorList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.d("EVENT", "there are " + element.size() + " items in schedule list");
                Log.d("EVENT", "there are " + eventMap.size() + " items in map");

                calendarView.refresh();
            }
        });*/



        final RealmResults<Transaction> scheduledTransactions = myRealm.where(Transaction.class).equalTo("dayType", DayType.SCHEDULED.toString()).findAllAsync();
        scheduledTransactions.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(final RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                Log.d("TIMER", "timer 1");
                ads(myRealm.copyFromRealm(element));
                Log.d("TIMER", "timer 2");
            }
        });
    }

    private void ads(final List<Transaction> element){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("TIMER", "timer 3");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Log.d("TIMER", "timer 4");
                for (int i = 0; i < element.size(); i++) {
                    List<BudgetEvent> colorList = new ArrayList<>();
                    try {
                        //Only put 1 indication for the event per day
                        if(!eventMap.containsKey(element.get(i).getDate())){
                            colorList.add(new BudgetEvent(CategoryUtil.getColorID(getContext(), element.get(i).getCategory().getColor())));
                            eventMap.put(element.get(i).getDate(), colorList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                Log.d("TIMER", "timer 5");
                calendarView.refresh();
            }
        };
        loader.execute();
    }

    private void addDecorator(Date date, int color){
        if(!eventMap.containsKey(date)){
            List<BudgetEvent> colorList = new ArrayList<>();
            colorList.add(new BudgetEvent(color));
            eventMap.put(date, colorList);
            calendarView.refresh();
        }
    }

    private void removeDecorator(Date date){
        if(eventMap.containsKey(date)){
            eventMap.remove(date);
            calendarView.refresh();
        }
    }

    private void confirmDeleteTransaction(final int position){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText("Confirm Delete");
        message.setText("Are you sure you want to delete this transaction?");

        new AlertDialog.Builder(getActivity())
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteTransaction(position);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //updateTransactionList();

                        closeSwipeItem(position);

                    }
                })
                .create()
                .show();
    }

    private void deleteTransaction(int position){
        myRealm.beginTransaction();
        Log.d(TAG, "remove " + position + "-> from result");
        Log.d(TAG, "b4 There are "+resultsTransactionForDay.size()+" transactions today");
        resultsTransactionForDay.deleteFromRealm(position);
        myRealm.commitTransaction();
        Log.d(TAG, "After There are " + resultsTransactionForDay.size() + " transactions today");
        //updateTransactionList();


        updateScheduledTransactionsForDecoration();
    }

    private void openSwipeItem(int position){
        currentSwipeLayoutTarget = (SwipeLayout) linearLayoutManager.findViewByPosition(position);
        currentSwipeLayoutTarget.open();
    }

    private void closeSwipeItem(int position){
        currentSwipeLayoutTarget = (SwipeLayout) linearLayoutManager.findViewByPosition(position);
        currentSwipeLayoutTarget.close();
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
                updateMonthInToolbar();
                calendarView.moveToPreviousMonth();
                return true;
            case R.id.rightChevron:
                updateMonthInToolbar();
                calendarView.moveToNextMonth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data.getExtras() != null) {
            if(requestCode == Constants.RETURN_NEW_TRANSACTION){
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_TRANSACTION));

                populateTransactionsForDate(tt.getDate());
                updateTransactionStatus();
                updateScheduledTransactionsForDecoration();
                calendarView.selectDate(tt.getDate());
            }else if(requestCode == Constants.RETURN_EDIT_TRANSACTION){
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_TRANSACTION));

                populateTransactionsForDate(tt.getDate());
                updateTransactionStatus();
                updateScheduledTransactionsForDecoration();
                calendarView.selectDate(tt.getDate());
            }
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
    public void onClickTransaction(int position){
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

    @Override
    public void onDeleteTransaction(int position){
        confirmDeleteTransaction(position);
    }

    @Override
    public void onApproveTransaction(int position){
        myRealm.beginTransaction();
        resultsTransactionForDay.get(position).setDayType(DayType.COMPLETED.toString());
        myRealm.commitTransaction();
        updateTransactionList();

        updateScheduledTransactionsForDecoration();
    }

    @Override
    public void onUnapproveTransaction(int position){
        myRealm.beginTransaction();
        resultsTransactionForDay.get(position).setDayType(DayType.SCHEDULED.toString());
        myRealm.commitTransaction();
        updateTransactionList();

        updateScheduledTransactionsForDecoration();
    }

    @Override
    public void onPullDownAllow(boolean value){
        isPulldownAllow = value;
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
