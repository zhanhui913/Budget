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
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.R;

import java.util.List;


/**
 * Created by zhanyap on 15-08-19.
 */
public class AccountListAdapter extends ArrayAdapter<Account> {

    private OnAccountAdapterInteractionListener mListener;
    private boolean displayCost;

    static class ViewHolder {
        public TextView name, cost;
        public SwipeLayout swipeLayout;
        public ImageView deleteBtn, editBtn;
        public ImageView defaultAccountIndicatorOn, defaultAccountIndicatorOff;
    }

    public AccountListAdapter(Activity activity, List<Account> accountList, boolean displayCost){
        super(activity, R.layout.item_account, accountList);
        setNotifyOnChange(true);
        this.displayCost = displayCost;

        //Any activity or fragment that uses this adapter needs to implement the OnAccountAdapterInteractionListener interface
        if(activity instanceof OnAccountAdapterInteractionListener){
            mListener = (OnAccountAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnAccountAdapterInteractionListener.");
        }
    }

    public AccountListAdapter(Fragment fragment,  List<Account> accountList, boolean displayCost) {
        super(fragment.getActivity(), R.layout.item_account, accountList);
        setNotifyOnChange(true);
        this.displayCost = displayCost;

        //Any activity or fragment that uses this adapter needs to implement the OnAccountAdapterInteractionListener interface
        if (fragment instanceof OnAccountAdapterInteractionListener) {
            mListener = (OnAccountAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnAccountAdapterInteractionListener.");
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Avoid un-necessary calls to findViewById() on each row, which is expensive!
        final ViewHolder viewHolder;

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
            viewHolder.swipeLayout = (SwipeLayout) convertView.findViewById(R.id.swipeAccount);
            viewHolder.deleteBtn = (ImageView) convertView.findViewById(R.id.deleteBtn);
            viewHolder.editBtn = (ImageView) convertView.findViewById(R.id.editBtn);
            viewHolder.defaultAccountIndicatorOn = (ImageView) convertView.findViewById(R.id.defaultAccountIndicatorOn);
            viewHolder.defaultAccountIndicatorOff = (ImageView) convertView.findViewById(R.id.defaultAccountIndicatorOff);

            viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    Log.d("AccountFragment 1", "onstartopen");
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    Log.d("AccountFragment 1", "on open");
                }

                @Override
                public void onStartClose(SwipeLayout layout) {
                    Log.d("AccountFragment 1", "onstartclose");
                }

                @Override
                public void onClose(SwipeLayout layout) {
                    Log.d("AccountFragment 1", "onclose");
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    Log.d("AccountFragment 1", "onupdate "+leftOffset+","+topOffset);
                    mListener.onPullDownAllow(false);
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    Log.d("AccountFragment 1", "onhandrelease :"+xvel+","+yvel);
                    mListener.onPullDownAllow(true);
                }
            });

            viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickAccount(position);
                }
            });

            viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteAccount(position);
                }
            });

            viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditAccount(position);
                }
            });

            // The tag can be any Object, this just happens to be the ViewHolder
            convertView.setTag(viewHolder);
        }else {
            // Get the ViewHolder back to get fast access to the Views
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // getting account_popup data for the row
        Account account = getItem(position);

        //Name
        viewHolder.name.setText(account.getName());

        if(displayCost){
            viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(Math.abs(account.getCost()), Constants.BUDGET_LOCALE));
        }else{
            viewHolder.cost.setVisibility(View.GONE);
        }

        //Default indicator
        if(account.isDefault()){
            viewHolder.defaultAccountIndicatorOn.setVisibility(View.VISIBLE);
            viewHolder.defaultAccountIndicatorOff.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.defaultAccountIndicatorOn.setVisibility(View.INVISIBLE);
            viewHolder.defaultAccountIndicatorOff.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public void updateList(List<Account> list){
        clear();
        addAll(list);
    }

    public interface OnAccountAdapterInteractionListener {
        void onClickAccount(int position);

        void onDeleteAccount(int position);

        void onEditAccount(int position);

        void onPullDownAllow(boolean value);
    }
}