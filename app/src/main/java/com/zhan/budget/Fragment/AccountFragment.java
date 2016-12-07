package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.AccountInfoActivity;
import com.zhan.budget.Activity.Transactions.TransactionsForAccount;
import com.zhan.budget.Adapter.AccountRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import io.realm.Realm;
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

    private TextView centerPanelLeftTextView, centerPanelRightTextView, emptyAccountText;

    private RecyclerView accountListView;
    private AccountRecyclerAdapter accountRecyclerAdapter;

    private RealmResults<Account> resultsAccount;
    private List<Account> accountList;

    private PieChartFragment pieChartFragment;

    private Date currentMonth;
    private RealmResults<Transaction> resultsTransaction;
    private List<Transaction> transactionMonthList;

    private int accountIndexEdited;//The index of the account that the user just finished edited.

    private LinearLayoutManager linearLayoutManager;
    private SwipeLayout currentSwipeLayoutTarget;

    private BudgetCurrency currentCurrency;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_account;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();
        currentMonth = new Date();

        accountList = new ArrayList<>();

        centerPanelLeftTextView = (TextView)view.findViewById(R.id.dateTextView);
        centerPanelRightTextView = (TextView)view.findViewById(R.id.totalCostTextView);

        getDefaultCurrency();

        linearLayoutManager = new LinearLayoutManager(getActivity());

        accountListView = (RecyclerView)view.findViewById(R.id.accountListView);
        accountListView.setLayoutManager(linearLayoutManager);

        accountRecyclerAdapter = new AccountRecyclerAdapter(this, accountList, currentCurrency, true, false);
        accountListView.setAdapter(accountRecyclerAdapter);

        //Add divider
        accountListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyAccountLayout);
        emptyAccountText = (TextView) view.findViewById(R.id.pullDownText);
        emptyAccountText.setText(R.string.empty_account);

        ImageView downArrow = (ImageView) view.findViewById(R.id.downChevronIcon);
        downArrow.setVisibility(View.INVISIBLE);

        //Setup pie chart
        pieChartFragment = PieChartFragment.newInstance(accountList, false, false, getString(R.string.account));
        getFragmentManager().beginTransaction().replace(R.id.chartContentFrame, pieChartFragment).commit();

        populateAccountWithNoInfo();

        //0 represents no change in month relative to currentMonth variable.
        //false because we dont need to get all transactions yet.
        //This may conflict with populateAccountWithNoInfo async where its trying to get the initial
        //accounts
        updateMonthInToolbar(0, false);
    }

    private void getDefaultCurrency(){
        final Realm myRealm = Realm.getDefaultInstance();

        currentCurrency = myRealm.where(BudgetCurrency.class).equalTo("isDefault", true).findFirst();
        if(currentCurrency == null){
            currentCurrency = new BudgetCurrency();
            currentCurrency.setCurrencyCode(Constants.DEFAULT_CURRENCY_CODE);
            currentCurrency.setCurrencyName(Constants.DEFAULT_CURRENCY_NAME);
        }else{
            currentCurrency = myRealm.copyFromRealm(currentCurrency);
        }

        Toast.makeText(getContext(), "Account fragment, default currency : "+currentCurrency.getCurrencyName(), Toast.LENGTH_LONG).show();

        myRealm.close();
    }

    /**
     * Gets the list of accounts. (called once only)
     */
    private void populateAccountWithNoInfo(){
        resultsAccount = myRealm.where(Account.class).findAllAsync();
        resultsAccount.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                element.removeChangeListener(this);

                Log.d(TAG, "there's a change in results account ");
                accountList = myRealm.copyFromRealm(element);
                accountRecyclerAdapter.setAccountList(accountList);

                updateAccountStatus();
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
        //final Date endMonth = DateUtil.getPreviousDate(DateUtil.getNextMonth(currentMonth));
        final Date endMonth = DateUtil.getLastDateOfMonth(currentMonth);

        Log.d("DEBUG","Get all transactions from month is "+startMonth.toString()+", to next month is "+endMonth.toString());

        //Reset all values in list
        for(int i = 0 ; i < accountList.size(); i++){
            accountList.get(i).setCost(0);
        }

        resultsTransaction = myRealm.where(Transaction.class).between("date", startMonth, endMonth).equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync();
        resultsTransaction.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListeners();

                Log.d("REALM", "got this month transaction, " + element.size());

                transactionMonthList = myRealm.copyFromRealm(element);

                aggregateAccountInfo(animate);
            }
        });
    }

    private void aggregateAccountInfo(final boolean animate){
        Log.d("DEBUG", "1) There are " + transactionMonthList.size() + " transactions for this month");

        AsyncTask<Void, Void, Float> loader = new AsyncTask<Void, Void, Float>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("DEBUG", "preparing to aggregate results");
            }

            @Override
            protected Float doInBackground(Void... voids) {

                startTime = System.nanoTime();

                float totalCost = 0f;

                //Go through each COMPLETED transaction and put them into the correct account
                for(int t = 0; t < transactionMonthList.size(); t++){
                    for(int c = 0; c < accountList.size(); c++){
                        if(transactionMonthList.get(t).getAccount() != null){
                            if(transactionMonthList.get(t).getAccount().getId().equalsIgnoreCase(accountList.get(c).getId())){
                                float transactionPrice = transactionMonthList.get(t).getPrice();
                                float currentAccountPrice = accountList.get(c).getCost();
                                accountList.get(c).setCost(transactionPrice + currentAccountPrice);
                                totalCost += transactionPrice;
                            }
                        }
                    }
                }

                return totalCost;
            }

            @Override
            protected void onPostExecute(Float result) {
                super.onPostExecute(result);

                for(int i = 0; i < accountList.size(); i++){
                    Log.d("ZHAN1", "category : "+accountList.get(i).getName()+" -> "+accountList.get(i).getCost());
                }

                accountRecyclerAdapter.setAccountList(accountList);

                pieChartFragment.setData(accountList, animate);

                centerPanelRightTextView.setText(CurrencyTextFormatter.formatFloat(result, currentCurrency));

                if(result > 0){
                    centerPanelRightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                }else if(result < 0){
                    centerPanelRightTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                }else{
                    centerPanelRightTextView.setTextColor(Colors.getColorFromAttr(getContext(), R.attr.themeColorText));
                }

                endTime = System.nanoTime();
                duration = (endTime - startTime);

                long milli = (duration/1000000);
                long second = (milli/1000);
                float minutes = (second / 60.0f);
                Log.d("DEBUG_ACC", " aggregating took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
    }

    private void editAccount(int position){
        Log.d("ACCOUNT_INFO", "trying to edit account at pos : "+position);
        Log.d("ACCOUNT_INFO", "accoutn name : " +accountList.get(position).getName());
        Log.d("ACCOUNT_INFO", "accoutn id : " +accountList.get(position).getId());
        Log.d("ACCOUNT_INFO", "accoutn color : " +accountList.get(position).getColor());



        Intent editAccountIntent = new Intent(getContext(), AccountInfoActivity.class);
        Parcelable wrapped = Parcels.wrap(accountRecyclerAdapter.getAccountList().get(position));
        editAccountIntent.putExtra(Constants.REQUEST_NEW_ACCOUNT, false);
        editAccountIntent.putExtra(Constants.REQUEST_EDIT_ACCOUNT, wrapped);
        startActivityForResult(editAccountIntent, Constants.RETURN_EDIT_ACCOUNT);
    }

    private void updateAccountStatus(){
        if(accountRecyclerAdapter.getItemCount() > 0){
            emptyLayout.setVisibility(View.GONE);
            accountListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            accountListView.setVisibility(View.GONE);
        }
    }

    private void updateMonthInToolbar(int direction, boolean updateAccountInfo){
        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(getContext(), currentMonth));

        centerPanelLeftTextView.setText(DateUtil.convertDateToStringFormat2(getContext(), currentMonth));

        if(updateAccountInfo) {
            populateAccountWithInfo(true);
        }
    }

    private void confirmDelete(final int position){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(getString(R.string.warning_delete_account));

        new AlertDialog.Builder(getContext())
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAccount(position);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        closeSwipeItem(position);
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
        populateAccountWithNoInfo();
    }

    private void openSwipeItem(int position){
        currentSwipeLayoutTarget = (SwipeLayout) linearLayoutManager.findViewByPosition(position);
        currentSwipeLayoutTarget.open();
    }

    private void closeSwipeItem(int position){
        currentSwipeLayoutTarget = (SwipeLayout) linearLayoutManager.findViewByPosition(position);
        currentSwipeLayoutTarget.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_EDIT_ACCOUNT) {
/*
                boolean deleteAccount = data.getExtras().getBoolean(Constants.RESULT_DELETE_ACCOUNT);

                if(!deleteAccount) {
                    final Account accountReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_ACCOUNT));

                    Log.i("ZHAN", "----------- onActivityResult edit account ----------");
                    Log.d("ZHAN", "account name is " + accountReturned.getName());
                    Log.d("ZHAN", "account color is " + accountReturned.getColor());
                    Log.d("ZHAN", "account id is " + accountReturned.getId());
                    Log.i("ZHAN", "----------- onActivityResult edit account ----------");

                    accountList.set(accountIndexEdited, accountReturned);
                }else{
                    accountList.remove(accountIndexEdited);
                }

                accountRecyclerAdapter.setAccountList(accountList);
                updateAccountStatus();
                */

                //recalculate everything
                populateAccountWithNoInfo();
            }else if(requestCode == Constants.RETURN_NEW_ACCOUNT){
                final Account accountReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_ACCOUNT));

                Log.i("ZHAN", "----------- onActivityResult new account ----------");
                Log.d("ZHAN", "account name is "+accountReturned.getName());
                Log.d("ZHAN", "account color is "+accountReturned.getColor());
                Log.d("ZHAN", "account id is "+accountReturned.getId());
                Log.i("ZHAN", "----------- onActivityResult new account ----------");

                accountList.add(accountReturned);
                accountRecyclerAdapter.setAccountList(accountList);

                updateAccountStatus();

                //Scroll to the last position
                accountListView.scrollToPosition(accountRecyclerAdapter.getItemCount() - 1);
            }else if(requestCode == Constants.RETURN_HAS_CHANGED){
                boolean hasChanged = data.getExtras().getBoolean(Constants.CHANGED);

                if(hasChanged){
                    //If something has been changed, update the list
                    populateAccountWithInfo(false);
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
        closeSwipeItem(position);

        Intent viewAllTransactionsForAccount = new Intent(getContext(), TransactionsForAccount.class);
        viewAllTransactionsForAccount.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_GENERIC_MONTH, DateUtil.convertDateToString(getContext(), currentMonth));

        Parcelable wrapped = Parcels.wrap(accountList.get(position));
        viewAllTransactionsForAccount.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_ACCOUNT_ACCOUNT, wrapped);
        startActivityForResult(viewAllTransactionsForAccount, Constants.RETURN_HAS_CHANGED);
    }

    @Override
    public void onDeleteAccount(int position){
        confirmDelete(position);
    }

    @Override
    public void onEditAccount(int position){
        closeSwipeItem(position);

        accountIndexEdited = position;
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
                updateMonthInToolbar(-1, true);
                return true;
            case R.id.rightChevron:
                updateMonthInToolbar(1, true);
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
