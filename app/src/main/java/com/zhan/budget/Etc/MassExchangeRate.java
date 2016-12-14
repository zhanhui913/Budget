package com.zhan.budget.Etc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.zhan.budget.Model.Realm.BudgetCurrency;

import java.util.List;

/**
 * Created by Zhan on 2016-12-11.
 */

public class MassExchangeRate extends AsyncTask<Void, Integer, Void> {

    private OnMassExchangeRateInteractionListener mListener;

    private final String TAG = "CURRENCY";

    private Context context;
    private final ProgressDialog mDialog;
    private String fromCurrency;
    private int numFiles;
    private int index;
    private List<BudgetCurrency> currencyList;

    private int numProcessed;//number of exchange rate calculation that has been completed

    public void OnMassExchangeRateInteraction(OnMassExchangeRateInteractionListener listener){
        mListener = listener;
    }

    public MassExchangeRate(Context context, BudgetCurrency defaultCurrency, List<BudgetCurrency> currencyList, MassExchangeRate.OnMassExchangeRateInteractionListener mListener) {
        this.context = context;
        this.fromCurrency = defaultCurrency.getCurrencyCode();
        this.currencyList = currencyList;
        this.numFiles = currencyList.size();
        this.mListener = mListener;
        this.numProcessed = 0;

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage("Packaging.");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setCancelable(false);
        mDialog.show();
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
                        Log.d("EXCHANGE", currencyList.get(position).getCurrencyCode()+" -----> "+currencyList.get(position).getRate());
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
        int percent = (int)(100.0 * (((double)progress[0] + 1) / (numFiles + 0.5)));
        mDialog.setProgress(percent);
        numProcessed++;

        if(numProcessed == numFiles){
            onPostExecute(null);
        }
    }

    @Override
    protected void onPostExecute(Void params) {
        if(numProcessed == numFiles){
            mDialog.dismiss();
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
