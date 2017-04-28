package com.zhan.budget.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Data.AppDataManager;
import com.zhan.budget.Data.Realm.RealmHelper;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Model.Calendar.BudgetEvent;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.DateUtil;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listens to user actions from the UI {@link CalendarFragment}, retrieves the data and updates the
 * UI as required.
 */
public class CalendarPresenter implements CalendarContract.Presenter{

    private static final String TAG = "CalendarPresenter";

    private Date selectedDate;
    private Map<Date,List<BudgetEvent>> scheduledMap;
    private double currentSumValue;

    @NonNull
    private final CalendarContract.View  mView;

    @NonNull
    private final AppDataManager mAppDataManager;

    public CalendarPresenter(@NonNull AppDataManager appDataManager, @NonNull CalendarContract.View view){
        mAppDataManager = appDataManager;
        mView = view;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Calender Presenter Interface
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void start() {
        Log.d(TAG, "start CalendarPresenter");

        //Grab today's date
        selectedDate = DateUtil.refreshDate(new Date());
        populateTransactionsForDate1(selectedDate, new RealmHelper.RealmOperationCallback() {
            @Override
            public void onComplete() {
                Log.d(TAG, "start populateTransactionsForDate1 completed, start decoration operation");
                updateDecorations();
            }
        });
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop CalendarPresenter");
    }

    @Override
    public void populateTransactionsForDate1(Date date){
        mView.closeAllSwipe();
        populateTransactionsForDate1(date, null);
    }

    @Override
    public void populateTransactionsForDate1(Date date, final RealmHelper.RealmOperationCallback callback){
        final Date startDate = DateUtil.refreshDate(date);
        final Date endDate = DateUtil.getNextDate(date);

        //Update date text view in center panel
        mView.updateDateTextview(DateUtil.convertDateToStringFormat1(mView.getContext(), startDate));

        Log.d(TAG, " populate transaction list (" + DateUtil.convertDateToStringFormat5(mView.getContext(), startDate) + " -> " + DateUtil.convertDateToStringFormat5(mView.getContext(), endDate) + ")");

        //Change to VISIBLE while preparing to do calculation
        mView.setLoadingIndicator(true);

        mAppDataManager.getTransactions(startDate, new RealmHelper.LoadTransactionsCallback() {
            @Override
            public void onTransactionsLoaded(List<Transaction> list) {
                Log.d(TAG, "on transactions loaded "+list.size());

                currentSumValue = CurrencyTextFormatter.findTotalCostForTransactions(list);

                mView.updateTotalCostView(currentSumValue);
                mView.updateTransactions(list);

                populateTransactionsForDateDone(startDate, callback);
            }

            @Override
            public void onDataNotAvailable() {
                Log.d(TAG, "on transactions no data available");

                mView.updateTotalCostView(0);
                mView.updateTransactions(new ArrayList<Transaction>());

                populateTransactionsForDateDone(startDate, callback);
            }

            @Override
            public void onFail(){
                Log.d(TAG, "on failed");

                mView.setLoadingIndicator(false);
                populateTransactionsForDateDone(startDate, callback);
            }
        });
    }

    @Override
    public void updateDecorations(){
        //Should only be called the first time the presenter is initialized
        scheduledMap = new HashMap<>();

        mAppDataManager.getScheduledTransactions(new RealmHelper.LoadTransactionsCallback() {
            @Override
            public void onTransactionsLoaded(List<Transaction> list) {
                performCalculationForDecorators(list);
            }

            @Override
            public void onDataNotAvailable() {
                //No need to do anything
            }

            @Override
            public void onFail(){
                //No need to do anything
            }
        });
    }

    @Override
    public List<BudgetEvent> getDecorations(Date date){
        if(scheduledMap == null){
            return new ArrayList<>();
        }else{
            return scheduledMap.get(date);
        }
    }

    @Override
    public void result(int requestCode, int resultCode, Intent data){
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data.getExtras() != null) {
            if(requestCode == RequestCodes.NEW_TRANSACTION){
                mView.updateCalendarView(selectedDate);

                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(TransactionInfoActivity.RESULT_TRANSACTION));

                populateTransactionsForDate1(tt.getDate(), new RealmHelper.RealmOperationCallback() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "new transaction populateTransactionsForDate1 completed, start decoration operation");

                        //Need to update all decorations as it is possible that the new transaction
                        //will have scheduled transactions set.
                        updateDecorations();
                    }
                });
            }else if(requestCode == RequestCodes.EDIT_TRANSACTION){
                mView.updateCalendarView(selectedDate);

                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(TransactionInfoActivity.RESULT_TRANSACTION));

                populateTransactionsForDate1(tt.getDate(), new RealmHelper.RealmOperationCallback() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "edit transaction populateTransactionsForDate1 completed, start decoration operation");

                        //Need to update all decorations as it is possible that the edited transaction
                        //will have scheduled transactions set.
                        updateDecorations();
                    }
                });
            }
        }
    }

    @Override
    public void addTransaction(){
        mView.showAddTransaction();
    }

    @Override
    public void editTransaction(int position){
        mView.showEditTransaction(mView.getTransactions().get(position));
    }

    @Override
    public void deleteTransaction(final int position){
        mAppDataManager.deleteTransaction(mView.getTransactions().get(position).getId(), new RealmHelper.DeleteTransactionCallback() {
            @Override
            public void onSuccess() {
                //Update sum only if it was COMPLETED since SCHEDULED wouldnt be in the sum.
                if(mView.getTransactions().get(position).getDayType().equalsIgnoreCase(DayType.COMPLETED.toString())){
                    currentSumValue -= mView.getTransactions().get(position).getPrice();
                    mView.updateTotalCostView(currentSumValue);
                }

                //Remove transaction from list
                mView.removeTransaction(position);

                updateDecorationsAsNeeded();
            }

            @Override
            public void onFailed() {
                mView.showSnackbar("Delete transaction failed");
            }
        });
    }

    @Override
    public void approveTransaction(final int position){
        mAppDataManager.approveTransaction(mView.getTransactions().get(position).getId(), new RealmHelper.LoadTransactionCallback() {
            @Override
            public void onTransactionLoaded(Transaction transaction) {
                //Update new transaction
                mView.updateTransaction(position, transaction);

                //Update sum
                currentSumValue += transaction.getPrice();
                mView.updateTotalCostView(currentSumValue);

                updateDecorationsAsNeeded();
            }

            @Override
            public void onDataNotAvailable() {
                mView.showSnackbar("Approve transaction failed");
            }

            @Override
            public void onFail(){
                mView.showSnackbar("Approve transaction failed");
            }
        });
    }

    @Override
    public void unApproveTransaction(final int position){
        mAppDataManager.unapproveTransaction(mView.getTransactions().get(position).getId(), new RealmHelper.LoadTransactionCallback() {
            @Override
            public void onTransactionLoaded(Transaction transaction) {
                //Update new transaction
                mView.updateTransaction(position, transaction);

                //Update sum
                currentSumValue -= transaction.getPrice();
                mView.updateTotalCostView(currentSumValue);

                updateDecorationsAsNeeded();
            }

            @Override
            public void onDataNotAvailable() {
                mView.showSnackbar("Unapproved transaction failed");
            }

            @Override
            public void onFail(){
                mView.showSnackbar("Unapproved transaction failed");
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Helper functions
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * If the current transaction list has at least 1 SCHEDULED transaction, update it.
     * Otherwise, remove the date from the hashmap and update it.
     */
    private void updateDecorationsAsNeeded(){
        //Update decorators as needed
        if(isThereScheduledTransactionsOnDate()){
            addDecoratorOnDate(getFirstScheduledTransaction(), true);
        }else{
            //If there is no SCHEDULED transaction, remove the date from the hashmap that
            //contains the decorators
            scheduledMap.remove(selectedDate);
            mView.refreshCalendarView();
        }
    }

    /**
     * Go through transaction list and see if there are any SCHEDULED transactions
     * @return true if there are any SCHEDULED transactions, false otherwise
     */
    private boolean isThereScheduledTransactionsOnDate(){
        for(int i = 0; i < mView.getTransactions().size(); i++){
            if(mView.getTransactions().get(i).getDayType().equalsIgnoreCase(DayType.SCHEDULED.toString())){
                return true;
            }
        }
        return false;
    }

    /**
     * Go through transaction list and get the first SCHEDULED transaction, otherwise return null
     * @return The first SCHEDULED transaction
     */
    private Transaction getFirstScheduledTransaction(){
        for(int i = 0; i < mView.getTransactions().size(); i++){
            if(mView.getTransactions().get(i).getDayType().equalsIgnoreCase(DayType.SCHEDULED.toString())){
                return mView.getTransactions().get(i);
            }
        }
        return null;
    }

    /***
     * Add a decorator onto the date using the Transaction's Category color if applicable.
     * Otherwise use the application's colorPrimary value.
     * @param transaction Target Transaction
     * @param refreshView refresh the calendar view or not.
     */
    private void addDecoratorOnDate(Transaction transaction, boolean refreshView){
        List<BudgetEvent> colorList = new ArrayList<>();

        try {
            if (transaction != null && transaction.getCategory() != null) {
                colorList.add(new BudgetEvent(CategoryUtil.getColorID(mView.getContext(), transaction.getCategory().getColor())));
                scheduledMap.put(transaction.getDate(), colorList);
            } else {
                colorList.add(new BudgetEvent(R.color.colorPrimary));
                scheduledMap.put(selectedDate, colorList);
            }

            //Any call from asyncTask should not update this
            if(refreshView){
                mView.refreshCalendarView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform an AsyncTask for adding multiple decorators into the calendar view
     * @param element The list of transactions
     */
    private void performCalculationForDecorators(final List<Transaction> element){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                for (int i = 0; i < element.size(); i++) {
                    //Put only 1 indication for the event per day
                    if(!scheduledMap.containsKey(element.get(i).getDate())){
                        addDecoratorOnDate(element.get(i), false);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                mView.refreshCalendarView();
            }
        };
        loader.execute();
    }

    /**
     * Callback for populateTransactionsForDate1
     * @param date Date to update selectedDate to
     * @param callback Callback (if not null) to be called
     */
    private void populateTransactionsForDateDone(Date date, RealmHelper.RealmOperationCallback callback){
        //Wait until data has been fetch before trying to smooth scroll to the top,
        //otherwise the scroll will lag
        if(!DateUtil.refreshDate(date).equals(selectedDate)){
            mView.smoothScrollToPosition(0);
        }

        //Update date
        selectedDate = date;

        if(callback != null){
            callback.onComplete();
        }
    }
}
