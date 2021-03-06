package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.AccountInfoActivity;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Activity.Transactions.TransactionsForAccount;
import com.zhan.budget.Adapter.AccountRecyclerAdapter;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends BaseRealmFragment implements
        AccountRecyclerAdapter.OnAccountAdapterInteractionListener{

    private static final String TAG = "AccountFragment";

    private OnAccountInteractionListener mListener;

    private ViewGroup emptyLayout;

    private TextView centerPanelRightTextView, emptyAccountPrimaryText, emptyAccountSecondaryText;

    private RecyclerView accountListView;
    private AccountRecyclerAdapter accountRecyclerAdapter;

    private RealmResults<Account> resultsAccount;
    private List<Account> accountList;

    private PieChartFragment pieChartFragment;

    private Date currentMonth;
    private RealmResults<Transaction> resultsTransaction;
    private List<Transaction> transactionMonthList;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_account;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();

        once = false;

        currentMonth = new Date();

        centerPanelRightTextView = (TextView)view.findViewById(R.id.totalCostTextView);

        accountListView = (RecyclerView)view.findViewById(R.id.accountListView);
        accountListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        accountList = new ArrayList<>();
        accountRecyclerAdapter = new AccountRecyclerAdapter(this, accountList, true, false);
        accountListView.setAdapter(accountRecyclerAdapter);

        //Add divider
        accountListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyAccountLayout);
        emptyAccountPrimaryText = (TextView) view.findViewById(R.id.emptyPrimaryText);
        emptyAccountPrimaryText.setText(R.string.empty_account);
        emptyAccountSecondaryText = (TextView) view.findViewById(R.id.emptySecondaryText);
        emptyAccountSecondaryText.setText("Add one in the settings");

        //Initially, the empty layout should be hidden
        emptyLayout.setVisibility(View.GONE);

        //Setup pie chart
        pieChartFragment = PieChartFragment.newInstance(accountList, false, false, false, getString(R.string.account));
        getFragmentManager().beginTransaction().replace(R.id.chartContentFrame, pieChartFragment).commit();

        //0 represents no change in month relative to currentMonth variable.
        updateMonthInToolbar(0);
    }

    private void updateMonthInToolbar(int direction){ Log.d(TAG, "updateMonthInToolbar");
        accountListView.smoothScrollToPosition(0);

        //reset pie chart data & total cost text view
        pieChartFragment.resetPieChart();
        updatePriceStatus(0); //reset it back to 0

        //reset account list view
        for(int i = 0 ; i < accountList.size(); i++){
            accountList.get(i).setCost(0);
        }
        accountRecyclerAdapter.setAccountList(accountList);

        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(getContext(), currentMonth));

        //option 1
        if(!once){
            populateAccountWithNoInfo();
            once = true;
        }else{
            populateAccountWithInfo(true);
        }

        //option 2
        //getListOfTransactionsForMonth();
    }

//option 1 start
private boolean once;

    /**
     * Gets the list of accounts. (called once only)
     */
    private void populateAccountWithNoInfo(){
        Log.d(TAG, "populateAccountWithNoInfo");

        resultsAccount = myRealm.where(Account.class).findAllAsync();
        resultsAccount.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                element.removeChangeListener(this);

                accountList = myRealm.copyFromRealm(element);
                accountRecyclerAdapter.setAccountList(accountList);

                populateAccountWithInfo(true);
            }
        });
    }

    /**
     * Resets data for all accounts and start new calculation.
     */
    private void populateAccountWithInfo(final boolean animate){
        final Date startMonth = DateUtil.refreshMonth(currentMonth);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = DateUtil.getLastDateOfMonth(currentMonth);

        Log.d(TAG, "populateAccountWithInfo for "+startMonth.toString());

        //Reset all values in list
        for(int i = 0 ; i < accountList.size(); i++){
            accountList.get(i).setCost(0);
        }

        resultsTransaction = myRealm.where(Transaction.class).between("date", startMonth, endMonth).equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync();
        resultsTransaction.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListeners();

                Log.d(TAG, "got this month transaction, size: " + element.size());

                transactionMonthList = myRealm.copyFromRealm(element);

                aggregateAccountInfo(animate);
            }
        });
    }

    private void aggregateAccountInfo(final boolean animate){
        Log.d("DEBUG", "1) There are " + transactionMonthList.size() + " transactions for this month");

        AsyncTask<Void, Void, Double> loader = new AsyncTask<Void, Void, Double>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("DEBUG", "preparing to aggregate results");
            }

            @Override
            protected Double doInBackground(Void... voids) {

                startTime = System.nanoTime();

                double totalCost = 0f;

                //Go through each COMPLETED transaction and put them into the correct account
                for(int t = 0; t < transactionMonthList.size(); t++){
                    for(int c = 0; c < accountList.size(); c++){
                        if(transactionMonthList.get(t).getAccount() != null){
                            if(transactionMonthList.get(t).getAccount().getId().equalsIgnoreCase(accountList.get(c).getId())){
                                double transactionPrice = transactionMonthList.get(t).getPrice();
                                double currentAccountPrice = accountList.get(c).getCost();
                                accountList.get(c).setCost(transactionPrice + currentAccountPrice);
                                totalCost += transactionPrice;
                            }
                        }
                    }
                }

                return totalCost;
            }

            @Override
            protected void onPostExecute(Double result) {
                super.onPostExecute(result);

                for(int i = 0; i < accountList.size(); i++){
                    Log.d("ZHAN1", "category : "+accountList.get(i).getName()+" -> "+accountList.get(i).getCost());
                }

                accountRecyclerAdapter.setAccountList(accountList);

                pieChartFragment.setData(accountList, animate);

                updatePriceStatus(result);

                endTime = System.nanoTime();
                duration = (endTime - startTime);

                long milli = (duration/1000000);
                long second = (milli/1000);
                float minutes = (second / 60.0f);

                updateAccountStatus();
                Log.d("DEBUG_ACC", " aggregating took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
    }
//option 1 end


//option 2 start
    private void getListOfTransactionsForMonth(){ Log.d(TAG, "getListOfTransactionsForMonth");
        final Date startMonth = DateUtil.refreshMonth(currentMonth);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = DateUtil.getLastDateOfMonth(currentMonth);

        startTimeNow = System.nanoTime();
        myRealm.where(Transaction.class)
                .between("date", startMonth, endMonth)
                .equalTo("dayType", DayType.COMPLETED.toString())
                .findAllAsync()
                .addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(final RealmResults<Transaction> element) {
                endTimeNow = System.nanoTime();
                durationNow = (endTimeNow - startTimeNow);
                long milli = (durationNow/1000000);
                Log.d(TAG, "realm took "+milli+" ms");
                element.removeChangeListener(this);
                Log.d(TAG, "There is "+element.size()+" results");

                aggregateAccount2(myRealm.copyFromRealm(element), true);
            }
        });
    }

    long startTimeNow,endTimeNow, durationNow;
    private void aggregateAccount(List<Transaction> tempList, boolean animate){ Log.d(TAG, "aggregate here");
        HashMap<Account, Double> accountHash = new HashMap<>();

        double totalCost = 0;

        for(int i = 0; i < tempList.size(); i++){
            if(tempList.get(i).getAccount() != null) {
                if (!accountHash.containsKey(tempList.get(i).getAccount())) {
                    accountHash.put(tempList.get(i).getAccount(), tempList.get(i).getPrice());
                } else {
                    accountHash.put(tempList.get(i).getAccount(), accountHash.get(tempList.get(i).getAccount()) + tempList.get(i).getPrice());
                }
                totalCost += tempList.get(i).getPrice();
            }
        }

        accountList = new ArrayList<>();
        for(Account key : accountHash.keySet()){
            key.setCost(accountHash.get(key));
            accountList.add(key);
        }

        accountRecyclerAdapter.setAccountList(accountList);
        pieChartFragment.setData(accountList, animate);

        updatePriceStatus(totalCost);
        updateAccountStatus();
    }

    private void aggregateAccount2(final List<Transaction> tempList, final boolean animate){
        AsyncTask<Void, Void, Double> loader = new AsyncTask<Void, Void, Double>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d(TAG, "preparing to aggregate results");
            }

            @Override
            protected Double doInBackground(Void... voids) {

                startTime = System.nanoTime();

                HashMap<Account, Double> accountHash = new HashMap<>();

                double totalCost = 0;

                for(int i = 0; i < tempList.size(); i++){
                    if(tempList.get(i).getAccount() != null) {
                        if (!accountHash.containsKey(tempList.get(i).getAccount())) {
                            accountHash.put(tempList.get(i).getAccount(), tempList.get(i).getPrice());
                        } else {
                            accountHash.put(tempList.get(i).getAccount(), accountHash.get(tempList.get(i).getAccount()) + tempList.get(i).getPrice());
                        }
                        totalCost += tempList.get(i).getPrice();
                    }
                }

                accountList = new ArrayList<>();
                for(Account key : accountHash.keySet()){
                    key.setCost(accountHash.get(key));
                    accountList.add(key);
                }

                return totalCost;
            }

            @Override
            protected void onPostExecute(Double result) {
                super.onPostExecute(result);

                for(int i = 0; i < accountList.size(); i++){
                    Log.d(TAG, "category : "+accountList.get(i).getName()+" -> "+accountList.get(i).getCost());
                }

                accountRecyclerAdapter.setAccountList(accountList);
                pieChartFragment.setData(accountList, animate);

                endTime = System.nanoTime();
                duration = (endTime - startTime);

                long milli = (duration/1000000);
                long second = (milli/1000);
                float minutes = (second / 60.0f);

                updatePriceStatus(result);
                updateAccountStatus();
                Log.d(TAG, " aggregating took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        Log.d(TAG, "about to execute asynctask");
        loader.execute();
    }
//option 2 end

    private void editAccount(int position){
        startActivityForResult(AccountInfoActivity.createIntentToEditAccount(getContext(), accountRecyclerAdapter.getAccountList().get(position)), RequestCodes.EDIT_ACCOUNT);
    }

    private void updateAccountStatus(){ Log.d(TAG,"update account status "+accountRecyclerAdapter.getItemCount());
        if(accountRecyclerAdapter.getItemCount() > 0){
            emptyLayout.setVisibility(View.GONE);
            accountListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            accountListView.setVisibility(View.GONE);
        }
    }

    private void updatePriceStatus(double cost){
        centerPanelRightTextView.setText(CurrencyTextFormatter.formatDouble(cost));
        if(cost > 0){
            centerPanelRightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }else if(cost < 0){
            centerPanelRightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }else{
            centerPanelRightTextView.setTextColor(Colors.getColorFromAttr(getContext(), R.attr.themeColorText));
        }
    }

    private void confirmDelete(final int position){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(getString(R.string.warning_delete_account));

        new AlertDialog.Builder(getContext())
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAccount(position);
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

    private void deleteAccount(int position){
        myRealm.beginTransaction();
        resultsAccount.get(position).deleteFromRealm();
        myRealm.commitTransaction();

        //recalculate everything
        //option 1
        populateAccountWithNoInfo();

        //option 2
        //getListOfTransactionsForMonth();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == RequestCodes.EDIT_ACCOUNT) {

                getListOfTransactionsForMonth();
            }else if(requestCode == RequestCodes.HAS_TRANSACTION_CHANGED){
                boolean hasChanged = data.getExtras().getBoolean(TransactionInfoActivity.HAS_CHANGED);

                if(hasChanged){
                    //If something has been changed, update the list
                    getListOfTransactionsForMonth();
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAccountInteractionListener) {
            mListener = (OnAccountInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAccountInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickAccount(int position){
        startActivityForResult(TransactionsForAccount.createIntentToViewAllTransactionsForAccountForMonth(getContext(), accountList.get(position), currentMonth), RequestCodes.HAS_TRANSACTION_CHANGED);
    }

    @Override
    public void onDeleteAccount(int position){
        confirmDelete(position);
    }

    @Override
    public void onEditAccount(int position){
        editAccount(position);
    }

    @Override
    public void onPullDownAllow(boolean value){
        //cannot pull down
    }

    @Override
    public void onAccountSetAsDefault(int position){
        //Cant set account as default here
    }

    @Override
    public void onAccountDeSetFromDefault(int position){
        //Cant unset account from default here
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Etc
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.change_month_year, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.leftChevron:
                updateMonthInToolbar(-1);
                return true;
            case R.id.rightChevron:
                updateMonthInToolbar(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAccountInteractionListener {
        void updateToolbar(String date);
    }

}
