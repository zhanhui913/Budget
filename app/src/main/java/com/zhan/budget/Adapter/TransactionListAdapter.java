package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.util.List;


/**
 * Created by zhanyap on 15-08-19.
 */
public class TransactionListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Transaction> transactionList;

    public TransactionListAdapter(Activity activity, List<Transaction> transactionList) {
        this.activity = activity;
        this.transactionList = transactionList;
    }

    @Override
    public int getCount() {
        return this.transactionList.size();
    }

    @Override
    public Object getItem(int location) {
        return transactionList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_transaction, null);

        TextView note = (TextView) convertView.findViewById(R.id.transactionNote);
        TextView cost = (TextView) convertView.findViewById(R.id.transactionCost);

        // getting transaction data for the row
        Transaction transaction = transactionList.get(position);

        // Note
        note.setText(transaction.getNote());

        // Cost
        cost.setText("$"+ Util.setPriceToCorrectDecimalInString(transaction.getPrice()));

        return convertView;
    }

    public void refreshList(List<Transaction> transactionList){
        this.transactionList = transactionList;
        notifyDataSetChanged();
    }
}
