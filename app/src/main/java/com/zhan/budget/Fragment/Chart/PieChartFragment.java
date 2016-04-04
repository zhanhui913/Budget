package com.zhan.budget.Fragment.Chart;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PieChartFragment extends BaseChartFragment {

    private PieChart pieChart;

    public PieChartFragment() {
        // Required empty public constructor
    }

    public static PieChartFragment newInstance(List<Category> categoryList){
        PieChartFragment pieChartFragment = new PieChartFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHART, Parcels.wrap(categoryList));
        pieChartFragment.setArguments(args);

        return pieChartFragment;
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_chart_pie;
    }

    @Override
    public void init(){ Log.d("CHART", "pie chart fragment init");

        pieChart = (PieChart) view.findViewById(R.id.pieChart);


        List<Category> catList = Parcels.unwrap(getArguments().getParcelable(ARG_CHART));
        setData(catList);
    }

    public void setData(List<Category> categoryList){
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setExtraOffsets(5, 5, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setDrawCenterText(false);

        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        pieChart.setDrawSliceText(false);

        // add a selection listener
        //pieChart.setOnChartValueSelectedListener(this);

        setData1(categoryList);

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        pieChart.spin(2000, 0, 360, Easing.EasingOption.EaseInOutQuad);

        //Remove legend
        pieChart.getLegend().setEnabled(false);
    }

    private void setData1(List<Category> categoryList) {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < categoryList.size(); i++) {
            yVals1.add(new Entry(Math.abs(categoryList.get(i).getCost()), i));
        }

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < categoryList.size(); i++) {
            xVals.add(i + " string");
        }

        PieDataSet dataSet = new PieDataSet(yVals1, "Election Results");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(5f);

        // Add colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for(int i = 0; i < categoryList.size(); i++){
            try {
                colors.add(ContextCompat.getColor(getContext(), CategoryUtil.getColorID(getContext(), categoryList.get(i).getColor())));
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        dataSet.setColors(colors);
        dataSet.setSelectionShift(10f);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(0f);
        data.setValueTextColor(Color.WHITE);
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }

}
