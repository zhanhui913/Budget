package com.zhan.budget.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zhan.budget.Model.Account;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Util;
import com.zhan.circularview.CircularView;

import java.util.List;


/**
 * Created by zhanyap on 15-08-19.
 */
public class AccountListAdapter extends ArrayAdapter<Account> {

    private Activity activity;
    private List<Account> accountList;


    static class ViewHolder {
        public TextView name;
        public TextView cost;
    }

    public AccountListAdapter(Activity activity,  List<Account> accountList) {
        super(activity, R.layout.item_category, accountList);
        this.activity = activity;
        this.accountList = accountList;
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
            convertView = inflater.inflate(R.layout.item_account, parent, false);


            viewHolder.name = (TextView) convertView.findViewById(R.id.accountName);
            viewHolder.cost = (TextView) convertView.findViewById(R.id.accountCost);

            // The tag can be any Object, this just happens to be the ViewHolder
            convertView.setTag(viewHolder);
        }else {
            // Get the ViewHolder back to get fast access to the Views
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting transaction data for the row
        Account account = accountList.get(position);

        //Name
        viewHolder.name.setText(account.getName());
        viewHolder.cost.setText(account.getName());

        return convertView;
    }
}