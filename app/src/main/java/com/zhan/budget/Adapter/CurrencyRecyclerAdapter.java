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
import java.util.LinkedList;
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
        final BudgetCurrency budgetCurrency = filteredList.get(position);

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
        return this.filteredList.size();
    }

    public void setBudgetCurrencyList(List<BudgetCurrency> list){
        this.budgetCurrencyList = list;
        this.filteredList = list;
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
            budgetCurrencyFilter = new BudgetCurrencyFilter(this, budgetCurrencyList);
        }

        return budgetCurrencyFilter;
    }

    /**
     * Custom filter for BudgetCurrency list
     * Filter content in BudgetCurrency list according to the search text
     */
    private static class BudgetCurrencyFilter extends Filter {

        private final CurrencyRecyclerAdapter adapter;
        private final List<BudgetCurrency> originalList;
        private final List<BudgetCurrency> filteredList;

        private BudgetCurrencyFilter(CurrencyRecyclerAdapter adapter, List<BudgetCurrency> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final BudgetCurrency currency : originalList) {
                    if (currency.getCurrencyName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(currency);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.filteredList.clear();
            adapter.filteredList.addAll((ArrayList<BudgetCurrency>) results.values);
            adapter.notifyDataSetChanged();
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
