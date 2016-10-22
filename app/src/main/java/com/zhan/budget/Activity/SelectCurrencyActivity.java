package com.zhan.budget.Activity;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.CurrencyRecyclerAdapter;
import com.zhan.budget.Etc.ExchangeRate;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class SelectCurrencyActivity extends BaseRealmActivity implements
        CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener{

    private Toolbar toolbar;
    private List<BudgetCurrency> currencyList ;
    private OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private CurrencyRecyclerAdapter currencyAdapter;
    private RecyclerView currencyListView;

    private Activity instance;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_select_currency;
    }

    @Override
    protected void init(){
        instance = this;

        createToolbar();
        addListener();

        currencyList= new ArrayList<>();

        currencyListView = (RecyclerView)findViewById(R.id.currencyListview);
        currencyListView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Getting currency", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //getListOfCurrencies();

            }
        });

        createCurrencies();

        currencyAdapter = new CurrencyRecyclerAdapter(this, currencyList);
        currencyListView.setAdapter(currencyAdapter);

        //Add divider
        currencyListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());






        /*
        * ask for base currency
        * allow them to change currency while inputing transaction but default is base
        * if changing base currency, dont change existing ones but future ones will have its default
        * if setting currency to one that isnt in API, need to provide dialog and ask them  for manual exchange rate
        * if setting currency to one that is in API, we provide current exchange rate and allow them to edit it
        *
        *
        *
        * */

    }

    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_clear);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Select default currency");
        }
    }

    private void addListener(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createCurrencies(){
        String[] locales = Locale.getISOCountries();
        myRealm = Realm.getDefaultInstance();
        myRealm.beginTransaction();
        for (String countryCode : locales) {
            Locale obj = new Locale("", countryCode);
            Currency c = Currency.getInstance(obj);

            if(c == null){
                continue;
            }

            BudgetCurrency budgetCurrency = myRealm.createObject(BudgetCurrency.class);
            budgetCurrency.setId(Util.generateUUID());
            budgetCurrency.setCountry(obj.getDisplayCountry());
            budgetCurrency.setCurrencyCode(c.getCurrencyCode());
            budgetCurrency.setSymbol(c.getSymbol());
            budgetCurrency.setDefault(false);
            budgetCurrency.setRate(0f);
            budgetCurrency.setDate(DateUtil.formatDate(new Date()));

            currencyList.add(budgetCurrency);
        }
        myRealm.commitTransaction();

        //sort
        Collections.sort(currencyList, new Comparator<BudgetCurrency>() {
            @Override
            public int compare(BudgetCurrency c1, BudgetCurrency c2) {
                return c1.getCountry().compareTo(c2.getCountry()); // Ascending
            }

        });
    }

    @Override
    public void onClickCurrency(final int position){
/*
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText("Confirm Currency");
        message.setText("Are you sure you want to set "+currencyList.get(position).getCurrencyCode()+" as your default currency");

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {




                        myRealm.beginTransaction();

                        currencyList.get(position).setDefault(true);

                        //find exchange rate for this default compared to the rest of the currencies
                        for(int i = 0; i < currencyList.size(); i++){

                        }


                        myRealm.commitTransaction();
                        finish();


                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();

*/





        try{
            ExchangeRate exchangeRate = new ExchangeRate("USD", currencyList.get(position).getCurrencyCode(), 1000.50, new ExchangeRate.OnCurrencyConverterInteractionListener() {
                @Override
                public void onCompleteCalculation(double result) {
                    createToast("USD", currencyList.get(position).getCurrencyCode(), 1, result);
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void createToast(final String fromCurrency, final String toCurrency, final double fromAmount, final double toAmount){
        instance.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(instance, fromCurrency+fromAmount+" = "+toCurrency+toAmount, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
