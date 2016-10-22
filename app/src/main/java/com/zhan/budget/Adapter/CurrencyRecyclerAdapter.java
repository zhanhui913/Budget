package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

/**
 * Created by Zhan on 2016-10-19.
 */

public class CurrencyRecyclerAdapter extends RecyclerView.Adapter<CurrencyRecyclerAdapter.ViewHolder> {

    private Context context;
    private boolean inSettings;
    private List<BudgetCurrency> budgetCurrencyList;
    private OnCurrencyAdapterInteractionListener mListener;

    public CurrencyRecyclerAdapter(Fragment fragment, boolean inSettings, List<BudgetCurrency> budgetCurrencyList) {
        this.context = fragment.getContext();
        this.inSettings = inSettings;
        this.budgetCurrencyList = budgetCurrencyList;

        //Any activity or fragment that uses this adapter needs to implement the OnCurrencyAdapterInteractionListener interface
        if (fragment instanceof CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener) {
            mListener = (CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnCurrencyAdapterInteractionListener.");
        }
    }

    public CurrencyRecyclerAdapter(Activity activity, boolean inSettings, List<BudgetCurrency> budgetCurrencyList){
        this.context = activity;
        this.inSettings = inSettings;
        this.budgetCurrencyList = budgetCurrencyList;

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

        viewHolder.name.setText(budgetCurrency.getCountry());
        viewHolder.currencyCode.setText(budgetCurrency.getCurrencyCode());
        viewHolder.symbol.setText(budgetCurrency.getSymbol());
        viewHolder.icon.setCircleColor(R.color.colorPrimary);

        if(inSettings){
            if(budgetCurrency.isDefault()){
                viewHolder.defaultCurrencyIndicatorOn.setVisibility(View.VISIBLE);
                viewHolder.defaultCurrencyIndicatorOff.setVisibility(View.INVISIBLE);
            }else{
                viewHolder.defaultCurrencyIndicatorOn.setVisibility(View.INVISIBLE);
                viewHolder.defaultCurrencyIndicatorOff.setVisibility(View.VISIBLE);
            }
        }else{
            viewHolder.defaultCurrencyIndicatorOn.setVisibility(View.GONE);
            viewHolder.defaultCurrencyIndicatorOff.setVisibility(View.GONE);
        }

        viewHolder.icon.setText(""+ Util.getFirstCharacterFromString(budgetCurrency.getCountry().toUpperCase()));
        viewHolder.icon.setTextColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));
        viewHolder.icon.setTextSizeInDP(30);
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
        public TextView name, currencyCode, symbol;
        public CircularView icon;
        public SwipeLayout swipeLayout;
        public ImageView defaultCurrencyIndicatorOn, defaultCurrencyIndicatorOff;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeCurrency);
            icon = (CircularView) itemView.findViewById(R.id.currencyIcon);
            name = (TextView) itemView.findViewById(R.id.currencyCountry);
            currencyCode = (TextView) itemView.findViewById(R.id.currencyCode);
            symbol = (TextView) itemView.findViewById(R.id.currencySymbol);
            defaultCurrencyIndicatorOn = (ImageView) itemView.findViewById(R.id.defaultCurrencyIndicatorOn);
            defaultCurrencyIndicatorOff = (ImageView) itemView.findViewById(R.id.defaultCurrencyIndicatorOff);

            if(inSettings){
                defaultCurrencyIndicatorOff.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mListener.onCurrencySetAsDefault(getLayoutPosition());
                        return false;
                    }
                });

                defaultCurrencyIndicatorOn.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mListener.onCurrencyDeSetFromDefault(getLayoutPosition());
                        return false;
                    }
                });
            }else{
                swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onClickCurrency(getLayoutPosition());
                    }
                });
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnCurrencyAdapterInteractionListener {
        void onClickCurrency(int position);

        void onCurrencySetAsDefault(int position);

        void onCurrencyDeSetFromDefault(int position);
    }
}
