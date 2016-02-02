package com.zhan.budget.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Util;
import com.zhan.circularview.CircularView;

import java.util.List;


/**
 * Created by zhanyap on 15-08-19.
 */
public class TransactionListAdapter extends ArrayAdapter<Transaction> {

    private Activity activity;
    private List<Transaction> transactionList;


    static class ViewHolder {
        public CircularView circularView;
        public TextView name;
        public TextView cost;
    }

    public TransactionListAdapter(Activity activity,  List<Transaction> transactionList) {
        super(activity, R.layout.item_category, transactionList);
        this.activity = activity;
        this.transactionList = transactionList;
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
            convertView = inflater.inflate(R.layout.item_transaction, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.transactionNote);
            viewHolder.cost = (TextView) convertView.findViewById(R.id.transactionCost);

            // The tag can be any Object, this just happens to be the ViewHolder
            convertView.setTag(viewHolder);
        }else {
            // Get the ViewHolder back to get fast access to the Views
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting transaction data for the row
        Transaction transaction = transactionList.get(position);

        //Icon
        if(transaction.getDayType().equalsIgnoreCase(DayType.COMPLETED.toString())) {
            viewHolder.circularView.setCircleColor(Color.parseColor(transaction.getCategory().getColor()));
            viewHolder.circularView.setIconDrawable(ResourcesCompat.getDrawable(activity.getResources(),
                    CategoryUtil.getIconResourceId(transaction.getCategory().getIcon()), activity.getTheme()));
        }else{
            viewHolder.circularView.setStrokeWidth(2); //in dp
            viewHolder.circularView.setStrokeColor(Color.parseColor("#808080"));
            viewHolder.circularView.setIconDrawable(ResourcesCompat.getDrawable(activity.getResources(),
                    CategoryUtil.getIconResourceId(transaction.getCategory().getIcon()), activity.getTheme()));
            viewHolder.circularView.setIconColor(Color.parseColor("#808080"));
        }

        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(transaction.getNote())){
            viewHolder.name.setText(transaction.getNote());
        }else{
            viewHolder.name.setText(transaction.getCategory().getName());
        }

        viewHolder.cost.setText(Util.setPriceToCorrectDecimalInString(transaction.getPrice()));

        return convertView;
    }
}