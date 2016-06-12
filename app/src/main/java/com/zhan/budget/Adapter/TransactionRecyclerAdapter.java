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
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Adapter.Helper.ItemTouchHelperAdapter;
import com.zhan.budget.Adapter.Helper.ItemTouchHelperViewHolder;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

/**
 * Simple CategoryRecyclerAdapter that implements {@link ItemTouchHelperAdapter} to respond to move
 * from a {@link android.support.v7.widget.helper.ItemTouchHelper}.
 *
 */
public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private boolean showDate;
    private static OnTransactionAdapterInteractionListener mListener;

    public TransactionRecyclerAdapter(Fragment fragment, List<Transaction> transactionList, boolean showDate) {
        this.context = fragment.getContext();
        this.showDate = showDate;
        this.transactionList = transactionList;

        //Any activity or fragment that uses this adapter needs to implement the OnTransactionAdapterInteractionListener interface
        if (fragment instanceof OnTransactionAdapterInteractionListener) {
            mListener = (OnTransactionAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnTransactionAdapterInteractionListener.");
        }
    }

    public TransactionRecyclerAdapter(Activity activity, List<Transaction> transactionList, boolean showDate){
        this.context = activity;
        this.showDate = showDate;
        this.transactionList = transactionList;

        //Any activity or fragment that uses this adapter needs to implement the OnTransactionAdapterInteractionListener interface
        if(activity instanceof  OnTransactionAdapterInteractionListener){
            mListener = (OnTransactionAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnTransactionAdapterInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting Transaction data for the row
        final Transaction transaction = transactionList.get(position);

        //Icon
        viewHolder.circularView.setCircleColor(transaction.getCategory().getColor());
        viewHolder.circularView.setIconResource(CategoryUtil.getIconID(context, transaction.getCategory().getIcon()));
        viewHolder.account.setText(transaction.getAccount().getName());
        viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(transaction.getPrice(), Constants.BUDGET_LOCALE));

        //If transaction's dayType is COMPLETED
        if(transaction.getDayType().equalsIgnoreCase(DayType.COMPLETED.toString())) {
            viewHolder.circularView.setStrokeWidthInDP(0);
            viewHolder.circularView.setCircleRadiusInDP(25);
            viewHolder.circularView.setStrokeColor(R.color.transparent);
            viewHolder.circularView.setCircleColor(transaction.getCategory().getColor());
            viewHolder.circularView.setIconResource(CategoryUtil.getIconID(context, transaction.getCategory().getIcon()));
            viewHolder.circularView.setIconColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));

            //If the transaction is completed, there is no need for the approve btn in the swipemenulayout
            viewHolder.approveBtn.setVisibility(View.GONE);
            viewHolder.unapproveBtn.setVisibility(View.VISIBLE);
        }else{ //If transaction's dayType is SCHEDULED but not COMPLETED
            viewHolder.circularView.setStrokeWidthInDP(2);
            viewHolder.circularView.setCircleRadiusInDP(23);

            viewHolder.circularView.setCircleColor(R.color.transparent);
            viewHolder.circularView.setIconResource(CategoryUtil.getIconID(context, transaction.getCategory().getIcon()));
            viewHolder.circularView.setStrokeColor(Colors.getHexColorFromAttr(context, R.attr.themeColorText));
            viewHolder.circularView.setIconColor(Colors.getHexColorFromAttr(context, R.attr.themeColorText));

            viewHolder.approveBtn.setVisibility(View.VISIBLE);
            viewHolder.unapproveBtn.setVisibility(View.GONE);
        }

        //If there is no note, use Category's name instead
        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(transaction.getNote())){
            viewHolder.name.setText(transaction.getNote());
        }else{
            viewHolder.name.setText(transaction.getCategory().getName());
        }

        //If this is used in Calendar Fragment (no need to show date), everywhere else use it
        if(this.showDate){
            viewHolder.date.setVisibility(View.VISIBLE);
            viewHolder.date.setText(DateUtil.convertDateToStringFormat1(transaction.getDate()));
        }else{
            viewHolder.date.setVisibility(View.GONE);
        }

        //If there are no location
        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(transaction.getLocation())){
            viewHolder.location.setVisibility(View.VISIBLE);
            viewHolder.locationIcon.setVisibility(View.VISIBLE);
            viewHolder.location.setText(transaction.getLocation());
        }else{
            viewHolder.location.setVisibility(View.GONE);
            viewHolder.locationIcon.setVisibility(View.GONE);
        }

        //Set Transaction's cost color based on Category type
        if(transaction.getCategory().getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
            viewHolder.cost.setTextColor(ContextCompat.getColor(context, R.color.red));
        }else{
            viewHolder.cost.setTextColor(ContextCompat.getColor(context, R.color.green));
        }
    }

    @Override
    public int getItemCount() {
        return this.transactionList.size();
    }

    public void setTransactionList(List<Transaction> list){
        this.transactionList = list;
        notifyDataSetChanged();
    }

    public List<Transaction> getTransactionList(){
        return this.transactionList;
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     * Provide a direct reference to each of the views within a data item.
     * Used to cache the views within the item layout for fast access
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public CircularView circularView;
        public TextView name, cost, date, account, location;

        public SwipeLayout swipeLayout;
        public ImageView deleteBtn, approveBtn, unapproveBtn, locationIcon;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            circularView = (CircularView) itemView.findViewById(R.id.categoryIcon);
            name = (TextView) itemView.findViewById(R.id.transactionNote);
            cost = (TextView) itemView.findViewById(R.id.transactionCost);
            date = (TextView) itemView.findViewById(R.id.transactionDate);
            account = (TextView) itemView.findViewById(R.id.transactionAccount);
            location = (TextView) itemView.findViewById(R.id.transactionLocation);
            locationIcon = (ImageView) itemView.findViewById(R.id.locationIcon);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeTransaction);
            deleteBtn = (ImageView) itemView.findViewById(R.id.deleteBtn);
            approveBtn = (ImageView) itemView.findViewById(R.id.approveBtn);
            unapproveBtn = (ImageView) itemView.findViewById(R.id.unapproveBtn);

            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    Log.d("TRANSACTION_ADAPTER", "onstartopen");

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    Log.d("TRANSACTION_ADAPTER", "on open");

                }

                @Override
                public void onStartClose(SwipeLayout layout) {
                    Log.d("TRANSACTION_ADAPTER", "onstartclose");

                }

                @Override
                public void onClose(SwipeLayout layout) {
                    Log.d("TRANSACTION_ADAPTER", "onclose");

                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    //Log.d("TRANSACTION_ADAPTER", "onupdate "+leftOffset+","+topOffset);
                    mListener.onPullDownAllow(false);
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    //Log.d("TRANSACTION_ADAPTER", "onhandrelease :"+xvel+","+yvel);
                    mListener.onPullDownAllow(true);
                }
            });

            swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickTransaction(getLayoutPosition());
                    Log.d("ZHAP", "on click : " + getLayoutPosition());
                }
            });

            approveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ZHAP", "editting category : " + getLayoutPosition());
                    mListener.onApproveTransaction(getLayoutPosition());
                }
            });

            unapproveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ZHAP", "editting category : " + getLayoutPosition());
                    mListener.onUnapproveTransaction(getLayoutPosition());
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteTransaction(getLayoutPosition());
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnTransactionAdapterInteractionListener {
        void onClickTransaction(int position);

        void onDeleteTransaction(int position);

        void onApproveTransaction(int position);

        void onUnapproveTransaction(int position);

        void onPullDownAllow(boolean value);
    }
}

