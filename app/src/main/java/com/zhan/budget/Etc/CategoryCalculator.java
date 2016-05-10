package com.zhan.budget.Etc;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;

import java.math.BigDecimal;
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

    private Activity activity;
    private List<Transaction> transactionList;
    private List<Category> categoryList;
    private Date month;

    public CategoryCalculator(Activity activity, List<Transaction> transactionList, List<Category> categoryList, Date month, OnCategoryCalculatorInteractionListener mListener) {
        // We set the context this way so we don't accidentally leak activities
        this.activity = activity;
        this.transactionList = transactionList;
        this.categoryList = categoryList;
        this.mListener = mListener;
        this.month = month;

        Log.d(TAG, "1 just after calling category calculator there are "+categoryList.size()+" cat list");
    }

    @Override
    protected Void doInBackground(Void... params){
        float sumCost = 0;

        Log.d(TAG, "2 just after calling category calculator there are "+categoryList.size()+" cat list");
        Log.d(TAG, "Transaction size : "+transactionList.size());
        Log.d(TAG, "-----> month : "+this.month);

        //Go through each transaction and put them into the correct category
        for(int t = 0; t < transactionList.size(); t++){
            for(int c = 0; c < categoryList.size(); c++){
                if(transactionList.get(t).getCategory().getId().equalsIgnoreCase(categoryList.get(c).getId())){
                    float transactionPrice = transactionList.get(t).getPrice();
                    float currentCategoryPrice = categoryList.get(c).getCost();
                    categoryList.get(c).setCost(transactionPrice + currentCategoryPrice);
                }
            }
        }

        //List of string that is the ID of category in categoryList who's sum for cost is 0
        // or INCOME type
        List<Category> zeroSumList = new ArrayList<>();

        //Get position of Category who's sum cost is 0 or INCOME type
        for(int i = 0; i < categoryList.size(); i++){
            if(categoryList.get(i).getCost() == 0f || categoryList.get(i).getType().equalsIgnoreCase(BudgetType.INCOME.toString())){
                Log.d(TAG, "Category : " + categoryList.get(i).getName() + " -> with cost " + categoryList.get(i).getCost());
                zeroSumList.add(categoryList.get(i));
            }
        }
        Log.d(TAG, "BEFORE REMOVING THERE ARE "+categoryList.size());

        for(int i = 0; i < zeroSumList.size(); i++){
            Log.d(TAG, "ZERO SUM LIST : "+zeroSumList.get(i).getName());
        }

        //Remove those category who's sum for cost is 0 or INCOME type
        for(int i = 0; i < zeroSumList.size(); i++){
            categoryList.remove(zeroSumList.get(i));
        }
        Log.d(TAG, "AFTER REMOVING THERE ARE " + categoryList.size());

        //Go through list cost to get sumCost
        for(int i = 0; i < categoryList.size(); i++){
            sumCost += categoryList.get(i).getCost();
        }

        //Sort from largest to smallest percentage
        Collections.sort(categoryList, new Comparator<Category>() {
            @Override
            public int compare(Category c1, Category c2) {
                float cost1 = c1.getCost();
                float cost2 = c2.getCost();

                //ascending order
                return ((int) cost1) - ((int) cost2);
            }
        });

        //Now calculate percentage for each category
        for(int i = 0; i < categoryList.size(); i++){
            BigDecimal current = BigDecimal.valueOf(categoryList.get(i).getCost());
            BigDecimal total = BigDecimal.valueOf(sumCost);
            BigDecimal hundred = new BigDecimal(100);
            BigDecimal percent = current.divide(total, 4, BigDecimal.ROUND_HALF_EVEN);

            categoryList.get(i).setPercent(percent.multiply(hundred).floatValue());
        }
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
