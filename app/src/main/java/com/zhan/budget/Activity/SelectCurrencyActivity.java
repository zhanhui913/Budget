package com.zhan.budget.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.CurrencyRecyclerAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyXMLHandler;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import org.parceler.Parcels;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.SAXParserFactory;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;


public class SelectCurrencyActivity extends BaseRealmActivity implements
        CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener{

    private Toolbar toolbar;
    private Activity instance;

    private List<BudgetCurrency> currencyList ;
    private CurrencyRecyclerAdapter currencyAdapter;
    private RecyclerView currencyListView;
    private HashMap<String, Double> currencyMap;

    //by default this activity is in settings, when this is called the first time it should be false
    private boolean inSettings = true;

    //by default this activity is just listing currency
    private boolean defaultCurrencySelection = false;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_select_currency;
    }

    @Override
    protected void init(){
        super.init();

        instance = this;

        if(getIntent().getExtras() != null){
            inSettings = (getIntent().getExtras()).getBoolean(Constants.REQUEST_CURRENCY_IN_SETTINGS);
            defaultCurrencySelection =(getIntent().getExtras()).getBoolean(Constants.REQUEST_DEFAULT_CURRENCY);
        }

        createToolbar();
        addListener();

        currencyList= new ArrayList<>();
        currencyMap = new HashMap<>();

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



        currencyAdapter = new CurrencyRecyclerAdapter(this, inSettings, currencyList);
        currencyListView.setAdapter(currencyAdapter);

        //Add divider
        currencyListView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());

        createCurrencies();

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
            if(defaultCurrencySelection){
                getSupportActionBar().setTitle("Select default currency");
            }else{
                getSupportActionBar().setTitle("Select currency");
            }
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
        boolean isFirstTimeCurrency = BudgetPreference.getFirstTimeCurrency(getApplicationContext());

        if(isFirstTimeCurrency){
            readFromCurrencyXML();
            BudgetPreference.setFirstTimeCurrency(getApplicationContext());
        }else{
            readFromCurrencyRealm();
        }

    }

    private void readFromCurrencyXML(){
        try{
            InputStream iss = getResources().openRawResource(R.raw.currencies);

            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

            CurrencyXMLHandler currencyXMLHandler = new CurrencyXMLHandler();
            xmlReader.setContentHandler(currencyXMLHandler);
            xmlReader.parse(new InputSource(iss));

            List<BudgetCurrency> tempList = currencyXMLHandler.getCurrencies();


            //Add into realm
            myRealm.beginTransaction();
            try{
                for(int i = 0; i < tempList.size(); i++){
                    BudgetCurrency budgetCurrency = myRealm.createObject(BudgetCurrency.class);

                    budgetCurrency.setCurrencyName(tempList.get(i).getCurrencyName());
                    budgetCurrency.setCurrencyCode(tempList.get(i).getCurrencyCode());
                    budgetCurrency.setDefault(false);
                    budgetCurrency.setRate(0f);

                    currencyList.add(budgetCurrency);
                }

                Toast.makeText(getApplicationContext(), "Read from xml currency", Toast.LENGTH_SHORT).show();
            }catch(RealmPrimaryKeyConstraintException e){
                e.printStackTrace();
            }
            myRealm.commitTransaction();

            currencyAdapter.setBudgetCurrencyList(currencyList);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void readFromCurrencyRealm(){
        RealmResults<BudgetCurrency> results = myRealm.where(BudgetCurrency.class).findAllSortedAsync("currencyCode");
        results.addChangeListener(new RealmChangeListener<RealmResults<BudgetCurrency>>() {
            @Override
            public void onChange(RealmResults<BudgetCurrency> element) {
                element.removeChangeListener(this);

                currencyList = myRealm.copyFromRealm(element);
                currencyAdapter.setBudgetCurrencyList(currencyList);
                Toast.makeText(getApplicationContext(), "Read from realm currency", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onClickCurrency(final int position){
        if(defaultCurrencySelection){
            View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

            TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
            TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

            title.setText("Confirm Currency");
            message.setText("Are you sure you want to set "+currencyList.get(position).getCurrencyCode()+" as your default currency.");

            new AlertDialog.Builder(this)
                    .setView(promptView)
                    .setCancelable(true)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            myRealm.beginTransaction();

                            currencyList.get(position).setDefault(true);

                            //find exchange rate for this default compared to the rest of the currencies

                        /*
                        final String defaultCurrencyCode = currencyList.get(position).getCurrencyCode();

                        //option 2
                        Iterator iterator = currencyMap.entrySet().iterator();
                        while(iterator.hasNext()) {
                             Map.Entry pair = (Map.Entry)iterator.next();
                            final String toCurrency = pair.getKey().toString();

                            ExchangeRate exchangeRate = new ExchangeRate(defaultCurrencyCode, toCurrency, 1, new ExchangeRate.OnExchangeRateInteractionListener() {
                                @Override
                                public void onCompleteCalculation(double result) {
                                    Log.d("EXCHANGE", 1+defaultCurrencyCode+" = "+result+toCurrency);
                                }

                                @Override
                                public void onFailedCalculation(){
                                    Log.d("EXCHANGE", 1+defaultCurrencyCode+" = "+ toCurrency+" -----------  FAILED");
                                }
                            });
                        }*/


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
        }else{
            Intent intent = new Intent();

            Parcelable wrapped = Parcels.wrap(currencyList.get(position));
            intent.putExtra(Constants.RESULT_EDIT_CURRENCY, wrapped);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onCurrencySetAsDefault(int position){
        myRealm.beginTransaction();
        for(int i = 0; i < currencyList.size(); i++){
            currencyList.get(i).setDefault(false);
        }
        currencyList.get(position).setDefault(true);
        myRealm.commitTransaction();

        //Using any subclass of view to get parent view (cannot use root view as it will appear on (devices with navigation panel) the bottom
        Util.createSnackbar(this, (View)currencyListView.getParent(), "Set "+currencyList.get(position).getCurrencyCode()+" as default currency");

        //currencyList = myRealm.copyFromRealm(currencyAdapter);
        currencyAdapter.setBudgetCurrencyList(currencyList);
    }

    @Override
    public void onCurrencyDeSetFromDefault(int position){
        myRealm.beginTransaction();
        currencyList.get(position).setDefault(false);
        myRealm.commitTransaction();

        //Using any subclass of view to get parent view (cannot use root view as it will appear on (devices with navigation panel) the bottom
        Util.createSnackbar(this, (View)currencyListView.getParent(), "Remove "+currencyList.get(position).getCurrencyCode()+" as default currency");

        //currencyList = myRealm.copyFromRealm(resultsAccount);
        currencyAdapter.setBudgetCurrencyList(currencyList);
    }
}
