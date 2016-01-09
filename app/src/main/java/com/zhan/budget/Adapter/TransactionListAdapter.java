package com.zhan.budget.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        ViewHolder viewHolder;

        // reuse views
        if (convertView == null) {
            // configure view holder
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_category, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.categoryName);
            viewHolder.cost = (TextView) convertView.findViewById(R.id.categoryCost);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting transaction data for the row
        Transaction transaction = transactionList.get(position);

        Log.d("DEUBG", "index:" + position + "- Transaction " + transaction.toString());

        //Icon
        viewHolder.circularView.setBgColor(Color.parseColor(transaction.getCategory().getColor()));
        viewHolder.circularView.setIconDrawable(ResourcesCompat.getDrawable(activity.getResources(),
                CategoryUtil.getIconResourceId(transaction.getCategory().getIcon()), activity.getTheme()));

        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(transaction.getNote())){
            viewHolder.name.setText(transaction.getNote());
        }else{
            viewHolder.name.setText(transaction.getCategory().getName());
        }

        viewHolder.cost.setText("$"+Util.setPriceToCorrectDecimalInString(transaction.getPrice()));

        return convertView;
    }
}