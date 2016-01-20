package com.zhan.budget.Fragment;

/**
 * Created by Zhan on 16-01-11.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import com.zhan.budget.Adapter.CategoryGridAdapter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.circularview.CircularView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OverviewFragment.OnOverviewInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionIncomeFragment extends Fragment {

    private OnTransactionIncomeFragmentInteractionListener mListener;

    private Realm myRealm;
    private View view;

    private RealmResults<Category> resultsIncomeCategory;
    private List<Category> categoryIncomeList;
    private GridView categoryGridView;
    private CategoryGridAdapter categoryGridAdapter;

    private Category selectedIncomeCategory;

    public TransactionIncomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OverviewFragment.
     */
    public static TransactionIncomeFragment newInstance() {
        return new TransactionIncomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_transaction_income, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        categoryIncomeList = new ArrayList<>();
        categoryGridView = (GridView) view.findViewById(R.id.categoryIncomeGrid);
        categoryGridAdapter = new CategoryGridAdapter(getContext(), categoryIncomeList);
        categoryGridView.setAdapter(categoryGridAdapter);

        populateCategoryExpense();
        addListeners();
    }

    private void populateCategoryExpense(){
        resultsIncomeCategory = myRealm.where(Category.class).equalTo("type", BudgetType.INCOME.toString()).findAllAsync();
        resultsIncomeCategory.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                categoryIncomeList = myRealm.copyFromRealm(resultsIncomeCategory);
                categoryGridAdapter.clear();
                categoryGridAdapter.addAll(categoryIncomeList);

                listenToGridView();
            }
        });
    }

    private void listenToGridView(){
        categoryGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        //default selected expense category
                        selectedIncomeCategory = categoryIncomeList.get(0);
                        mListener.onCategoryIncomeClick(selectedIncomeCategory);

                        //Set first category as selected by default
                        ViewGroup gridChild = (ViewGroup)categoryGridView.getChildAt(0);
                        CircularView cv = (CircularView)gridChild.findViewById(R.id.categoryIcon);
                        cv.setStrokeColor(ContextCompat.getColor(getActivity(), R.color.darkgray));

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
                    ccv.setStrokeColor(ContextCompat.getColor(getActivity(), R.color.transparent));
                }

                View childView = parent.getChildAt(position);
                CircularView ccv = (CircularView) (childView.findViewById(R.id.categoryIcon));
                ccv.setStrokeColor(ContextCompat.getColor(getActivity(), R.color.darkgray));



                selectedIncomeCategory = categoryIncomeList.get(position);
                mListener.onCategoryIncomeClick(selectedIncomeCategory);
            }
        });
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTransactionIncomeFragmentInteractionListener) {
            mListener = (OnTransactionIncomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTransactionIncomeFragmentInteractionListener");
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
    public interface OnTransactionIncomeFragmentInteractionListener {
        void onCategoryIncomeClick(Category category);
    }
}
