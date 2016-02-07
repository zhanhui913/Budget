package com.zhan.budget.Adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Model.Account;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;

import java.util.List;


/**
 * Created by zhanyap on 15-08-19.
 */
public class AccountListAdapter extends ArrayAdapter<Account> {

    private Activity activity;
    private List<Account> accountList;
    private OnAccountAdapterInteractionListener mListener;

    static class ViewHolder {
        public TextView name;
        public TextView cost;
        public SwipeLayout swipeLayout;
        public ImageView deleteBtn;
        public ImageView editBtn;
    }

    public AccountListAdapter(Activity activity, List<Account> accountList){
        super(activity, R.layout.item_account, accountList);
        this.activity = activity;
        this.accountList = accountList;

        //Any activity or fragment that uses this adapter needs to implement the OnAccountAdapterInteractionListener interface
        if(activity instanceof OnAccountAdapterInteractionListener){
            mListener = (OnAccountAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnAccountAdapterInteractionListener.");
        }
    }

    public AccountListAdapter(Fragment fragment,  List<Account> accountList) {
        super(fragment.getActivity(), R.layout.item_account, accountList);
        this.activity = fragment.getActivity();
        this.accountList = accountList;

        //Any activity or fragment that uses this adapter needs to implement the OnAccountAdapterInteractionListener interface
        if (fragment instanceof OnAccountAdapterInteractionListener) {
            mListener = (OnAccountAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnAccountAdapterInteractionListener.");
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

            viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onstartopen");
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "on open");
                }

                @Override
                public void onStartClose(SwipeLayout layout) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onstartclose");
                }

                @Override
                public void onClose(SwipeLayout layout) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onclose");
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onupdate "+leftOffset+","+topOffset);
                    mListener.onDisablePtrPullDown(true);
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    //Log.d("ACCOUNT_LIST_ADAPTER", "onhandrelease :"+xvel+","+yvel);
                    mListener.onDisablePtrPullDown(false);
                }
            });

            viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "delete", Toast.LENGTH_SHORT).show();
                    mListener.onDeleteAccount(position);
                }
            });

            viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "edit", Toast.LENGTH_SHORT).show();
                    mListener.onEditAccount(position);
                }
            });

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

    public interface OnAccountAdapterInteractionListener {
        void onDeleteAccount(int position);

        void onEditAccount(int position);

        void onDisablePtrPullDown(boolean value);
    }
}