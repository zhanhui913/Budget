package com.zhan.budget.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Adapter.AccountListAdapter;
import com.zhan.budget.Model.Account;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.PlusView;

import java.util.ArrayList;
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
public class AccountFragment extends Fragment implements
        AccountListAdapter.OnAccountAdapterInteractionListener{

    private View view;

    private ViewGroup emptyLayout;
    private PtrFrameLayout frame;
    private PlusView header;

    private TextView emptyAccountText;

    //private SwipeMenuListView accountListView;
    private ListView accountListView;
    private AccountListAdapter accountListAdapter;
    private List<Account> accountList;

    private SwipeLayout ssw;

    private RealmResults<Account> resultsAccount;

    private Realm myRealm;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
        createPullToAddAccount();
        addListener();

        populateAccount();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        accountList = new ArrayList<>();

        //accountListView = (SwipeMenuListView) view.findViewById(R.id.accountListView);
        accountListView = (ListView) view.findViewById(R.id.accountListView);
        accountListAdapter = new AccountListAdapter(this, accountList);
        accountListView.setAdapter(accountListAdapter);

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyAccountLayout);
        emptyAccountText = (TextView) view.findViewById(R.id.pullDownText);
        emptyAccountText.setText("Pull down to add an account");
    }

    private void populateAccount(){
        resultsAccount = myRealm.where(Account.class).findAllAsync();
        resultsAccount.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                accountList = myRealm.copyFromRealm(resultsAccount);

                updateAccountStatus();

                accountListAdapter.addAll(accountList);
            }
        });
    }

    private void createPullToAddAccount(){
        frame = (PtrFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);

        header = new PlusView(getContext());

        frame.setHeaderView(header);

        frame.setPtrHandler(enablePullDown);

        frame.addPtrUIHandler(new PtrUIHandler() {

            @Override
            public void onUIReset(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "onUIReset");
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "onUIRefreshPrepare");
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "onUIRefreshBegin");
                header.playRotateAnimation();
            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout frame) {
                Log.d("CALENDAR_FRAGMENT", "onUIRefreshComplete");
                addNewAccount();
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

            }
        });
    }

    PtrHandler enablePullDown = new PtrHandler() {
        @Override
        public void onRefreshBegin(PtrFrameLayout insideFrame) {
            Log.d("CALENDAR_FRAGMENT", "-- on refresh begin");
            insideFrame.postDelayed(new Runnable() {
                @Override
                public void run() {
                    frame.refreshComplete();
                }
            }, 500);
        }

        @Override
        public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
            return PtrDefaultHandler.checkContentCanBePulledDown(frame, accountListView, header);
        }
    };

    PtrHandler disablePullDown = new PtrHandler() {
        @Override
        public void onRefreshBegin(PtrFrameLayout insideFrame) {
        }

        @Override
        public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
            return false;
        }
    };

    /**
     * Displays prompt for user to add new account.
     */
    private void addNewAccount(){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.alertdialog_account_add, null);

        final EditText input = (EditText) promptView.findViewById(R.id.editTextAccount);

        new AlertDialog.Builder(getActivity())
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myRealm.beginTransaction();

                        Account account = myRealm.createObject(Account.class);
                        account.setId(Util.generateUUID());
                        account.setName(input.getText().toString());

                        accountListAdapter.clear();
                        myRealm.commitTransaction();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void updateAccountStatus(){
        if(accountList.size() > 0){
            emptyLayout.setVisibility(View.GONE);
            accountListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            accountListView.setVisibility(View.GONE);
        }
    }

    private void addListener(){

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(!myRealm.isClosed()) {
            myRealm.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteAccount(int position){
        myRealm.beginTransaction();
        resultsAccount.remove(position);
        myRealm.commitTransaction();

        accountListAdapter.clear();
        accountListAdapter.addAll(accountList);
    }

    @Override
    public void onEditAccount(int position){
        Toast.makeText(getContext(), "editting account "+accountList.get(position).getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisablePtrPullDown(boolean value){
        if(value){ //disable
            frame.setPtrHandler(disablePullDown);
        }else{ //enable
            frame.setPtrHandler(enablePullDown);
        }
    }
}
