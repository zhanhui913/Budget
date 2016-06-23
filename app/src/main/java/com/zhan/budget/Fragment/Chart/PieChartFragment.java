package com.zhan.budget.Fragment.Chart;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.zhan.budget.Model.PieDataCostInterface;
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
    protected List<? extends PieDataCostInterface> dataList;
    protected PieDataSet dataSet;

    public PieChartFragment() {
        // Required empty public constructor
    }

    public static PieChartFragment newInstance(List<? extends PieDataCostInterface> transactionList, boolean initImmediately){
        PieChartFragment pieChartFragment = new PieChartFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHART, Parcels.wrap(transactionList));
        args.putBoolean(ARG_CHART_2, initImmediately);
        pieChartFragment.setArguments(args);

        return pieChartFragment;
    }

    public static PieChartFragment newInstance(List<? extends PieDataCostInterface> categoryList){
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
        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setDrawCenterText(true);

        int color = Colors.getColorFromAttr(getContext(), R.attr.themeColorText);
        pieChart.setCenterTextColor(color);

        int textSize = (int) (getResources().getDimension(R.dimen.text_content_size) / getResources().getDisplayMetrics().density);
        pieChart.setCenterTextSize(textSize);
        pieChart.setHoleColor(ContextCompat.getColor(getContext(), R.color.transparent));

        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        pieChart.setDrawSliceText(false);

        //Remove legend
        pieChart.getLegend().setEnabled(false);

        //Change color of text info when there are no data
        pieChart.getPaint(Chart.PAINT_INFO).setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        //pieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        //pieChart.spin(2000, 0, 360, Easing.EasingOption.EaseInOutQuad);

        if(getArguments().getBoolean(ARG_CHART_2)) {
            dataList = Parcels.unwrap(getArguments().getParcelable(ARG_CHART));
            setData(dataList);

            if(dataList.size() > 0){
                pieChart.setCenterText(dataList.get(0).getClass().getSimpleName());
            }
        }
    }

    /**
     * Called when wants to display data
     * @param list The list of objects that extends PieDataCostInterface
     */
    public void setData(List<? extends PieDataCostInterface> list){
        dataList = list;
        pieChart.clear();

        if(dataList.size() > 0 && checkEmptyPieDataCost(dataList)){
            displayPieChart(dataList);
        }
    }

    /**
     * Returns the list that contains the data.
     */
    public List<? extends PieDataCostInterface> getList(){
        return this.dataList;
    }

    /**
     * Even if there are data name (x value), the cost (y value) could be empty and therefore
     * have nothing in pie chart to draw.
     * @param list The list of objects that extends PieDataCostInterface
     * @return true if there's at least 1 (y value) data.
     */
    private boolean checkEmptyPieDataCost(List<? extends PieDataCostInterface> list){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getPieDataCost() != 0){
                return true;
            }
        }
        return false;
    }

    /**
     * Update data while keeping the colors of each pie the same.
     * @param list The list of objects that extends PieDataCostInterface
     */
    public void updateData(List<? extends PieDataCostInterface> list){
        updateData(list, false);
    }

    /**
     * Update data while keeping the colors of each pie the same.
     * @param list The list of objects that extends PieDataCostInterface
     * @param animate Whether or not to animate with new data and change its color
     */
    public void updateData(List<? extends PieDataCostInterface> list, boolean animate){

        ArrayList<Integer> colors = new ArrayList<>();

        //Go through each existing entry and update information
        for(int i = 0; i < dataSet.getEntryCount(); i++){
            dataSet.getEntryForXIndex(i).setVal(Math.abs(list.get(i).getPieDataCost()));

            try {
                colors.add(ContextCompat.getColor(getContext(), CategoryUtil.getColorID(getContext(), list.get(i).getPieDataColor())));
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //Update color order (this automatically comes down from highest to lowest)
        //So i can assume the colors list will be the same as well.
        dataSet.setColors(colors);

        // undo all highlights
        pieChart.highlightValues(null);

        if(animate){
            pieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        }

        if(Build.VERSION.SDK_INT >= 21){
            pieChart.setElevation(20);
        }

        dataSet.notifyDataSetChanged(); //let data know a dataSet changed
        pieChart.notifyDataSetChanged();// let the chart know it's data changed
        pieChart.invalidate(); //refresh
    }

    private void displayPieChart(List<? extends PieDataCostInterface> list){
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            names.add(list.get(i).getPieDataName());
        }

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        ArrayList<Entry> value = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            value.add(new Entry(Math.abs(list.get(i).getPieDataCost()), i));
        }

        dataSet = new PieDataSet(value, "");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(10f);

        // Add colors
        ArrayList<Integer> colors = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            try {
                colors.add(ContextCompat.getColor(getContext(), CategoryUtil.getColorID(getContext(), list.get(i).getPieDataColor())));
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        dataSet.setColors(colors);

        PieData data = new PieData(names, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(0f);
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);

        if(list.size() > 0){
            pieChart.setCenterText(list.get(0).getClass().getSimpleName());
        }

        if(Build.VERSION.SDK_INT >= 21){
            pieChart.setElevation(20);
        }

        pieChart.invalidate();
    }
}
