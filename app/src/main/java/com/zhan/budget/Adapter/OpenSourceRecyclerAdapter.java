package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhan.budget.Model.OpenSource;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import java.util.List;

public class OpenSourceRecyclerAdapter extends RecyclerView.Adapter<OpenSourceRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<OpenSource> openSourceList;

    public OpenSourceRecyclerAdapter(Fragment fragment,List<OpenSource> openSourceList) {
        this.context = fragment.getContext();
        this.openSourceList = openSourceList;
    }

    public OpenSourceRecyclerAdapter(Activity activity, List<OpenSource> openSourceList){
        this.context = activity;
        this.openSourceList = openSourceList;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_open_source, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting Location data for the row
        OpenSource data = openSourceList.get(position);

        viewHolder.name.setText(data.getName());
        viewHolder.author.setText(data.getAuthor());
        viewHolder.icon.setCircleColor(data.getColor());
        viewHolder.icon.setText(""+ Util.getFirstCharacterFromString(data.getName().toUpperCase()));
        viewHolder.icon.setTextColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));
        viewHolder.icon.setTextSizeInDP(30);
    }

    @Override
    public int getItemCount() {
        return this.openSourceList.size();
    }

    public void setOpenSourceList(List<OpenSource> list){
        this.openSourceList = list;
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

            icon = (CircularView) itemView.findViewById(R.id.openSourceIcon);
            name = (TextView) itemView.findViewById(R.id.openSourceName);
            author = (TextView) itemView.findViewById(R.id.openSourceAuthor);
        }
    }
}
