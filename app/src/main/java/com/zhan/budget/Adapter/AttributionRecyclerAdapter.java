package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhan.budget.Model.Attribution;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

public class AttributionRecyclerAdapter extends RecyclerView.Adapter<AttributionRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Attribution> attributionList;
    private OnOpenSourceInteractionListener mListener;


    public AttributionRecyclerAdapter(Fragment fragment, List<Attribution> attributionList) {
        this.context = fragment.getContext();
        this.attributionList = attributionList;

        //Any activity or fragment that uses this adapter needs to implement the OnOpenSourceInteractionListener interface
        if (fragment instanceof OnOpenSourceInteractionListener) {
            mListener = (OnOpenSourceInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnOpenSourceInteractionListener.");
        }
    }

    public AttributionRecyclerAdapter(Activity activity, List<Attribution> attributionList){
        this.context = activity;
        this.attributionList = attributionList;

        //Any activity or fragment that uses this adapter needs to implement the OnOpenSourceInteractionListener interface
        if (activity instanceof OnOpenSourceInteractionListener) {
            mListener = (OnOpenSourceInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnOpenSourceInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attribution, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting Location data for the row
        Attribution data = attributionList.get(position);

        viewHolder.name.setText(data.getName());
        viewHolder.author.setText(data.getAuthor());
        viewHolder.icon.setCircleColor(data.getColor());
        viewHolder.icon.setText(""+ Util.getFirstCharacterFromString(data.getName().toUpperCase()));
        viewHolder.icon.setTextColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));
    }

    @Override
    public int getItemCount() {
        return this.attributionList.size();
    }

    public void setAttributionList(List<Attribution> list){
        this.attributionList = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public CircularView icon;
        public TextView name, author;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            icon = (CircularView) itemView.findViewById(R.id.attributionCV);
            name = (TextView) itemView.findViewById(R.id.attributionTitle);
            author = (TextView) itemView.findViewById(R.id.attributionContent);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(getLayoutPosition());
                }
            });
        }
    }

    public interface OnOpenSourceInteractionListener{
        void onClick(int position);
    }
}
