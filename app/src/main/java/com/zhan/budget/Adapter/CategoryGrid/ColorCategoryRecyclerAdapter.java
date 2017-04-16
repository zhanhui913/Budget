package com.zhan.budget.Adapter.CategoryGrid;

import android.support.v4.app.Fragment;
import android.view.View;

import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;

import java.util.List;

/**
 * Created by Zhan on 16-06-14.
 */
public class ColorCategoryRecyclerAdapter extends CategoryGridRecyclerAdapter {

    public ColorCategoryRecyclerAdapter(Fragment fragment, List<Category> list) {
        this.context = fragment.getContext();
        this.categoryList = list;

        //Any activity or fragment that uses this adapter needs to implement the OnCategoryGridAdapterInteractionListener interface
        if (fragment instanceof OnCategoryGridAdapterInteractionListener) {
            mListener = (OnCategoryGridAdapterInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnCategoryGridAdapterInteractionListener.");
        }
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        // getting category data for the row
        final Category category = categoryList.get(position);

        viewHolder.circularView.setCircleColor(category.getColor());
        viewHolder.name.setVisibility(View.GONE);

        if(category.isSelected()){
            viewHolder.circularView.setStrokeColor(category.getColor());
        }else{
            viewHolder.circularView.setStrokeColor(R.color.transparent);
        }
    }


}
