package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import com.zhan.budget.Adapter.CategoryGridAdapter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.library.CircularView;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Zhan on 16-03-09.
 */
public class TransactionFragment extends BaseRealmFragment {

    private static final String TAG = "TransactionFragment";

    private static final String ARG_1 = "selectedCategoryBudgetType";
    private static final String ARG_2 = "selectedCategoryId";

    private OnTransactionFragmentInteractionListener mListener;

    private RealmResults<Category> resultsCategory;
    private List<Category> categoryList;
    private GridView categoryGridView;
    private CategoryGridAdapter categoryGridAdapter;

    private Category selectedCategory;

    private String selectedCategoryId;
    private String budgetType;

    public TransactionFragment() {
        // Required empty public constructor
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
        return R.layout.fragment_transaction;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();

        selectedCategoryId = budgetType = "";
        Log.d(TAG, "1 selectedCategoryId : "+selectedCategoryId);
        categoryList = new ArrayList<>();
        categoryGridView = (GridView) view.findViewById(R.id.categoryExpenseAndIncomeGrid);
        categoryGridAdapter = new CategoryGridAdapter(getContext(), categoryList);
        categoryGridView.setAdapter(categoryGridAdapter);

        if(getArguments() != null) {
            this.budgetType = getArguments().getString(ARG_1);
            this.selectedCategoryId = getArguments().getString(ARG_2);
        }

        populateCategory(budgetType);
        addListeners();
    }

    private void populateCategory(String budgetType){Log.d(TAG, "2 selectedCategoryId : "+selectedCategoryId);
        resultsCategory = myRealm.where(Category.class).equalTo("type", budgetType).findAllSortedAsync("index");
        resultsCategory.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                resultsCategory.removeChangeListener(this);

                categoryList = myRealm.copyFromRealm(resultsCategory);
                categoryGridAdapter.clear();
                categoryGridAdapter.addAll(categoryList);

                listenToGridView();
            }
        });
    }

    private void listenToGridView(){
        categoryGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        //Default is 0 if selectedCategoryId is not defined.
                        int pos = 0;
                        Log.d(TAG, "3 selectedCategoryId : "+selectedCategoryId);
                        for (int i = 0; i < categoryList.size(); i++) {
                            if (selectedCategoryId.equalsIgnoreCase(categoryList.get(i).getId())) {
                                pos = i;
                                break;
                            }
                        }

                        selectedCategory = categoryList.get(pos);

                        if(budgetType.equalsIgnoreCase(BudgetType.INCOME.toString())) {
                            mListener.onCategoryIncomeClick(selectedCategory);
                        }else{
                            mListener.onCategoryExpenseClick(selectedCategory);
                        }

                        //Set first category as selected by default
                        ViewGroup gridChild = (ViewGroup) categoryGridView.getChildAt(pos);
                        CircularView cv = (CircularView) gridChild.findViewById(R.id.categoryIcon);
                        cv.setStrokeColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColorText));

                        // unregister listener (this is important)
                        categoryGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }

    private void addListeners(){
        categoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View childView = parent.getChildAt(i);
                    CircularView ccv = (CircularView) (childView.findViewById(R.id.categoryIcon));
                    ccv.setStrokeColor(R.color.transparent);

                    if (i == position) {
                        ccv.setStrokeColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColorText));
                    }
                }

                selectedCategory = categoryList.get(position);

                if(budgetType.equalsIgnoreCase(BudgetType.INCOME.toString())){
                    mListener.onCategoryIncomeClick(selectedCategory);
                }else{
                    mListener.onCategoryExpenseClick(selectedCategory);
                }

            }
        });
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
