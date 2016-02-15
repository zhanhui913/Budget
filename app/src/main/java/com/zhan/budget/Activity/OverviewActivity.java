package com.zhan.budget.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.zhan.budget.Adapter.CategoryListAdapter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.percentview.Model.Slice;
import com.zhan.percentview.PercentView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class OverviewActivity extends AppCompatActivity implements
        CategoryListAdapter.OnCategoryAdapterInteractionListener{

    private Toolbar toolbar;
    private Date currentMonth;
    private TextView dateTextView, totalCostForMonthTextView;
    private PercentView percentView;

    private Realm myRealm;
    private RealmResults<Category> resultsCategory;
    private RealmResults<Transaction> transactionsResults;

    private List<Transaction> transactionList;
    private List<Category> categoryList;
    private List<Slice> sliceList;

    private ListView categoryListView;
    private CategoryListAdapter categoryListAdapter;

    //private HashMap<String, Float> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        currentMonth = (Date)(getIntent().getExtras()).get(Constants.REQUEST_NEW_OVERVIEW_MONTH);

        init();
        createToolbar();
        addListeners();

        getCategoryList();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        categoryList = new ArrayList<>();
        categoryListView = (ListView) findViewById(R.id.percentCategoryListView);
        categoryListAdapter = new CategoryListAdapter(this, categoryList);
        categoryListView.setAdapter(categoryListAdapter);

        totalCostForMonthTextView = (TextView) findViewById(R.id.totalCostForMonth);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setText(Util.convertDateToStringFormat2(currentMonth));

        percentView = (PercentView) findViewById(R.id.percentView);
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Overview");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addListeners(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Should be called only the first time when the activity is created
    private void getCategoryList(){
        resultsCategory = myRealm.where(Category.class).findAllAsync();
        resultsCategory.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                categoryList.clear();
                categoryList = myRealm.copyFromRealm(resultsCategory);

                categoryListAdapter.addAll(categoryList);
                categoryListAdapter.notifyDataSetChanged();

                getMonthReport(currentMonth);
            }
        });
    }

    private void getMonthReport(Date date){
        //Refresh these variables
        final Date month = Util.refreshMonth(date);

        //Need to go a day before as Realm's between date does inclusive on both end
        final Date endMonth = Util.getPreviousDate(Util.getNextMonth(month));

        sliceList = new ArrayList<>();

        //map = new HashMap<String, Float>();

        Log.d("OVERVIEW_ACT", "("+Util.convertDateToStringFormat1(month) + "-> "+Util.convertDateToStringFormat1(endMonth)+")");

        transactionsResults = myRealm.where(Transaction.class).between("date", month, endMonth).findAllAsync();
        transactionsResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                if (transactionList != null) {
                    transactionList.clear();
                }

                transactionList = myRealm.copyFromRealm(transactionsResults);

                performAsyncCalculation();
            }
        });
    }

    /**
     * Perform tedious calculation asynchronously to avoid blocking main thread
     */
    private void performAsyncCalculation(){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("OVERVIEW_ACT", "preparing to aggregate results");
            }

            @Override
            protected Void doInBackground(Void... voids) {

                Log.d("OVERVIEW_ACT", "Transaction size : "+transactionList.size());

                startTime = System.nanoTime();

                //Go through each transaction and put them into the correct category
                for(int t = 0; t < transactionList.size(); t++){
                    for(int c = 0; c < categoryList.size(); c++){
                        if(transactionList.get(t).getCategory().getId().equalsIgnoreCase(categoryList.get(c).getId())){
                            float transactionPrice = transactionList.get(t).getPrice();
                            float currentCategoryPrice = categoryList.get(c).getCost();
                            categoryList.get(c).setCost(transactionPrice + currentCategoryPrice);
                        }
                    }
                }



/*
                for (int i = 0; i < transactionList.size(); i++) {
                    if(!map.containsKey(transactionList.get(i).getCategory().getId())){
                        map.put(transactionList.get(i).getCategory().getId(), transactionList.get(i).getPrice());
                    }else{
                        float f = transactionList.get(i).getPrice();
                        float m = map.get(transactionList.get(i).getCategory().getId());
                        map.put(transactionList.get(i).getCategory().getId(), f + m);
                    }
                }*/

                categoryListAdapter.notifyDataSetChanged();

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);

                float sumCost = 0;

/*
                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    //System.out.println(pair.getKey() + " = " + pair.getValue());

                    Slice slice = new Slice();
                    slice.setColor(R.color.colorPrimary);
                    slice.setWeight(Math.abs((float) (pair.getValue())));

                    Log.d("OVERVIEW_ACT", "weight :"+slice.getWeight());

                    sliceList.add(slice);

                    it.remove(); // avoids a ConcurrentModificationException
                }*/

                for(int i = 0; i < categoryList.size(); i++){
                    Log.d("ZHAN1", "category : " + categoryList.get(i).getName() + " -> " + categoryList.get(i).getCost());

                    if(categoryList.get(i).getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                        Slice slice = new Slice();
                        slice.setColor(categoryList.get(i).getColor());
                        slice.setWeight(categoryList.get(i).getCost());

                        sliceList.add(slice);
                        sumCost += categoryList.get(i).getCost();
                    }
                }

                totalCostForMonthTextView.setText(CurrencyTextFormatter.formatFloat(sumCost, Locale.CANADA));

                percentView.setSliceList(sliceList);

                endTime = System.nanoTime();
                duration = (endTime - startTime);
                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);
                Log.d("OVERVIEW_ACT", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(!myRealm.isClosed()){
            myRealm.close();
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        myRealm = Realm.getDefaultInstance();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Interfaces from CategoryListAdapter
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeleteCategory(int position){

    }

    @Override
    public void onEditCategory(int position){

    }

    @Override
    public void onDisablePtrPullDown(boolean value){

    }
}


