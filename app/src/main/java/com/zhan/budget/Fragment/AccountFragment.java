package com.zhan.budget.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.zhan.budget.Model.Account;
import com.zhan.budget.Adapter.AccountListAdapter;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private View view;

    private SwipeMenuListView accountListView;
    private AccountListAdapter accountListAdapter;
    private List<Account> accountList;

    private RealmResults<Account> resultsAccount;

    private Realm myRealm;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccountFragment.
     */
    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        return fragment;
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
        addListener();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        accountList = new ArrayList<>();

        accountListView = (SwipeMenuListView) view.findViewById(R.id.accountListView);
        accountListAdapter = new AccountListAdapter(getActivity(), accountList);
        accountListView.setAdapter(accountListAdapter);

        resultsAccount = myRealm.where(Account.class).findAllAsync();
        resultsAccount.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                accountList = myRealm.copyFromRealm(resultsAccount);

                accountListAdapter.addAll(accountList);
                //accountListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addListener(){

    }

}
