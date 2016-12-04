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
                R.drawable.screen1);

        TutorialItem page2 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_2),
                context.getResources().getString(R.string.tutorial_description_2),
                R.color.colorPrimary,
                R.drawable.screen2);

        TutorialItem page3 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_3),
                context.getResources().getString(R.string.tutorial_description_3),
                R.color.colorPrimary,
                R.drawable.screen3);

        TutorialItem page4 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_4),
                context.getResources().getString(R.string.tutorial_description_4),
                R.color.colorPrimary,
                R.drawable.screen4);

        TutorialItem page5 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_5),
                context.getResources().getString(R.string.tutorial_description_5),
                R.color.colorPrimary,
                R.drawable.screen5);

        TutorialItem page6 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_6),
                context.getResources().getString(R.string.tutorial_description_6),
                R.color.colorPrimary,
                R.drawable.screen6);

        TutorialItem page7 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_7),
                context.getResources().getString(R.string.tutorial_description_7),
                R.color.colorPrimary,
                R.drawable.screen7);

        TutorialItem page8 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_8),
                context.getResources().getString(R.string.tutorial_description_8),
                R.color.colorPrimary,
                R.drawable.screen8);

        TutorialItem page9 = new TutorialItem(
                context.getResources().getString(R.string.tutorial_title_9),
                context.getResources().getString(R.string.tutorial_description_9),
                R.color.colorPrimary,
                R.drawable.screen9);

        ArrayList<TutorialItem> tourItems = new ArrayList<>();
        tourItems.add(page1);
        tourItems.add(page2);
        tourItems.add(page3);
        tourItems.add(page4);
        tourItems.add(page5);
        tourItems.add(page6);
        tourItems.add(page7);
        tourItems.add(page8);
        tourItems.add(page9);

        return tourItems;
    }
}
