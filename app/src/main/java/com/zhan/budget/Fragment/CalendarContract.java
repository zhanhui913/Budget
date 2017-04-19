package com.zhan.budget.Fragment;

import android.content.Intent;

import com.zhan.budget.BasePresenter;
import com.zhan.budget.BaseView;
import com.zhan.budget.Model.Calendar.BudgetEvent;
import com.zhan.budget.Model.Realm.Transaction;

import java.util.Date;
import java.util.List;

/**
 * Created by Zhan on 2017-04-16.
 */

public interface CalendarContract {
    interface View extends BaseView<Presenter> {
        void updateMonthInToolbar();

        void updateCalendarView(Date date);

        void updateTransactionStatus();

        void smoothScrollToPosition(int position);

        void updateDateTextview(String value);

        void updateTotalCostView(double cost);

        void setProgressVisibility(boolean isVisible);

        void updateTransactions(List<Transaction> transactionList);

        void showEditTransaction(Transaction editTransaction);

        void showAddTransaction();
    }

    interface Presenter extends BasePresenter{
        void populateTransactionsForDate1(Date date);

        void updateDecorations();

        List<BudgetEvent> getDecorations(Date date);

        void result(int requestCode, int resultCode, Intent data);

        void addTransaction();

        void editTransaction(int position);

        void deleteTransaction(int position);

        void approveTransaction(int position);

        void unApproveTransaction(int position);
    }
}
