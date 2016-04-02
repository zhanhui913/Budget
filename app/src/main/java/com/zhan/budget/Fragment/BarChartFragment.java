package com.zhan.budget.Fragment;

import android.support.v4.app.Fragment;

import com.db.chart.view.BarChartView;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.

 */
public class BarChartFragment extends BaseFragment {

    private BarChartView barChartView;

    public BarChartFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_chart_bar;
    }

    @Override
    protected void init(){
        barChartView = (BarChartView) view.findViewById(R.id.barChart);
    }

    public void setData(List<Category> categoryList){

        notifyDataChanged();
    }

    private void notifyDataChanged(){

    }

}
