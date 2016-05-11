package com.zhan.budget.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Adapter.AccountListAdapter;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.PlusView;

import java.util.ArrayList;
import java.util.List;

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

    private ListView accountListView;
    private AccountListAdapter accountListAdapter;

    private RealmResults<Account> resultsAccount;
    private List<Account> accountList;

    private Boolean isPulldownAllow = true;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_settings_account;
    }

    @Override
    protected void init(){
        super.init();

        createToolbar();

        accountList = new ArrayList<>();
        accountListView = (ListView) findViewById(R.id.accountListView);
        accountListAdapter = new AccountListAdapter(this, accountList, false);
        accountListView.setAdapter(accountListAdapter);

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
                element.removeChangeListener(this);

                Log.d(TAG, "there's a change in results account ");
                accountList = myRealm.copyFromRealm(element);
                accountListAdapter.updateList(accountList);

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

    private void editAccount(final int position){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.alertdialog_generic, null);

        final Account account = resultsAccount.get(position);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        title.setText("Edit Account");

        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);
        input.setText(account.getName());
        input.setHint("Account");

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myRealm.beginTransaction();
                        account.setName(input.getText().toString());
                        myRealm.copyToRealmOrUpdate(account);
                        myRealm.commitTransaction();

                        accountList.get(position).setName(input.getText().toString());
                        accountListAdapter.updateList(accountList);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void addAccount(){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.alertdialog_generic, null);

        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);
        input.setText("");
        input.setHint("Account");

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        title.setText("Add Account");

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myRealm.beginTransaction();
                        Account newAccount = myRealm.createObject(Account.class);
                        newAccount.setId(Util.generateUUID());
                        newAccount.setName(input.getText().toString());
                        myRealm.commitTransaction();

                        Account acc = myRealm.copyFromRealm(newAccount);
                        accountList.add(acc);
                        accountListAdapter.updateList(accountList);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void updateAccountStatus(){
        if(accountListAdapter.getCount() > 0){
            emptyLayout.setVisibility(View.GONE);
            accountListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            accountListView.setVisibility(View.GONE);
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
        resultsAccount.remove(position);
        myRealm.commitTransaction();

        // accountListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditAccount(int position){
        editAccount(position);
    }

    @Override
    public void onPullDownAllow(boolean value){
        isPulldownAllow = value;
    }

    @Override
    public void onAccountSetAsDefault(final Account account){
        /*myRealm.beginTransaction();

        for(int i = 0; i < accountList.size(); i++){
            if(account.getId().equalsIgnoreCase(accountList.get(i).getId())){
                accountList.get(i).setIsDefault(true);
            }else{
                accountList.get(i).setIsDefault(false);
            }
        }

        myRealm.commitTransaction();
        myRealm.close();

        //refresh lish
        accountListAdapter.notifyDataSetChanged();
        */




        final RealmResults<Account> accounts = myRealm.where(Account.class).findAllAsync();
        accounts.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                element.removeChangeListener(this);

                myRealm.beginTransaction();

                //Update in realm
                for(int i = 0; i < element.size(); i++){
                    if(account.getId().equalsIgnoreCase(element.get(i).getId())){
                        element.get(i).setIsDefault(true);
                    }else{
                        element.get(i).setIsDefault(false);
                    }
                }

                myRealm.commitTransaction();
                //myRealm.close();


                //Update in temp list
                for(int i = 0; i < accountList.size(); i++){
                    if(account.getId().equalsIgnoreCase(accountList.get(i).getId())){
                        accountList.get(i).setIsDefault(true);
                    }else{
                        accountList.get(i).setIsDefault(false);
                    }
                }

                accountListAdapter.notifyDataSetChanged();
            }
        });
    }
}
