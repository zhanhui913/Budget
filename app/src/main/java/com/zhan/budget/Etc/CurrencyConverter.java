package com.zhan.budget.Etc;

import android.util.Log;

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

/**
 * Created by zhanyap on 2016-10-21.
 */

public class CurrencyConverter {
    private CurrencyConverter.OnCurrencyConverterInteractionListener mListener;

    private final String TAG = "CURRENCY";
    private OkHttpClient client = new OkHttpClient();

    private String URL_INT = "https://www.google.com/finance/converter?a=%s&from=%s&to=%1$d";
    private String URL_DOUBLE = "https://www.google.com/finance/converter?a=%s&from=%s&to=%.2f";

    private String fromCurrency;
    private String toCurrency;
    private int fromAmountInteger;
    private double fromAmountDouble;

    public CurrencyConverter(String fromCurrency, String toCurrency, int fromAmount, OnCurrencyConverterInteractionListener mListener) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmountInteger = fromAmount;
        this.mListener = mListener;

        callGoogleFinanceAPI(String.format(Locale.US, URL_INT, fromCurrency, toCurrency, fromAmount));
    }

    public CurrencyConverter(String fromCurrency, String toCurrency, double fromAmount, OnCurrencyConverterInteractionListener mListener) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmountDouble = fromAmount;
        this.mListener = mListener;

        callGoogleFinanceAPI(String.format(Locale.US, URL_DOUBLE, fromCurrency, toCurrency, fromAmount));
    }

    public void callGoogleFinanceAPI(String url){
        Log.d("CURRENCY", "url : "+url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                /*Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    Log.d("CURRENCY",responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }*/

                //Can only call response.body().string() once
                String body = response.body().string();
                Document doc = Jsoup.parse(body);

                Element el = doc.select("#currency_converter_result .bld").first(); Log.d("CURRENCY", "dd : "+el.text());
                String val =  el.text().split("\\s+")[0];
                Log.d("CURRENCY", val);

                //createToast(fromCurrency, toCurrency, fromAmount, Double.parseDouble(val));

                if(mListener != null){
                    mListener.onCompleteCalculation(Double.parseDouble(val));
                }
            }
        });
    }

    //Interface needed for caller
    public interface OnCurrencyConverterInteractionListener {
        void onCompleteCalculation(double result);
    }
}
