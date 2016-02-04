package com.zhan.budget.Util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

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
            return ContextCompat.getDrawable(context, R.drawable.food);
        }else if(iconId == 1){
            return ContextCompat.getDrawable(context, R.drawable.food);
        }else if(iconId == 2){
            return ContextCompat.getDrawable(context, R.drawable.food);
        }else if(iconId == 3){
            return ContextCompat.getDrawable(context, R.drawable.food);
        }else if(iconId == 4){
            return ContextCompat.getDrawable(context, R.drawable.cafe);
        }else if(iconId == 5){
            return ContextCompat.getDrawable(context, R.drawable.house);
        }else if(iconId == 6){
            return ContextCompat.getDrawable(context, R.drawable.airplane);
        }else if(iconId == 7){
            return ContextCompat.getDrawable(context, R.drawable.car);
        }else if(iconId == 8){
            return ContextCompat.getDrawable(context, R.drawable.shirt);
        }else if(iconId == 9){
            return ContextCompat.getDrawable(context, R.drawable.etc);
        }else if(iconId == 10){
            return ContextCompat.getDrawable(context, R.drawable.utilities);
        }else if(iconId == 11){
            return ContextCompat.getDrawable(context, R.drawable.bill);
        }else{
            return ContextCompat.getDrawable(context, R.drawable.groceries);
        }
    }

    public static int getIconResourceId(int iconId){
        if(iconId == 0){
            return R.drawable.food;
        }else if(iconId == 1){
            return R.drawable.food;
        }else if(iconId == 2){
            return R.drawable.food;
        }else if(iconId == 3){
            return R.drawable.food;
        }else if(iconId == 4){
            return R.drawable.cafe;
        }else if(iconId == 5){
            return R.drawable.house;
        }else if(iconId == 6){
            return R.drawable.airplane;
        }else if(iconId == 7){
            return R.drawable.car;
        }else if(iconId == 8){
            return R.drawable.shirt;
        }else if(iconId == 9){
            return R.drawable.etc;
        }else if(iconId == 10){
            return R.drawable.utilities;
        }else if(iconId == 11){
            return R.drawable.bill;
        }else{
            return R.drawable.groceries;
        }
    }

    public static List<Integer> getListOfUniqueIcon(){
        List<Integer> iconList = new ArrayList<>();
        iconList.add(R.drawable.food);
        iconList.add(R.drawable.cafe);
        iconList.add(R.drawable.house);
        iconList.add(R.drawable.airplane);
        iconList.add(R.drawable.car);
        iconList.add(R.drawable.shirt);
        iconList.add(R.drawable.etc);
        iconList.add(R.drawable.utilities);
        iconList.add(R.drawable.bill);
        iconList.add(R.drawable.groceries);

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
