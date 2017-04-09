package com.zhan.budget.Util;

import android.content.Context;

import com.zhan.budget.R;

import java.util.ArrayList;

import za.co.riggaroo.materialhelptutorial.TutorialItem;

/**
 * Created by zhanyap on 2016-06-28.
 */
public final class Tutorial {

    private Tutorial() {
    }//private constructor

    public static ArrayList<TutorialItem> getTutorialPages(Context context){
        TutorialItem page1 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_1),
                context.getResources().getString(R.string.tutorial_description_1),
                R.color.colorPrimary,
                R.drawable.t_account);

        TutorialItem page2 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_2),
                context.getResources().getString(R.string.tutorial_description_2),
                R.color.colorPrimary,
                R.drawable.t_theme);

        TutorialItem page3 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_3),
                context.getResources().getString(R.string.tutorial_description_3),
                R.color.colorPrimary,
                R.drawable.t_calculator);

        TutorialItem page4 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_4),
                context.getResources().getString(R.string.tutorial_description_4),
                R.color.colorPrimary,
                R.drawable.t_balance);

        TutorialItem page5 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_5),
                context.getResources().getString(R.string.tutorial_description_5),
                R.color.colorPrimary,
                R.drawable.t_pie);

        TutorialItem page6 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_6),
                context.getResources().getString(R.string.tutorial_description_6),
                R.color.colorPrimary,
                R.drawable.t_location);

        ArrayList<TutorialItem> tourItems = new ArrayList<>();
        tourItems.add(page1);
        tourItems.add(page2);
        tourItems.add(page3);
        tourItems.add(page4);
        tourItems.add(page5);
        tourItems.add(page6);

        return tourItems;
    }
}
