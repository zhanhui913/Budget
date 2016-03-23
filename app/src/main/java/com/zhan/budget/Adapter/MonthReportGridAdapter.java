package com.zhan.budget.Adapter;

import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.MonthReport;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by zhanyap on 2016-01-22.
 */
public class MonthReportGridAdapter extends ArrayAdapter<MonthReport>{

    private OnMonthReportAdapterInteractionListener mListener;

    static class ViewHolder {
        public CardView background;
        public TextView month;
        public TextView costThisMonth;
        public TextView changeCost;
        public CircularProgressBar progressBar;
    }

    public MonthReportGridAdapter(Fragment fragment, List<MonthReport> monthReportList) {
        super(fragment.getActivity(), R.layout.item_month_report, monthReportList);

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
            convertView = inflater.inflate(R.layout.item_month_report, parent, false);

            viewHolder.background = (CardView) convertView.findViewById(R.id.monthCardView);
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
        MonthReport monthReport = getItem(position);

        viewHolder.month.setText(DateUtil.convertDateToStringFormat4(monthReport.getMonth()));
        viewHolder.costThisMonth.setText(CurrencyTextFormatter.formatFloat(monthReport.getCostThisMonth(), Constants.BUDGET_LOCALE));
        viewHolder.changeCost.setText(CurrencyTextFormatter.formatFloat(monthReport.getChangeCost(), Constants.BUDGET_LOCALE));

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
