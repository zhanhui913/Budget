package com.zhan.budget.Util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import com.zhan.budget.Model.CategoryIconColor;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-01-04.
 */
public final class CategoryUtil {

    private CategoryUtil() {
    }//private constructor

    public static List<CategoryIconColor> getListOfUniqueIcons(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_icons);

        List<CategoryIconColor> iconList = new ArrayList<>();
        for(int i = 0; i < icons.length(); i++){
            // get resource ID by index
            int s = icons.getResourceId(i, 0);
            //Log.d("ZHAN", i+"->"+s+", name :"+context.getResources().getResourceName(s)+", entry name:"+context.getResources().getResourceEntryName(s));

            CategoryIconColor cc = new CategoryIconColor();
            cc.setIcon(s);
            cc.setIsSelected(false); //default

            iconList.add(cc);
        }

        return iconList;
    }

    public static List<CategoryIconColor> getListOfCategoryColors(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_colors);

        List<CategoryIconColor> colorList = new ArrayList<>();
        for(int i = 0; i < icons.length(); i++){
            // get resource ID by index
            int s = icons.getResourceId(i, 0);
            Log.d("CATEGORY_UTIL", i + "->" + s + ", name :" + context.getResources().getResourceName(s) + ", entry name:" + context.getResources().getResourceEntryName(s) + " -> " + context.getResources().getString(s));

            CategoryIconColor cc = new CategoryIconColor();




            String hex = context.getResources().getString(s);
            int dd = Hex2Decimal.hex2decimal(hex);
            Log.d("CATEGORY_UTIL", hex + " -> " + dd);
            String hex2 = Hex2Decimal.decimal2hex(dd);
            Log.d("CATEGORY_UTIL", dd + " -> " + hex2);




            cc.setColor(s);
            cc.setIsSelected(false); //default
            colorList.add(cc);
        }

        return colorList;
    }

}
