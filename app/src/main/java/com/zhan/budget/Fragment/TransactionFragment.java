package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zhan.budget.Activity.CategoryInfoActivity;
import com.zhan.budget.Adapter.CategoryGrid.CategoryGridRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.View.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Zhan on 16-03-09.
 */
public class TransactionFragment extends BaseRealmFragment implements
        CategoryGridRecyclerAdapter.OnCategoryGridAdapterInteractionListener{

    private static final String TAG = "TransactionFragment";
    private static final int NUM_COLUMNS = 5;

    private static final String ARG_1 = "selectedCategoryBudgetType";
    private static final String ARG_2 = "selectedCategoryId";

    private OnTransactionFragmentInteractionListener mListener;

    private RealmResults<Category> resultsCategory;
    private List<Category> categoryList;
    private RecyclerView categoryGridView;
    private CategoryGridRecyclerAdapter categoryGridAdapter;

    private Category selectedCategory;

    private String selectedCategoryId;
    private String budgetType;

    public TransactionFragment() {
        // Required empty public constructor
    }

    public static TransactionFragment newInstance(String budgetType) {
        return newInstance(budgetType, "");
    }

    public static TransactionFragment newInstance(String budgetType, String categoryId) {
        TransactionFragment fragment = new TransactionFragment();

        Bundle args = new Bundle();
        args.putString(ARG_1, budgetType);
        args.putString(ARG_2, categoryId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_category_grid;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();
        selectedCategoryId = budgetType = "";
        Log.d(TAG, "1 selectedCategoryId : "+selectedCategoryId);
        categoryList = new ArrayList<>();
        categoryGridView = (RecyclerView) view.findViewById(R.id.categoryGrid);
        categoryGridView.setLayoutManager(new GridLayoutManager(getContext(), NUM_COLUMNS));

        //Add padding
        categoryGridView.addItemDecoration(new SpacesItemDecoration(getContext(), R.dimen.grid_view_horizontal_offset, R.dimen.grid_view_vertical_offset));

        categoryGridAdapter = new CategoryGridRecyclerAdapter(this, categoryList);
        categoryGridView.setAdapter(categoryGridAdapter);

        if(getArguments() != null) {
            this.budgetType = getArguments().getString(ARG_1);
            this.selectedCategoryId = getArguments().getString(ARG_2);
        }

        populateCategory(budgetType);
    }

    private void populateCategory(final String budgetType){Log.d(TAG, "2 selectedCategoryId : "+selectedCategoryId);
        resultsCategory = myRealm.where(Category.class).equalTo("type", budgetType).findAllSortedAsync("index");
        resultsCategory.addChangeListener(new RealmChangeListener<RealmResults<Category>>() {
            @Override
            public void onChange(RealmResults<Category> element) {
                element.removeChangeListener(this);

                categoryList = myRealm.copyFromRealm(element);

                int pos = 0;
                for(int i = 0; i < categoryList.size(); i++){
                    if(categoryList.get(i).getId().equalsIgnoreCase(selectedCategoryId)){
                        categoryList.get(i).setSelected(true);
                        pos = i;
                    }else{
                        categoryList.get(i).setSelected(false);
                    }
                }
                onClick(pos);

                categoryGridAdapter.setCategoryList(categoryList);
                categoryGridAdapter.addExpenseOrIncome(BudgetType.valueOf(budgetType));
            }
        });
    }

    @Override
    public void onClick(int position){
        for(int i = 0; i < categoryList.size(); i++){
            categoryList.get(i).setSelected(false);
        }
        categoryList.get(position).setSelected(true);

        categoryGridAdapter.setCategoryList(categoryList);

        selectedCategory = categoryList.get(position);

        if(budgetType.equalsIgnoreCase(BudgetType.INCOME.toString())) {
            mListener.onCategoryIncomeClick(selectedCategory);
        }else{
            mListener.onCategoryExpenseClick(selectedCategory);
        }
    }

    @Override
    public void onClickAddNewCategory(BudgetType type){
        //Toast.makeText(getContext(), "add new cat "+type.toString(), Toast.LENGTH_SHORT).show();

        Intent addNewCategoryIntent = new Intent(getContext(), CategoryInfoActivity.class);
        addNewCategoryIntent.putExtra(Constants.REQUEST_NEW_CATEGORY, true);
        addNewCategoryIntent.putExtra(Constants.REQUEST_NEW_CATEGORY_TYPE, type.toString());
        startActivityForResult(addNewCategoryIntent, Constants.RETURN_NEW_CATEGORY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data.getExtras() != null) {
            if(requestCode == Constants.RETURN_NEW_CATEGORY) {
                populateCategory(budgetType);
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
        if (context instanceof OnTransactionFragmentInteractionListener) {
            mListener = (OnTransactionFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTransactionFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnTransactionFragmentInteractionListener {
        void onCategoryIncomeClick(Category category);

        void onCategoryExpenseClick(Category category);
    }
}
