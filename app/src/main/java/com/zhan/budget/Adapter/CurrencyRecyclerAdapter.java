package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhan on 2016-10-19.
 */

public class CurrencyRecyclerAdapter extends RecyclerView.Adapter<CurrencyRecyclerAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<BudgetCurrency> budgetCurrencyList;
    private List<BudgetCurrency> filteredList;
    private BudgetCurrencyFilter budgetCurrencyFilter;
    private OnCurrencyAdapterInteractionListener mListener;

    public CurrencyRecyclerAdapter(Fragment fragment, List<BudgetCurrency> budgetCurrencyList) {
        this.context = fragment.getContext();
        this.budgetCurrencyList = budgetCurrencyList;
        this.filteredList = budgetCurrencyList;

        //Any activity or fragment that uses this adapter needs to implement the OnCurrencyAdapterInteractionListener interface
        if (fragment instanceof CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener) {
            mListener = (CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnCurrencyAdapterInteractionListener.");
        }
    }

    public CurrencyRecyclerAdapter(Activity activity, List<BudgetCurrency> budgetCurrencyList){
        this.context = activity;
        this.budgetCurrencyList = budgetCurrencyList;
        this.filteredList = budgetCurrencyList;

        //Any activity or fragment that uses this adapter needs to implement the OnCurrencyAdapterInteractionListener interface
        if(activity instanceof CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener){
            mListener = (CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnCurrencyAdapterInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public CurrencyRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);

        // Return a new holder instance
        return new CurrencyRecyclerAdapter.ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final CurrencyRecyclerAdapter.ViewHolder viewHolder, int position) {
        // getting BudgetCurrency data for the row
        final BudgetCurrency budgetCurrency = budgetCurrencyList.get(position);

        viewHolder.currencyName.setText(budgetCurrency.getCurrencyName() + " (" + budgetCurrency.getCurrencyCode() + ")");
        viewHolder.icon.setCircleColor(R.color.colorPrimary);
        viewHolder.icon.setText(""+ Util.getFirstCharacterFromString(budgetCurrency.getCurrencyCode().toUpperCase().trim()));
        viewHolder.icon.setTextColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));

        if(budgetCurrency.isDefault()){
            viewHolder.defaultCurrencyIndicator.setVisibility(View.VISIBLE);
            viewHolder.currencyName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }else{
            viewHolder.defaultCurrencyIndicator.setVisibility(View.GONE);
            viewHolder.currencyName.setTextColor(Colors.getColorFromAttr(context, R.attr.themeColorText));
        }
    }

    @Override
    public int getItemCount() {
        return this.budgetCurrencyList.size();
    }

    public void setBudgetCurrencyList(List<BudgetCurrency> list){
        this.budgetCurrencyList = list;
        notifyDataSetChanged();
    }

    public List<BudgetCurrency> getBudgetCurrencyList(){
        return this.budgetCurrencyList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView currencyName;
        public CircularView icon;
        public SwipeLayout swipeLayout;
        public ImageView defaultCurrencyIndicator;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeCurrency);
            icon = (CircularView) itemView.findViewById(R.id.currencyIcon);
            currencyName = (TextView) itemView.findViewById(R.id.currencyName);
            defaultCurrencyIndicator = (ImageView) itemView.findViewById(R.id.defaultCurrencyIndicator);

            swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickCurrency(getLayoutPosition());
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Filterables
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (budgetCurrencyFilter == null) {
            budgetCurrencyFilter = new BudgetCurrencyFilter();
        }

        return budgetCurrencyFilter;
    }


    /**
     * Custom filter for BudgetCurrency list
     * Filter content in BudgetCurrency list according to the search text
     */
    private class BudgetCurrencyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new Filter.FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<BudgetCurrency> tempList = new ArrayList<>();

                // search content in BudgetCurrency list
                for (BudgetCurrency currency : budgetCurrencyList) {
                    if (currency.getCurrencyName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(currency);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = budgetCurrencyList.size();
                filterResults.values = budgetCurrencyList;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<BudgetCurrency>) results.values;

            notifyDataSetChanged();

            Log.d("filter_adapter", "res: "+filteredList.size());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnCurrencyAdapterInteractionListener {
        void onClickCurrency(int position);
    }
}
