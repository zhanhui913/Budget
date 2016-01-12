package com.zhan.budget.Fragment;

/**
 * Created by Zhan on 16-01-11.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.zhan.budget.Adapter.CategoryGridAdapter;
import com.zhan.budget.Database.Database;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.circularview.CircularView;

import java.util.ArrayList;

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
    private View view;

    private Database db;

    private ArrayList<Category> categoryIncomeList;
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
        TransactionIncomeFragment fragment = new TransactionIncomeFragment();
        return fragment;
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
        openDatabase();

        categoryIncomeList = new ArrayList<>();
        categoryGridView = (GridView) view.findViewById(R.id.categoryIncomeGrid);
        categoryGridAdapter = new CategoryGridAdapter(getContext(), categoryIncomeList);
        categoryGridView.setAdapter(categoryGridAdapter);

        populateCategoryExpense();
        addListeners();
    }

    private void populateCategoryExpense(){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("ASYNC", "preparing to get categories");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                categoryIncomeList = db.getAllCategoryByType(BudgetType.INCOME);
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                Log.d("ASYNC", "done getting categories");
                categoryGridAdapter.addAll(categoryIncomeList);

                //Set first category as selected by default
                /*CircularView cv = (CircularView)((View) categoryGridView.getChildAt(0)).findViewById(R.id.categoryIcon);
                cv.setStrokeColor(getResources().getColor(R.color.darkgray));

                selectedIncomeCategory = categoryIncomeList.get(0);
                */
            }
        };
        loader.execute();
    }

    private void addListeners(){
        categoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View childView = parent.getChildAt(i);
                    CircularView ccv = (CircularView) (childView.findViewById(R.id.categoryIcon));
                    ccv.setStrokeColor(getResources().getColor(android.R.color.transparent));
                }

                View childView = parent.getChildAt(position);
                CircularView ccv = (CircularView) (childView.findViewById(R.id.categoryIcon));
                ccv.setStrokeColor(getResources().getColor(R.color.darkgray));

                selectedIncomeCategory = categoryIncomeList.get(position);
            }
        });
    }

    public void openDatabase(){
        if(db == null) {
            db = new Database(getActivity().getApplicationContext());
        }
    }

    public void closeDatabase(){
        if(db != null){
            db.close();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        closeDatabase();
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
        //void onOverviewInteraction(String value);
    }
}
