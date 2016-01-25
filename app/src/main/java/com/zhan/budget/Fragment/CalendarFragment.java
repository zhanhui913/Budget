package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.p_v.flexiblecalendar.view.BaseCellView;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Adapter.TransactionListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Calendar.CustomEvent;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Parcelable.ParcelableTransaction;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.PlusView;
import com.zhan.budget.View.RectangleCellView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
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
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    private OnCalendarListener mListener;
    private View view;
    private FlexibleCalendarView calendarView;

    private TextView  totalCostForDay, dateTextView;
    private ViewGroup infoPanel;


    private SwipeMenuListView transactionListView;
    private TransactionListAdapter transactionAdapter;
    private List<Transaction> transactionList;

    private Date selectedDate;

    private Realm myRealm;

    private Map<String, List<CustomEvent>> eventMap;

    private RealmResults<Transaction> resultsTransactionForDay;
    private RealmResults<Transaction> resultsTransactionForMonth;

    private PtrClassicFrameLayout mPtrFrame;
    private PlusView header;

    private View emptyLayout;

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
        return new CalendarFragment();
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
        createPullToAddTransaction();
        createCalendar();
        createSwipeMenu();

        createEmptyListPanel();

        //List all transactions for today
        populateTransactionsForDate(selectedDate);
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        //By default it will be the current date;
        selectedDate = new Date();

        eventMap = new HashMap<>();

        calendarView = (FlexibleCalendarView) view.findViewById(R.id.calendarView);
        totalCostForDay = (TextView) view.findViewById(R.id.totalCostForDay);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);

        transactionListView = (SwipeMenuListView) view.findViewById(R.id.transactionListView);
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionListAdapter(getActivity(), transactionList);
        transactionListView.setAdapter(transactionAdapter);

        dateTextView.setText(Util.convertDateToStringFormat1(selectedDate));

        //infoPanel = (ViewGroup) view.findViewById(R.id.infoPanel);
    }

    private void createPullToAddTransaction(){
        mPtrFrame = (PtrClassicFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);

        header = new PlusView(getContext());

        mPtrFrame.setHeaderView(header);

        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "-- on refresh begin");
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrFrame.refreshComplete();
                    }
                }, 500);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });

        mPtrFrame.addPtrUIHandler(new PtrUIHandler() {

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
                //This is temporary for now because when we move to a new month, the 1st of that month is selected by default
                selectedDate = (new GregorianCalendar(year, month, 1)).getTime();
                populateTransactionsForDate(selectedDate);

                dateTextView.setText(Util.convertDateToStringFormat1(selectedDate));

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
        Date beginDate = Util.refreshDate(date);
        Date endDate = Util.getNextDate(date);

        Log.d("CALENDAR_FRAGMENT", " populate transaction list for date between " + beginDate.toString() + " and " + endDate.toString());

        transactionAdapter.clear();

        resultsTransactionForDay = myRealm.where(Transaction.class).greaterThanOrEqualTo("date", beginDate).lessThan("date", endDate).findAllAsync();
        resultsTransactionForDay.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d("CALENDAR_FRAGMENT", "received " + resultsTransactionForDay.size() + " transactions");

                totalCostForDay.setText(Util.setPriceToCorrectDecimalInString(resultsTransactionForDay.sum("price")));

                transactionList = myRealm.copyFromRealm(resultsTransactionForDay);
                updateTransactionStatus();

                transactionAdapter.clear();
                transactionAdapter.addAll(transactionList);
            }
        });
    }

    /**
     * Add swipe capability on list view to delete that item.
     * From 3rd party library.
     */
    private void createSwipeMenu(){
        //transactionListView = new SwipeMenuListView(getContext());
        //transactionListView.setDividerHeight(Util.dp2px(getContext(), 1));

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getContext());
                deleteItem.setBackground(R.color.red);// set item background
                deleteItem.setWidth(Util.dp2px(getContext(), 90));// set item width
                deleteItem.setIcon(R.drawable.ic_delete);// set a icon
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
                        myRealm.beginTransaction();
                        resultsTransactionForDay.get(position).removeFromRealm();
                        myRealm.commitTransaction();
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
    }

    private void createEmptyListPanel(){
       emptyLayout = (LayoutInflater.from(getContext())).inflate(R.layout.empty_transaction_indicator, mPtrFrame, false);
    }

    private void updateTransactionStatus(){
        if(transactionList.size() > 0){
  /*          mPtrFrame.removeView(emptyLayout);

            if(transactionListView.getParent() == null){
                mPtrFrame.addView(transactionListView);
            }
*/

            replaceView(emptyLayout, transactionListView);
        }else{
            replaceView(transactionListView, emptyLayout);

/*
            Log.d("ZHAN", "1 child count : " + mPtrFrame.getChildCount());

            //mPtrFrame.removeView(transactionListView);

            Log.d("ZHAN", "2 child count : " + mPtrFrame.getChildCount());

            if(emptyLayout.getParent() == null) {

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mPtrFrame.getLayoutParams();
                params.width = PtrClassicFrameLayout.LayoutParams.MATCH_PARENT;
                params.height = PtrClassicFrameLayout.LayoutParams.WRAP_CONTENT;

                emptyLayout.setLayoutParams(params);


                //mPtrFrame.setHeaderView(header);
                //mPtrFrame.addView(emptyLayout);

                replaceView(transactionListView, emptyLayout);


                Log.d("ZHAN", "3 child count : " + mPtrFrame.getChildCount());
            }*/
        }
    }

    public void replaceView(View currentView, View newView) {
        ViewGroup parent = getParent(currentView);
        if(parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        removeView(newView);
        parent.addView(newView, index);
    }

    public ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    public void removeView(View view) {
        ViewGroup parent = getParent(view);
        if(parent != null) {
            parent.removeView(view);
        }
    }

    private void addNewTransaction(){
        Intent newTransaction = new Intent(getContext(), TransactionInfoActivity.class);

        //This is not edit mode
        newTransaction.putExtra(Constants.REQUEST_NEW_TRANSACTION, false);
        newTransaction.putExtra(Constants.REQUEST_NEW_TRANSACTION_DATE, Util.convertDateToString(selectedDate));
        startActivityForResult(newTransaction, Constants.RETURN_NEW_TRANSACTION);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_NEW_TRANSACTION){

                Log.i("ZHAN", "----------- onActivityResult ----------");

                final ParcelableTransaction parcelableTransaction = data.getExtras().getParcelable(Constants.RESULT_NEW_TRANSACTION);

                Log.d("ZHAN", "transaction name is " + parcelableTransaction.getNote() + " cost is " + parcelableTransaction.getPrice());
                Log.d("ZHAN", "category is " + parcelableTransaction.getCategory().getName() + ", " + parcelableTransaction.getCategory().getId());
                Log.d("ZHAN", "account name : " +parcelableTransaction.getAccount().getName());
                Log.i("ZHAN", "----------- onActivityResult ----------");

                //1st option (not async)
                final RealmResults<Category> cateList = myRealm.where(Category.class).equalTo("id", parcelableTransaction.getCategory().getId()).findAllAsync();
                cateList.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {

                        if (cateList.size() != 0) {
                            Category cat = myRealm.copyToRealm(cateList.get(0));

                            myRealm.beginTransaction();
                            Transaction transactionReturnedFromTransaction = myRealm.createObject(Transaction.class);
                            transactionReturnedFromTransaction.setId(Util.generateUUID());
                            transactionReturnedFromTransaction.setPrice(parcelableTransaction.getPrice());
                            transactionReturnedFromTransaction.setDate(parcelableTransaction.getDate());
                            transactionReturnedFromTransaction.setNote(parcelableTransaction.getNote());
                            transactionReturnedFromTransaction.setCategory(cat);
                            myRealm.commitTransaction();


                            Log.d("ZHAN", "successfully added transaction : " + transactionReturnedFromTransaction.getNote() + " for cat : " + transactionReturnedFromTransaction.getCategory().getName());
                            transactionList.add(transactionReturnedFromTransaction);

                            transactionAdapter.notifyDataSetChanged();
                            updateTransactionStatus();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.change_month, menu);
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
