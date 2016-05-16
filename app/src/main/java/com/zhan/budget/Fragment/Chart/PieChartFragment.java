package com.zhan.budget.Fragment.Chart;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.zhan.budget.Model.Location;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PieChartFragment extends BaseChartFragment {

    private PieChart pieChart;
    protected static final String ARG_CHART_2 = "displayDataImmediately";

    public PieChartFragment() {
        // Required empty public constructor
    }

    public static PieChartFragment newInstance(List<?> categoryList, boolean initImmediately){
        PieChartFragment pieChartFragment = new PieChartFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHART, Parcels.wrap(categoryList));
        args.putBoolean(ARG_CHART_2, initImmediately);
        pieChartFragment.setArguments(args);

        return pieChartFragment;
    }
    public static PieChartFragment newInstance(List<?> categoryList){
        return newInstance(categoryList, false);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_chart_pie;
    }

    @Override
    public void init(){
        pieChart = (PieChart) view.findViewById(R.id.pieChart);

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

        //Remove legend
        pieChart.getLegend().setEnabled(false);

        //Change color of text info when there are no data
        pieChart.getPaint(Chart.PAINT_INFO).setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        pieChart.spin(2000, 0, 360, Easing.EasingOption.EaseInOutQuad);

        if(getArguments().getBoolean(ARG_CHART_2)) {
            setData((List<?>) Parcels.unwrap(getArguments().getParcelable(ARG_CHART)));
        }
    }

    /**
     * Called when wants to display data
     * @param list
     */
    public void setData(List<?> list){

        // add a selection listener
        //pieChart.setOnChartValueSelectedListener(this);

        if(list.size() > 0){
            if(list.get(0) instanceof Category){
                displayPieChartForCategory((List<Category>)list);
            }else if(list.get(0) instanceof Location){
                displayPieChartForGeneric((List<Location>)list);
            }
        }else{
            pieChart.clear();
        }
    }

    private void displayPieChartForCategory(List<Category> categoryList) {
        ArrayList<Entry> yVals1 = new ArrayList<>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < categoryList.size(); i++) {
            yVals1.add(new Entry(Math.abs(categoryList.get(i).getCost()), i));
        }

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < categoryList.size(); i++) {
            xVals.add(categoryList.get(i).getName());
        }

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(5f);

        // Add colors
        ArrayList<Integer> colors = new ArrayList<>();
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
        //data.setValueTextColor(Color.WHITE);
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }

    private void displayPieChartForGeneric(List<Location> list) {
        ArrayList<Entry> yVals1 = new ArrayList<>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < list.size(); i++) {
            yVals1.add(new Entry(Math.abs(list.get(i).getAmount()), i));
        }

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            xVals.add(list.get(i).getName());
        }

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(5f);

        // Add colors
        ArrayList<Integer> colors = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            try {
                colors.add(ContextCompat.getColor(getContext(), CategoryUtil.getColorID(getContext(), list.get(i).getColor())));
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        dataSet.setColors(colors);
        dataSet.setSelectionShift(10f);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(0f);
        //data.setValueTextColor(Color.WHITE);
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }

}
