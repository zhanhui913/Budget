package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
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
import com.p_v.flexiblecalendar.entity.CalendarEvent;
import com.p_v.flexiblecalendar.view.BaseCellView;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Adapter.TransactionListAdapter;
import com.zhan.budget.Database.Database;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.RectangleCellView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
    }

    private void init(){
        openDatabase();

        //By default it will be the current date;
        selectedDate = new Date();

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

        populateTransactionsForDate(selectedDate);
    }

    private void populateTransactionsForDate(Date date){

        Log.d("ZHAN", "populate "+Util.convertDateToString(date)+" transaction list");

        //Populate the date's transaction list (if any)
        transactionList = db.getAllTransaction(date);
        transactionAdapter.refreshList(transactionList);


        Log.d("ZHAN", "there are "+transactionList.size()+" transaction for "+Util.convertDateToString(date));
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

                selectedDate = (new GregorianCalendar(year, month, 1)).getTime();

                Toast.makeText(getActivity(), "moved :" +Util.convertDateToString(selectedDate) , Toast.LENGTH_SHORT).show();

                populateTransactionsForDate(selectedDate);
            }
        });

        calendarView.setOnDateClickListener(new FlexibleCalendarView.OnDateClickListener() {
            @Override
            public void onDateClick(int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day);
                snapPanelUp();

                selectedDate = (new GregorianCalendar(year, month, day)).getTime();

                Toast.makeText(getActivity(), "clicked :" +Util.convertDateToString(selectedDate) , Toast.LENGTH_SHORT).show();

                populateTransactionsForDate(selectedDate);
            }
        });

        calendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<CalendarEvent> getEventsForTheDay(int year, int month, int day) {
                if (year == 2015 && month == 11 && day == 12) {
                    List<CalendarEvent> eventColors = new ArrayList<>(2);
                    eventColors.add(new CalendarEvent(android.R.color.holo_blue_light));
                    eventColors.add(new CalendarEvent(android.R.color.holo_purple));
                    return eventColors;
                }
                if (year == 2015 && month == 11 && day == 7 ||
                        year == 2015 && month == 11 && day == 29 ||
                        year == 2015 && month == 11 && day == 5 ||
                        year == 2015 && month == 11 && day == 9) {
                    List<CalendarEvent> eventColors = new ArrayList<>(1);
                    eventColors.add(new CalendarEvent(android.R.color.holo_blue_light));
                    return eventColors;
                }

                if (year == 2016 && month == 00 && day == 31 ||
                        year == 2015 && month == 11 && day == 22 ||
                        year == 2015 && month == 11 && day == 18 ||
                        year == 2015 && month == 11 && day == 11) {
                    List<CalendarEvent> eventColors = new ArrayList<>(3);
                    eventColors.add(new CalendarEvent(android.R.color.holo_red_dark));
                    eventColors.add(new CalendarEvent(android.R.color.holo_orange_light));
                    eventColors.add(new CalendarEvent(android.R.color.holo_purple));
                    eventColors.add(new CalendarEvent(android.R.color.holo_blue_bright));
                    eventColors.add(new CalendarEvent(android.R.color.holo_green_light));
                    eventColors.add(new CalendarEvent(android.R.color.holo_red_dark));
                    eventColors.add(new CalendarEvent(android.R.color.holo_orange_light));
                    eventColors.add(new CalendarEvent(android.R.color.holo_purple));
                    eventColors.add(new CalendarEvent(android.R.color.holo_blue_bright));
                    eventColors.add(new CalendarEvent(android.R.color.holo_green_light));
                    return eventColors;
                }

                return null;
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
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //Delete the specific .json and all images it has
                        Toast.makeText(getContext(), "deleting ", Toast.LENGTH_SHORT).show();

                        transactionList.remove(position);
                        transactionAdapter.notifyDataSetChanged();


                        updateTransactionStatus();


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

                Log.i("ZHAN", "finished creating new transaction ----------");

                Transaction transaction = data.getExtras().getParcelable(Constants.RESULT_NEW_TRANSACTION);

                Log.d("ZHAN", "transaction name is "+transaction.getNote()+" cost is "+transaction.getPrice());
                Log.d("ZHAN", "category is "+transaction.getCategory().getName()+", "+transaction.getCategory().getId());
                Log.i("ZHAN", "finished creating new transaction ----------");
                db.createTransaction(transaction);

                transactionList.add(transaction);
                transactionAdapter.refreshList(transactionList);
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
