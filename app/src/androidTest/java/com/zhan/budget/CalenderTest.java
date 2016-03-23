package com.zhan.budget;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.zhan.budget.Activity.MainActivity;
import com.zhan.budget.View.CalendarSmallCircleView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.is;


/*
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.action.ViewActions.*;
import static org.hamcrest.CoreMatchers.*;
 */

@RunWith(AndroidJUnit4.class)
public class CalenderTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
/*
    @Test
    public void calendarInteraction() {
        Date currentMonth = new Date();
        Date nextMonth = DateUtil.refreshMonth(currentMonth);

        for(int i = 0; i < 5; i++) {
            nextMonth = DateUtil.getMonthWithDirection(nextMonth, 1);

            //Swipe left
            onView(withId(R.id.calendarView)).perform(swipeLeft());
            onView(withId(R.id.dateTextView)).check(matches(withText(DateUtil.convertDateToStringFormat1(nextMonth))));

            matchToolbarTitle(DateUtil.convertDateToStringFormat2(nextMonth));
        }

        Date prevMonth = DateUtil.refreshMonth(nextMonth);
        for(int i = 0; i < 5; i++) {
            prevMonth = DateUtil.getMonthWithDirection(prevMonth, -1);

            //Swipe right
            onView(withId(R.id.calendarView)).perform(swipeRight());
            onView(withId(R.id.dateTextView)).check(matches(withText(DateUtil.convertDateToStringFormat1(prevMonth))));

            matchToolbarTitle(DateUtil.convertDateToStringFormat2(prevMonth));
        }
    }

    @Test
    public void calendarInteractionMenu() {
        Date currentMonth = new Date();

        Date nextMonth = DateUtil.refreshMonth(currentMonth);

        for(int i = 0; i < 5; i++) {
            nextMonth = DateUtil.getMonthWithDirection(nextMonth, 1);

            //Click right chevron
            onView(withId(R.id.rightChevron)).perform(click());
            onView(withId(R.id.dateTextView)).check(matches(withText(DateUtil.convertDateToStringFormat1(nextMonth))));

            matchToolbarTitle(DateUtil.convertDateToStringFormat2(nextMonth));
        }

        Date prevMonth = DateUtil.refreshMonth(nextMonth);
        for(int i = 0; i < 5; i++) {
            prevMonth = DateUtil.getMonthWithDirection(prevMonth, -1);

            //Click left chevron
            onView(withId(R.id.leftChevron)).perform(click());
            onView(withId(R.id.dateTextView)).check(matches(withText(DateUtil.convertDateToStringFormat1(prevMonth))));

            matchToolbarTitle(DateUtil.convertDateToStringFormat2(prevMonth));
        }
    }*/

    @Test
    public void transactionListView(){
//        onData(is(instanceOf(MonthViewPagerAdapter.class))).inAdapterView(withId(R.id.calendarView)).atPosition(0).perform(click());


        //onData(instanceOf(MonthViewPagerAdapter.class)).atPosition(0).inAdapterView(allOf(withId(R.id.calendarView), isDisplayed())).perform(click());

//        onView(allOf(withId(R.id.calendarView))).perform(click());

        //onView(nthChildOf(withParent(withId(R.id.calendarView)), 1) );


    //onView(withChild(withId(R.id.calendarView)), withText("0")).perform(click());
        onView(matchCalendarDateView(withId(R.id.calendarView))).perform(click());

    }

    public static Matcher<View> matchCalendarDateView(final Matcher<View> stringMatcher) {
        return new BoundedMatcher<View, CalendarSmallCircleView>(CalendarSmallCircleView.class) {

            @Override
            public void describeTo(final Description description) {
                description.appendText("with error text: ");
                stringMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(final CalendarSmallCircleView calendarSmallCircleView) {
                return stringMatcher.matches(calendarSmallCircleView);
            }
        };
    }



    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int nth) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with first child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {

                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }
                ViewGroup group = (ViewGroup) view.getParent();
                return parentMatcher.matches(view.getParent()) && group.getChildAt(nth).equals(view);
            }
        };
    }


    private static ViewInteraction matchToolbarTitle(CharSequence title) {
        return onView(isAssignableFrom(Toolbar.class)).check(matches(withToolbarTitle(is(title))));
    }

    private static Matcher<Object> withToolbarTitle(final Matcher<CharSequence> textMatcher) {
        return new BoundedMatcher<Object, Toolbar>(Toolbar.class) {
            @Override
            public boolean matchesSafely(Toolbar toolbar) {
                return textMatcher.matches(toolbar.getTitle());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with toolbar title: ");
                textMatcher.describeTo(description);
            }
        };
    }
}
