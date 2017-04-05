package com.zhan.budget.Fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.CategoryInfoActivity;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Activity.Transactions.TransactionsForCategory;
import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Etc.CategoryCalculator;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class OverviewGenericFragment extends BaseRealmFragment implements
        CategoryGenericRecyclerAdapter.OnCategoryGenericAdapterInteractionListener{

    private static final String TAG = "OverviewGenericFragment";
    private static final String ARG_1 = "budgetType";
    private static final String ARG_2 = "currentMonth";
    private Date currentMonth;
    private BudgetType budgetType;

    private CircularProgressBar circularProgressBar;
    private ViewGroup emptyLayout;
    private TextView emptyAccountPrimaryText, emptyAccountSecondaryText;

    private RealmResults<Category> resultsCategory;
    private RealmResults<Transaction> transactionsResults;
    private List<Transaction> transactionList = new ArrayList<>();
    private List<Category> categoryList;

    private CategoryGenericRecyclerAdapter categoryPercentListAdapter;
    private RecyclerView categoryListView;
    private OverviewInteractionListener mListener;

    private Category categoryEdited;

    public static OverviewGenericFragment newInstance(BudgetType budgetType, Date currentMonth) {
        OverviewGenericFragment fragment = new OverviewGenericFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_1, budgetType);
        args.putSerializable(ARG_2, currentMonth);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_overview_generic;
    }

    @Override
    protected void init() {
        super.init();

        budgetType = (BudgetType) getArguments().getSerializable(ARG_1);
        currentMonth = (Date) getArguments().getSerializable(ARG_2);

        circularProgressBar = (CircularProgressBar) view.findViewById(R.id.overviewProgressBar);

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyOverviewLayout);
        emptyAccountPrimaryText = (TextView) view.findViewById(R.id.emptyPrimaryText);
        emptyAccountPrimaryText.setText(String.format(getString(R.string.empty_category), budgetType.toString()));
        emptyAccountSecondaryText = (TextView) view.findViewById(R.id.emptySecondaryText);
        emptyAccountSecondaryText.setText("");

        //Initially, the empty layout should be hidden
        emptyLayout.setVisibility(View.GONE);

        categoryList = new ArrayList<>();
        categoryListView = (RecyclerView) view.findViewById(R.id.percentCategoryListView);
        categoryPercentListAdapter = new CategoryGenericRecyclerAdapter(this, categoryList, CategoryGenericRecyclerAdapter.ARRANGEMENT.PERCENT, null);

        categoryListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoryListView.setAdapter(categoryPercentListAdapter);

        //Add divider
        categoryListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        getCategoryList();
    }

    public void setCurrentMonth(Date date){
        this.currentMonth = date;
    }

    //Should be called only the first time when the fragment is created
    public void getCategoryList(){
        //final Realm myRealm = Realm.getDefaultInstance();
        resultsCategory = myRealm.where(Category.class).equalTo("type", budgetType.toString()).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListener(this);

                categoryList.clear();
                categoryList = myRealm.copyFromRealm(element);
                Log.d("qwer","there are "+categoryList.size()+" category of type : "+budgetType.toString());
                getMonthReport(currentMonth, true);
            }
        });
    }

    private void getMonthReport(Date date, final boolean animate){
        //Refresh these variables
        final Date month = DateUtil.refreshMonth(date);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = DateUtil.getLastDateOfMonth(month);

        Log.d("OVERVIEW_ACT", "("+DateUtil.convertDateToStringFormat1(getContext(), month) + "-> "+DateUtil.convertDateToStringFormat1(getContext(), endMonth)+")");

        //final Realm myRealm = Realm.getDefaultInstance();  BudgetPreference.addRealmCache(getContext());
        transactionsResults = myRealm.where(Transaction.class).between("date", month, endMonth).equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                transactionList.clear();
                transactionList = myRealm.copyFromRealm(element);
               // myRealm.close();  BudgetPreference.removeRealmCache(getContext());
                performAsyncCalculation(animate);
            }
        });
    }

    /**
     * Perform tedious calculation asynchronously to avoid blocking main thread
     */
    private void performAsyncCalculation(final boolean animate){
        resetCategoryValues();

        CategoryCalculator cc = new CategoryCalculator(transactionList, categoryList, new Date(), budgetType, new CategoryCalculator.OnCategoryCalculatorInteractionListener() {
            @Override
            public void onCompleteCalculation(List<Category> catList) {
                categoryList = catList;

                //Calculate total cost
                float sumCost = 0f;
                for(int i = 0; i < categoryList.size(); i++){
                    sumCost += categoryList.get(i).getCost();
                }

                //Now calculate percentage for each category
                for(int i = 0; i < categoryList.size(); i++){
                    BigDecimal current = BigDecimal.valueOf(categoryList.get(i).getCost());
                    BigDecimal total = BigDecimal.valueOf(sumCost);
                    BigDecimal hundred = new BigDecimal(100);
                    BigDecimal percent = current.divide(total, 4, BigDecimal.ROUND_HALF_EVEN);

                    categoryList.get(i).setPercent(percent.multiply(hundred).floatValue());
                }

                categoryPercentListAdapter.setCategoryList(categoryList);

                //Once the calculation is done, remove it
                circularProgressBar.setVisibility(View.GONE);

                updateCategoryStatus();

                mListener.onComplete(budgetType, categoryList, sumCost, animate);
            }
        });
        cc.execute();
    }

    private void resetCategoryValues(){
        for(int i = 0; i  < categoryList.size(); i++){
            categoryList.get(i).setCost(0);
            categoryList.get(i).setPercent(0);
        }
    }

    private void confirmDelete(final int position){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(getString(R.string.warning_delete_category));

        new AlertDialog.Builder(getContext())
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCategory(position);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Does nothing for now
                    }
                })
                .create()
                .show();
    }

    private void deleteCategory(int position){
        Log.d(TAG, "view, remove " + position + "-> from result "+categoryList.get(position).getName());
        Log.d(TAG, "b4 There are "+resultsCategory.size()+" category, trying to remove "+resultsCategory.get(position).getName());

        final RealmResults<Category> categoryToBeRemove = myRealm.where(Category.class).equalTo("id", categoryList.get(position).getId()).findAllAsync();
        categoryToBeRemove.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListener(this);

                myRealm.beginTransaction();
                Log.d(TAG, "removing found category : "+element.get(0).getName());

                //grab the first one as there should only have 1 category due to the ID being unique
                categoryToBeRemove.deleteFirstFromRealm();

                myRealm.commitTransaction();
            }
        });

        //myRealm.commitTransaction();
        Log.d(TAG, "After There are " + resultsCategory.size() + " category");

        categoryList.remove(position);
        categoryPercentListAdapter.setCategoryList(categoryList);

        //this recalculates
        getMonthReport(currentMonth, true);
    }

    private void editCategory(int position){
        categoryEdited = categoryList.get(position);
        startActivityForResult(CategoryInfoActivity.createIntentToEditCategory(getContext(), categoryEdited), RequestCodes.EDIT_CATEGORY);
    }

    private void updateCategoryStatus(){
        if(categoryPercentListAdapter.getCategoryList().size() > 0){
            emptyLayout.setVisibility(View.GONE);
            categoryListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            categoryListView.setVisibility(View.GONE);
        }
    }

    public void setListener(OverviewInteractionListener mListener){
        this.mListener = mListener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data.getExtras() != null) {
            if(requestCode == RequestCodes.HAS_TRANSACTION_CHANGED){
                boolean hasChanged = data.getExtras().getBoolean(TransactionInfoActivity.HAS_CHANGED);

                if(hasChanged){
                    //If something has been changed, update the list and the pie chart
                    getMonthReport(currentMonth, true);
                }
            }else if(requestCode == RequestCodes.EDIT_CATEGORY){
                final Category categoryReturned = Parcels.unwrap(data.getExtras().getParcelable(CategoryInfoActivity.RESULT_CATEGORY));

                if(!categoryReturned.checkEquals(categoryEdited)){
                    //If something has been changed, update the list and the pie chart
                    getCategoryList();
                }
            }
        }
    }

    //Different from resetCategoryValues(), this removes the adapters list
    public void resetCategoryList(){
        categoryPercentListAdapter.setCategoryList(new ArrayList<Category>());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OverviewInteractionListener) {
            mListener = (OverviewInteractionListener) context;
        } else if(mListener == null){
            throw new RuntimeException(context.toString()
                    + " must implement OverviewInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        editCategory(position);
    }

    @Override
    public void onPullDownAllow(boolean value){
        //not being used
    }

    @Override
    public void onDoneDrag(){
        //not being used
    }

    @Override
    public void onClick(int position){
        startActivityForResult(TransactionsForCategory.createIntentToViewAllTransactionsForCategoryForMonth(getContext(), categoryList.get(position), currentMonth), RequestCodes.HAS_TRANSACTION_CHANGED);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OverviewInteractionListener {
        void onComplete(BudgetType type ,List<Category> categoryList, float totalCost, boolean animate);
    }
}
