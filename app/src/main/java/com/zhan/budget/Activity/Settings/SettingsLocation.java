package com.zhan.budget.Activity.Settings;

import android.app.Activity;
import android.content.Intent;
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
import com.zhan.budget.Adapter.LocationRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.R;
import com.zhan.budget.View.PlusView;

import org.parceler.Parcels;

import in.srain.cube.views.ptr.PtrFrameLayout;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SettingsLocation extends BaseRealmActivity implements
        LocationRecyclerAdapter.OnLocationAdapterInteractionListener{

    private  Toolbar toolbar;

    private static final String TAG = "SettingsLocation";

    private ViewGroup emptyLayout;
    private PtrFrameLayout frame;
    private PlusView header;

    private TextView emptyLocationText;

    private RecyclerView locationListView;
    private LocationRecyclerAdapter locationListAdapter;

    private RealmResults<Location> resultsLocation;

    private Boolean isPulldownAllow = true;

    private Activity instance;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_settings_location;
    }

    @Override
    protected void init(){
        super.init();

        instance = this;

        createToolbar();

        locationListView = (RecyclerView)findViewById(R.id.locationListView);
        locationListView.setLayoutManager(new LinearLayoutManager(this));

        emptyLayout = (ViewGroup)findViewById(R.id.emptyAccountLayout);
        emptyLocationText = (TextView) findViewById(R.id.pullDownText);
        emptyLocationText.setText("Pull down to add a location");

        //createPullToAddAccount();
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
        resultsLocation = myRealm.where(Location.class).findAllSortedAsync("name");
        resultsLocation.addChangeListener(new RealmChangeListener<RealmResults<Location>>() {
            @Override
            public void onChange(RealmResults<Location> element) {
                element.removeChangeListener(this);

                Log.d(TAG, "there's a change in results account ");

                locationListAdapter = new LocationRecyclerAdapter(instance, resultsLocation, false);
                locationListView.setAdapter(locationListAdapter);

                //Add divider
                locationListView.addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(instance)
                                .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                                .build());

                locationListAdapter.setLocationList(element);

                updateAccountStatus();
            }
        });
    }
/*
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
*/
    private void addAccount(){
        Intent addAccountIntent = new Intent(this, AccountInfoActivity.class);
        addAccountIntent.putExtra(Constants.REQUEST_NEW_ACCOUNT, true);
        startActivityForResult(addAccountIntent, Constants.RETURN_NEW_ACCOUNT);
    }

    private void editAccount(int position){
        Log.d("ACCOUNT_INFO", "trying to edit location at pos : "+position);
        Log.d("ACCOUNT_INFO", "location name : " +resultsLocation.get(position).getName());
        Log.d("ACCOUNT_INFO", "location color : " +resultsLocation.get(position).getColor());

        /*
        Intent editAccountIntent = new Intent(this, AccountInfoActivity.class);
        Parcelable wrapped = Parcels.wrap(locationListAdapter.getLocationList().get(position));
        editAccountIntent.putExtra(Constants.REQUEST_NEW_ACCOUNT, false);
        editAccountIntent.putExtra(Constants.REQUEST_EDIT_ACCOUNT, wrapped);
        startActivityForResult(editAccountIntent, Constants.RETURN_EDIT_ACCOUNT);
        */
    }

    private void updateAccountStatus(){
        if(locationListAdapter.getItemCount() > 0){
            emptyLayout.setVisibility(View.GONE);
            locationListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            locationListView.setVisibility(View.GONE);
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
                locationListView.scrollToPosition(locationListAdapter.getItemCount() - 1);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickLocation(int position){

    }


}
