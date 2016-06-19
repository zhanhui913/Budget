package com.zhan.budget.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.CategoryInfoActivity;
import com.zhan.budget.Activity.Transactions.TransactionsForCategory;
import com.zhan.budget.Adapter.CategoryGenericRecyclerAdapter;
import com.zhan.budget.Etc.CategoryCalculator;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class OverviewGenericFragment extends BaseRealmFragment implements
        CategoryGenericRecyclerAdapter.OnCategoryGenericAdapterInteractionListener{

    private static final String ARG_1 = "budgetType";
    private static final String ARG_2 = "currentMonth";
    private Date currentMonth;
    private BudgetType budgetType;

    private CircularProgressBar circularProgressBar;

    private RealmResults<Category> resultsCategory;
    private RealmResults<Transaction> transactionsResults;
    private List<Transaction> transactionList;
    private List<Category> categoryList;

    private CategoryGenericRecyclerAdapter categoryPercentListAdapter;
    private OverviewInteractionListener mListener;

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

        categoryList = new ArrayList<>();
        RecyclerView categoryListView = (RecyclerView) view.findViewById(R.id.percentCategoryListView);
        categoryPercentListAdapter = new CategoryGenericRecyclerAdapter(this, categoryList, CategoryGenericRecyclerAdapter.ARRANGEMENT.PERCENT, null);
        categoryListView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryListView.setAdapter(categoryPercentListAdapter);

        //Add divider
        categoryListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        getCategoryList();
    }

    //Should be called only the first time when the fragment is created
    private void getCategoryList(){
        final Realm myRealm = Realm.getDefaultInstance();  BudgetPreference.addRealmCache(getContext());
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListener(this);

                categoryList.clear();
                categoryList = myRealm.copyFromRealm(element);
                myRealm.close();  BudgetPreference.removeRealmCache(getContext());
                getMonthReport(currentMonth);
            }
        });
    }

    private void getMonthReport(Date date){
        //Refresh these variables
        final Date month = DateUtil.refreshMonth(date);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = DateUtil.getLastDateOfMonth(month);

        Log.d("OVERVIEW_ACT", "("+DateUtil.convertDateToStringFormat1(month) + "-> "+DateUtil.convertDateToStringFormat1(endMonth)+")");

        final Realm myRealm = Realm.getDefaultInstance();  BudgetPreference.addRealmCache(getContext());
        transactionsResults = myRealm.where(Transaction.class).between("date", month, endMonth).equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                if (transactionList != null) {
                    transactionList.clear();
                }

                transactionList = myRealm.copyFromRealm(element);
                myRealm.close();  BudgetPreference.removeRealmCache(getContext());
                performAsyncCalculation();
            }
        });
    }

    /**
     * Perform tedious calculation asynchronously to avoid blocking main thread
     */
    private void performAsyncCalculation(){
        CategoryCalculator cc = new CategoryCalculator(transactionList, categoryList, new Date(), budgetType, new CategoryCalculator.OnCategoryCalculatorInteractionListener() {
            @Override
            public void onCompleteCalculation(List<Category> catList) {
                Toast.makeText(getContext(), "DONE CATEGORY CALCULATION", Toast.LENGTH_LONG).show();

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

                mListener.onComplete(budgetType, categoryList, sumCost);
            }
        });
        cc.execute();
    }

    private void confirmDelete(final int position){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);


        title.setText("Confirm Delete");
        message.setText("Are you sure you want to delete this category?");

        new AlertDialog.Builder(getContext())
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

    private void editCategory(int position){
        Intent editCategoryActivity = new Intent(getContext(), CategoryInfoActivity.class);

        Parcelable wrapped = Parcels.wrap(categoryList.get(position));

        editCategoryActivity.putExtra(Constants.REQUEST_EDIT_CATEGORY, wrapped);
        editCategoryActivity.putExtra(Constants.REQUEST_NEW_CATEGORY, false);

        startActivityForResult(editCategoryActivity, Constants.RETURN_EDIT_CATEGORY);
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
        } else {
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
        Toast.makeText(getContext(), "click on category :" + categoryList.get(position).getName(), Toast.LENGTH_SHORT).show();

        Intent viewAllTransactionsForCategory = new Intent(getContext(), TransactionsForCategory.class);
        viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_GENERIC_MONTH, DateUtil.convertDateToString(currentMonth));

        Parcelable wrapped = Parcels.wrap(categoryList.get(position));

        viewAllTransactionsForCategory.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_CATEGORY_CATEGORY, wrapped);
        startActivity(viewAllTransactionsForCategory);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OverviewInteractionListener {
        void onComplete(BudgetType type ,List<Category> categoryList, float totalCost);
    }
}
