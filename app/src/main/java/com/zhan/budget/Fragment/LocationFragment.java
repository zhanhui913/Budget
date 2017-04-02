package com.zhan.budget.Fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.LocationInfoActivity;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Activity.Transactions.TransactionsForLocation;
import com.zhan.budget.Adapter.LocationRecyclerAdapter;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Fragment.Chart.PieChartFragment;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
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

    private ViewGroup fullLayout;
    private ViewGroup emptyLayout;

    private RealmResults<Location> resultsLocation;
    private List<Location> locationList;
    private LocationRecyclerAdapter locationAdapter;
    private RecyclerView locationListview;

    private PieChartFragment pieChartFragment;

    private TextView centerPanelRightTextView, emptyLocationPrimaryText, emptyLocationSecondaryText;

    private LinearLayoutManager linearLayoutManager;
    private SwipeLayout currentSwipeLayoutTarget;

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

        //Center Panel left textview
        view.findViewById(R.id.dateTextView).setVisibility(View.GONE);
        centerPanelRightTextView = (TextView)view.findViewById(R.id.totalCostTextView);

        linearLayoutManager = new LinearLayoutManager(getActivity());

        locationListview = (RecyclerView) view.findViewById(R.id.locationListview);
        locationListview.setLayoutManager(linearLayoutManager);

        locationList = new ArrayList<>();
        locationAdapter = new LocationRecyclerAdapter(this, locationList);
        locationListview.setAdapter(locationAdapter);

        //Add divider
        locationListview.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        fullLayout = (ViewGroup)view.findViewById(R.id.fullPanelLayout);

        emptyLayout = (ViewGroup)view.findViewById(R.id.emptyAccountLayout);
        emptyLocationPrimaryText = (TextView) view.findViewById(R.id.emptyPrimaryText);
        emptyLocationPrimaryText.setText(getString(R.string.empty_location));
        emptyLocationSecondaryText = (TextView) view.findViewById(R.id.emptySecondaryText);
        emptyLocationSecondaryText.setText("Add one in the settings");

        //Setup pie chart
        pieChartFragment = PieChartFragment.newInstance(locationList, false, false, getString(R.string.location));
        getFragmentManager().beginTransaction().replace(R.id.chartContentFrame, pieChartFragment).commit();

        //0 represents no change in month relative to currentMonth variable.
        updateMonthInToolbar(0);
    }

    private void updateMonthInToolbar(int direction){
        locationListview.smoothScrollToPosition(0);

        //reset pie chart data & total # text view
        pieChartFragment.resetPieChart();
        updateAmountStatus(0); //reset it back to 0

        //reset location list view
        locationAdapter.setLocationList(new ArrayList<Location>());

        currentMonth = DateUtil.getMonthWithDirection(currentMonth, direction);
        mListener.updateToolbar(DateUtil.convertDateToStringFormat2(getContext(), currentMonth));

        getListOfTransactionsForMonth();
    }

    private void getListOfTransactionsForMonth(){
        final Date startMonth = DateUtil.refreshMonth(currentMonth);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = DateUtil.getLastDateOfMonth(currentMonth);

        RealmResults<Transaction> transactionRealmResults = myRealm.where(Transaction.class).between("date", startMonth, endMonth).equalTo("dayType", DayType.COMPLETED.toString()).findAllAsync();
        transactionRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);

                aggregateLocation(myRealm.copyFromRealm(element), true);
            }
        });
    }

    /**
     * Aggregate all transactions with the same location and update its pie chart.
     * @param tempList list of transactions
     * @param animate to animate pie chart or not
     */
    private void aggregateLocation(List<Transaction> tempList, boolean animate){
        HashMap<Location, Integer> locationHash = new HashMap<>();

        //Keep track of total locations count
        int totalLocationsCount = 0;

        for(int i = 0; i < tempList.size(); i++){
            if(tempList.get(i).getLocation() != null) {
                if (!locationHash.containsKey(tempList.get(i).getLocation())) {
                    locationHash.put(tempList.get(i).getLocation(), 1);
                } else {
                    locationHash.put(tempList.get(i).getLocation(), locationHash.get(tempList.get(i).getLocation()) + 1);
                }
                totalLocationsCount++;
            }
        }

        locationList = new ArrayList<>();
        for(Location key: locationHash.keySet()){
            key.setAmount(locationHash.get(key));
            locationList.add(key);
        }

        //Sort from highest to lowest first, then by name
        Collections.sort(locationList, new Comparator<Location>() {
            @Override
            public int compare(Location l1, Location l2) {
                //descending order
                int compare = (l2.getAmount() - l1.getAmount());
                if(compare != 0){
                    return compare;
                }
                //ascending order
                return (l1.getName().compareToIgnoreCase(l2.getName()));
            }
        });

        locationAdapter.setLocationList(locationList);

        //This gives pie chart new location list
        pieChartFragment.setData(locationList, animate);

        updateAmountStatus(totalLocationsCount);
        updateLocationStatus();
    }

    private void updateLocationStatus(){
        if(locationAdapter.getItemCount() > 0){
            emptyLayout.setVisibility(View.GONE);
            fullLayout.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
            fullLayout.setVisibility(View.GONE);
        }
    }

    private void openSwipeItem(int position){
        currentSwipeLayoutTarget = (SwipeLayout) linearLayoutManager.findViewByPosition(position);
        currentSwipeLayoutTarget.open();
    }

    private void closeSwipeItem(int position){
        currentSwipeLayoutTarget = (SwipeLayout) linearLayoutManager.findViewByPosition(position);
        currentSwipeLayoutTarget.close();
    }

    private void updateAmountStatus(int amount){
        if(amount == 0){
            centerPanelRightTextView.setText(R.string.na);
        }else{
            if(amount > 1){
                centerPanelRightTextView.setText(String.format(getString(R.string.location_times), amount));
            }else{
                centerPanelRightTextView.setText(String.format(getString(R.string.location_time), amount));
            }
        }
    }

    private void confirmDelete(final int position){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(R.string.warning_delete_location);

        new AlertDialog.Builder(getContext())
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteLocation(position);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        closeSwipeItem(position);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        closeSwipeItem(position);
                    }
                })
                .create()
                .show();
    }

    private void deleteLocation(final int position){
        resultsLocation = myRealm.where(Location.class).findAllAsync();
        resultsLocation.addChangeListener(new RealmChangeListener<RealmResults<Location>>() {
            @Override
            public void onChange(RealmResults<Location> element) {
                element.removeChangeListener(this);

                for(int i = 0; i < element.size(); i++) {
                    if (element.get(i).getName().equalsIgnoreCase(locationList.get(position).getName())) {
                        myRealm.beginTransaction();
                        element.deleteFromRealm(i);
                        myRealm.commitTransaction();
                        break;
                    }
                }

                getListOfTransactionsForMonth();
            }
        });

    }

    private void editLocation(int position){
        startActivityForResult(LocationInfoActivity.createIntentToEditLocation(getContext(), locationList.get(position)), RequestCodes.EDIT_LOCATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data.getExtras() != null) {
            if(requestCode == RequestCodes.HAS_TRANSACTION_CHANGED){
                boolean hasChanged = data.getExtras().getBoolean(TransactionInfoActivity.HAS_CHANGED);

                if(hasChanged){
                    //If something has been changed, update the list and the pie chart
                    getListOfTransactionsForMonth();

                }
                updateLocationStatus();
            }else if(requestCode == RequestCodes.EDIT_LOCATION){
                //Todo : handle location editing from here
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
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickLocation(int position){
        closeSwipeItem(position);
        startActivityForResult(TransactionsForLocation.createIntentToViewAllTransactionsForLocationForMonth(getContext(), locationList.get(position), currentMonth), RequestCodes.HAS_TRANSACTION_CHANGED);
    }

    @Override
    public void onDeleteLocation(int position){
        confirmDelete(position);
    }

    @Override
    public void onEditLocation(int position){
        closeSwipeItem(position);
        editLocation(position);
    }


    @Override
    public void onPullDownAllow(boolean value){
        //cannot pull down here
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
}
