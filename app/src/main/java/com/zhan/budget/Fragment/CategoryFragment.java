package com.zhan.budget.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.zhan.budget.Activity.CategoryInfo;
import com.zhan.budget.Activity.TransactionsForCategory;
import com.zhan.budget.Adapter.CategoryListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Parcelable.ParcelableCategory;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CategoryFragment.OnCategoryInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment {

    private OnCategoryInteractionListener mListener;
    private View view;
    private FloatingActionButton fab;

    private SwipeMenuListView categoryListView;
    private CategoryListAdapter categoryAdapter;
    private TextView balanceText;

    private List<Category> categoryList;

    private int categoryIndexEditted;//The index of the category that the user just finished editted.

    private Date currentMonth;

    private List<Transaction> transactionMonthList;

    private Realm myRealm;

    private RealmResults<Category> resultsCategory;
    private RealmResults<Transaction> resultsTransaction;

    private SwipeRefreshLayout swipeContainer;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CategoryFragment.
     */
    public static CategoryFragment newInstance() {
        return new CategoryFragment();
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
        view = inflater.inflate(R.layout.fragment_category, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
        addListener();
        createSwipeMenu();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        currentMonth = new Date();

        fab = (FloatingActionButton) view.findViewById(R.id.addCategoryFAB);
        categoryListView = (SwipeMenuListView) view.findViewById(R.id.categoryListView);
        balanceText = (TextView) view.findViewById(R.id.categoryMonthBalance);

        transactionMonthList = new ArrayList<>();

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryListAdapter(getActivity(), categoryList);
        categoryListView.setAdapter(categoryAdapter);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        try {
            Field f = swipeContainer.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView img = (ImageView)f.get(swipeContainer);
            img.setImageResource(R.drawable.ic_add);
            img.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        populateCategoryWithNoInfo();

        //0 represents no change in month relative to currentMonth variable
        //false because we dont need to get all transactions yet.
        //This may conflict with populateCategoryWithNoInfo async where its trying to get the initial
        //categories
        updateMonthInToolbar(0, false);
    }

    private void addListener(){
        fab.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                displayPrompt();
            }
        });

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableCategory parcelableCategory = new ParcelableCategory();
                parcelableCategory.convertCategoryToParcelable(categoryList.get(position));

                Intent viewAllTransactionsForCategory = new Intent(getContext(), TransactionsForCategory.class);
                viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_MONTH, Util.convertDateToString(currentMonth));
                viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY, parcelableCategory);
                startActivity(viewAllTransactionsForCategory);
            }
        });

        categoryListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (categoryListView == null || categoryListView.getChildCount() == 0) ?
                                0 : categoryListView.getChildAt(0).getTop();
                swipeContainer.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
    }

    /**
     * Displays prompt for user to type new category.
     */
    private void displayPrompt(){
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
                        c.setColor("#000000");
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

                categoryAdapter.addAll(categoryList);
                populateCategoryWithInfo();
            }
        });
    }


    private void populateCategoryWithInfo(){
        //final Date startMonth = new GregorianCalendar(year, month, 1).getTime();
        final Date startMonth = Util.refreshMonth(currentMonth);
        final Date endMonth = Util.getNextMonth(currentMonth);

        Log.d("REALM","This month is "+startMonth.toString()+", next month is "+endMonth.toString());

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
                    //Log.d("POP", "transaction with category id : " +resultsTransaction.get(t).getCategory().getId());
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
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "edit" item
                SwipeMenuItem editItem = new SwipeMenuItem(getContext());
                editItem.setBackground(R.color.colorPrimary);// set item background
                editItem.setWidth(Util.dp2px(getContext(), 90));// set item width
                editItem.setIcon(R.drawable.ic_mode_edit);// set a icon
                menu.addMenuItem(editItem);// add to menu

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getContext());
                deleteItem.setBackground(R.color.red);// set item background
                deleteItem.setWidth(Util.dp2px(getContext(), 90));// set item width
                deleteItem.setIcon(R.drawable.ic_delete);// set a icon
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

                        Intent editCategory = new Intent(getContext(), CategoryInfo.class);

                        //This is edit mode
                        //editCategory.putExtra(Constants.REQUEST_EDIT_CATEGORY, categoryList.get(position));
                        startActivityForResult(editCategory, Constants.RETURN_EDIT_CATEGORY);

                        break;
                    case 1:
                        //delete

                        categoryList.get(position).removeFromRealm();

                        categoryAdapter.remove(categoryList.get(position));
                        categoryList.remove(position);

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
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_EDIT_CATEGORY){

                Log.i("ZHAN", "----------- onActivityResult ----------");

                Category category = data.getExtras().getParcelable(Constants.RESULT_EDIT_CATEGORY);

                Log.d("ZHAN", "category name is "+category.getName());
                Log.i("ZHAN", "----------- onActivityResult ----------");

                //db.updateCategory(category);


                categoryList.set(categoryIndexEditted, category);
                categoryAdapter.notifyDataSetChanged();
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
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentMonth);
        cal.add(Calendar.MONTH, direction);

        currentMonth = cal.getTime();

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
        //closeDatabase();

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
}
