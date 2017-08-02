package com.zhan.budget.Fragment;

import android.content.Intent;

import com.zhan.budget.BasePresenter;
import com.zhan.budget.BaseView;
import com.zhan.budget.Data.Realm.RealmHelper;
import com.zhan.budget.Model.Calendar.BudgetEvent;
import com.zhan.budget.Model.Realm.Transaction;

import java.util.Date;
import java.util.List;

/**
 * This specifies the contract between the VIEW {@link CalendarFragment} and PRESENTER {@link CalendarPresenter}
 */
public interface CalendarContract {
    interface View extends BaseView<Presenter> {
        void updateMonthInToolbar();

        void updateCalendarView(Date date);

        void refreshCalendarView();

        void updateTransactionStatus();

        void smoothScrollToPosition(int position);

        void updateDateTextview(String value);

        void updateTotalCostView(double cost);

        void setLoadingIndicator(boolean active);

        void updateTransactions(List<Transaction> transactionList);

        void updateTransaction(int position, Transaction transaction);

        void removeTransaction(int position);

        void showEditTransaction(Transaction editTransaction);

        void showAddTransaction();

        boolean isActive();

        List<Transaction> getTransactions();

        void closeAllSwipe();
    }

    interface Presenter extends BasePresenter{

        void populateTransactionsForDate1(Date date);

        void populateTransactionsForDate1(Date date, RealmHelper.RealmOperationCallback callback);

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