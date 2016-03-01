package com.zhan.budget.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Activity.CategoryInfoActivity;
import com.zhan.budget.Activity.TransactionsForCategory;
import com.zhan.budget.Adapter.CategoryListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.PlusView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CategoryFragment extends Fragment implements
        CategoryListAdapter.OnCategoryAdapterInteractionListener{

    private OnCategoryInteractionListener mListener;
    private View view;

    private PtrFrameLayout frame;
    private PlusView header;
    private ViewGroup emptyLayout;

    private ListView categoryListView;
    private CategoryListAdapter categoryAdapter;
    private TextView emptyCategoryText;

    private List<Category> categoryList;

    private int categoryIndexEditted;//The index of the category that the user just finished editted.

    private Date currentMonth;

    private List<Transaction> transactionMonthList;

    private Realm myRealm;

    private RealmResults<Category> resultsCategory;
    private RealmResults<Transaction> resultsTransaction;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_category, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
        createPullDownToAddCategory();
        addListener();
        createSwipeMenu();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        currentMonth = new Date();

        categoryListView = (ListView) view.findViewById(R.id.categoryListView);
        emptyCategoryText = (TextView) view.findViewById(R.id.pullDownText);
        emptyCategoryText.setText("Pull down to add a category");

        transactionMonthList = new ArrayList<>();

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryListAdapter(this, categoryList);
        categoryListView.setAdapter(categoryAdapter);

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyCategoryLayout);

        populateCategoryWithNoInfo();

        //0 represents no change in month relative to currentMonth variable
        //false because we dont need to get all transactions yet.
        //This may conflict with populateCategoryWithNoInfo async where its trying to get the initial
        //categories
        updateMonthInToolbar(0, false);
    }

    private void createPullDownToAddCategory(){
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
                addNewCategory();
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

            }
        });
    }

    private void addListener(){
        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), "click on category :"+categoryList.get(position).getName(), Toast.LENGTH_SHORT).show();

                Intent viewAllTransactionsForCategory = new Intent(getContext(), TransactionsForCategory.class);
                viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_MONTH, Util.convertDateToString(currentMonth));

                Parcelable wrapped = Parcels.wrap(categoryList.get(position));

                viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY, wrapped);
                startActivity(viewAllTransactionsForCategory);
            }
        });
    }

    private void updateCategoryStatus(){
        if(categoryList.size() > 0){
            emptyLayout.setVisibility(View.GONE);
            categoryListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            categoryListView.setVisibility(View.GONE);
        }
    }

    /**
     * Displays prompt for user to add new category.
     */
    private void addNewCategory(){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.zz_mission_prompt, null);

        final EditText input = (EditText) promptView.findViewById(R.id.editTextCategory);

        new AlertDialog.Builder(getActivity())
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myRealm.beginTransaction();

                        Category c = myRealm.createObject(Category.class);
                        c.setId(Util.generateUUID());
                        c.setName(input.getText().toString());
                        c.setColor("#FF000000");
                        c.setIcon(6);
                        c.setBudget(100.0f);
                        c.setCost(0);

                        myRealm.commitTransaction();

                        categoryList.add(c);
                        categoryAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    //Should be called only the first time when the fragment is created
    private void populateCategoryWithNoInfo(){
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                categoryList = myRealm.copyFromRealm(resultsCategory);

                updateCategoryStatus();

                categoryAdapter.addAll(categoryList);
                populateCategoryWithInfo();
            }
        });
    }

    private void populateCategoryWithInfo(){
        final Date startMonth = Util.refreshMonth(currentMonth);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = Util.getPreviousDate(Util.getNextMonth(currentMonth));

        Log.d("DEBUG","Get all transactions from month is "+startMonth.toString()+", to next month is "+endMonth.toString());

        resultsTransaction = myRealm.where(Transaction.class).between("date", startMonth, endMonth).findAllAsync();
        resultsTransaction.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d("REALM", "got this month transaction, " + resultsTransaction.size());

                transactionMonthList = myRealm.copyFromRealm(resultsTransaction);

                aggregateCategoryInfo();
            }
        });
    }

    private void aggregateCategoryInfo(){
        Log.d("DEBUG","1) There are "+categoryList.size()+" categories");
        Log.d("DEBUG", "1) There are " + transactionMonthList.size() + " transactions for this month");

        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("DEBUG", "preparing to aggregate results");
            }

            @Override
            protected Void doInBackground(Void... voids) {

                startTime = System.nanoTime();

                //Go through each transaction and put them into the correct category
                for(int t = 0; t < transactionMonthList.size(); t++){
                    for(int c = 0; c < categoryList.size(); c++){
                        if(transactionMonthList.get(t).getCategory().getId().equalsIgnoreCase(categoryList.get(c).getId())){
                            float transactionPrice = transactionMonthList.get(t).getPrice();
                            float currentCategoryPrice = categoryList.get(c).getCost();
                            categoryList.get(c).setCost(transactionPrice + currentCategoryPrice);
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);

                for(int i = 0; i < categoryList.size(); i++){
                    Log.d("ZHAN1", "category : "+categoryList.get(i).getName()+" -> "+categoryList.get(i).getCost());
                }

                categoryAdapter.notifyDataSetChanged();

                endTime = System.nanoTime();
                duration = (endTime - startTime);

                long milli = (duration/1000000);
                long second = (milli/1000);
                float minutes = (second / 60.0f);
                Log.d("DEBUG", " aggregating took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
    }

    /**
     * Add swipe capability on list view to delete that item.
     * From 3rd party library.
     */
    private void createSwipeMenu() {
        // step 1. create a MenuCreator
        /*SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "edit" item
                SwipeMenuItem editItem = new SwipeMenuItem(getContext());
                editItem.setBackground(R.color.colorPrimary);// set item background
                editItem.setWidth(Util.dp2px(getContext(), 90));// set item width
                editItem.setIcon(R.drawable.svg_ic_edit);// set a icon
                menu.addMenuItem(editItem);// add to menu

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getContext());
                deleteItem.setBackground(R.color.red);// set item background
                deleteItem.setWidth(Util.dp2px(getContext(), 90));// set item width
                deleteItem.setIcon(R.drawable.svg_ic_delete);// set a icon
                menu.addMenuItem(deleteItem);// add to menu
            }
        };
        //set creator
        categoryListView.setMenuCreator(creator);

        // step 2. listener item click event
        categoryListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //edit
                        categoryIndexEditted = position;

                        editCategory(position);

                        break;
                    case 1:
                        //delete
                        //categoryList.get(position).removeFromRealm();

                        //categoryAdapter.remove(categoryList.get(position));
                        //categoryList.remove(position);
                        Toast.makeText(getContext(), "Deleting "+categoryList.get(position).getName(), Toast.LENGTH_SHORT).show();
                        break;
                }
                //False: Close the menu
                //True: Did not close the menu
                return false;
            }
        });

        // set SwipeListener
        categoryListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
                Log.d("CATEGORY_FRAGMENT", "swiping...");
                frame.setPtrHandler(disablePullDown);
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
                Log.d("CATEGORY_FRAGMENT", "swiping... done");
                frame.setPtrHandler(enablePullDown);
            }
        });*/
    }

    PtrHandler enablePullDown = new PtrHandler() {
        @Override
        public void onRefreshBegin(PtrFrameLayout insideFrame) {
            Log.d("CATEGORY_FRAGMENT", "-- on refresh begin");
            insideFrame.postDelayed(new Runnable() {
                @Override
                public void run() {
                    frame.refreshComplete();
                }
            }, 500);
        }

        @Override
        public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
            return PtrDefaultHandler.checkContentCanBePulledDown(frame, categoryListView, header);
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

    private void editCategory(int position){
        Intent editCategoryActivity = new Intent(getContext(), CategoryInfoActivity.class);

        Parcelable wrapped = Parcels.wrap(categoryList.get(position));

        editCategoryActivity.putExtra(Constants.REQUEST_EDIT_CATEGORY, wrapped);
        editCategoryActivity.putExtra(Constants.REQUEST_NEW_CATEGORY, true);

        startActivityForResult(editCategoryActivity, Constants.RETURN_EDIT_CATEGORY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_EDIT_CATEGORY) {

                Log.i("ZHAN", "----------- onActivityResult ----------");

                final Category categoryReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_CATEGORY));


                Log.d("ZHAN", "category name is "+categoryReturned.getName());
                Log.d("ZHAN", "category color is "+categoryReturned.getColor());
                Log.d("ZHAN", "category icon is "+categoryReturned.getIcon());
                Log.d("ZHAN", "category budget is "+categoryReturned.getBudget());
                Log.d("ZHAN", "category cost is "+categoryReturned.getCost());

                Log.i("ZHAN", "----------- onActivityResult ----------");

                Log.i("ZHAN", "eddited index :"+categoryIndexEditted);

                updateCategoryStatus();

                categoryList.set(categoryIndexEditted, categoryReturned);

                categoryAdapter.clear();
                categoryAdapter.addAll(categoryList);
                //categoryAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCategoryInteractionListener) {
            mListener = (OnCategoryInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void updateMonthInToolbar(int direction, boolean updateCategoryInfo){
        currentMonth = Util.getMonthWithDirection(currentMonth, direction);

        if(((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Util.convertDateToStringFormat2(currentMonth));
        }

        if(updateCategoryInfo) {
            resetCategoryInfo();
            populateCategoryWithInfo();
        }
    }

    private void resetCategoryInfo(){
        for(int i = 0; i < categoryList.size(); i++){
            categoryList.get(i).setCost(0);
        }
        categoryAdapter.notifyDataSetChanged();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.change_month_year, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.leftChevron:
                updateMonthInToolbar(-1, true);
                return true;
            case R.id.rightChevron:
                updateMonthInToolbar(1, true);
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
    public interface OnCategoryInteractionListener {
        void nextMonth();

        void previousMonth();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteCategory(int position){
        /*myRealm.beginTransaction();
        resultsAccount.remove(position);
        myRealm.commitTransaction();

        accountListAdapter.clear();
        accountListAdapter.addAll(accountList);*/

        Toast.makeText(getContext(), "deleting account "+categoryList.get(position).getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditCategory(int position){
        Toast.makeText(getContext(), "editting account "+categoryList.get(position).getName(), Toast.LENGTH_SHORT).show();
        categoryIndexEditted = position;
        editCategory(position);
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
