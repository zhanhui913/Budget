package com.zhan.budget.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.zhan.budget.R;
import com.zhan.circularview.CircularView;

import java.util.List;

/**
 * Created by zhanyap on 2016-02-04.
 */
public class IconCategoryGridAdapter extends ArrayAdapter<Integer> {
    private Context context;
    private List<Integer> iconList;
    private int color;

    static class ViewHolder {
        public CircularView circularView;
    }

    public IconCategoryGridAdapter(Context context, List<Integer> iconList, int color) {
        super(context, R.layout.item_category_grid, iconList);
        this.context = context;
        this.iconList = iconList;
        this.color = color;
    }

    public void updateColor(int color){
        this.color = color;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_icon_picker_category, parent, false);

            viewHolder.circularView = (CircularView) convertView.findViewById(R.id.categoryIcon);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //get drawable data
        viewHolder.circularView.setCircleColor(this.color);
        viewHolder.circularView.setIconDrawable(
                ResourcesCompat.getDrawable(
                        this.context.getResources(),
                        iconList.get(position),
                        this.context.getTheme())
                );

        return convertView;
    }
}
