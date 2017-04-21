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

import io.realm.RealmObject;

/**
 * Listens to user actions from the UI {@link CalendarFragment}, retrieves the data and updates the
 * UI as required.
 */
public class CalendarPresenter implements CalendarContract.Presenter{

    private static final String TAG = "CalendarPresenter";

    private List<Transaction> transactions;
    private Date selectedDate;
    private Map<Date,List<BudgetEvent>> scheduledMap;

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
        populateTransactionsForDate1(date, null);
    }

    @Override
    public void populateTransactionsForDate1(Date date, final RealmHelper.RealmOperationCallback callback){
        final Date startDate = DateUtil.refreshDate(date);
        final Date endDate = DateUtil.getNextDate(date);

        //Update date text view in center panel
        mView.updateDateTextview(DateUtil.convertDateToStringFormat1(mView.getContext(), startDate));

        Log.d(TAG, " populate transaction list (" + DateUtil.convertDateToStringFormat5(mView.getContext(), startDate) + " -> " + DateUtil.convertDateToStringFormat5(mView.getContext(), endDate) + ")");

        //Remove all list while preparing for new list
        mView.updateTransactions(new ArrayList<Transaction>());
        mView.updateTotalCostView(0);

        //Change to VISIBLE while preparing to do calculation
        mView.setLoadingIndicator(true);

        mAppDataManager.getTransactions(startDate, new RealmHelper.LoadTransactionsCallback() {
            @Override
            public void onTransactionsLoaded(List<Transaction> list) {
                Log.d(TAG, "on transactions loaded "+list.size());

                double sumValue = CurrencyTextFormatter.findTotalCostForTransactions(list);

                transactions = list;

                mView.updateTotalCostView(sumValue);
                mView.updateTransactions(list);

                //Wait until data has been fetch before trying to smooth scroll to the top,
                //otherwise the scroll will lag
                if(!DateUtil.refreshDate(startDate).equals(selectedDate)){
                    mView.smoothScrollToPosition(0);
                }

                //Update date
                selectedDate = startDate;

                if(callback != null){
                    callback.onComplete();
                }
            }

            @Override
            public void onDataNotAvailable() {
                Log.d(TAG, "on transactions no data available");

                transactions = new ArrayList<>();

                mView.updateTotalCostView(0);
                mView.updateTransactions(new ArrayList<Transaction>());

                //Wait until data has been fetch before trying to smooth scroll to the top,
                //otherwise the scroll will lag
                if(!DateUtil.refreshDate(startDate).equals(selectedDate)){
                    mView.smoothScrollToPosition(0);
                }

                //Update date
                selectedDate = startDate;

                if(callback != null){
                    callback.onComplete();
                }
            }

            @Override
            public void onFail(){
                Log.d(TAG, "on failed");

                mView.setLoadingIndicator(false);

                if(callback != null){
                    callback.onComplete();
                }
            }
        });
    }

    @Override
    public void updateDecorations(){
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

            }
        });
    }

    private void performCalculationForDecorators(final List<Transaction> element){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                for (int i = 0; i < element.size(); i++) {
                    List<BudgetEvent> colorList = new ArrayList<>();
                    try {
                        //Only put 1 indication for the event per day
                        if(!scheduledMap.containsKey(element.get(i).getDate())){
                            if(element.get(i).getCategory() != null){
                                colorList.add(new BudgetEvent(CategoryUtil.getColorID(mView.getContext(), element.get(i).getCategory().getColor())));
                            }else{
                                colorList.add(new BudgetEvent(R.color.colorPrimary));
                            }

                            scheduledMap.put(element.get(i).getDate(), colorList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                mView.updateCalendarView(selectedDate);
            }
        };
        loader.execute();
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
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(TransactionInfoActivity.RESULT_TRANSACTION));

                populateTransactionsForDate1(tt.getDate(), new RealmHelper.RealmOperationCallback() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "new transaction populateTransactionsForDate1 completed, start decoration operation");

                        updateDecorations();
                    }
                });

                mView.updateCalendarView(selectedDate);
            }else if(requestCode == RequestCodes.EDIT_TRANSACTION){
                Transaction tt = Parcels.unwrap(data.getExtras().getParcelable(TransactionInfoActivity.RESULT_TRANSACTION));

                populateTransactionsForDate1(tt.getDate(), new RealmHelper.RealmOperationCallback() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "edit transaction populateTransactionsForDate1 completed, start decoration operation");

                        updateDecorations();
                    }
                });

                mView.updateCalendarView(selectedDate);
            }
        }
    }

    @Override
    public void addTransaction(){
        mView.showAddTransaction();
    }

    @Override
    public void editTransaction(int position){
        mView.showEditTransaction(transactions.get(position));
    }

    @Override
    public void deleteTransaction(int position){
        mAppDataManager.deleteTransaction(transactions.get(position).getId(), new RealmHelper.DeleteTransactionCallback() {
            @Override
            public void onSuccess() {
                populateTransactionsForDate1(selectedDate, new RealmHelper.RealmOperationCallback() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "delete populateTransactionsForDate1 completed, start decoration operation");

                        updateDecorations();
                    }
                });
            }

            @Override
            public void onFailed() {
                mView.showSnackbar("Delete transaction failed");
            }
        });
    }

    @Override
    public void approveTransaction(int position){
        mAppDataManager.approveTransaction(transactions.get(position).getId(), new RealmHelper.LoadTransactionCallback() {
            @Override
            public <T extends RealmObject> void onTransactionLoaded(T realmObject) {
                populateTransactionsForDate1(selectedDate, new RealmHelper.RealmOperationCallback() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "approve populateTransactionsForDate1 completed, start decoration operation");

                        updateDecorations();
                    }
                });
            }

            @Override
            public void onDataNotAvailable() {
                //No need to do anything
                mView.showSnackbar("Approve transaction failed");
            }
        });
    }

    @Override
    public void unApproveTransaction(int position){
        mAppDataManager.unapproveTransaction(transactions.get(position).getId(), new RealmHelper.LoadTransactionCallback() {
            @Override
            public <T extends RealmObject> void onTransactionLoaded(T realmObject) {
                populateTransactionsForDate1(selectedDate, new RealmHelper.RealmOperationCallback() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "unapprove populateTransactionsForDate1 completed, start decoration operation");

                        updateDecorations();
                    }
                });
            }

            @Override
            public void onDataNotAvailable() {
                //No need to do anything
                mView.showSnackbar("Unapprove transaction failed");
            }
        });
    }
}
