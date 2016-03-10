package com.zhan.budget.Fragment;

/**
 * Created by Zhan on 16-01-11.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
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
import com.zhan.library.CircularView;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionExpenseFragment extends BaseFragment {

    private static final String TAG = "TransactionEXPFragment";

    private OnTransactionExpenseFragmentInteractionListener mListener;

    private RealmResults<Category> resultsExpenseCategory;
    private List<Category> categoryExpenseList;
    private GridView categoryGridView;
    private CategoryGridAdapter categoryGridAdapter;

    private Category selectedExpenseCategory;

    private String selectedExpenseCategoryId = "";

    public TransactionExpenseFragment() {
        // Required empty public constructor
    }

    public void setSelectedExpenseCategoryId(String id){
        this.selectedExpenseCategoryId = id;
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_transaction_expense_and_income;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();

        categoryExpenseList = new ArrayList<>();
        categoryGridView = (GridView) view.findViewById(R.id.categoryExpenseAndIncomeGrid);
        categoryGridAdapter = new CategoryGridAdapter(getContext(), categoryExpenseList);
        categoryGridView.setAdapter(categoryGridAdapter);

        populateCategoryExpense();
        addListeners();
    }

    private void populateCategoryExpense(){
        resultsExpenseCategory = myRealm.where(Category.class).equalTo("type", BudgetType.EXPENSE.toString()).findAllAsync();
        resultsExpenseCategory.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                categoryExpenseList = myRealm.copyFromRealm(resultsExpenseCategory);
                categoryGridAdapter.clear();
                categoryGridAdapter.addAll(categoryExpenseList);

                listenToGridView();
            }
        });
    }

    private void listenToGridView(){
        categoryGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        //Default is 0 if selectedExpenseCategoryId is not defined.
                        int pos = 0;

                        for (int i = 0; i < categoryExpenseList.size(); i++) {
                            if (selectedExpenseCategoryId.equalsIgnoreCase(categoryExpenseList.get(i).getId())) {
                                pos = i;
                                break;
                            }
                        }

                        selectedExpenseCategory = categoryExpenseList.get(pos);
                        mListener.onCategoryExpenseClick(selectedExpenseCategory);

                        //Set first category as selected by default
                        ViewGroup gridChild = (ViewGroup) categoryGridView.getChildAt(pos);
                        CircularView cv = (CircularView) gridChild.findViewById(R.id.categoryIcon);
                        cv.setStrokeColor(R.color.darkgray);

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
                        ccv.setStrokeColor(R.color.darkgray);
                    }
                }

                selectedExpenseCategory = categoryExpenseList.get(position);
                mListener.onCategoryExpenseClick(selectedExpenseCategory);
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
        if (context instanceof OnTransactionExpenseFragmentInteractionListener) {
            mListener = (OnTransactionExpenseFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTransactionExpenseFragmentInteractionListener");
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
    public interface OnTransactionExpenseFragmentInteractionListener {
        void onCategoryExpenseClick(Category category);
    }
}
