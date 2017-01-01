package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.CurrencyRecyclerAdapter;
import com.zhan.budget.Etc.CurrencyXMLHandler;
import com.zhan.budget.Etc.MassExchangeRate;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.R;

import org.parceler.Parcels;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;


public class SelectCurrencyActivity extends BaseRealmActivity implements
        CurrencyRecyclerAdapter.OnCurrencyAdapterInteractionListener,
        SearchView.OnQueryTextListener{

    public static final String RESULT_CURRENCY = "Result Currency";

    public static final String NEED_RETURN_CURRENCY = "Currency";

    private Toolbar toolbar;
    private Activity instance;

    private List<BudgetCurrency> currencyList ;
    private CurrencyRecyclerAdapter currencyAdapter;
    private RecyclerView currencyListView;
    private HashMap<String, Double> currencyMap;

    private RealmResults<BudgetCurrency> resultsCurrency;

    /**
     * If true, this selection on a specific currency returns the Currency.
     * If false, this selection on a specific currency simply sets the default currency. (No need to return anything)
     */
    private boolean returnCurrency = true;

    public static Intent createIntentToGetCurrency(Context context){
        Intent intent = new Intent(context, SelectCurrencyActivity.class);
        intent.putExtra(NEED_RETURN_CURRENCY, true);
        return intent;
    }

    public static Intent createIntentToSetDefaultCurrency(Context context){
        Intent intent = new Intent(context, SelectCurrencyActivity.class);
        intent.putExtra(NEED_RETURN_CURRENCY, false);
        return intent;
    }

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_select_currency;
    }

    @Override
    protected void init(){
        super.init();

        instance = this;

        if(getIntent().getExtras() != null){
            returnCurrency = (getIntent().getExtras()).getBoolean(NEED_RETURN_CURRENCY);
        }

        createToolbar();
        addListener();

        currencyList= new ArrayList<>();
        currencyMap = new HashMap<>();

        currencyListView = (RecyclerView)findViewById(R.id.currencyListview);
        currencyListView.setLayoutManager(new LinearLayoutManager(this));

        currencyAdapter = new CurrencyRecyclerAdapter(this, currencyList);
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
            if(!returnCurrency){
                getSupportActionBar().setTitle("Set currency");
            }else{
                getSupportActionBar().setTitle("Get currency");
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
        //Check for number of rows in BudgetCurrency to determine if there is any data at all
        resultsCurrency = myRealm.where(BudgetCurrency.class).findAllSortedAsync("currencyCode");
        resultsCurrency.addChangeListener(new RealmChangeListener<RealmResults<BudgetCurrency>>() {
            @Override
            public void onChange(RealmResults<BudgetCurrency> element) {
                element.removeChangeListener(this);

                Toast.makeText(instance, "there are "+element.size()+" in list", Toast.LENGTH_SHORT).show();

                // If there are for some reason fewer currencies in Realm than in XML.
                // Delete all entries in the realm first, then recreate it by reading from xml
                if(element.size() < getNumOfCurrenciesInXML()){
                    deleteAllCurrenciesInRealm();
                    addCurrencyToRealmFromXML();
                }

                readFromCurrencyRealm();
            }
        });
    }

    /**
     * Reads the currencies.xml file and returns the number of currencies
     * @return The number of currencies in list
     */
    private int getNumOfCurrenciesInXML(){
        try {
            InputStream iss = getResources().openRawResource(R.raw.currencies);

            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

            CurrencyXMLHandler currencyXMLHandler = new CurrencyXMLHandler();
            xmlReader.setContentHandler(currencyXMLHandler);
            xmlReader.parse(new InputSource(iss));

            return currencyXMLHandler.getCurrencies().size();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Delete all entries in BudgetCurrency table
     */
    private void deleteAllCurrenciesInRealm(){
        resultsCurrency = myRealm.where(BudgetCurrency.class).findAll();

        // All changes to data must happen in a transaction
        myRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Delete all matches
                resultsCurrency.deleteAllFromRealm();
            }
        });
    }

    private void addCurrencyToRealmFromXML(){
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
                }

                Toast.makeText(getApplicationContext(), "Read from xml currency", Toast.LENGTH_SHORT).show();
            }catch(RealmPrimaryKeyConstraintException e){
                e.printStackTrace();
            }
            myRealm.commitTransaction();

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void readFromCurrencyRealm(){
        resultsCurrency = myRealm.where(BudgetCurrency.class).findAllSortedAsync("currencyCode");
        resultsCurrency.addChangeListener(new RealmChangeListener<RealmResults<BudgetCurrency>>() {
            @Override
            public void onChange(RealmResults<BudgetCurrency> element) {
                element.removeChangeListener(this);

                currencyList = myRealm.copyFromRealm(element);
                currencyAdapter.setBudgetCurrencyList(currencyList);
                Toast.makeText(getApplicationContext(), "Read from realm currency", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Perform mass exchange rate conversion between Currency and all others
     * @param currency Selected Currency
     */
    private void convertDefaultCurrency(final BudgetCurrency currency){
        List<BudgetCurrency>  tempCurrencyList = myRealm.copyFromRealm(resultsCurrency);

        MassExchangeRate massExchangeRate = new MassExchangeRate(instance, currency, tempCurrencyList, new MassExchangeRate.OnMassExchangeRateInteractionListener() {
            @Override
            public void onCompleteAllCurrencyCalculation(List<BudgetCurrency> results) {
                Toast.makeText(getApplicationContext(), "MASS CALCULATION COMPLETED",Toast.LENGTH_SHORT).show();

                //update results in Realm
                myRealm.beginTransaction();

                for(int i = 0; i < results.size(); i++){
                    for(int k = 0; k < resultsCurrency.size(); k++){
                        if(results.get(i).getCurrencyCode().equalsIgnoreCase(resultsCurrency.get(k).getCurrencyCode())){
                            resultsCurrency.get(k).setRate(results.get(i).getRate());
                        }
                    }
                }

                myRealm.commitTransaction();


                Intent intent = new Intent();
                Parcelable wrapped = Parcels.wrap(currency);
                intent.putExtra(RESULT_CURRENCY, wrapped);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        massExchangeRate.execute();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Menu
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem searchItem = menu.findItem(R.id.searchBtn);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Search functionality
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("filter", newText);
        currencyAdapter.getFilter().filter(newText);

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClickCurrency(final int position){
        //No need to process the click on a Budget Currency that is already the default
        if(!resultsCurrency.get(position).isDefault()) {

            //Coming from CalendarFragment or SettingsFragment
            if (!returnCurrency) {
                View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

                TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
                TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

                title.setText("Confirm Currency");
                message.setText("Are you sure you want to set " + currencyList.get(position).getCurrencyCode() + " as your default currency.");

                new AlertDialog.Builder(this)
                        .setView(promptView)
                        .setCancelable(true)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                myRealm.beginTransaction();
                                for (int k = 0; k < resultsCurrency.size(); k++) {
                                    resultsCurrency.get(k).setDefault(false);
                                    if(resultsCurrency.get(k).getCurrencyName().equalsIgnoreCase(currencyList.get(position).getCurrencyName())){
                                        resultsCurrency.get(k).setDefault(true);
                                    }
                                }

                                myRealm.commitTransaction();

                                convertDefaultCurrency(resultsCurrency.get(position));
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
            } else {
                //Coming from TransactionInfoActivity
                Intent intent = new Intent();

                Parcelable wrapped = Parcels.wrap(currencyList.get(position));
                intent.putExtra(RESULT_CURRENCY, wrapped);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}
