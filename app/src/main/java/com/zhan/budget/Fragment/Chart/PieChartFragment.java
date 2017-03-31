package com.zhan.budget.Fragment.Chart;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
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
    protected static final String ARG_CHART_3 = "animate";
    protected static final String ARG_CHART_4 = "name";

    public static int ANIMATION_DURATION_MILLI = 500;

    protected List<? extends PieDataCostInterface> dataList;
    protected PieDataSet dataSet;

    private boolean drawLegend = false;

    public PieChartFragment() {
        // Required empty public constructor
    }

    public static PieChartFragment newInstance(List<? extends PieDataCostInterface> list){
        return newInstance(list, false, false, "");
    }

    public static PieChartFragment newInstance(List<? extends PieDataCostInterface> list, boolean initImmediately){
        return newInstance(list, initImmediately, false, "");
    }

    public static PieChartFragment newInstance(List<? extends PieDataCostInterface> list, boolean initImmediately, boolean animate){
        return newInstance(list, initImmediately, animate, "");
    }

    public static PieChartFragment newInstance(List<? extends PieDataCostInterface> list, boolean initImmediately, boolean animate, String name){
        PieChartFragment pieChartFragment = new PieChartFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHART, Parcels.wrap(list));
        args.putBoolean(ARG_CHART_2, initImmediately);
        args.putBoolean(ARG_CHART_3, animate);
        args.putString(ARG_CHART_4, name);
        pieChartFragment.setArguments(args);

        return pieChartFragment;
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
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);

        pieChart.setDrawSliceText(false);

        pieChart.getLegend().setEnabled(drawLegend);
        pieChart.getLegend().setPosition(Legend.LegendPosition.LEFT_OF_CHART);
        pieChart.getLegend().setTextColor(Colors.getColorFromAttr(getContext(), R.attr.themeColorText));

        //Change color of text info when there are no data
        pieChart.getPaint(Chart.PAINT_INFO).setColor(Colors.getColorFromAttr(getContext(), R.attr.themeColorText));

        if(getArguments().getBoolean(ARG_CHART_2)) {
            dataList = Parcels.unwrap(getArguments().getParcelable(ARG_CHART));
            setData(dataList, getArguments().getBoolean(ARG_CHART_3));

            if(dataList.size() > 0){
                if(getArguments().getString(ARG_CHART_4).equalsIgnoreCase("")){
                    pieChart.setCenterText(getString(R.string.na));
                }else{
                    pieChart.setCenterText(getArguments().getString(ARG_CHART_4));
                }
            }
        }
    }

    /**
     * Called when wants to display data without animating
     * @param list The list of objects that extends PieDataCostInterface
     */
    public void setData(List<? extends PieDataCostInterface> list){
        setData(list, false);
    }

    /**
     * Called when wants to display data
     * @param list The list of objects that extends PieDataCostInterface
     * @param animate To animate the graph or not
     */
    public void setData(List<? extends PieDataCostInterface> list, boolean animate){
        dataList = list;
        pieChart.clear();

        if(dataList.size() > 0 && checkEmptyPieDataCost(dataList)){
            displayPieChart(dataList, animate);
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

    private void displayPieChart(List<? extends PieDataCostInterface> list, boolean animate){
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            names.add(list.get(i).getPieDataName());
        }

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        ArrayList<Entry> value = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            value.add(new Entry(Math.abs((float)list.get(i).getPieDataCost()), i));
        }

        dataSet = new PieDataSet(value, "");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(10f);

        // Add colors
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            try {
                colors.add(ContextCompat.getColor(getContext(), CategoryUtil.getColorID(getContext(), list.get(i).getPieDataColor())));
            } catch (Exception e) {
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

        if(animate){
            pieChart.animateY(ANIMATION_DURATION_MILLI, Easing.EasingOption.EaseInOutQuad);
        }

        if(list.size() > 0){
            if(getArguments().getString(ARG_CHART_4).equalsIgnoreCase("")){
                pieChart.setCenterText(getString(R.string.na));
            }else{
                pieChart.setCenterText(getArguments().getString(ARG_CHART_4));
            }
        }

        pieChart.invalidate();
    }

    public void resetPieChart(){
        /*ArrayList<Entry> entry = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        PieDataSet set = new PieDataSet(entry, "");
        PieData data = new PieData(names, set);
        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.invalidate();*/



    }

    /**
     * Draw legend to the left of the chart
     */
    public void displayLegend(){
        drawLegend = true;
    }
}
