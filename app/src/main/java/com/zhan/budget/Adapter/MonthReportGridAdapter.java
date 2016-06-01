package com.zhan.budget.Adapter;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.MonthReport;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;
import com.zhan.library.CircularView;

import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by zhanyap on 2016-01-22.
 */
public class MonthReportGridAdapter extends ArrayAdapter<MonthReport>{

    private OnMonthReportAdapterInteractionListener mListener;

    static class ViewHolder {
        public ViewGroup background;
        public TextView month;
        public TextView costThisMonth;
        public TextView changeCost;
        public CircularProgressBar progressBar;

        public ViewGroup container1, container2, container3;
        public CircularView  category1, category2, category3;
        public TextView categoryName1, categoryName2, categoryName3;
    }

    public MonthReportGridAdapter(Fragment fragment, List<MonthReport> monthReportList) {
        super(fragment.getActivity(), R.layout.item_month_report_extended, monthReportList);

        //Any activity or fragment that uses this adapter needs to implement the OnMonthReportAdapterInteractionListener interface
        if(fragment instanceof  OnMonthReportAdapterInteractionListener){
            mListener = (OnMonthReportAdapterInteractionListener) fragment;
        }else {
            throw new RuntimeException(fragment.toString() + " must implement OnMonthReportAdapterInteractionListener.");
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Avoid un-necessary calls to findViewById() on each row, which is expensive!
        ViewHolder viewHolder;

        /*
         * If convertView is not null, we can reuse it directly, no inflation required!
         * We only inflate a new View when the convertView is null.
         */
        if (convertView == null) {

            // Create a ViewHolder and store references to the two children views
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_month_report_extended, parent, false);

            viewHolder.background = (ViewGroup) convertView.findViewById(R.id.monthPanel);
            viewHolder.month = (TextView) convertView.findViewById(R.id.monthName);
            viewHolder.costThisMonth = (TextView) convertView.findViewById(R.id.monthTotalCost);
            //viewHolder.changeCost = (TextView) convertView.findViewById(R.id.monthChangeCost);
            viewHolder.progressBar = (CircularProgressBar) convertView.findViewById(R.id.monthReportProgressBar);

            viewHolder.container1 = (ViewGroup) convertView.findViewById(R.id.topContainer1);
            viewHolder.container2 = (ViewGroup) convertView.findViewById(R.id.topContainer2);
            viewHolder.container3 = (ViewGroup) convertView.findViewById(R.id.topContainer3);

            viewHolder.categoryName1 = (TextView) convertView.findViewById(R.id.title1);
            viewHolder.categoryName2 = (TextView) convertView.findViewById(R.id.title2);
            viewHolder.categoryName3 = (TextView) convertView.findViewById(R.id.title3);

            viewHolder.category1 = (CircularView) convertView.findViewById(R.id.categoryIcon1);
            viewHolder.category2 = (CircularView) convertView.findViewById(R.id.categoryIcon2);
            viewHolder.category3 = (CircularView) convertView.findViewById(R.id.categoryIcon3);

            // The tag can be any Object, this just happens to be the ViewHolder
            convertView.setTag(viewHolder);
        }else {
            // Get the ViewHolder back to get fast access to the Views
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting monthReport data for the row
        MonthReport monthReport = getItem(position);

        viewHolder.month.setText(DateUtil.convertDateToStringFormat4(monthReport.getMonth()));
        viewHolder.costThisMonth.setText(CurrencyTextFormatter.formatFloat(monthReport.getCostThisMonth(), Constants.BUDGET_LOCALE));
        //viewHolder.changeCost.setText(CurrencyTextFormatter.formatFloat(monthReport.getChangeCost(), Constants.BUDGET_LOCALE));

        if(monthReport.getFirstCategory() != null){
            viewHolder.container1.setVisibility(View.VISIBLE);

            viewHolder.categoryName1.setText(monthReport.getFirstCategory().getName());

            viewHolder.category1.setStrokeWidthInDP(0);
            viewHolder.category1.setCircleRadiusInDP(25);
            viewHolder.category1.setStrokeColor(R.color.transparent);
            viewHolder.category1.setCircleColor(monthReport.getFirstCategory().getColor());
            viewHolder.category1.setIconResource(CategoryUtil.getIconID(getContext(), monthReport.getFirstCategory().getIcon()));
            viewHolder.category1.setIconColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColor));
        }else{
            viewHolder.container1.setVisibility(View.GONE);

            /*viewHolder.categoryName1.setText("null");
            viewHolder.category1.setCircleColor(R.color.black);
            viewHolder.category1.setIconResource(0);*/
        }

        if(monthReport.getSecondCategory() != null){
            viewHolder.container2.setVisibility(View.VISIBLE);

            viewHolder.categoryName2.setText(monthReport.getSecondCategory().getName());

            viewHolder.category2.setStrokeWidthInDP(0);
            viewHolder.category2.setCircleRadiusInDP(25);
            viewHolder.category2.setStrokeColor(R.color.transparent);
            viewHolder.category2.setCircleColor(monthReport.getSecondCategory().getColor());
            viewHolder.category2.setIconResource(CategoryUtil.getIconID(getContext(), monthReport.getSecondCategory().getIcon()));
            viewHolder.category2.setIconColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColor));
        }else{
            viewHolder.container2.setVisibility(View.GONE);

            /*viewHolder.categoryName2.setText("null");
            viewHolder.category2.setCircleColor(R.color.black);
            viewHolder.category2.setIconResource(0);*/
        }

        if(monthReport.getThirdCategory() != null){
            viewHolder.container3.setVisibility(View.VISIBLE);

            viewHolder.categoryName3.setText(monthReport.getThirdCategory().getName());

            viewHolder.category3.setStrokeWidthInDP(0);
            viewHolder.category3.setCircleRadiusInDP(25);
            viewHolder.category3.setStrokeColor(R.color.transparent);
            viewHolder.category3.setCircleColor(monthReport.getThirdCategory().getColor());
            viewHolder.category3.setIconResource(CategoryUtil.getIconID(getContext(), monthReport.getThirdCategory().getIcon()));
            viewHolder.category3.setIconColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColor));
        }else{
            viewHolder.container3.setVisibility(View.GONE);

            /*viewHolder.categoryName3.setText("null");
            viewHolder.category3.setCircleColor(R.color.black);
            viewHolder.category3.setIconResource(0);*/
        }

        if(monthReport.isDoneCalculation()){
            viewHolder.costThisMonth.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
        }else{
            viewHolder.costThisMonth.setVisibility(View.GONE);
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        }

        viewHolder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickMonth(position);
            }
        });

        return convertView;
    }

    public interface OnMonthReportAdapterInteractionListener {
        void onClickMonth(int position);
    }
}
