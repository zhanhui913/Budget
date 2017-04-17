package com.zhan.budget.Fragment;

import com.zhan.budget.BasePresenter;
import com.zhan.budget.BaseView;

import java.util.Date;

/**
 * Created by Zhan on 2017-04-16.
 */

public interface CalendarContract {
    interface View extends BaseView<Presenter> {
        void updateDecorators();
    }

    interface Presenter extends BasePresenter{
        void populateTransactionsForDate(Date date);
    }
}
