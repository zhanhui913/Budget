package com.zhan.budget.View;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;

/**
 * Created by Zhan on 16-03-06.
 */
public class ExtendedNumberPicker extends NumberPicker {

    public ExtendedNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        Class<?> numberPickerClass = null;
        try {
            numberPickerClass = Class.forName("android.widget.NumberPicker");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Field selectionDivider = null;
        try {
            if(numberPickerClass != null) {
                selectionDivider = numberPickerClass.getDeclaredField("mSelectionDivider");
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            if(selectionDivider != null) {
                selectionDivider.setAccessible(true);
                selectionDivider.set(this, null);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    private void updateView(View view) {
        if(view instanceof EditText){
            ((EditText) view).setTextSize(25);
            //((EditText) view).setTextColor(Color.parseColor("#fff00f"));
            Log.d("NUMBER","update views");
        }
    }
}
