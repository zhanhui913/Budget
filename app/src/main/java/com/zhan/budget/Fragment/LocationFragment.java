package com.zhan.budget.Fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.LocationRecyclerAdapter;
import com.zhan.budget.Model.Location;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

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
    private LocationRecyclerAdapter listAdapter;


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
    protected void init() {
        Log.d(TAG, "init");
        super.init();

        currentMonth = DateUtil.refreshMonth(new Date());

        locationListview = (RecyclerView) view.findViewById(R.id.locationListview);
        locationListview.setLayoutManager(new LinearLayoutManager(getActivity()));

        locationList = new ArrayList<>();
        locationAdapter = new LocationRecyclerAdapter(this, locationList);
        locationListview.setAdapter(locationAdapter);

        //Add divider
        locationListview.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.right_padding_divider, R.dimen.right_padding_divider)
                        .build());

        //0 represents no change in month relative to currentMonth variable.
        updateMonthInToolbar(0);
    }

    private void countLocationList(List<Transaction> ttList){
        HashMap<String, Integer> locationHash = new HashMap<>();

        for(int i = 0; i < ttList.size(); i++){
            if(!locationHash.containsKey(ttList.get(i).getLocation())){
                locationHash.put(ttList.get(i).getLocation(), 1);
            }else{
                locationHash.put(ttList.get(i).getLocation(), locationHash.get(ttList.get(i).getLocation()) + 1);
            }
        }

        locationList.clear();

        //Go through each hashmap
        for(String key: locationHash.keySet()){
            Location location = new Location();
            location.setName(key);
            location.setAmount(locationHash.get(key));

            locationList.add(location);
        }

        //Sort from highest to lowest
        Collections.sort(locationList, new Comparator<Location>() {
            @Override
            public int compare(Location l1, Location l2) {
                int amount1 = l1.getAmount();
                int amount2 = l2.getAmount();

                //descending order
                return (amount2 - amount1);
            }
        });

        locationAdapter.setLocationList(locationList);
    }

    private void updateMonthInToolbar(int direction){
        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(currentMonth));

        fetchNewLocationData(currentMonth);
    }

    private void fetchNewLocationData(Date month){
        Date endMonth = DateUtil.getLastDateOfMonth(month);

        RealmResults<Transaction> transactionRealmResults = myRealm.where(Transaction.class).between("date", month, endMonth).findAllAsync();
        transactionRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                countLocationList(myRealm.copyFromRealm(element));
            }
        });
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
    public void onClickTransaction(int index){
        Toast.makeText(getContext(), "click on location :"+index, Toast.LENGTH_SHORT).show();
    }
}
