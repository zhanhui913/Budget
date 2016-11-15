package com.zhan.budget.Fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.Transactions.TransactionsForLocation;
import com.zhan.budget.Adapter.LocationRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends BaseRealmFragment
        implements LocationRecyclerAdapter.OnLocationAdapterInteractionListener{
    private static final String TAG = "LocationFragment";

    private OnLocationInteractionListener mListener;
    private Date currentMonth;

    private List<Location> locationList;
    private LocationRecyclerAdapter locationAdapter;
    private RecyclerView locationListview;

    private PieChartFragment pieChartFragment;

    private TextView centerPanelLeftTextView, centerPanelRightTextView;

    public LocationFragment() {
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
        return R.layout.fragment_location;
    }

    @Override
    protected void init() {Log.d(TAG, "init");
        super.init();
        currentMonth = DateUtil.refreshMonth(new Date());

        centerPanelLeftTextView = (TextView)view.findViewById(R.id.dateTextView);
        centerPanelRightTextView = (TextView)view.findViewById(R.id.totalCostTextView);

        locationListview = (RecyclerView) view.findViewById(R.id.locationListview);
        locationListview.setLayoutManager(new LinearLayoutManager(getActivity()));

        locationList = new ArrayList<>();
        locationAdapter = new LocationRecyclerAdapter(this, locationList);
        locationListview.setAdapter(locationAdapter);

        //Add divider
        locationListview.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        //Setup pie chart
        pieChartFragment = PieChartFragment.newInstance(locationList);
        getFragmentManager().beginTransaction().replace(R.id.chartContentFrame, pieChartFragment).commit();

        //0 represents no change in month relative to currentMonth variable.
        updateMonthInToolbar(0);
    }

    private void updateMonthInToolbar(int direction){
        locationListview.smoothScrollToPosition(0);

        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(currentMonth));

        centerPanelLeftTextView.setText(DateUtil.convertDateToStringFormat2(currentMonth));

        fetchNewLocationData(currentMonth, true);
    }

    /**
     * Get new location data that is in the current month
     * @param month Current month to target
     * @param animate whether or not to animate the pie chart
     */
    private void fetchNewLocationData(Date month, final boolean animate){
        Date endMonth = DateUtil.getLastDateOfMonth(month);

        RealmResults<Transaction> transactionRealmResults = myRealm.where(Transaction.class).between("date", month, endMonth).equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync();
        transactionRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                countLocationList(myRealm.copyFromRealm(element), animate);
                /*if(isNew){
                    countLocationList(myRealm.copyFromRealm(element));
                }else{
                    updateLocationList(myRealm.copyFromRealm(element));
                }*/
            }
        });
    }

    /**
     * Aggregate all transactions with the same location and update its pie chart.
     * @param ttList list of transactions
     */
    private void countLocationList(List<Transaction> ttList, boolean animate){
        HashMap<Location, Integer> locationHash = new HashMap<>();

        for(int i = 0; i < ttList.size(); i++){
            if(ttList.get(i).getLocation() != null) {
                if (!locationHash.containsKey(ttList.get(i).getLocation())) {
                    locationHash.put(ttList.get(i).getLocation(), 1);
                } else {
                    locationHash.put(ttList.get(i).getLocation(), locationHash.get(ttList.get(i).getLocation()) + 1);
                }
            }
        }

        locationList.clear();

        //Keep track of total locations count
        int totalLocationsCount = 0;

        //Go through each hashmap
        for(Location key: locationHash.keySet()){
            Location location2 = new Location();
            location2.setName(key.getName());
            location2.setAmount(locationHash.get(key));
            location2.setColor(key.getColor());

            totalLocationsCount += locationHash.get(key);
            locationList.add(location2);
        }

        //Sort from highest to lowest
        Collections.sort(locationList, new Comparator<Location>() {
            @Override
            public int compare(Location l1, Location l2) {
                //descending order
                return (l2.getAmount() - l1.getAmount());
            }
        });

        locationAdapter.setLocationList(locationList);

        //This gives pie chart new location list
        pieChartFragment.setData(locationList, animate);

        if(totalLocationsCount == 0){
            centerPanelRightTextView.setText("NA");
        }else{
            String appendString = (totalLocationsCount > 1) ? " times" : " time" ;
            centerPanelRightTextView.setText(totalLocationsCount + appendString);
        }
    }

    /**
     * Updates the location count in the hashmap while keeping the colors the same
     * @param ttList list of transactions
     */
    private void updateLocationList(List<Transaction> ttList){
        HashMap<Location, Integer> locationHash = new HashMap<>();

        for(int i = 0; i < ttList.size(); i++){
            if(ttList.get(i).getLocation() != null) {
                if (!locationHash.containsKey(ttList.get(i).getLocation())) {
                    locationHash.put(ttList.get(i).getLocation(), 1);
                } else {
                    locationHash.put(ttList.get(i).getLocation(), locationHash.get(ttList.get(i).getLocation()) + 1);
                }
            }
        }

        locationList.clear();

        //Keep track of total locations count
        int totalLocationsCount = 0;

        //Go through each hashmap
        for(Location key: locationHash.keySet()){
            Location location2 = new Location();
            location2.setName(key.getName());
            location2.setAmount(locationHash.get(key));
            location2.setColor(key.getColor());

            totalLocationsCount += locationHash.get(key);
            locationList.add(location2);
        }

        //Sort from highest to lowest
        Collections.sort(locationList, new Comparator<Location>() {
            @Override
            public int compare(Location l1, Location l2) {
                //descending order
                return (l2.getAmount() - l1.getAmount());
            }
        });

        locationAdapter.setLocationList(locationList);

        //This updates pie chart with new location list while keeping the colors the same
        pieChartFragment.setData(locationList);

        String appendString = (totalLocationsCount > 0) ? " times" : " time" ;
        centerPanelRightTextView.setText(totalLocationsCount + appendString);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data.getExtras() != null) {
            if(requestCode == Constants.RETURN_HAS_CHANGED){
                boolean hasChanged = data.getExtras().getBoolean(Constants.CHANGED);

                if(hasChanged){
                    //If something has been changed, update the list and the pie chart
                    fetchNewLocationData(currentMonth, false);
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
        if (context instanceof OnLocationInteractionListener) {
            mListener = (OnLocationInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLocationInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                updateMonthInToolbar(-1);
                return true;
            case R.id.rightChevron:
                updateMonthInToolbar(1);
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
    public interface OnLocationInteractionListener {
        void updateToolbar(String date);
    }

    @Override
    public void onClickLocation(int index){
        Intent viewAllTransactionsForLocation = new Intent(getContext(), TransactionsForLocation.class);
        viewAllTransactionsForLocation.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_GENERIC_MONTH, DateUtil.convertDateToString(currentMonth));
        //viewAllTransactionsForLocation.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_LOCATION_LOCATION, locationList.get(index).getName());

        Parcelable wrapped = Parcels.wrap(locationList.get(index));
        viewAllTransactionsForLocation.putExtra(Constants.REQUEST_ALL_TRANSACTION_FOR_LOCATION_LOCATION, wrapped);

        startActivityForResult(viewAllTransactionsForLocation, Constants.RETURN_HAS_CHANGED);
    }
}
