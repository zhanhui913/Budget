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
import com.zhan.budget.Activity.BaseRealmActivity;
import com.zhan.budget.Activity.LocationInfoActivity;
import com.zhan.budget.Adapter.LocationRecyclerAdapter;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.R;
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
    private List<Location> locationList;

    private int locationIndexEdited;

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
        emptyLocationText.setText(R.string.pull_down_add_location);

        createPullToAddLocation();
        populateLocation();
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

    private void populateLocation(){
        resultsLocation = myRealm.where(Location.class).findAllSortedAsync("name");
        resultsLocation.addChangeListener(new RealmChangeListener<RealmResults<Location>>() {
            @Override
            public void onChange(RealmResults<Location> element) {
                element.removeChangeListener(this);

                locationList = myRealm.copyFromRealm(element);

                locationListAdapter = new LocationRecyclerAdapter(instance, locationList, false);
                locationListView.setAdapter(locationListAdapter);

                //Add divider
                locationListView.addItemDecoration(
                        new HorizontalDividerItemDecoration.Builder(instance)
                                .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                                .build());

                locationListAdapter.setLocationList(element);

                updateLocationStatus();
            }
        });
    }

    private void createPullToAddLocation(){
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
                return isPulldownAllow && PtrDefaultHandler.checkContentCanBePulledDown(frame, locationListView, header);
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
                        addLocation();
                    }
                }, 250);
            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

            }
        });
    }

    private void addLocation(){
        startActivityForResult(LocationInfoActivity.createIntentForNewLocation(getApplicationContext()), RequestCodes.NEW_LOCATION);
    }

    private void editLocation(int position){
        startActivityForResult(LocationInfoActivity.createIntentToEditLocation(getApplicationContext(), locationList.get(position)), RequestCodes.EDIT_LOCATION);
    }

    private void updateLocationStatus(){
        if(locationListAdapter.getItemCount() > 0){
            emptyLayout.setVisibility(View.GONE);
            locationListView.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            locationListView.setVisibility(View.GONE);
        }
    }

    private void confirmDelete(final int position){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(R.string.warning_delete_location);

        new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteLocation(position);
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

    private void deleteLocation(int position){
        myRealm.beginTransaction();
        resultsLocation.get(position).deleteFromRealm();
        myRealm.commitTransaction();

        populateLocation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if(requestCode == RequestCodes.EDIT_LOCATION) {

                boolean deleteLocation = data.getExtras().getBoolean(LocationInfoActivity.DELETE_LOCATION);

                if(!deleteLocation){
                    Location locationReturned = Parcels.unwrap(data.getExtras().getParcelable(LocationInfoActivity.RESULT_LOCATION));

                    Log.i(TAG, "----------- onActivityResult edit location ----------");
                    Log.d(TAG, "location name is "+locationReturned.getName());
                    Log.d(TAG, "location color is "+locationReturned.getColor());
                    Log.i(TAG, "----------- onActivityResult edit location ----------");

                    locationList.set(locationIndexEdited, locationReturned);
                }else{
                    locationList.remove(locationIndexEdited);
                }

                locationListAdapter.setLocationList(locationList);
                updateLocationStatus();
            }else if(requestCode == RequestCodes.NEW_LOCATION){
                Location locationReturned = Parcels.unwrap(data.getExtras().getParcelable(LocationInfoActivity.RESULT_LOCATION));

                Log.i(TAG, "----------- onActivityResult new location ----------");
                Log.d(TAG, "location name is "+locationReturned.getName());
                Log.d(TAG, "location color is "+locationReturned.getColor());
                Log.i(TAG, "----------- onActivityResult new location ----------");

                locationList.add(locationReturned);
                locationListAdapter.setLocationList(locationList);
                updateLocationStatus();

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
        locationIndexEdited = position;
        editLocation(position);
    }

    @Override
    public void onDeleteLocation(int position){
        confirmDelete(position);
    }

    @Override
    public void onEditLocation(int position){
        locationIndexEdited = position;
        editLocation(position);
    }

    @Override
    public void onPullDownAllow(boolean value){
        isPulldownAllow = value;
    }
}
