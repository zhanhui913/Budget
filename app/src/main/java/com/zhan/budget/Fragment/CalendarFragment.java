package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.p_v.flexiblecalendar.view.BaseCellView;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Adapter.TransactionListAdapter;
import com.zhan.budget.Database.Database;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Calendar.CustomEvent;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.RectangleCellView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFragment.OnCalendarListener} interface
 * to handle interaction events.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    private OnCalendarListener mListener;
    private View view;
    private FlexibleCalendarView calendarView;

    private int _yDelta;
    private ViewGroup root;
    private TextView  entryCountView, dateTextView, balanceText;
    private ImageView plusIcon;
    private ViewGroup infoPanel;

    private int centerPanelHeight;

    private SwipeMenuListView transactionListView;
    private TransactionListAdapter transactionAdapter;
    private ArrayList<Transaction> transactionList;

    private Boolean isScrollAtTop;
    private Boolean isTouchOffScroll;
    private Boolean isCenterPanelPulledDown;
    private Boolean isPanelCloseToTop;

    private Database db;
    private Date selectedDate;


    private Map<String, List<CustomEvent>> eventMap;

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CalendarFragment.
     */
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        addListener();
        createPanel();
        createCalendar();
        createSwipeMenu();
        updateTransactionStatus();

        //createFakeBulkData();
        createCustomEvents();
    }

    private void createCustomEvents(){
        /*
        eventMap = new HashMap<>();
        List<CustomEvent> colorLst = new ArrayList<>();
        colorLst.add(new CustomEvent(android.R.color.holo_red_dark));
        eventMap.put(25,colorLst);

        List<CustomEvent> colorLst1 = new ArrayList<>();
        colorLst1.add(new CustomEvent(android.R.color.holo_red_dark));
        colorLst1.add(new CustomEvent(android.R.color.holo_blue_light));
        colorLst1.add(new CustomEvent(android.R.color.holo_purple));
        eventMap.put(22,colorLst1);

        List<CustomEvent> colorLst2= new ArrayList<>();
        colorLst2.add(new CustomEvent(android.R.color.holo_red_dark));
        colorLst2.add(new CustomEvent(android.R.color.holo_blue_light));
        colorLst2.add(new CustomEvent(android.R.color.holo_purple));
        eventMap.put(28, colorLst1);

        List<CustomEvent> colorLst3 = new ArrayList<>();
        colorLst3.add(new CustomEvent(android.R.color.holo_red_dark));
        colorLst3.add(new CustomEvent(android.R.color.holo_blue_light));
        eventMap.put(29, colorLst1);*/
    }

    private void createFakeBulkData(){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("ASYNC", "preparing transaction");
                startTime = System.nanoTime();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] tempCategoryList = new String[]{"Breakfast","Lunch","Dinner", "Snacks","Drink","Rent","Travel", "Shopping","Necessity","Bill","Groceries"};

                final ArrayList<Category> tempCategoryArrayList = new ArrayList<>();

                //create category first
                for(int i = 0; i < tempCategoryList.length; i++){
                    Category c = new Category();
                    c.setName(tempCategoryList[i]);

                    Random random = new Random();

                    float budget = random.nextFloat() * 100.0f;
                    float cost = random.nextInt((int)budget);

                    c.setBudget(budget);
                    c.setCost(cost);

                    tempCategoryArrayList.add(c);

                    long categoryID = db.createCategory(c);

                    if(categoryID == -1){
                        Log.e("ZHAN", "db.createCategory returned -1");
                        continue;
                    }
                    c.setId((int)categoryID);
                }

                //create transactions
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date startDate = formatter.parse("2015-01-01");
                    Date endDate = formatter.parse("2015-12-31");

                    Calendar start = Calendar.getInstance();
                    start.setTime(startDate);
                    Calendar end = Calendar.getInstance();
                    end.setTime(endDate);

                    final ArrayList<Transaction> transactionArrayList = new ArrayList<>();


                    for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
                        // Do your job here with `date`.
                        Log.d("ASYNC",date.toString());

                        //Create 25 transactions per day
                        for(int i = 0; i < 25; i++){
                            Random random = new Random();

                            Log.d("TEST", "there are "+tempCategoryArrayList.size()+" categories");

                            int f = random.nextInt(tempCategoryArrayList.size() - 1);
                            Log.d("TEST", "f :"+f+" ->"+tempCategoryArrayList.get(f).getName());
                            Transaction transaction = new Transaction();
                            transaction.setDate(date);
                            transaction.setCategory(tempCategoryArrayList.get(f));
                            transaction.setPrice(random.nextFloat() * 120.0f);
                            transaction.setNote("Note " + i + " for " + Util.convertDateToString(date));

                            transactionArrayList.add(transaction);
                        }
                    }

                    db.createBulkTransaction(transactionArrayList);

                }catch (Exception e ){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                Log.d("ASYNC", "done transaction");

                db.exportDB();

                endTime = System.nanoTime();

                duration = (endTime - startTime);

                long milli = (duration/1000000);
                long second = (milli/1000);
                float minutes = (second/ 60.0f);

                Log.d("ASYNC", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };

        loader.execute();
    }

    private void init(){
        openDatabase();

        //By default it will be the current date;
        selectedDate = new Date();

        eventMap = new HashMap<>();

        root = (ViewGroup) view.findViewById(R.id.root);
        plusIcon = (ImageView) view.findViewById(R.id.plusIcon);
        calendarView = (FlexibleCalendarView) view.findViewById(R.id.calendarView);
        entryCountView = (TextView) view.findViewById(R.id.entryCount);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        balanceText = (TextView) view.findViewById(R.id.balanceText);
        transactionListView = (SwipeMenuListView) view.findViewById(R.id.transactionListView);

       // infoPanel = (ViewGroup) view.findViewById(R.id.infoPanel);

        isScrollAtTop = true;
        isTouchOffScroll = true;
        isCenterPanelPulledDown = false;
        isPanelCloseToTop = true;

        transactionList = new ArrayList<Transaction>();
        transactionAdapter = new TransactionListAdapter(getActivity(), transactionList);
        transactionListView.setAdapter(transactionAdapter);

        updateCalendarDecoratorsForMonth(selectedDate);

        //List all transactions for today
        populateTransactionsForDate(selectedDate);
    }

    private void createPanel(){
        //Used to get the height of the centerPanel to calculate dragging after it is drawn.
        //The height is different in pixels based on the DPI of devices.

        ViewTreeObserver vto = plusIcon.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                plusIcon.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                centerPanelHeight = plusIcon.getHeight();
                snapPanelUp();
                Toast.makeText(getContext(), "height is " + centerPanelHeight, Toast.LENGTH_SHORT).show();
            }
        });

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isScrollAtTop && isTouchOffScroll) {

                    final int Y = (int) event.getRawY();

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN: //CLICK DOWN
                            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

                            _yDelta = Y - lParams.topMargin;
                            break;
                        case MotionEvent.ACTION_UP: //CLICK UP
                            if (isPanelCloseToTop) {
                                Log.i("ZHAN2", "panel is close to top");
                                snapPanelUp();
                            }

                            if (isCenterPanelPulledDown) {
                                snapPanelDown();
                            }
                            break;
                        case MotionEvent.ACTION_POINTER_DOWN:
                            break;
                        case MotionEvent.ACTION_POINTER_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

                            int dY = Y - _yDelta;

                            if (dY >= 0) { //Prevents root from going too far down
                                layoutParams.topMargin = 0;
                                isCenterPanelPulledDown = true;
                                isPanelCloseToTop = false;
                            } else if (dY < -centerPanelHeight) { //Prevents root from going too far up
                                layoutParams.topMargin = -centerPanelHeight;
                                isCenterPanelPulledDown = false;
                                isPanelCloseToTop = true;
                            } else {
                                layoutParams.topMargin = dY;
                                isCenterPanelPulledDown = false;
                                isPanelCloseToTop = true;
                            }

                            layoutParams.bottomMargin = -250;

                            v.setLayoutParams(layoutParams);

                            break;
                    }
                    root.invalidate();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void createCalendar(){
        Calendar cal = Calendar.getInstance();
        updateTitle(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));

        calendarView.setCalendarView(new FlexibleCalendarView.CalendarView() {
            @Override
            public BaseCellView getCellView(int position, View convertView, ViewGroup parent, @BaseCellView.CellType int cellType) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    cellView = (BaseCellView) inflater.inflate(R.layout.date_cell_view, null);
                }
                if (cellType == BaseCellView.OUTSIDE_MONTH) {
                    cellView.setTextColor(getResources().getColor(R.color.purple));
                }
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
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, 1);
                updateTitle(year, month);
                snapPanelUp();

                //This is temporary for now because when we move to a new month, the 1st of that month is selected by default
                selectedDate = (new GregorianCalendar(year, month, 1)).getTime();
                populateTransactionsForDate(selectedDate);

                // updateCalendarDecoratorsForMonth(year, month);

                //Toast.makeText(getActivity(), "moved :" + Util.convertDateToString(selectedDate), Toast.LENGTH_SHORT).show();
            }
        });

        calendarView.setOnDateClickListener(new FlexibleCalendarView.OnDateClickListener() {
            @Override
            public void onDateClick(int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day);
                snapPanelUp();

                selectedDate = (new GregorianCalendar(year, month, day)).getTime();

                populateTransactionsForDate(selectedDate);

                //Toast.makeText(getActivity(), "clicked :" + Util.convertDateToString(selectedDate), Toast.LENGTH_SHORT).show();
            }
        });
/*
        calendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<CustomEvent> getEventsForTheDay(int year, int month, int day) {
                return getCustomEvents(year, month, day);
            }
        });*/
    }

    public List<CustomEvent> getCustomEvents(int year, int month, int day){ Log.d("VIEW", "getcustomEvents");
        String dateString = Util.convertDateToString((new GregorianCalendar(year, month, day)).getTime());
        return eventMap.get(dateString);
    }

    private void populateTransactionsForDate(final Date date){
        Log.d("ZHAN", "-------- populate transaction list for date " + Util.convertDateToString(date));

        //Populate the date's transaction list (if any)
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("ASYNC", "preparing transaction");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                transactionList = db.getAllTransactionInDate(date);
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                Log.d("ASYNC", "done transaction");
                transactionAdapter.refreshList(transactionList);
            }
        };

        loader.execute();

        Log.d("ZHAN", "-------- there are " + transactionList.size() + " transactions for " + Util.convertDateToString(date));
    }

    private void updateCalendarDecoratorsForMonth(Date date){
        //Update decorators for the given month
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        updateCalendarDecoratorsForMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
    }

    private void updateCalendarDecoratorsForMonth(final int year, final int month){ Log.d("VIEW", "updating decorators");
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            ArrayList<Transaction> thisMonthTransactionList = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("VIEW", "preparing transaction");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                //thisMonthTransactionList = db.getAllTransactionInMonth(year, month);
                thisMonthTransactionList = db.getAllTransactionInMonth(selectedDate, true);
                Log.d("VIEW", "TOTAL = there are " + thisMonthTransactionList.size() + " days with transactions in " + (month + 1) + ", " + year);

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                Log.d("VIEW", "done transaction");

                refreshView(thisMonthTransactionList);
            }
        };

        loader.execute();
    }

    private void refreshView(final ArrayList<Transaction> thisMonthTransactionList){ Log.d("VIEW","refreshView"); Log.d("VIEW","current month is "+selectedDate.toString());
        /*
        calendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<CalendarEvent> getEventsForTheDay(int year, int month, int day) { Log.d("VIEW", "try update year:"+year+", month:"+(month+1)+", day:"+day);

                for (int i = 0; i < thisMonthTransactionList.size(); i++) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(thisMonthTransactionList.get(i).getDate());

                    if (year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH) && day == cal.get(Calendar.DAY_OF_MONTH)) {
                        Log.d("VIEW", "success UPDATE DECORATOR FOR " + year + ", " + (month + 1) + ", " + day);
                        List<CalendarEvent> eventColors = new ArrayList<>();
                        eventColors.add(new CalendarEvent(android.R.color.holo_red_light));
                        return eventColors;
                    }
                }

                return null;
            }
        });*/

        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("VIEW", "preparing transaction");
            }

            @Override
            protected Void doInBackground(Void... voids) {

                for(int i = 0; i < thisMonthTransactionList.size(); i++){
                    List<CustomEvent> colorList = new ArrayList<>();
                    colorList.add(new CustomEvent(android.R.color.holo_red_dark));
                    eventMap.put(Util.convertDateToString(thisMonthTransactionList.get(i).getDate()),colorList);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                Log.d("VIEW", "done putting to hashMAP");
                doneHashMap();
            }
        };

        loader.execute();
    }

    private void doneHashMap(){ Log.d("VIEW", "doneHashMAP");
        calendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<CustomEvent> getEventsForTheDay(int year, int month, int day) {
                return getCustomEvents(year, month, day);
            }
        });
    }

    /**
     * Add swipe capability on list view to delete that item.
     * From 3rd party library.
     */
    private void createSwipeMenu(){
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getContext());
                deleteItem.setBackground(R.color.lightBlue);// set item background
                deleteItem.setWidth(Util.dp2px(getContext(), 90));// set item width
                deleteItem.setIcon(R.drawable.ic_down_arrow);// set a icon
                menu.addMenuItem(deleteItem);// add to menu
            }
        };
        //set creator
        transactionListView.setMenuCreator(creator);

        // step 2. listener item click event
        transactionListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //Delete the specific .json and all images it has
                        Toast.makeText(getContext(), "deleting ", Toast.LENGTH_SHORT).show();





                        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                Log.d("VIEW", "preparing transaction");
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                db.deleteTransaction(transactionList.get(position));
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void voids) {
                                super.onPostExecute(voids);
                                Log.d("VIEW", "done transaction");

                                transactionList.remove(position);
                                transactionAdapter.refreshList(transactionList);

                                updateTransactionStatus();
                            }
                        };

                        loader.execute();







                        break;
                }
                //False: Close the menu
                //True: Did not close the menu
                return false;
            }
        });

        // set SwipeListener
        transactionListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        transactionListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    Log.i("ZHAN2", "scrollStateChanged: stop (idle)");
                    isTouchOffScroll = true;

                    if (isCenterPanelPulledDown) {
                        snapPanelDown();
                    }

                } else if (scrollState == 1) {
                    Log.i("ZHAN2", "scrollStateChanged: still moving (touch)");
                    isTouchOffScroll = false;
                } else if (scrollState == 2) {
                    Log.i("ZHAN2", "scrollStateChanged: preparing to stop (fling)");
                    isTouchOffScroll = false;
                }
                dateTextView.setText("a:" + isScrollAtTop + ", b:" + isTouchOffScroll);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    View v = transactionListView.getChildAt(0);

                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
                        // reached the top:
                        isScrollAtTop = true;
                        entryCountView.setText("top reached");
                    } else {
                        isScrollAtTop = false;
                        entryCountView.setText("top not reached");
                    }
                } else if (totalItemCount - visibleItemCount == firstVisibleItem) {
                    isScrollAtTop = false;
                    if (transactionListView.getLastVisiblePosition() == transactionListView.getAdapter().getCount() - 1
                            && transactionListView.getChildAt(transactionListView.getChildCount() - 1).getBottom() <= transactionListView.getHeight()) {

                        entryCountView.setText("bottom reached");
                    } else {
                        entryCountView.setText("bottom not reached");
                    }
                } else {
                    isScrollAtTop = false;
                    entryCountView.setText("middle");
                }
            }
        });

        transactionListView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: //CLICK DOWN
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) root.getLayoutParams();
                        _yDelta = Y - lParams.topMargin;

                        break;
                    case MotionEvent.ACTION_UP: //CLICK UP
                        Log.i("ZHAN2", "action up");
                        if (isPanelCloseToTop) {
                            Log.i("ZHAN2", "panel is close to top");
                            snapPanelUp();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) root.getLayoutParams();

                        int dY = Y - _yDelta;

                        balanceText.setText("" + dY);

                        if (isScrollAtTop) {
                            if (dY >= 0) { //Prevents root from going too far down
                                layoutParams.topMargin = 0;
                                //Log.i("ZHAN2", "down at max");
                                isCenterPanelPulledDown = true;
                                isPanelCloseToTop = false;
                            } else if (dY < -centerPanelHeight) { //Prevents root from going too far up
                                layoutParams.topMargin = -centerPanelHeight;
                                //Log.i("ZHAN2", "up at max");
                                isCenterPanelPulledDown = false;
                                isPanelCloseToTop = true;
                            } else {
                                //Log.i("ZHAN2", "still going");
                                layoutParams.topMargin = dY;
                                isCenterPanelPulledDown = false;
                                isPanelCloseToTop = true;
                                v.setClickable(false);
                            }
                        }

                        root.setLayoutParams(layoutParams);
                        break;
                }
                return false;
            }
        });
    }

    private void updateTransactionStatus(){
/*
        if(transactionList.size() > 0){
            infoPanel.setVisibility(View.GONE);
        }else{
            infoPanel.setVisibility(View.VISIBLE);
        }*/

       // entryCountView.setText(transactionList.size() + "");
    }

    private void snapPanelUp(){ Log.i("ZHAN2", "snapping panel up");
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) root.getLayoutParams();
        layoutParams.topMargin = -centerPanelHeight;
        root.setLayoutParams(layoutParams);

    }

    private void snapPanelDown(){ Log.i("ZHAN2", "snapping panel down");
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) root.getLayoutParams();
        layoutParams.topMargin = 0;
        root.setLayoutParams(layoutParams);

        playRotateAnimation();
    }

    private void playRotateAnimation(){
        Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_rotate);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent newTransaction = new Intent(getContext(), TransactionInfoActivity.class);

                        //This is not edit mode
                        newTransaction.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
                        newTransaction.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, Util.convertDateToString(selectedDate));
                        startActivityForResult(newTransaction, Constants.RETURN_NEW_TRANSACTION);
                        snapPanelUp();
                    }
                }, 500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //play rotate animation of the plus icon
        plusIcon.startAnimation(anim);
    }

    private void addListener(){
    }

    private void updateTitle(int year, int month){
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        if(((AppCompatActivity)getActivity()).getSupportActionBar() != null){
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(cal.getDisplayName(Calendar.MONTH, Calendar.LONG,
                    this.getResources().getConfiguration().locale) + " " + year);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_NEW_TRANSACTION){

                Log.i("ZHAN", "----------- onActivityResult ----------");

                Transaction transaction = data.getExtras().getParcelable(Constants.RESULT_NEW_TRANSACTION);

                Log.d("ZHAN", "transaction name is "+transaction.getNote()+" cost is "+transaction.getPrice());
                Log.d("ZHAN", "category is "+transaction.getCategory().getName()+", "+transaction.getCategory().getId());
                Log.i("ZHAN", "----------- onActivityResult ----------");
                db.createTransaction(transaction);

                transactionList.add(transaction);
                transactionAdapter.refreshList(transactionList);

                updateCalendarDecoratorsForMonth(selectedDate);
            }
        }
    }

    public void openDatabase(){
        db = new Database(getActivity().getApplicationContext());
    }

    public void closeDatabase(){
        db.close();
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
        closeDatabase();
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
}
