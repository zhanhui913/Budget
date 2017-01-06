package com.zhan.budget.Etc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.R;

import java.util.List;

/**
 * Created by Zhan on 2016-12-11.
 */

public class MassExchangeRate extends AsyncTask<Void, Integer, Void> {

    private OnMassExchangeRateInteractionListener mListener;

    private final String TAG = "CURRENCY";

    private Context context;
    private RoundCornerProgressBar mDialog;
    private String fromCurrency;
    private int numFiles;
    private int index;
    private List<BudgetCurrency> currencyList;
    private AlertDialog alertDialog;
    private TextView percentTextView, progressTextView;

    private int numProcessed;//number of exchange rate calculation that has been completed

    public void OnMassExchangeRateInteraction(OnMassExchangeRateInteractionListener listener){
        mListener = listener;
    }

    public MassExchangeRate(Context context, BudgetCurrency defaultCurrency, List<BudgetCurrency> currencyList) {
        this.context = context;
        this.fromCurrency = defaultCurrency.getCurrencyCode();
        this.currencyList = currencyList;
        this.numFiles = currencyList.size();
        this.numProcessed = 0;

        View promptView = View.inflate(context, R.layout.alertdialog_progressbar, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        title.setText(R.string.packaging_currency_title);

        mDialog = (RoundCornerProgressBar) promptView.findViewById(R.id.progressBar);
        mDialog.setMax(currencyList.size());
        mDialog.setProgressColor(ContextCompat.getColor(context, R.color.colorPrimary));

        percentTextView = (TextView)promptView.findViewById(R.id.percentTextView);
        progressTextView = (TextView)promptView.findViewById(R.id.progressTextView);

        percentTextView.setText(String.format(context.getString(R.string.dialog_progress_percent), 0));
        progressTextView.setText(String.format(context.getString(R.string.dialog_progress_total), 0, currencyList.size()));

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(promptView)
                .setNegativeButton(context.getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        MassExchangeRate.this.cancel(true);
                    }
                });

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void setMassExchangeListener(MassExchangeRate.OnMassExchangeRateInteractionListener mListener){
        this.mListener = mListener;
    }

    @Override
    protected Void doInBackground(Void... params){
        try {

            for(int i = 0; i < currencyList.size(); i++){
                index = i;
                final String toCurrency = currencyList.get(index).getCurrencyCode();

                new ExchangeRate(fromCurrency, toCurrency, 1, index, new ExchangeRate.OnExchangeRateInteractionListener() {
                    @Override
                    public void onCompleteCalculation(int position, double result) {
                        //Log.d("EXCHANGE", "1 "+fromCurrency+" -> "+result+" "+toCurrency);
                        currencyList.get(position).setRate(result);
                        //Log.d("EXCHANGE", currencyList.get(position).getCurrencyCode()+" -----> "+currencyList.get(position).getRate());
                        publishProgress(index);
                    }

                    @Override
                    public void onFailedCalculation(int position) {
                        currencyList.get(position).setRate(0f);
                        publishProgress(index);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        //int percent = (int)(100.0 * (((double)progress[0] + 1) / (numFiles + 0.5)));
        //mDialog.setProgress(percent);
        numProcessed++;

        Log.d("EXCHANGE", "progress : "+progress[0]+", numPro "+numProcessed+", numFiles: "+numFiles);


        mDialog.setProgress(progress[0]);
        percentTextView.setText(String.format(context.getString(R.string.dialog_progress_percent), Math.round((progress[0] / (float)currencyList.size()) * 100)));
        progressTextView.setText(String.format(context.getString(R.string.dialog_progress_total), progress[0], currencyList.size()));


        if(numProcessed == numFiles){
            onPostExecute(null);
        }
    }

    @Override
    protected void onPostExecute(Void params) {
        if(numProcessed == numFiles){
            alertDialog.dismiss();
            Log.d("EXCHANGE", "=========== DONE ===========");

            if(mListener != null) {
                mListener.onCompleteAllCurrencyCalculation(currencyList);
            }
        }
    }

    //Interface needed for caller
    public interface OnMassExchangeRateInteractionListener {
        void onCompleteAllCurrencyCalculation(List<BudgetCurrency> results);
    }
}
