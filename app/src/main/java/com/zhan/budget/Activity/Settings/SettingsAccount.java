package com.zhan.budget.Activity.Settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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
import com.zhan.budget.Adapter.AccountRecyclerAdapter;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.PlusView;

import org.parceler.Parcels;

import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SettingsAccount extends BaseRealmActivity implements
        AccountRecyclerAdapter.OnAccountAdapterInteractionListener{

    private  Toolbar toolbar;

    private static final String TAG = "SettingsAccount";

    private ViewGroup emptyLayout;
    private PtrFrameLayout frame;
    private PlusView header;

    private TextView emptyAccountText;

    private RecyclerView accountListView;
    private AccountRecyclerAdapter accountRecyclerAdapter;

    private RealmResults<Account> resultsAccount;
    private List<Account> accountList;

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

        accountListView = (RecyclerView)findViewById(R.id.accountListView);
        accountListView.setLayoutManager(new LinearLayoutManager(this));

        emptyLayout = (ViewGroup)findViewById(R.id.emptyAccountLayout);
        emptyAccountText = (TextView) findViewById(R.id.pullDownText);
        emptyAccountText.setText(getString(R.string.pull_down_add_account));

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

                accountList = myRealm.copyFromRealm(element);

                accountRecyclerAdapter = new AccountRecyclerAdapter(instance, accountList, false, true);
                accountListView.setAdapter(accountRecyclerAdapter);

                //Add divider
                accountListView.addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(instance)
                                .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                                .build());

                accountRecyclerAdapter.setAccountList(accountList);

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
        startActivityForResult(AccountInfoActivity.createIntentForNewAccount(getApplicationContext()), RequestCodes.NEW_ACCOUNT);
    }

    private void editAccount(int position){
        startActivityForResult(AccountInfoActivity.createIntentToEditAccount(getApplicationContext(), accountList.get(position)), RequestCodes.EDIT_ACCOUNT);
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

    private void confirmDelete(final int position){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(R.string.warning_delete_account);

        new AlertDialog.Builder(instance)
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
        populateAccount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if(requestCode == RequestCodes.EDIT_ACCOUNT) {

                boolean deleteAccount = data.getExtras().getBoolean(AccountInfoActivity.DELETE_ACCOUNT);

                if(!deleteAccount){
                    Account accountReturned = Parcels.unwrap(data.getExtras().getParcelable(AccountInfoActivity.RESULT_ACCOUNT));

                    Log.i(TAG, "----------- onActivityResult edit account ----------");
                    Log.d(TAG, "account name is "+accountReturned.getName());
                    Log.d(TAG, "account color is "+accountReturned.getColor());
                    Log.d(TAG, "account id is "+accountReturned.getId());
                    Log.i(TAG, "----------- onActivityResult edit account ----------");

                    accountList.set(accountIndexEdited, accountReturned);
                }else{
                    accountList.remove(accountIndexEdited);
                }

                accountRecyclerAdapter.setAccountList(accountList);
                updateAccountStatus();
            }else if(requestCode == RequestCodes.NEW_ACCOUNT){
                Account accountReturned = Parcels.unwrap(data.getExtras().getParcelable(AccountInfoActivity.RESULT_ACCOUNT));
                Log.i(TAG, "----------- onActivityResult new account ----------");
                Log.d(TAG, "account name is "+accountReturned.getName());
                Log.d(TAG, "account color is "+accountReturned.getColor());
                Log.d(TAG, "account id is "+accountReturned.getId());
                Log.i(TAG, "----------- onActivityResult new account ----------");

                accountList.add(accountReturned);
                accountRecyclerAdapter.setAccountList(accountList);
                updateAccountStatus();

                //Scroll to the last position
                accountListView.scrollToPosition(accountRecyclerAdapter.getItemCount() - 1);
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
        accountIndexEdited = position;
        editAccount(position);
    }

    @Override
    public void onDeleteAccount(int position){
        confirmDelete(position);
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
    public void onAccountSetAsDefault(int position){
        myRealm.beginTransaction();
        for(int i = 0; i < resultsAccount.size(); i++){
            resultsAccount.get(i).setIsDefault(false);
        }
        resultsAccount.get(position).setIsDefault(true);

        myRealm.commitTransaction();

        //Using any subclass of view to get parent view (cannot use root view as it will appear on (devices with navigation panel) the bottom
        Util.createSnackbar(this, (View)emptyAccountText.getParent(), "Set "+resultsAccount.get(position).getName()+" as default account");

        accountList = myRealm.copyFromRealm(resultsAccount);
        accountRecyclerAdapter.setAccountList(accountList);
    }

    @Override
    public void onAccountDeSetFromDefault(int position){
        myRealm.beginTransaction();
/*        for(int i = 0; i < resultsAccount.size(); i++){
            resultsAccount.get(i).setIsDefault(false);
        }
        resultsAccount.get(position).setIsDefault(true);
*/
        resultsAccount.get(position).setIsDefault(false);



        myRealm.commitTransaction();

        //Using any subclass of view to get parent view (cannot use root view as it will appear on (devices with navigation panel) the bottom
        Util.createSnackbar(this, (View)emptyAccountText.getParent(), "Remove "+resultsAccount.get(position).getName()+" as default account");

        accountList = myRealm.copyFromRealm(resultsAccount);
        accountRecyclerAdapter.setAccountList(accountList);
    }
}
