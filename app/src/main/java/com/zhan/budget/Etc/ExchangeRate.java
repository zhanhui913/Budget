package com.zhan.budget.Etc;

/*
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
*/
/**
 * Created by zhanyap on 2016-10-21.
 */

public class ExchangeRate {
    /*private ExchangeRate.OnExchangeRateInteractionListener mListener;

    private final String TAG = "CURRENCY";
    private OkHttpClient client = new OkHttpClient();

    private String URL_INT = "https://www.google.com/finance/converter?a=%1d&from=%s&to=%s";
    private String URL_DOUBLE = "https://www.google.com/finance/converter?a=%.2f&from=%s&to=%s";

    private String fromCurrency;
    private String toCurrency;
    private int fromAmountInteger;
    private double fromAmountDouble;
    private int index;

    public ExchangeRate(String fromCurrency, String toCurrency, int fromAmount, int index, OnExchangeRateInteractionListener mListener) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmountInteger = fromAmount;
        this.index = index;
        this.mListener = mListener;

        //If converting to same currency, just return 1.
        if(fromCurrency.equalsIgnoreCase(toCurrency)){
            Log.d("EXCHANGE", "1) "+fromCurrency+" -> "+toCurrency+", "+1d);

            mListener.onCompleteCalculation(index, 1d);
        }else{
            callGoogleFinanceAPI(String.format(Locale.US, URL_INT, fromAmount, fromCurrency, toCurrency));
        }
    }

    public ExchangeRate(String fromCurrency, String toCurrency, double fromAmount, int index, OnExchangeRateInteractionListener mListener) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmountDouble = fromAmount;
        this.index = index;
        this.mListener = mListener;

        //If converting to same currency, just return 1.
        if(fromCurrency.equalsIgnoreCase(toCurrency)){
            Log.d("EXCHANGE", "1) "+fromCurrency+" -> "+toCurrency+", "+1d);

            mListener.onCompleteCalculation(index, 1d);
        }else{
            callGoogleFinanceAPI(String.format(Locale.US, URL_DOUBLE, fromAmount, fromCurrency, toCurrency));
        }
    }

    public void callGoogleFinanceAPI(String url){
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //No internet
                mListener.onFailedCalculation(index);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    mListener.onFailedCalculation(index);
                    throw new IOException("Unexpected code " + response);
                }
                //Headers responseHeaders = response.headers();
                //for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                //    Log.d("CURRENCY",responseHeaders.name(i) + ": " + responseHeaders.value(i));
                //}

                //Can only call response.body().string() once
                String body = response.body().string();
                Document doc = Jsoup.parse(body);

                Element el = doc.select("#currency_converter_result .bld").first();
                if(el != null){
                    String val =  el.text().split("\\s+")[0];
                    mListener.onCompleteCalculation(index, Double.parseDouble(val));
                }else{
                    mListener.onFailedCalculation(index);
                }
            }
        });
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public int getFromAmountInteger() {
        return fromAmountInteger;
    }

    public void setFromAmountInteger(int fromAmountInteger) {
        this.fromAmountInteger = fromAmountInteger;
    }

    public double getFromAmountDouble() {
        return fromAmountDouble;
    }

    //Interface needed for caller
    public interface OnExchangeRateInteractionListener {
        void onCompleteCalculation(int position, double result);

        void onFailedCalculation(int position);
    }*/
}
