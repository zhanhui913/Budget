package com.zhan.budget.Activity.Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.AccountInfoActivity;
import com.zhan.budget.Activity.BaseRealmActivity;
import com.zhan.budget.Adapter.AccountListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.R;
import com.zhan.budget.View.PlusView;

import org.parceler.Parcels;


import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SettingsAccount extends BaseRealmActivity implements
        AccountListAdapter.OnAccountAdapterInteractionListener{

    private  Toolbar toolbar;

    private static final String TAG = "SettingsAccount";

    private ViewGroup emptyLayout;
    private PtrFrameLayout frame;
    private PlusView header;

    private TextView emptyAccountText;

    private RecyclerView accountListView;
    private AccountListAdapter accountListAdapter;

    private RealmResults<Account> resultsAccount;
    //private List<Account> accountList;

    private Boolean isPulldownAllow = true;

    private int accountIndexEdited;//The index of the account that the user just finished edited.

    private Activity instance;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_settings_account;
    }

    @Override
    protected void init(){
        super.init();

        instance = this;

        createToolbar();

        //accountList = new ArrayList<>();
        accountListView = (RecyclerView)findViewById(R.id.accountListView);
        accountListView.setLayoutManager(new LinearLayoutManager(this));
/*
        accountListAdapter = new AccountListAdapter(this, accountList, false, true);
        accountListView.setAdapter(accountListAdapter);

        //Add divider
        accountListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());
*/

        emptyLayout = (ViewGroup)findViewById(R.id.emptyAccountLayout);
        emptyAccountText = (TextView) findViewById(R.id.pullDownText);
        emptyAccountText.setText("Pull down to add an account");

        createPullToAddAccount();
        populateAccount();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void populateAccount(){
        resultsAccount = myRealm.where(Account.class).findAllAsync();
        resultsAccount.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                //element.removeChangeListener(this);

                Log.d(TAG, "there's a change in results account ");
                //accountList = myRealm.copyFromRealm(element);
                //accountListAdapter.setAccountList(accountList);

                accountListAdapter = new AccountListAdapter(instance, resultsAccount, false, true);
                accountListView.setAdapter(accountListAdapter);

                //Add divider
                accountListView.addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(instance)
                                .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                                .build());

                accountListAdapter.setAccountList(element);

                updateAccountStatus();
            }
        });
    }

    private void createPullToAddAccount(){
        frame = (PtrFrameLayout) findViewById(R.id.rotate_header_list_view_frame);

        header = new PlusView(this);

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
        Intent addAccountIntent = new Intent(this, AccountInfoActivity.class);
        addAccountIntent.putExtra(Constants.REQUEST_NEW_ACCOUNT, true);
        startActivityForResult(addAccountIntent, Constants.RETURN_NEW_ACCOUNT);
    }

    private void editAccount(int position){
        Log.d("ACCOUNT_INFO", "trying to edit account at pos : "+position);
        Log.d("ACCOUNT_INFO", "accoutn name : " +resultsAccount.get(position).getName());
        Log.d("ACCOUNT_INFO", "accoutn id : " +resultsAccount.get(position).getId());
        Log.d("ACCOUNT_INFO", "accoutn color : " +resultsAccount.get(position).getColor());


        Intent editAccountIntent = new Intent(this, AccountInfoActivity.class);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_EDIT_ACCOUNT) {
                 Account accountReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_ACCOUNT));

                Log.i("ZHAN", "----------- onActivityResult edit account ----------");
                Log.d("ZHAN", "account name is "+accountReturned.getName());
                Log.d("ZHAN", "account color is "+accountReturned.getColor());
                Log.d("ZHAN", "account id is "+accountReturned.getId());
                Log.i("ZHAN", "----------- onActivityResult edit account ----------");

                //accountList.set(accountIndexEdited, accountReturned);
                //accountListAdapter.setAccountList(accountList);
                //updateAccountStatus();
            }else if(requestCode == Constants.RETURN_NEW_ACCOUNT){
                Account accountReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_ACCOUNT));


                Log.i("ZHAN", "----------- onActivityResult new account ----------");
                Log.d("ZHAN", "account name is "+accountReturned.getName());
                Log.d("ZHAN", "account color is "+accountReturned.getColor());
                Log.d("ZHAN", "account id is "+accountReturned.getId());
                Log.i("ZHAN", "----------- onActivityResult new account ----------");

                //accountList.add(accountReturned);
                //accountListAdapter.setAccountList(accountList);
                //updateAccountStatus();

                //Scroll to the last position
                accountListView.scrollToPosition(accountListAdapter.getItemCount() - 1);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickAccount(int position){

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
    public void onAccountSetAsDefault(String accountID){
        //final RealmResults<Account> accounts = myRealm.where(Account.class).findAllAsync();
/*
        resultsAccount = myRealm.where(Account.class).findAllAsync();
        resultsAccount.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
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

                //Update in temp list
                for(int i = 0; i < accountList.size(); i++){
                    if(accountID.equalsIgnoreCase(accountList.get(i).getId())){
                        accountList.get(i).setIsDefault(true);
                    }else{
                        accountList.get(i).setIsDefault(false);
                    }
                }

                accountListAdapter.notifyDataSetChanged();
            }
        });*/

        myRealm.beginTransaction();
        for(int a = 0; a < resultsAccount.size(); a++){
            if(resultsAccount.get(a).getId().equalsIgnoreCase(accountID)){
                resultsAccount.get(a).setIsDefault(true);
            }else{
                resultsAccount.get(a).setIsDefault(false);
            }
        }
        myRealm.commitTransaction();
        //accountListAdapter.notifyDataSetChanged();
    }
}
