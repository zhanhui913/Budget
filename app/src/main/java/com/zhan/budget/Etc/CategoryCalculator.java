package com.zhan.budget.Etc;

import android.os.AsyncTask;
import android.util.Log;

import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Created by Zhan on 16-05-10.
 */
public class CategoryCalculator extends AsyncTask<Void, Integer, Void> {

    private OnCategoryCalculatorInteractionListener mListener;

    private final String TAG = "CAT_CAL";

    private List<Transaction> transactionList;
    private List<Category> categoryList;
    private Date month;
    private BudgetType budgetType;

    public CategoryCalculator(List<Transaction> transactionList, List<Category> categoryList, Date month, BudgetType budgetType,OnCategoryCalculatorInteractionListener mListener) {
        this.transactionList = transactionList;
        this.categoryList = categoryList;
        this.mListener = mListener;
        this.month = month;
        this.budgetType = budgetType;
    }

    @Override
    protected Void doInBackground(Void... params){
        //Log.d(TAG, DateUtil.convertDateToString(context, this.month)+" has "+transactionList.size()+" transactions");

        //Go through each transaction and put them into the correct category
        for(int t = 0; t < transactionList.size(); t++){
            for(int c = 0; c < categoryList.size(); c++){
                if(transactionList.get(t).getCategory() != null && transactionList.get(t).getCategory().getId().equalsIgnoreCase(categoryList.get(c).getId())){
                    //Convert currency in transaction to default currency price using the rate that was added at the time the transaction was created
                    double transactionPrice = transactionList.get(t).getPrice();
                    double currentCategoryPrice = categoryList.get(c).getCost();
                    categoryList.get(c).setCost(transactionPrice + currentCategoryPrice);
                }
            }
        }

        for(int i = 0; i < categoryList.size(); i++){
            Log.d(TAG, i+" : "+categoryList.get(i).getName());
        }

        // List of string that is the ID of category in categoryList who's sum for cost is 0
        // or belongs to the other category budget type.
        // Example if the budgetType value is EXPENSE, then all category of type INCOME  should be
        // included here.
        List<Category> zeroSumList = new ArrayList<>();

        //Get position of Category who's sum cost is 0 or belongs to other BudgetType
        for(int i = 0; i < categoryList.size(); i++){
            if(categoryList.get(i).getCost() == 0f || !categoryList.get(i).getType().equalsIgnoreCase(budgetType.toString())){
                //Log.d(TAG, "Category : " + categoryList.get(i).getName() + " -> with cost " + categoryList.get(i).getCost());
                zeroSumList.add(categoryList.get(i));
            }
            Log.d(TAG, "checking "+categoryList.get(i).getName()+", it has cost : "+categoryList.get(i).getCost());
        }
        Log.d(TAG, "BEFORE REMOVING THERE ARE "+categoryList.size());

        for(int i = 0; i < zeroSumList.size(); i++){
            Log.d(TAG, "ZERO SUM LIST : "+zeroSumList.get(i).getName()+" with "+zeroSumList.get(i).getCost());
        }

        //Remove those category who's sum for cost is 0 or belongs to other BudgetType
        for(int i = 0; i < zeroSumList.size(); i++){
            categoryList.remove(zeroSumList.get(i));
        }
        Log.d(TAG, "AFTER REMOVING THERE ARE " + categoryList.size());

        //Sort from largest to smallest percentage
        Collections.sort(categoryList, new Comparator<Category>() {
            @Override
            public int compare(Category c1, Category c2) {
                //ascending order
                return ((int) c1.getCost()) - ((int) c2.getCost());
            }
        });

        return null;
    }

    @Override
    protected void onPostExecute(Void  params) {
        if(mListener != null) {
            mListener.onCompleteCalculation(categoryList);
        }
    }

    //Interface needed for caller
    public interface OnCategoryCalculatorInteractionListener {
        void onCompleteCalculation(List<Category> categoryList);
    }
}
