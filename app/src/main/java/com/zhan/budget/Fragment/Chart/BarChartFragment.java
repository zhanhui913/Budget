package com.zhan.budget.Fragment.Chart;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;

import org.parceler.Parcels;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.

 */
public class BarChartFragment extends BaseChartFragment{

    private BarChartView barChartView;
    private BarSet barSet;

    public BarChartFragment() {
        // Required empty public constructor
    }

    public static BarChartFragment newInstance(List<Category> categoryList){
        BarChartFragment barChartFragment = new BarChartFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHART, Parcels.wrap(categoryList));
        barChartFragment.setArguments(args);

        return barChartFragment;
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_chart_bar;
    }

    @Override
    public void init(){ Log.d("CHART", "bar chart fragment init");
        barChartView = (BarChartView) view.findViewById(R.id.barChart);
        barChartView.setXAxis(false);
        barChartView.setYAxis(false);
        barChartView.setYLabels(AxisController.LabelPosition.NONE);
        barChartView.setXLabels(AxisController.LabelPosition.NONE);

        List<Category> catList = Parcels.unwrap(getArguments().getParcelable(ARG_CHART));
        setData(catList);
    }

    public void setData(List<Category> categoryList){
        barSet = new BarSet();

        for(int i = 0 ; i < categoryList.size(); i++){
            Bar bar = new Bar(categoryList.get(i).getName(), Math.abs(categoryList.get(i).getCost()));
            try {
                bar.setColor(ContextCompat.getColor(getContext(), CategoryUtil.getColorID(getContext(), categoryList.get(i).getColor())));
            }catch(Exception e){
                e.printStackTrace();
            }
            barSet.addBar(bar);
            Log.d("BAR", "adding bar " + i);
        }

        barChartView.addData(barSet);

        notifyDataChanged();
    }

    private void notifyDataChanged(){
        Log.d("BAR", "displaying bar chart view" );

        barChartView.show();
    }


}
