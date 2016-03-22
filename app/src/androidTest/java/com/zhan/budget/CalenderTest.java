package com.zhan.budget;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.zhan.budget.Activity.MainActivity;
import com.zhan.budget.Util.DateUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class CalenderTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void calendarInteraction() {
        Date currentMonth = new Date();
        Date nextMonth = DateUtil.refreshMonth(DateUtil.getMonthWithDirection(currentMonth, 1));

        // Type text and then press the button.
        onView(withId(R.id.calendarView)).perform(swipeLeft());
        onView(withId(R.id.dateTextView)).check(matches(withText(DateUtil.convertDateToStringFormat1(nextMonth))));




/*
        onView(withId(R.id.operand2)).perform(typeText("5"), closeSoftKeyboard());

        // Check that the text was changed.
        onView(withId(R.id.calculateBtn)).perform(click());

        onView(withId(R.id.answer)).check(matches(withText("$9")));
        */
    }
}
