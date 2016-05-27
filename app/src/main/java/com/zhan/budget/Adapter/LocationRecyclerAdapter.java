package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhan.budget.Model.Location;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

public class LocationRecyclerAdapter extends RecyclerView.Adapter<LocationRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Location> locationList;
    private OnLocationAdapterInteractionListener mListener;

    public LocationRecyclerAdapter(Fragment fragment, List<Location> locationList) {
        this.context = fragment.getContext();
        this.locationList = locationList;

        //Any activity or fragment that uses this adapter needs to implement the OnLocationAdapterInteractionListener interface
        if (fragment instanceof OnLocationAdapterInteractionListener) {
            mListener = (OnLocationAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnLocationAdapterInteractionListener.");
        }
    }

    public LocationRecyclerAdapter(Activity activity, List<Location> locationList){
        this.context = activity;
        this.locationList = locationList;

        //Any activity or fragment that uses this adapter needs to implement the OnLocationAdapterInteractionListener interface
        if(activity instanceof  OnLocationAdapterInteractionListener){
            mListener = (OnLocationAdapterInteractionListener) activity;
        }else {
            throw new RuntimeException(activity.toString() + " must implement OnLocationAdapterInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting Location data for the row
        Location location = locationList.get(position);

        viewHolder.name.setText(location.getName());
        viewHolder.amount.setText(location.getAmount()+ ((location.getAmount() > 1)? " times":" time"));
        viewHolder.icon.setCircleColor(location.getColor());
        viewHolder.icon.setText(""+ Util.getFirstCharacterFromString(location.getName().toUpperCase()));
        viewHolder.icon.setTextColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));
        viewHolder.icon.setTextSizeInDP(30);
    }

    @Override
    public int getItemCount() {
        return this.locationList.size();
    }

    public void setLocationList(List<Location> list){
        this.locationList = list;
        notifyDataSetChanged();
    }

    public List<Location> getLocationList(){
        return this.locationList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public CircularView icon;
        public TextView name, amount;
        public ViewGroup panel;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            icon = (CircularView) itemView.findViewById(R.id.locationIcon);
            panel = (ViewGroup) itemView.findViewById(R.id.locationPanel);
            name = (TextView) itemView.findViewById(R.id.locationName);
            amount = (TextView) itemView.findViewById(R.id.locationAmount);

            panel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickLocation(getLayoutPosition());
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnLocationAdapterInteractionListener {
        void onClickLocation(int position);
    }
}
