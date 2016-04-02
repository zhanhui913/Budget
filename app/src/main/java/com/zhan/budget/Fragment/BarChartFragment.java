package com.zhan.budget.Fragment;

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

import java.util.List;

/**
 * A simple {@link Fragment} subclass.

 */
public class BarChartFragment extends BaseFragment implements ChartDataListener{

    private BarChartView barChartView;
    private BarSet barSet;

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
        barChartView.setXAxis(false);
        barChartView.setYAxis(false);
        barChartView.setYLabels(AxisController.LabelPosition.NONE);
        barChartView.setXLabels(AxisController.LabelPosition.NONE);
    }

    @Override
    public void setData(List<Category> categoryList){
        barSet = new BarSet();

        for(int i = 0 ; i < categoryList.size(); i++){
            Bar bar = new Bar(categoryList.get(i).getName(), categoryList.get(i).getCost());
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
