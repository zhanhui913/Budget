package com.zhan.budget.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.MonthReport;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.util.List;
import java.util.Locale;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by zhanyap on 2016-01-22.
 */
public class MonthReportGridAdapter extends ArrayAdapter<MonthReport>{

    private Activity activity;
    private List<MonthReport> monthReportList;

    static class ViewHolder {
        public TextView month;
        public TextView costThisMonth;
        public TextView changeCost;
        public CircularProgressBar progressBar;
    }

    public MonthReportGridAdapter(Activity activity, List<MonthReport> monthReportList) {
        super(activity, R.layout.item_month_report, monthReportList);
        this.activity = activity;
        this.monthReportList = monthReportList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
            convertView = inflater.inflate(R.layout.item_month_report, parent, false);

            viewHolder.month = (TextView) convertView.findViewById(R.id.monthName);
            viewHolder.costThisMonth = (TextView) convertView.findViewById(R.id.monthTotalCost);
            viewHolder.changeCost = (TextView) convertView.findViewById(R.id.monthChangeCost);
            viewHolder.progressBar = (CircularProgressBar) convertView.findViewById(R.id.monthReportProgressBar);

            // The tag can be any Object, this just happens to be the ViewHolder
            convertView.setTag(viewHolder);
        }else {
            // Get the ViewHolder back to get fast access to the Views
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting monthReport data for the row
        MonthReport monthReport = monthReportList.get(position);

        viewHolder.month.setText(Util.convertDateToStringFormat4(monthReport.getMonth()));
        viewHolder.costThisMonth.setText(CurrencyTextFormatter.formatFloat(monthReport.getCostThisMonth(), Locale.CANADA));
        viewHolder.changeCost.setText(CurrencyTextFormatter.formatFloat(monthReport.getChangeCost(), Locale.CANADA));

        if(monthReport.isDoneCalculation()){
            viewHolder.costThisMonth.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
        }else{
            viewHolder.costThisMonth.setVisibility(View.GONE);
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
