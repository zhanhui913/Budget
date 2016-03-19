package com.zhan.budget.Adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
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
 * Created by zhanyap on 15-08-19.
 */
public class TransactionListAdapter extends ArrayAdapter<Transaction> {

    private Activity activity;
    private List<Transaction> transactionList;
    private OnTransactionAdapterInteractionListener mListener;
    private boolean showDate;

    static class ViewHolder {
        public CircularView circularView;
        public TextView name;
        public TextView cost;
        public TextView date;

        public SwipeLayout swipeLayout;
        public ImageView deleteBtn;
        public ImageView approveBtn;
    }

    public TransactionListAdapter(Fragment fragment, List<Transaction> transactionList, boolean showDate) {
        super(fragment.getActivity(), R.layout.item_transaction, transactionList);
        this.activity = fragment.getActivity();
        this.transactionList = transactionList;
        this.showDate = showDate;

        //Any activity or fragment that uses this adapter needs to implement the OnTransactionAdapterInteractionListener interface
        if (fragment instanceof OnTransactionAdapterInteractionListener) {
            mListener = (OnTransactionAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnTransactionAdapterInteractionListener.");
        }
    }

    public TransactionListAdapter(Activity activity, List<Transaction> transactionList, boolean showDate){
        super(activity, R.layout.item_transaction, transactionList);
        this.activity = activity;
        this.transactionList = transactionList;
        this.showDate = showDate;

        //Any activity or fragment that uses this adapter needs to implement the OnTransactionAdapterInteractionListener interface
        if(activity instanceof  OnTransactionAdapterInteractionListener){
            mListener = (OnTransactionAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnTransactionAdapterInteractionListener.");
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
            convertView = inflater.inflate(R.layout.item_transaction, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.transactionNote);
            viewHolder.cost = (TextView) convertView.findViewById(R.id.transactionCost);
            viewHolder.date = (TextView) convertView.findViewById(R.id.transactionDate);

            viewHolder.swipeLayout = (SwipeLayout) convertView.findViewById(R.id.swipeTransaction);
            viewHolder.deleteBtn = (ImageView) convertView.findViewById(R.id.deleteBtn);
            viewHolder.approveBtn = (ImageView) convertView.findViewById(R.id.approveBtn);

            // The tag can be any Object, this just happens to be the ViewHolder
            convertView.setTag(viewHolder);
        }else {
            // Get the ViewHolder back to get fast access to the Views
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting transaction data for the row
        Transaction transaction = transactionList.get(position);

        //If transaction is COMPLETED
        if(transaction.getDayType().equalsIgnoreCase(DayType.COMPLETED.toString())) {
            viewHolder.circularView.setStrokeWidthInDP(0);
            viewHolder.circularView.setCircleRadiusInDP(25);
            viewHolder.circularView.setStrokeColor(R.color.transparent);
            viewHolder.circularView.setCircleColor(transaction.getCategory().getColor());
            viewHolder.circularView.setIconResource(CategoryUtil.getIconID(getContext(), transaction.getCategory().getIcon()));
            viewHolder.circularView.setIconColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColor));

            //If the transaction is completed, there is no need for the approve btn in the swipemenulayout
            viewHolder.approveBtn.setVisibility(View.GONE);
        }else{ //If transaction is SCHEDULED but not COMPLETED
            viewHolder.circularView.setStrokeWidthInDP(2);
            viewHolder.circularView.setCircleRadiusInDP(23);

            viewHolder.circularView.setCircleColor(R.color.transparent);
            viewHolder.circularView.setIconResource(CategoryUtil.getIconID(getContext(), transaction.getCategory().getIcon()));
            viewHolder.circularView.setStrokeColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColorText));
            viewHolder.circularView.setIconColor(Colors.getHexColorFromAttr(getContext(), R.attr.themeColorText));

            viewHolder.approveBtn.setVisibility(View.VISIBLE);
        }

        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(transaction.getNote())){
            viewHolder.name.setText(transaction.getNote());
        }else{
            viewHolder.name.setText(transaction.getCategory().getName());
        }

        if(this.showDate){
            viewHolder.date.setVisibility(View.VISIBLE);
            viewHolder.date.setText(DateUtil.convertDateToStringFormat1(transaction.getDate()));
        }else{
            viewHolder.date.setVisibility(View.GONE);
        }

        viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(transaction.getPrice(), Constants.BUDGET_LOCALE));

        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
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
                mListener.onDisablePtrPullDown(true);
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //Log.d("TRANSACTION_ADAPTER", "onhandrelease :"+xvel+","+yvel);
                mListener.onDisablePtrPullDown(false);
            }
        });

        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteTransaction(position);
            }
        });

        viewHolder.approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onApproveTransaction(position);
            }
        });

        return convertView;
    }

    public interface OnTransactionAdapterInteractionListener {
        void onDeleteTransaction(int position);

        void onApproveTransaction(int position);

        void onDisablePtrPullDown(boolean value);
    }
}