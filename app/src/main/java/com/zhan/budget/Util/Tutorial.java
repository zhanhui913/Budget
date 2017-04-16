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
                context.getResources().getString(R.string.tutorial_title_location),
                context.getResources().getString(R.string.tutorial_description_location),
                R.color.colorPrimary,
                R.drawable.t_location);

        TutorialItem page2 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_theme),
                context.getResources().getString(R.string.tutorial_description_theme),
                R.color.colorPrimary,
                R.drawable.t_theme);

        TutorialItem page3 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_account),
                context.getResources().getString(R.string.tutorial_description_account),
                R.color.colorPrimary,
                R.drawable.t_account);

        TutorialItem page4 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_calculator),
                context.getResources().getString(R.string.tutorial_description_calculator),
                R.color.colorPrimary,
                R.drawable.t_calculator);

        TutorialItem page5 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_budget),
                context.getResources().getString(R.string.tutorial_description_budget),
                R.color.colorPrimary,
                R.drawable.t_balance);

        TutorialItem page6 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_chart),
                context.getResources().getString(R.string.tutorial_description_chart),
                R.color.colorPrimary,
                R.drawable.t_pie);

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
