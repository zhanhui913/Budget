package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.library.CircularView;

import java.util.List;

public class CircularViewRecyclerAdapter extends RecyclerView.Adapter<CircularViewRecyclerAdapter.ViewHolder> {

    public enum ARRANGEMENT{
        ICON,
        COLOR,
        BOTH
    }

    public ARRANGEMENT arrangement;

    protected Context context;
    protected List<Category> categoryList;
    protected OnCircularViewAdapterInteractionListener mListener;

    //Dont use this to instantiate, only created for subclass purposes
    public CircularViewRecyclerAdapter(){}

    public CircularViewRecyclerAdapter(Fragment fragment, List<Category> list, ARRANGEMENT arrangement) {
        this.context = fragment.getContext();
        this.categoryList = list;
        this.arrangement = arrangement;

        //Any activity or fragment that uses this adapter needs to implement the OnCircularViewAdapterInteractionListener interface
        if (fragment instanceof OnCircularViewAdapterInteractionListener) {
            mListener = (OnCircularViewAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnCircularViewAdapterInteractionListener.");
        }
    }

    public CircularViewRecyclerAdapter(Activity activity, List<Category> list, ARRANGEMENT arrangement) {
        this.context = activity;
        this.categoryList = list;
        this.arrangement = arrangement;

        //Any activity or fragment that uses this adapter needs to implement the OnCircularViewAdapterInteractionListener interface
        if (activity instanceof OnCircularViewAdapterInteractionListener) {
            mListener = (OnCircularViewAdapterInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnCircularViewAdapterInteractionListener.");
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circular_view, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting category data for the row
        final Category category = categoryList.get(position);

        if(arrangement == ARRANGEMENT.ICON){
            //viewHolder.circularView.setCircleColor(this.color);
            viewHolder.circularView.setIconResource(CategoryUtil.getIconID(context, category.getIcon()));
            viewHolder.circularView.setIconColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));
            viewHolder.name.setVisibility(View.GONE);
        }else if(arrangement == ARRANGEMENT.COLOR){
            viewHolder.circularView.setCircleColor(category.getColor());
            viewHolder.name.setVisibility(View.GONE);
        }else if(arrangement == ARRANGEMENT.BOTH){
            viewHolder.circularView.setCircleColor(category.getColor());
            viewHolder.circularView.setIconResource(CategoryUtil.getIconID(context, category.getIcon()));
            viewHolder.circularView.setIconColor(Colors.getHexColorFromAttr(context, R.attr.themeColor));

            viewHolder.name.setVisibility(View.VISIBLE);
            viewHolder.name.setText(category.getName());
        }


        if(category.isSelected()){
            viewHolder.circularView.setStrokeColor(Colors.getHexColorFromAttr(context, R.attr.themeColorText));
        }else{
            viewHolder.circularView.setStrokeColor(R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return this.categoryList.size();
    }

    public void setCategoryList(List<Category> list){
        this.categoryList = list;
        notifyDataSetChanged();
    }

    public List<Category> getCategoryList(){
        return this.categoryList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public CircularView circularView;
        public TextView name;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView){
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            circularView = (CircularView) itemView.findViewById(R.id.categoryIcon);
            name = (TextView) itemView.findViewById(R.id.categoryName);

            circularView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(getLayoutPosition());
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnCircularViewAdapterInteractionListener {
        void onClick(int position);
    }
}

