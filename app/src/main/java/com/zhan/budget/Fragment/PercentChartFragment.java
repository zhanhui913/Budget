package com.zhan.budget.Fragment;

import android.widget.TextView;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.percentview.Model.Slice;
import com.zhan.percentview.PercentView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PercentChartFragment extends BaseFragment {

    private PercentView percentView;
    private List<Slice> sliceList;
    private TextView totalCostForMonthTextView;
    private int screenWidth;
    private float sumCost;

    public PercentChartFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_percent_chart;
    }

    @Override
    protected void init(){
        percentView = (PercentView) view.findViewById(R.id.percentView);
        totalCostForMonthTextView = (TextView) view.findViewById(R.id.totalCostForMonth);
        sliceList = new ArrayList<>();
        screenWidth = Util.getScreenWidth(getActivity());
    }

    public void setData(List<Category> categoryList){

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
            //Create new slice as well
            Slice slice = new Slice();
            slice.setColor(categoryList.get(i).getColor());
            slice.setPixels((int) ((categoryList.get(i).getCost() / sumCost) * screenWidth));
            sliceList.add(slice);
        }

        //calculate remainder pixels left d ue to rounding errors
        int remainder = screenWidth;
        for(int i = 0; i < sliceList.size(); i++){
            remainder -= sliceList.get(i).getPixels();
        }

        //Give remainder pixels to first (largest) slice
        if(sliceList.size() > 0) {
            sliceList.get(0).setPixels(sliceList.get(0).getPixels() + remainder);
        }

        notifyDataChanged();
    }

    private void notifyDataChanged(){
        this.percentView.setSliceList(this.sliceList);
        totalCostForMonthTextView.setText(CurrencyTextFormatter.formatFloat(sumCost, Constants.BUDGET_LOCALE));
    }


}
