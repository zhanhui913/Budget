package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

public class LocationRecyclerAdapter extends RecyclerView.Adapter<LocationRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Location> locationList;
    private OnLocationAdapterInteractionListener mListener;

    /**
     * Default is true
     */
    private boolean showTimes = true;

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

    public LocationRecyclerAdapter(Context context, List<Location> locationList, boolean showTimes){
        this.context = context;
        this.locationList = locationList;
        this.showTimes = showTimes;

        if(context instanceof OnLocationAdapterInteractionListener){
            mListener = (OnLocationAdapterInteractionListener) context;
        }else{
            throw new RuntimeException(context.toString() + " must implement OnLocationAdapterInteractionListener.");
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
        if(location.getAmount() > 1){
            viewHolder.amount.setText(String.format(context.getString(R.string.location_times), location.getAmount()));
        }else{
            viewHolder.amount.setText(String.format(context.getString(R.string.location_time), location.getAmount()));
        }
        viewHolder.circularView.setCircleColor(location.getColor());
        viewHolder.circularView.setText(""+ Util.getFirstCharacterFromString(location.getName().toUpperCase().trim()));
        viewHolder.circularView.setTextColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));
        viewHolder.circularView.setStrokeColor(location.getColor());
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
        public CircularView circularView;
        public TextView name, amount;
        public SwipeLayout swipeLayout;
        public ImageView deleteBtn, editBtn;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            circularView = (CircularView) itemView.findViewById(R.id.locationIcon);
            name = (TextView) itemView.findViewById(R.id.locationName);
            amount = (TextView) itemView.findViewById(R.id.locationAmount);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeLocation);
            deleteBtn = (ImageView) itemView.findViewById(R.id.deleteBtn);
            editBtn = (ImageView) itemView.findViewById(R.id.editBtn);

            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    Log.d("LocationRecyclerAdapter", "onstartopen");
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    Log.d("AccountFragment 1", "on open");
                }

                @Override
                public void onStartClose(SwipeLayout layout) {
                    Log.d("LocationRecyclerAdapter", "onstartclose");
                }

                @Override
                public void onClose(SwipeLayout layout) {
                    Log.d("AccountFragment 1", "onclose");
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    Log.d("LocationRecyclerAdapter", "onupdate "+leftOffset+","+topOffset);
                    mListener.onPullDownAllow(false);
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    Log.d("LocationRecyclerAdapter", "onhandrelease :"+xvel+","+yvel);
                    mListener.onPullDownAllow(true);
                }
            });

            swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickLocation(getLayoutPosition());
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipeLayout.close(true);
                    mListener.onDeleteLocation(getLayoutPosition());
                }
            });

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipeLayout.close(true);
                    mListener.onEditLocation(getLayoutPosition());
                }
            });

            if(!showTimes){
                amount.setVisibility(View.GONE);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnLocationAdapterInteractionListener {
        void onClickLocation(int position);

        void onDeleteLocation(int position);

        void onEditLocation(int position);

        void onPullDownAllow(boolean value);
    }
}
