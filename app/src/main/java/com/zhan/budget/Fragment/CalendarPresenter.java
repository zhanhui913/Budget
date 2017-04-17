package com.zhan.budget.Fragment;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by Zhan on 2017-04-15.
 */

public class CalendarPresenter implements CalendarContract.Presenter{

    @NonNull
    private final CalendarContract.View  mView;

    public CalendarPresenter(@NonNull CalendarContract.View mView){
        this.mView = mView;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Calender Presenter Interface
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void start() {
        Log.d("calendarPresenter", "start");
    }

    @Override
    public void populateTransactionsForDate(Date date){
        Log.d("calendarPresenter", "populateTransactionsForDate for "+date.toString());
    }
}
