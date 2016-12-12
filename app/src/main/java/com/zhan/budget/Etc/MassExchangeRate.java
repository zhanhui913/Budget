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

public class MassExchangeRate extends AsyncTask<Void, Integer, Boolean> {

    private OnMassExchangeRateInteractionListener mListener;

    private final String TAG = "CURRENCY";

    private Context context;
    private final ProgressDialog mDialog;
    private String fromCurrency;
    private int numFiles;
    private int index;

    private List<BudgetCurrency> currencyList;

    public void OnMassExchangeRateInteraction(OnMassExchangeRateInteractionListener listener){
        mListener = listener;
    }

    public MassExchangeRate(Context context, BudgetCurrency defaultCurrency, List<BudgetCurrency> currencyList, MassExchangeRate.OnMassExchangeRateInteractionListener mListener) {
        this.context = context;
        this.fromCurrency = defaultCurrency.getCurrencyCode();
        this.currencyList = currencyList;
        this.numFiles = currencyList.size();
        this.mListener = mListener;


        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage("Packaging.");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params){
        try {
            String toCurrency;
            for(int i = 0; i < currencyList.size(); i++){
                index = i;
                toCurrency = currencyList.get(i).getCurrencyCode();

                Log.d("MassExchangeRate", "starting calculation for "+toCurrency);

                new ExchangeRate(fromCurrency, toCurrency, 1, new ExchangeRate.OnExchangeRateInteractionListener() {
                    @Override
                    public void onCompleteCalculation(double result) {
                        currencyList.get(index).setRate(result);
                        publishProgress(index);
                    }

                    @Override
                    public void onFailedCalculation() {
                        currencyList.get(index).setRate(0f);
                        publishProgress(index);
                    }
                });
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        int percent = (int)(100.0 * (((double)progress[0] + 1) / (numFiles + 0.5)));
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            showToast("Packaging successful.");
        } else {
            showToast("Packaging failed, try again.");
        }

        if(mListener != null) {
            mListener.onCompleteAllCurrencyCalculation();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    //Interface needed for caller
    public interface OnMassExchangeRateInteractionListener {
        void onCompleteAllCurrencyCalculation();
    }
}
