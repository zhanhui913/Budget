package com.zhan.budget.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
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
import com.zhan.budget.Activity.Transactions.TransactionsForAccount;
import com.zhan.budget.Adapter.AccountListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.View.PlusView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends BaseRealmFragment implements
        AccountListAdapter.OnAccountAdapterInteractionListener{

    private static final String TAG = "AccountFragment";

    private OnAccountInteractionListener mListener;

    private ViewGroup emptyLayout;
    private PtrFrameLayout frame;
    private PlusView header;

    private TextView centerPanelLeftTextView, centerPanelRightTextView, emptyAccountText;

    private RecyclerView accountListView;
    private AccountListAdapter accountListAdapter;

    private RealmResults<Account> resultsAccount;
    private List<Account> accountList;

    private PieChartFragment pieChartFragment;

    private Boolean isPulldownAllow = true;
    private Date currentMonth;
    private RealmResults<Transaction> resultsTransaction;
    private List<Transaction> transactionMonthList;

    private int accountIndexEdited;//The index of the account that the user just finished edited.

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

        accountListView = (RecyclerView)view.findViewById(R.id.accountListView);
        accountListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        accountListAdapter = new AccountListAdapter(this, accountList, true, false);
        accountListView.setAdapter(accountListAdapter);

        //Add divider
        accountListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyAccountLayout);
        emptyAccountText = (TextView) view.findViewById(R.id.pullDownText);
        emptyAccountText.setText("Pull down to add an account");

        //Setup pie chart
        pieChartFragment = PieChartFragment.newInstance(accountList);
        getFragmentManager().beginTransaction().replace(R.id.chartContentFrame, pieChartFragment).commit();

        //createPullToAddAccount();
        populateAccountWithNoInfo();

        //0 represents no change in month relative to currentMonth variable.
        //false because we dont need to get all transactions yet.
        //This may conflict with populateAccountWithNoInfo async where its trying to get the initial
        //accounts
        updateMonthInToolbar(0, false);
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
                accountListAdapter.setAccountList(accountList);

                updateAccountStatus();
                populateAccountWithInfo(true);
            }
        });
    }

    /**
     * Resets data for all accounts and start new calculation.
     */
    private void populateAccountWithInfo(final boolean isNew){
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

                aggregateAccountInfo(isNew);
            }
        });
    }

    private void aggregateAccountInfo(final boolean isNew){
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
                        if(transactionMonthList.get(t).getAccount().getId().equalsIgnoreCase(accountList.get(c).getId())){
                            float transactionPrice = transactionMonthList.get(t).getPrice();
                            float currentAccountPrice = accountList.get(c).getCost();
                            accountList.get(c).setCost(transactionPrice + currentAccountPrice);
                            totalCost += transactionPrice;
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

                accountListAdapter.setAccountList(accountList);

                if(isNew){
                    pieChartFragment.setData(accountList);
                }else{
                    pieChartFragment.updateData(accountList);
                }

                centerPanelRightTextView.setText(CurrencyTextFormatter.formatFloat(result, Constants.BUDGET_LOCALE));

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

    //Dont allow pull down in this fragment
    private void createPullToAddAccount(){
        frame = (PtrFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);

        header = new PlusView(getContext());

        frame.setHeaderView(header);

        frame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout insideFrame) {
                if (isPulldownAllow) {
                    insideFrame.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            frame.refreshComplete();
                        }
                    }, 500);
                }
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return isPulldownAllow && PtrDefaultHandler.checkContentCanBePulledDown(frame, accountListView, header);
            }
        });

        frame.addPtrUIHandler(new PtrUIHandler() {

            @Override
            public void onUIReset(PtrFrameLayout frame) {
                Log.d(TAG, "onUIReset");
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout frame) {
                Log.d(TAG, "onUIRefreshPrepare");
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout frame) {
                Log.d(TAG, "onUIRefreshBegin");
                header.playRotateAnimation();
            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout frame) {
                Log.d(TAG, "onUIRefreshComplete");
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addAccount();
                    }
                }, 250);
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

            }
        });
    }

    private void addAccount(){
        Intent addAccountIntent = new Intent(getContext(), AccountInfoActivity.class);
        addAccountIntent.putExtra(Constants.REQUEST_NEW_ACCOUNT, true);
        startActivityForResult(addAccountIntent, Constants.RETURN_NEW_ACCOUNT);
    }

    private void editAccount(int position){
        Log.d("ACCOUNT_INFO", "trying to edit account at pos : "+position);
        Log.d("ACCOUNT_INFO", "accoutn name : " +accountList.get(position).getName());
        Log.d("ACCOUNT_INFO", "accoutn id : " +accountList.get(position).getId());
        Log.d("ACCOUNT_INFO", "accoutn color : " +accountList.get(position).getColor());



        Intent editAccountIntent = new Intent(getContext(), AccountInfoActivity.class);
        Parcelable wrapped = Parcels.wrap(accountListAdapter.getAccountList().get(position));
        editAccountIntent.putExtra(Constants.REQUEST_NEW_ACCOUNT, false);
        editAccountIntent.putExtra(Constants.REQUEST_EDIT_ACCOUNT, wrapped);
        startActivityForResult(editAccountIntent, Constants.RETURN_EDIT_ACCOUNT);
    }

    private void updateAccountStatus(){
        if(accountListAdapter.getItemCount() > 0){
            emptyLayout.setVisibility(View.GONE);
            accountListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            accountListView.setVisibility(View.GONE);
        }
    }

    private void updateMonthInToolbar(int direction, boolean updateAccountInfo){
        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(currentMonth));

        centerPanelLeftTextView.setText(DateUtil.convertDateToStringFormat2(currentMonth));

        if(updateAccountInfo) {
            populateAccountWithInfo(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_EDIT_ACCOUNT) {
                final Account accountReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_ACCOUNT));

                Log.i("ZHAN", "----------- onActivityResult edit account ----------");
                Log.d("ZHAN", "account name is "+accountReturned.getName());
                Log.d("ZHAN", "account color is "+accountReturned.getColor());
                Log.d("ZHAN", "account id is "+accountReturned.getId());
                Log.i("ZHAN", "----------- onActivityResult edit account ----------");

                accountList.set(accountIndexEdited, accountReturned);
                accountListAdapter.setAccountList(accountList);

                updateAccountStatus();
            }else if(requestCode == Constants.RETURN_NEW_ACCOUNT){
                final Account accountReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_ACCOUNT));

                Log.i("ZHAN", "----------- onActivityResult new account ----------");
                Log.d("ZHAN", "account name is "+accountReturned.getName());
                Log.d("ZHAN", "account color is "+accountReturned.getColor());
                Log.d("ZHAN", "account id is "+accountReturned.getId());
                Log.i("ZHAN", "----------- onActivityResult new account ----------");

                accountList.add(accountReturned);
                accountListAdapter.setAccountList(accountList);

                updateAccountStatus();

                //Scroll to the last position
                accountListView.scrollToPosition(accountListAdapter.getItemCount() - 1);
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
        Intent viewAllTransactionsForAccount = new Intent(getContext(), TransactionsForAccount.class);
        viewAllTransactionsForAccount.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_GENERIC_MONTH, DateUtil.convertDateToString(currentMonth));

        Parcelable wrapped = Parcels.wrap(accountList.get(position));
        viewAllTransactionsForAccount.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_ACCOUNT_ACCOUNT, wrapped);
        startActivityForResult(viewAllTransactionsForAccount, Constants.RETURN_HAS_CHANGED);
    }

    @Override
    public void onDeleteAccount(int position){
        myRealm.beginTransaction();
        resultsAccount.get(position).deleteFromRealm();
        myRealm.commitTransaction();

       // accountListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditAccount(int position){
        accountIndexEdited = position;
        editAccount(position);
    }

    @Override
    public void onPullDownAllow(boolean value){
        isPulldownAllow = value;
    }

    @Override
    public void onAccountSetAsDefault(final String accountID){
       /*
        final RealmResults<Account> accounts = myRealm.where(Account.class).findAllAsync();
        accounts.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                element.removeChangeListener(this);

                myRealm.beginTransaction();

                //Update in realm
                for(int i = 0; i < element.size(); i++){
                    if(accountID.equalsIgnoreCase(element.get(i).getId())){
                        element.get(i).setIsDefault(true);
                    }else{
                        element.get(i).setIsDefault(false);
                    }
                }

                myRealm.commitTransaction();

                //update in temp list
                for(int i = 0; i < accountList.size(); i++){
                    if(accountID.equalsIgnoreCase(accountList.get(i).getId())){
                        accountList.get(i).setIsDefault(true);
                    }else{
                        accountList.get(i).setIsDefault(false);
                    }
                }

                accountListAdapter.notifyDataSetChanged();
            }
        });
        */
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
