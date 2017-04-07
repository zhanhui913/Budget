package com.zhan.budget.Fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.ScheduledTransactionInfoActivity;
import com.zhan.budget.Adapter.ScheduledTransactionRecyclerAdapter;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.ScheduledTransaction;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduledTransactionFragment extends BaseRealmFragment implements
        ScheduledTransactionRecyclerAdapter.OnScheduledTransactionAdapterInteractionListener{

    private static final String TAG = "ScheduledTranFragment";

    private ViewGroup emptyLayout;
    private TextView emptySTransactionPrimaryText, emptySTransactionSecondaryText;

    private RecyclerView sTransactionListView;
    private ScheduledTransactionRecyclerAdapter sTransactionRecyclerAdapter;

    private RealmResults<ScheduledTransaction> resultsScheduledTransaction;
    private List<ScheduledTransaction> sTransactionList;

    public ScheduledTransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_scheduled_transaction;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();

        sTransactionListView = (RecyclerView)view.findViewById(R.id.scheduledTransactionListView);
        sTransactionListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        sTransactionList = new ArrayList<>();
        sTransactionRecyclerAdapter = new ScheduledTransactionRecyclerAdapter(this, sTransactionList, false);
        sTransactionListView.setAdapter(sTransactionRecyclerAdapter);

        //Add divider
        sTransactionListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyScheduledTransactionLayout);
        emptySTransactionPrimaryText = (TextView) view.findViewById(R.id.emptyPrimaryText);
        emptySTransactionPrimaryText.setText(R.string.empty_scheduled_transaction);
        emptySTransactionSecondaryText = (TextView) view.findViewById(R.id.emptySecondaryText);
        emptySTransactionSecondaryText.setText("Add one in the settings");

        //Initially, the empty layout should be hidden
        emptyLayout.setVisibility(View.GONE);

        getScheduledTransactions();
    }

    private void getScheduledTransactions(){
        resultsScheduledTransaction = myRealm.where(ScheduledTransaction.class).findAllAsync();
        resultsScheduledTransaction.addChangeListener(new RealmChangeListener<RealmResults<ScheduledTransaction>>() {
            @Override
            public void onChange(RealmResults<ScheduledTransaction> element) {
                element.removeChangeListener(this);

                sTransactionList = myRealm.copyFromRealm(element);
                sTransactionRecyclerAdapter.setScheduledTransactionList(sTransactionList);

                updateStatus();
            }
        });
    }

    private void editScheduledTransaction(int position){
        startActivityForResult(ScheduledTransactionInfoActivity.createIntentToEditScheduledTransaction(getContext(), sTransactionRecyclerAdapter.getScheduledTransactionList().get(position)), RequestCodes.EDIT_SCHEDULED_TRANSACTION);
    }

    private void confirmDelete(final int position){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(getString(R.string.warning_delete_scheduled_transaction));

        new AlertDialog.Builder(getContext())
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteScheduledTransaction(position);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Does nothing for now
                    }
                })
                .create()
                .show();
    }

    private void deleteScheduledTransaction(int position){
        String scheduledTransactionID = resultsScheduledTransaction.get(position).getId();
        Date now = DateUtil.refreshDate(new Date());

        //Delete the Scheduled Transaction
        myRealm.beginTransaction();
        resultsScheduledTransaction.get(position).deleteFromRealm();
        myRealm.commitTransaction();

        //Delete all Transaction with dayType = SCHEDULED and scheduledTransactionId = ScheduledTransaction's ID that is past today
        myRealm.where(Transaction.class)
                .equalTo("scheduledTransactionId", scheduledTransactionID)
                .greaterThanOrEqualTo("date", now)
                .equalTo("dayType", DayType.SCHEDULED.toString())
                .findAllAsync()
                .addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                //Delete all transactions
                myRealm.beginTransaction();
                element.deleteAllFromRealm();
                myRealm.commitTransaction();
            }
        });

        //recalculate everything
        sTransactionRecyclerAdapter.getScheduledTransactionList().remove(position);
        sTransactionRecyclerAdapter.notifyDataSetChanged();
        updateStatus();
    }

    private void updateStatus(){
        if(sTransactionRecyclerAdapter.getScheduledTransactionList().size() > 0){
            emptyLayout.setVisibility(View.GONE);
            sTransactionListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            sTransactionListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data.getExtras() != null) {
            if(requestCode == RequestCodes.NEW_SCHEDULED_TRANSACTION){
                //Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(TransactionInfoActivity.RESULT_S));
                //populateTransactionsForDate(tt.getDate());
                //updateScheduledTransactionsForDecoration();
                //calendarView.selectDate(tt.getDate());
            }else if(requestCode == RequestCodes.EDIT_SCHEDULED_TRANSACTION){
                ScheduledTransaction st = Parcels.unwrap(data.getExtras().getParcelable(ScheduledTransactionInfoActivity.RESULT_SCHEDULED_TRANSACTION));

                Log.d(TAG, st.toString());

                //populateTransactionsForDate(tt.getDate());
                //updateScheduledTransactionsForDecoration();
                //calendarView.selectDate(tt.getDate());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickScheduledTransaction(int position){
        //startActivityForResult(TransactionsForAccount.createIntentToViewAllTransactionsForAccountForMonth(getContext(), accountList.get(position), currentMonth), RequestCodes.HAS_TRANSACTION_CHANGED);
    }

    @Override
    public void onDeleteScheduledTransaction(int position){
        confirmDelete(position);
    }

    @Override
    public void onEditScheduledTransaction(int position){
        editScheduledTransaction(position);
    }
}
