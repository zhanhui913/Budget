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
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.ScheduledTransaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

/**
 * Created by zhanyap on 2017-04-05.
 */

public class ScheduledTransactionRecyclerAdapter extends RecyclerView.Adapter<ScheduledTransactionRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<ScheduledTransaction> scheduledTransactionList;
    private boolean showDate;
    private OnScheduledTransactionAdapterInteractionListener mListener;

    public ScheduledTransactionRecyclerAdapter(Fragment fragment, List<ScheduledTransaction> sTransactionList, boolean showDate) {
        this.context = fragment.getContext();
        this.showDate = showDate;
        this.scheduledTransactionList = sTransactionList;

        //Any activity or fragment that uses this adapter needs to implement the OnScheduledTransactionAdapterInteractionListener interface
        if (fragment instanceof OnScheduledTransactionAdapterInteractionListener) {
            mListener = (OnScheduledTransactionAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnScheduledTransactionAdapterInteractionListener.");
        }
    }

    public ScheduledTransactionRecyclerAdapter(Activity activity, List<ScheduledTransaction> sTransactionList, boolean showDate){
        this.context = activity;
        this.showDate = showDate;
        this.scheduledTransactionList = sTransactionList;

        //Any activity or fragment that uses this adapter needs to implement the OnScheduledTransactionAdapterInteractionListener interface
        if(activity instanceof  OnScheduledTransactionAdapterInteractionListener){
            mListener = (OnScheduledTransactionAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnScheduledTransactionAdapterInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scheduled_transaction, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting Scheduled Transaction data for the row
        final ScheduledTransaction scheduledTransaction = scheduledTransactionList.get(position);

        //Account
        if(scheduledTransaction.getTransaction().getAccount() != null){
            viewHolder.account.setVisibility(View.VISIBLE);
            viewHolder.accountIcon.setVisibility(View.VISIBLE);
            viewHolder.account.setText(scheduledTransaction.getTransaction().getAccount().getName());
        }else{
            viewHolder.account.setVisibility(View.GONE);
            viewHolder.accountIcon.setVisibility(View.GONE);
        }

        //Location
        if(scheduledTransaction.getTransaction().getLocation() != null){
            viewHolder.location.setVisibility(View.VISIBLE);
            viewHolder.locationIcon.setVisibility(View.VISIBLE);
            viewHolder.location.setText(scheduledTransaction.getTransaction().getLocation().getName());
        }else{
            viewHolder.location.setVisibility(View.GONE);
            viewHolder.locationIcon.setVisibility(View.GONE);
        }

        //If there is no note, use Category's name instead
        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(scheduledTransaction.getTransaction().getNote())){
            viewHolder.name.setText(scheduledTransaction.getTransaction().getNote());
        }else{
            if(scheduledTransaction.getTransaction().getCategory() != null){
                viewHolder.name.setText(scheduledTransaction.getTransaction().getCategory().getName());
            }else{
                viewHolder.name.setText("");
            }
        }

        viewHolder.cost.setText(CurrencyTextFormatter.formatDouble(scheduledTransaction.getTransaction().getPrice()));

        //Category
        viewHolder.circularView.setIconColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));
        viewHolder.circularView.setTextColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));

        if(scheduledTransaction.getTransaction().getCategory() != null) {
            if (scheduledTransaction.getTransaction().getCategory().isText()) {
                viewHolder.circularView.setIconResource(0);
                viewHolder.circularView.setText(""+ Util.getFirstCharacterFromString(scheduledTransaction.getTransaction().getCategory().getName().toUpperCase().trim()));
            } else {
                viewHolder.circularView.setIconResource(CategoryUtil.getIconID(context, scheduledTransaction.getTransaction().getCategory().getIcon()));
                viewHolder.circularView.setText("");
            }

            if(scheduledTransaction.getTransaction().getCategory().getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                viewHolder.cost.setTextColor(ContextCompat.getColor(context, R.color.red));
            }else{
                viewHolder.cost.setTextColor(ContextCompat.getColor(context, R.color.green));
            }

            viewHolder.circularView.setCircleColor(scheduledTransaction.getTransaction().getCategory().getColor());
            viewHolder.circularView.setStrokeColor(scheduledTransaction.getTransaction().getCategory().getColor());
        }else{
            //If there is no category attached, put a question mark as the icon
            viewHolder.circularView.setIconResource(0);
            viewHolder.circularView.setText("?");
            viewHolder.cost.setTextColor(ContextCompat.getColor(context, R.attr.themeColorText));
        }

        //Repeat type
        viewHolder.repeat.setText(String.format(context.getString(R.string.scheduled_transaction_display), scheduledTransaction.getRepeatUnit(), scheduledTransaction.getRepeatType()));
    }

    @Override
    public int getItemCount() {
        return this.scheduledTransactionList.size();
    }

    public void setScheduledTransactionList(List<ScheduledTransaction> list){
        this.scheduledTransactionList = list;
        notifyDataSetChanged();
    }

    public List<ScheduledTransaction> getScheduledTransactionList(){
        return this.scheduledTransactionList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public CircularView circularView;
        public TextView name, cost, date, account, location, repeat;

        public SwipeLayout swipeLayout;
        public ImageView deleteBtn, editBtn, locationIcon, accountIcon;

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
            accountIcon = (ImageView) itemView.findViewById(R.id.accountIcon);
            repeat = (TextView) itemView.findViewById(R.id.transactionRepeat);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeTransaction);
            editBtn = (ImageView) itemView.findViewById(R.id.editBtn);
            deleteBtn = (ImageView) itemView.findViewById(R.id.deleteBtn);

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
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    //Log.d("TRANSACTION_ADAPTER", "onhandrelease :"+xvel+","+yvel);
                }
            });

            swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickScheduledTransaction(getLayoutPosition());
                }
            });

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipeLayout.close(true);
                    mListener.onEditScheduledTransaction(getLayoutPosition());
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipeLayout.close(true);
                    mListener.onDeleteScheduledTransaction(getLayoutPosition());
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnScheduledTransactionAdapterInteractionListener {
        void onClickScheduledTransaction(int position);

        void onDeleteScheduledTransaction(int position);

        void onEditScheduledTransaction(int position);
    }
}