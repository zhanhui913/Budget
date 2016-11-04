package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

public class AccountRecyclerAdapter extends RecyclerView.Adapter<AccountRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Account> accountList;
    private boolean displayCost, allowClickSetDefault;
    private BudgetCurrency currentCurrency;
    private OnAccountAdapterInteractionListener mListener;

    public AccountRecyclerAdapter(Fragment fragment, List<Account> accountList, BudgetCurrency currency, boolean displayCost, boolean allowClickSetDefault) {
        this.context = fragment.getContext();
        this.accountList = accountList;
        this.currentCurrency = currency;
        this.displayCost = displayCost;
        this.allowClickSetDefault = allowClickSetDefault;

        //Any activity or fragment that uses this adapter needs to implement the OnAccountAdapterInteractionListener interface
        if (fragment instanceof OnAccountAdapterInteractionListener) {
            mListener = (OnAccountAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnAccountAdapterInteractionListener.");
        }
    }

    public AccountRecyclerAdapter(Activity activity, List<Account> accountList, BudgetCurrency currency, boolean displayCost, boolean allowClickSetDefault){
        this.context = activity;
        this.accountList = accountList;
        this.currentCurrency = currency;
        this.displayCost = displayCost;
        this.allowClickSetDefault = allowClickSetDefault;

        //Any activity or fragment that uses this adapter needs to implement the OnAccountAdapterInteractionListener interface
        if(activity instanceof  OnAccountAdapterInteractionListener){
            mListener = (OnAccountAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnAccountAdapterInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting Account data for the row
        final Account account = accountList.get(position);

        viewHolder.name.setText(account.getName());

        viewHolder.icon.setCircleColor(account.getColor());
        viewHolder.icon.setText(""+ Util.getFirstCharacterFromString(account.getName().toUpperCase()));
        viewHolder.icon.setTextColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));
        viewHolder.icon.setTextSizeInDP(30);

        if(displayCost){
            viewHolder.cost.setText(CurrencyTextFormatter.formatFloat(account.getCost(), currentCurrency));

            if(account.getCost() >= 0){
                viewHolder.cost.setTextColor(ContextCompat.getColor(context, R.color.green));
            }else{
                viewHolder.cost.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        }else{
            viewHolder.cost.setVisibility(View.GONE);
        }

        if(allowClickSetDefault){
            //Default indicator
            if(account.isDefault()){
                viewHolder.defaultAccountIndicatorOn.setVisibility(View.VISIBLE);
                viewHolder.defaultAccountIndicatorOff.setVisibility(View.INVISIBLE);
            }else{
                viewHolder.defaultAccountIndicatorOn.setVisibility(View.INVISIBLE);
                viewHolder.defaultAccountIndicatorOff.setVisibility(View.VISIBLE);
            }
        }else{
            viewHolder.defaultAccountIndicatorOn.setVisibility(View.GONE);
            viewHolder.defaultAccountIndicatorOff.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.accountList.size();
    }

    public void setAccountList(List<Account> list){
        this.accountList = list;
        notifyDataSetChanged();
    }

    public List<Account> getAccountList(){
        return this.accountList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView name, cost;
        public SwipeLayout swipeLayout;
        public ImageView deleteBtn, editBtn, defaultAccountIndicatorOn, defaultAccountIndicatorOff;
        public CircularView icon;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            icon = (CircularView) itemView.findViewById(R.id.accountIcon);
            name = (TextView) itemView.findViewById(R.id.accountName);
            cost = (TextView) itemView.findViewById(R.id.accountCost);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeAccount);
            deleteBtn = (ImageView) itemView.findViewById(R.id.deleteBtn);
            editBtn = (ImageView) itemView.findViewById(R.id.editBtn);
            defaultAccountIndicatorOn = (ImageView) itemView.findViewById(R.id.defaultAccountIndicatorOn);
            defaultAccountIndicatorOff = (ImageView) itemView.findViewById(R.id.defaultAccountIndicatorOff);

            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
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

            swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickAccount(getLayoutPosition());
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteAccount(getLayoutPosition());
                }
            });

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditAccount(getLayoutPosition());
                }
            });

            if(allowClickSetDefault){
                defaultAccountIndicatorOff.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mListener.onAccountSetAsDefault(getLayoutPosition());
                        return false;
                    }
                });

                defaultAccountIndicatorOn.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mListener.onAccountDeSetFromDefault(getLayoutPosition());
                        return false;
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

    public interface OnAccountAdapterInteractionListener {
        void onClickAccount(int position);

        void onDeleteAccount(int position);

        void onEditAccount(int position);

        void onPullDownAllow(boolean value);

        void onAccountSetAsDefault(int position);

        void onAccountDeSetFromDefault(int position);
    }
}
