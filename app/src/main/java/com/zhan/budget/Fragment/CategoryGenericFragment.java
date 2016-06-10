package com.zhan.budget.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.CategoryInfoActivity;
import com.zhan.budget.Activity.Transactions.TransactionsForCategory;
import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Adapter.Helper.OnStartDragListener;
import com.zhan.budget.Adapter.Helper.SimpleItemTouchHelperCallback;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
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
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CategoryGenericFragment extends BaseRealmFragment implements
        CategoryGenericRecyclerAdapter.OnCategoryGenericAdapterInteractionListener {

    private static final String TAG = "CategoryEIFragment";
    private static final String ARG_1 = "budgetType";
    private static final String ARG_2 = "arrangementType";

    //Pull down
    private PtrFrameLayout frame;
    private PlusView header;
    private ViewGroup emptyLayout;

    private List<Category> categoryList;
    private CategoryGenericRecyclerAdapter categoryRecyclerAdapter;
    private RecyclerView categoryListView;

    private int categoryIndexEdited;//The index of the category that the user just finished edited.

    private Date currentMonth;

    private List<Transaction> transactionMonthList;

    private RealmResults<Category> resultsCategory;
    private RealmResults<Transaction> resultsTransaction;

    private Boolean isPulldownAllow = true;
    private ItemTouchHelper mItemTouchHelper;

    private BudgetType budgetType;
    private CategoryGenericRecyclerAdapter.ARRANGEMENT arrangementType;

    public CategoryGenericFragment() {
        // Required empty public constructor
    }

    public static CategoryGenericFragment newInstance(BudgetType budgetType, CategoryGenericRecyclerAdapter.ARRANGEMENT arrangementType) {
        CategoryGenericFragment fragment = new CategoryGenericFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_1, budgetType);
        args.putSerializable(ARG_2, arrangementType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_category_expense_income;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();
        budgetType = (BudgetType) getArguments().getSerializable(ARG_1);
        arrangementType = (CategoryGenericRecyclerAdapter.ARRANGEMENT) getArguments().getSerializable(ARG_2);

        currentMonth = new Date();

        TextView emptyCategoryText = (TextView) view.findViewById(R.id.pullDownText);
        emptyCategoryText.setText("Pull down to add an "+budgetType.toString()+" category");

        transactionMonthList = new ArrayList<>();

        categoryList = new ArrayList<>();

        categoryRecyclerAdapter = new CategoryGenericRecyclerAdapter(this, categoryList, arrangementType, new OnStartDragListener() {
                    @Override
                    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                        //isPulldownAllow = false;
                        mItemTouchHelper.startDrag(viewHolder);
                    }
                });

        categoryListView = (RecyclerView) view.findViewById(R.id.categoryListView);
        categoryListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoryListView.setAdapter(categoryRecyclerAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(categoryRecyclerAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(categoryListView);

        //Add divider
        categoryListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyCategoryLayout);

        populateCategoryWithNoInfo();

        createPullDownToAddCategory();
    }

    private void createPullDownToAddCategory(){
        frame = (PtrFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);

        header = new PlusView(getContext());

        frame.setHeaderView(header);

        frame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout insideFrame) {
                if(isPulldownAllow){
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
                return isPulldownAllow && PtrDefaultHandler.checkContentCanBePulledDown(frame, categoryListView, header);
            }
        });

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
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addNewCategory();
                    }
                }, 250);
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

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

    private void addNewCategory(){
        Intent addNewCategoryIntent = new Intent(getContext(), CategoryInfoActivity.class);
        addNewCategoryIntent.putExtra(Constants.REQUEST_NEW_CATEGORY, true);
        addNewCategoryIntent.putExtra(Constants.REQUEST_NEW_CATEGORY_TYPE, budgetType.toString());
        startActivityForResult(addNewCategoryIntent, Constants.RETURN_NEW_CATEGORY);
    }

    //Should be called only the first time when the fragment is created
    private void populateCategoryWithNoInfo(){
        resultsCategory = myRealm.where(Category.class).equalTo("type", budgetType.toString()).findAllSortedAsync("index");
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListeners();

                categoryList = myRealm.copyFromRealm(element);
                updateCategoryStatus();

                categoryRecyclerAdapter.setCategoryList(categoryList);

                if(arrangementType == CategoryGenericRecyclerAdapter.ARRANGEMENT.BUDGET) {
                    populateCategoryWithInfo();
                }
            }
        });
    }

    private void populateCategoryWithInfo(){
        final Date startMonth = DateUtil.refreshMonth(currentMonth);

        //Need to go a day before as Realm's between date does inclusive on both end
        //final Date endMonth = DateUtil.getPreviousDate(DateUtil.getNextMonth(currentMonth));
        final Date endMonth = DateUtil.getLastDateOfMonth(currentMonth);

        Log.d("DEBUG","Get all transactions from month is "+startMonth.toString()+", to next month is "+endMonth.toString());

        resultsTransaction = myRealm.where(Transaction.class).between("date", startMonth, endMonth).findAllAsync();
        resultsTransaction.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListeners();

                Log.d("REALM", "got this month transaction, " + element.size());

                transactionMonthList = myRealm.copyFromRealm(element);

                aggregateCategoryInfo();
            }
        });
    }

    private void aggregateCategoryInfo(){
        Log.d("DEBUG", "1) There are " + categoryList.size() + " categories");
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

                //categoryAdapter.notifyDataSetChanged();
                categoryRecyclerAdapter.setCategoryList(categoryList);

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

    private void editCategory(int position){
        Intent editCategoryActivity = new Intent(getContext(), CategoryInfoActivity.class);

        Parcelable wrapped = Parcels.wrap(categoryRecyclerAdapter.getCategoryList().get(position));

        editCategoryActivity.putExtra(Constants.REQUEST_EDIT_CATEGORY, wrapped);
        editCategoryActivity.putExtra(Constants.REQUEST_NEW_CATEGORY, false);

        startActivityForResult(editCategoryActivity, Constants.RETURN_EDIT_CATEGORY);
    }

    private void confirmDelete(final int position){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText("Confirm Delete");
        message.setText("Are you sure you want to delete this category?");

        new AlertDialog.Builder(getActivity())
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getContext(), "DELETE...", Toast.LENGTH_SHORT).show();
                        deleteCategory(position);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void deleteCategory(int position){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_EDIT_CATEGORY) {
                final Category categoryReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_CATEGORY));

                Log.i("ZHAN", "----------- onActivityResult edit category ----------");
                Log.d("ZHAN", "category name is "+categoryReturned.getName());
                Log.d("ZHAN", "category color is "+categoryReturned.getColor());
                Log.d("ZHAN", "category icon is "+categoryReturned.getIcon());
                Log.d("ZHAN", "category budget is "+categoryReturned.getBudget());
                Log.d("ZHAN", "category cost is " + categoryReturned.getCost());

                Log.i("ZHAN", "----------- onActivityResult edit category ----------");

                Log.i("ZHAN", "eddited index :" + categoryIndexEdited);

                categoryList.set(categoryIndexEdited, categoryReturned);
                categoryRecyclerAdapter.setCategoryList(categoryList);

                updateCategoryStatus();
            }else if(requestCode == Constants.RETURN_NEW_CATEGORY){
                final Category categoryReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_CATEGORY));

                Log.i("ZHAN", "----------- onActivityResult new category ----------");
                Log.d("ZHAN", "category name is "+categoryReturned.getName());
                Log.d("ZHAN", "category color is "+categoryReturned.getColor());
                Log.d("ZHAN", "category icon is "+categoryReturned.getIcon());
                Log.d("ZHAN", "category budget is "+categoryReturned.getBudget());
                Log.d("ZHAN", "category cost is " + categoryReturned.getCost());

                Log.i("ZHAN", "----------- onActivityResult new category ----------");

                categoryList.add(categoryReturned);
                categoryRecyclerAdapter.setCategoryList(categoryList);
                updateCategoryStatus();

                //Scroll to the last position
                categoryListView.scrollToPosition(categoryRecyclerAdapter.getItemCount() - 1);
            }
        }
    }

    public void updateMonthCategoryInfo(Date month){
        currentMonth = DateUtil.refreshMonth(month);
        resetCategoryInfo();
        populateCategoryWithInfo();
    }

    private void resetCategoryInfo(){
        if(categoryList != null) {
            for (int i = 0; i < categoryList.size(); i++) {
                categoryList.get(i).setCost(0);
            }
            //categoryAdapter.notifyDataSetChanged();
            categoryRecyclerAdapter.setCategoryList(categoryList);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteCategory(int position){
        confirmDelete(position);
    }

    @Override
    public void onEditCategory(int position){
        categoryIndexEdited = position;
        editCategory(position);
    }

    @Override
    public void onPullDownAllow(boolean value){
        isPulldownAllow = value;
        Toast.makeText(getContext(), "on pull down allow "+isPulldownAllow, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDoneDrag(){
        Log.d(TAG, "new suppose indices in fragment -----------");
        for(int i = 0; i < categoryRecyclerAdapter.getCategoryList().size(); i++){
            String name = categoryRecyclerAdapter.getCategoryList().get(i).getName();
            Log.d(TAG, i+"->"+name);
        }
        Log.d(TAG, "new suppose indices in fragment -----------");

        isPulldownAllow = true;
        Toast.makeText(getContext(), "on pull down allow "+isPulldownAllow, Toast.LENGTH_SHORT).show();

        resultsCategory = myRealm.where(Category.class).equalTo("type", budgetType.toString()).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListeners();

                myRealm.beginTransaction();

                for (int i = 0; i < element.size(); i++) {
                    for (int j = 0; j < categoryRecyclerAdapter.getCategoryList().size(); j++) {
                        String id1 = element.get(i).getId();
                        String name1 = element.get(i).getName();

                        String id2 = categoryRecyclerAdapter.getCategoryList().get(j).getId();
                        String name2 = categoryRecyclerAdapter.getCategoryList().get(j).getName();
                        Log.d(TAG, "comparing (" + id1 + "," + name1 + ") with (" + id2 + "," + name2 + ")");

                        if (element.get(i).getId().equalsIgnoreCase(categoryRecyclerAdapter.getCategoryList().get(j).getId())) {

                            Log.d(TAG, element.get(i).getName() + " old index is " + element.get(i).getIndex());
                            Log.d(TAG, "assigning new index : " + j + " came from " + categoryRecyclerAdapter.getCategoryList().get(j).getName());
                            element.get(i).setIndex(j);
                            Log.d(TAG, element.get(i).getName() + " new index is now " + element.get(i).getIndex());

                            break;
                        }
                    }
                }

                myRealm.commitTransaction();

                Log.d(TAG, "DONE UPDATING indices");
            }
        });
    }

    @Override
    public void onClick(int position){
        if(arrangementType == CategoryGenericRecyclerAdapter.ARRANGEMENT.BUDGET) {
            Toast.makeText(getContext(), "click on category :" + categoryRecyclerAdapter.getCategoryList().get(position).getName(), Toast.LENGTH_SHORT).show();

            Intent viewAllTransactionsForCategory = new Intent(getContext(), TransactionsForCategory.class);
            viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_GENERIC_MONTH, DateUtil.convertDateToString(currentMonth));

            Parcelable wrapped = Parcels.wrap(categoryRecyclerAdapter.getCategoryList().get(position));

            viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY, wrapped);
            startActivity(viewAllTransactionsForCategory);
        }
    }
}
