package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.MonthReport;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.library.CircularView;

import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by zhanyap on 2016-05-12.
 */
public class MonthReportRecyclerAdapter extends RecyclerView.Adapter<MonthReportRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<MonthReport> monthReportList;
    private BudgetCurrency currentCurrency;
    private OnMonthReportAdapterInteractionListener mListener;

    public MonthReportRecyclerAdapter(Fragment fragment, List<MonthReport> monthReportList, BudgetCurrency currentCurrency) {
        this.context = fragment.getContext();
        this.monthReportList = monthReportList;
        this.currentCurrency = currentCurrency;

        //Any activity or fragment that uses this adapter needs to implement the OnMonthReportAdapterInteractionListener interface
        if (fragment instanceof OnMonthReportAdapterInteractionListener) {
            mListener = (OnMonthReportAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnMonthReportAdapterInteractionListener.");
        }
    }

    public MonthReportRecyclerAdapter(Activity activity, List<MonthReport> monthReportList){
        this.context = activity;
        this.monthReportList = monthReportList;
        this.currentCurrency = currentCurrency;

        //Any activity or fragment that uses this adapter needs to implement the OnMonthReportAdapterInteractionListener interface
        if(activity instanceof  OnMonthReportAdapterInteractionListener){
            mListener = (OnMonthReportAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnMonthReportAdapterInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month_report_extended, parent, false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month_report_v2, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting MonthReport data for the row
        MonthReport monthReport = monthReportList.get(position);

        viewHolder.month.setText(DateUtil.convertDateToStringFormat4(monthReport.getMonth()));
        viewHolder.expenseThisMonth.setText("You spent "+CurrencyTextFormatter.formatFloat(monthReport.getCostThisMonth(), Constants.BUDGET_LOCALE));
        viewHolder.incomeThisMonth.setText("You earned "+CurrencyTextFormatter.formatFloat(monthReport.getIncomeThisMonth(), Constants.BUDGET_LOCALE));

        float savings = Math.abs(monthReport.getIncomeThisMonth()) + monthReport.getCostThisMonth();


        //Log.d("INCOME", monthReport.getMonth()+" savings : "+savings);
        Log.d("CHECK", "----- "+monthReport.getMonth()+" -----");
        Log.d("CHECK", "cost : "+monthReport.getCostThisMonth());
        Log.d("CHECK", "income : "+monthReport.getIncomeThisMonth());
        Log.d("CHECK", "----- end -----");


        if(savings > 0){
            viewHolder.netThisMonth.setTextColor(ContextCompat.getColor(context, R.color.green));
        }else if(savings < 0){
            viewHolder.netThisMonth.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
        viewHolder.netThisMonth.setText("You saved "+CurrencyTextFormatter.formatFloat(savings, Constants.BUDGET_LOCALE));

        /*
        if(monthReport.getFirstCategory() != null){
            viewHolder.container1.setVisibility(View.VISIBLE);

            viewHolder.categoryName1.setText(monthReport.getFirstCategory().getName());

            viewHolder.category1.setStrokeWidthInDP(0);
            viewHolder.category1.setCircleRadiusInDP(25);
            viewHolder.category1.setStrokeColor(R.color.transparent);
            viewHolder.category1.setCircleColor(monthReport.getFirstCategory().getColor());
            viewHolder.category1.setIconResource(CategoryUtil.getIconID(this.context, monthReport.getFirstCategory().getIcon()));
            viewHolder.category1.setIconColor(Colors.getHexColorFromAttr(this.context, R.attr.themeColor));
        }else{
            viewHolder.container1.setVisibility(View.GONE);

            viewHolder.categoryName1.setText("null");
            viewHolder.category1.setCircleColor(R.color.black);
            viewHolder.category1.setIconResource(0);
        }

        if(monthReport.getSecondCategory() != null){
            viewHolder.container2.setVisibility(View.VISIBLE);

            viewHolder.categoryName2.setText(monthReport.getSecondCategory().getName());

            viewHolder.category2.setStrokeWidthInDP(0);
            viewHolder.category2.setCircleRadiusInDP(25);
            viewHolder.category2.setStrokeColor(R.color.transparent);
            viewHolder.category2.setCircleColor(monthReport.getSecondCategory().getColor());
            viewHolder.category2.setIconResource(CategoryUtil.getIconID(this.context, monthReport.getSecondCategory().getIcon()));
            viewHolder.category2.setIconColor(Colors.getHexColorFromAttr(this.context, R.attr.themeColor));
        }else{
            viewHolder.container2.setVisibility(View.GONE);

            viewHolder.categoryName2.setText("null");
            viewHolder.category2.setCircleColor(R.color.black);
            viewHolder.category2.setIconResource(0);
        }

        if(monthReport.getThirdCategory() != null){
            viewHolder.container3.setVisibility(View.VISIBLE);

            viewHolder.categoryName3.setText(monthReport.getThirdCategory().getName());

            viewHolder.category3.setStrokeWidthInDP(0);
            viewHolder.category3.setCircleRadiusInDP(25);
            viewHolder.category3.setStrokeColor(R.color.transparent);
            viewHolder.category3.setCircleColor(monthReport.getThirdCategory().getColor());
            viewHolder.category3.setIconResource(CategoryUtil.getIconID(this.context, monthReport.getThirdCategory().getIcon()));
            viewHolder.category3.setIconColor(Colors.getHexColorFromAttr(this.context, R.attr.themeColor));
        }else{
            viewHolder.container3.setVisibility(View.GONE);

            viewHolder.categoryName3.setText("null");
            viewHolder.category3.setCircleColor(R.color.black);
            viewHolder.category3.setIconResource(0);
        }*/

        if(monthReport.isDoneCalculation()){
            viewHolder.expenseThisMonth.setVisibility(View.VISIBLE);
            viewHolder.incomeThisMonth.setVisibility(View.VISIBLE);
            viewHolder.netThisMonth.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
        }else{
            viewHolder.expenseThisMonth.setVisibility(View.GONE);
            viewHolder.incomeThisMonth.setVisibility(View.GONE);
            viewHolder.netThisMonth.setVisibility(View.GONE);
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return this.monthReportList.size();
    }

    public void setMonthReportList(List<MonthReport> list){
        this.monthReportList = list;
        notifyDataSetChanged();
    }

    public List<MonthReport> getMonthReportList(){
        return this.monthReportList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ViewGroup background;
        public TextView month;
        public TextView expenseThisMonth;
        public TextView incomeThisMonth;
        public TextView netThisMonth;
        public CircularProgressBar progressBar;

        public ViewGroup container1, container2, container3;
        public CircularView  category1, category2, category3;
        public TextView categoryName1, categoryName2, categoryName3;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            background = (ViewGroup) itemView.findViewById(R.id.monthPanel);
            month = (TextView) itemView.findViewById(R.id.monthName);
            expenseThisMonth = (TextView) itemView.findViewById(R.id.monthExpense);
            incomeThisMonth = (TextView) itemView.findViewById(R.id.monthIncome);
            netThisMonth = (TextView) itemView.findViewById(R.id.monthNet);
            progressBar = (CircularProgressBar) itemView.findViewById(R.id.monthReportProgressBar);

            /*container1 = (ViewGroup) itemView.findViewById(R.id.topContainer1);
            container2 = (ViewGroup) itemView.findViewById(R.id.topContainer2);
            container3 = (ViewGroup) itemView.findViewById(R.id.topContainer3);

            categoryName1 = (TextView) itemView.findViewById(R.id.title1);
            categoryName2 = (TextView) itemView.findViewById(R.id.title2);
            categoryName3 = (TextView) itemView.findViewById(R.id.title3);

            category1 = (CircularView) itemView.findViewById(R.id.categoryIcon1);
            category2 = (CircularView) itemView.findViewById(R.id.categoryIcon2);
            category3 = (CircularView) itemView.findViewById(R.id.categoryIcon3);
            */
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickMonth(getLayoutPosition());
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnMonthReportAdapterInteractionListener {
        void onClickMonth(int position);
    }
}