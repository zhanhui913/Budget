package com.zhan.budget.Util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-01-04.
 */
public final class CategoryUtil {

    private CategoryUtil() {
    }//private constructor


    public static Drawable getIconDrawable(Context context, int iconId){
        if(iconId == 0){
            return ContextCompat.getDrawable(context, R.drawable.c_food);
        }else if(iconId == 1){
            return ContextCompat.getDrawable(context, R.drawable.c_food);
        }else if(iconId == 2){
            return ContextCompat.getDrawable(context, R.drawable.c_food);
        }else if(iconId == 3){
            return ContextCompat.getDrawable(context, R.drawable.c_food);
        }else if(iconId == 4){
            return ContextCompat.getDrawable(context, R.drawable.c_cafe);
        }else if(iconId == 5){
            return ContextCompat.getDrawable(context, R.drawable.c_house);
        }else if(iconId == 6){
            return ContextCompat.getDrawable(context, R.drawable.c_airplane);
        }else if(iconId == 7){
            return ContextCompat.getDrawable(context, R.drawable.c_car);
        }else if(iconId == 8){
            return ContextCompat.getDrawable(context, R.drawable.c_shirt);
        }else if(iconId == 9){
            return ContextCompat.getDrawable(context, R.drawable.c_etc);
        }else if(iconId == 10){
            return ContextCompat.getDrawable(context, R.drawable.c_utilities);
        }else if(iconId == 11){
            return ContextCompat.getDrawable(context, R.drawable.c_bill);
        }else{
            return ContextCompat.getDrawable(context, R.drawable.c_groceries);
        }
    }

    public static int getIconResourceId(int iconId){
        if(iconId == 0){
            return R.drawable.c_food;
        }else if(iconId == 1){
            return R.drawable.c_food;
        }else if(iconId == 2){
            return R.drawable.c_food;
        }else if(iconId == 3){
            return R.drawable.c_food;
        }else if(iconId == 4){
            return R.drawable.c_cafe;
        }else if(iconId == 5){
            return R.drawable.c_house;
        }else if(iconId == 6){
            return R.drawable.c_airplane;
        }else if(iconId == 7){
            return R.drawable.c_car;
        }else if(iconId == 8){
            return R.drawable.c_shirt;
        }else if(iconId == 9){
            return R.drawable.c_etc;
        }else if(iconId == 10){
            return R.drawable.c_utilities;
        }else if(iconId == 11){
            return R.drawable.c_bill;
        }else{
            return R.drawable.c_groceries;
        }
    }

    public static List<Integer> getListOfUniqueIcon(Context context){
        TypedArray icons = context.getResources().obtainTypedArray(R.array.category_icons);

        List<Integer> iconList = new ArrayList<>();
        for(int i = 0; i < icons.length(); i++){
            // get resource ID by index
            int s = icons.getResourceId(i, 0);
            Log.d("ZHAN", i+"->"+s+", name :"+context.getResources().getResourceName(s)+", entry name:"+context.getResources().getResourceEntryName(s));
            iconList.add(s);
        }

        return iconList;
    }

    public static Drawable getColorDrawable(Context context, String colorId){
        int colorInt = Color.parseColor(colorId);
/*
        Color color = Color.parseColor("#"+Integer.toHexString(colorInt));
        new Drawable();

        */
        return new ColorDrawable(colorInt);

    }
}
