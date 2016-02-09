package com.zhan.budget.Util;

import android.content.Context;
import android.content.res.TypedArray;

import com.zhan.budget.Model.CategoryColor;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-01-04.
 */
public final class CategoryUtil {

    private CategoryUtil() {
    }//private constructor

    public static List<Integer> getListOfUniqueIcon(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_icons);

        List<Integer> iconList = new ArrayList<>();
        for(int i = 0; i < icons.length(); i++){
            // get resource ID by index
            int s = icons.getResourceId(i, 0);
            //Log.d("ZHAN", i+"->"+s+", name :"+context.getResources().getResourceName(s)+", entry name:"+context.getResources().getResourceEntryName(s));
            iconList.add(s);
        }

        return iconList;
    }

    public static List<CategoryColor> getListOfCategoryColors(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_colors);

        List<CategoryColor> colorList = new ArrayList<>();
        for(int i = 0; i < icons.length(); i++){
            // get resource ID by index
            int s = icons.getResourceId(i, 0);
            //Log.d("ZHAN", i+"->"+s+", name :"+context.getResources().getResourceName(s)+", entry name:"+context.getResources().getResourceEntryName(s));

            CategoryColor cc = new CategoryColor();
            cc.setColor(s);
            cc.setIsSelected(false); //default

            colorList.add(cc);
        }

        return colorList;
    }

}
