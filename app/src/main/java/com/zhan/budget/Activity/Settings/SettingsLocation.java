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
import com.zhan.budget.Activity.BaseRealmActivity;
import com.zhan.budget.Activity.LocationInfoActivity;
import com.zhan.budget.Adapter.LocationRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.R;
import com.zhan.budget.View.PlusView;

import org.parceler.Parcels;

import java.util.List;

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
        emptyLocationText.setText("Pull down to add a location");

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

    private void editLocation(int position){
        Intent editLocationIntent = new Intent(this, LocationInfoActivity.class);
        Parcelable wrapped = Parcels.wrap(locationList.get(position));
        editLocationIntent.putExtra(Constants.REQUEST_NEW_LOCATION, false);
        editLocationIntent.putExtra(Constants.REQUEST_EDIT_LOCATION, wrapped);
        startActivityForResult(editLocationIntent, Constants.RETURN_EDIT_LOCATION);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_EDIT_LOCATION) {

                boolean deleteLocation = data.getExtras().getBoolean(Constants.RESULT_DELETE_LOCATION);

                if(!deleteLocation){
                    Location locationReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_EDIT_LOCATION));

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
            }else if(requestCode == Constants.RETURN_NEW_LOCATION){
                Location locationReturned = Parcels.unwrap(data.getExtras().getParcelable(Constants.RESULT_NEW_LOCATION));

                Log.i(TAG, "----------- onActivityResult new location ----------");
                Log.d(TAG, "location name is "+locationReturned.getName());
                Log.d(TAG, "location color is "+locationReturned.getColor());
                Log.i(TAG, "----------- onActivityResult new location ----------");

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
}
