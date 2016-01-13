package com.zhan.budget.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.CalendarFragment;
import com.zhan.budget.Fragment.CategoryFragment;
import com.zhan.budget.Fragment.OverviewFragment;
import com.zhan.budget.Fragment.SendFragment;
import com.zhan.budget.Fragment.ShareFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarFragment.OnCalendarListener,
        CategoryFragment.OnCategoryInteractionListener,
        OverviewFragment.OnOverviewInteractionListener,
        SendFragment.OnSendInteractionListener,
        ShareFragment.OnShareInteractionListener{


    MainActivity activity;
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    private CalendarFragment calendarFragment;
    private CategoryFragment categoryFragment;
    private OverviewFragment overviewFragment;
    private ShareFragment shareFragment;
    private SendFragment sendFragment;

    private ArrayList<Category> categoryList = new ArrayList<>();

    private Realm myRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFragments();
        init();
    }

    private void createFragments(){
        calendarFragment = new CalendarFragment();
        categoryFragment = new CategoryFragment();
        overviewFragment = new OverviewFragment();
        shareFragment = new ShareFragment();
        sendFragment = new SendFragment();
    }

    private void init(){
        activity = MainActivity.this;

        myRealm = Realm.getInstance(getApplicationContext());

        isFirstTime();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //code to load my fragment
        getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, calendarFragment).commit();

        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void isFirstTime(){
        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        boolean isFirstTIme = sharedPreferences.getBoolean(Constants.FIRST_TIME, true);

        if(isFirstTIme){
            createDefaultCategory();
            createFakeTransactions();

            //set Constants.FIRST_TIME shared preferences to false
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.FIRST_TIME, false);
            editor.apply();
        }
    }

    private void createDefaultCategory(){
        myRealm.beginTransaction();

        String[] tempCategoryNameList = new String[]{"Breakfast","Lunch","Dinner", "Snacks","Drink","Rent","Travel","Car","Shopping","Necessity","Utilities","Bill","Groceries"};
        String[] tempCategoryColorList = new String[]{"F1C40F","E67E22","D35400", "F2784B","FDE3A7","6C7A89","19B5FE","16A085","BF55EC","E26A6A","81CFE0","26A65B","BFBFBF"};
        int[] tempCategoryIconList = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12};

        //create expense category
        for(int i = 0; i < tempCategoryNameList.length; i++){
            Category c = myRealm.createObject(Category.class);
            c.setId(Util.generateUUID());
            c.setName(tempCategoryNameList[i]);
            c.setColor("#" + tempCategoryColorList[i]);
            c.setIcon(tempCategoryIconList[i]);
            c.setBudget(100.0f);
            c.setType(BudgetType.EXPENSE.toString());
            c.setCost(0);

            categoryList.add(c);
        }


        String[] tempCategoryIncomeNameList = new String[]{"Salary", "Other"};
        String[] tempCategoryIncomeColorList  = new String[]{"8E44AD","34495E"};
        int[] tempCategoryIncomeIconList = new int[]{11,9};
        //create income category
        for(int i = 0; i < tempCategoryIncomeNameList.length; i++){
            Category c = myRealm.createObject(Category.class);
            c.setId(Util.generateUUID());
            c.setName(tempCategoryIncomeNameList[i]);
            c.setColor("#" + tempCategoryIncomeColorList[i]);
            c.setIcon(tempCategoryIncomeIconList[i]);
            c.setBudget(0);
            c.setType(BudgetType.INCOME.toString());
            c.setCost(0);

            categoryList.add(c);
        }

        myRealm.commitTransaction();
    }

    private void createFakeTransactions(){
        long startTime, endTime, duration;


        Date startDate = Util.convertStringToDate("2014-12-01");
        Date endDate = Util.convertStringToDate("2016-02-01");

        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);

        final ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        myRealm.beginTransaction();
        Random random = new Random();

        startTime = System.nanoTime();

        for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
            //Create 25 transactions per day
            for(int j = 0; j < 25; j++){
                Transaction transaction = myRealm.createObject(Transaction.class);
                transaction.setId(Util.generateUUID());
                transaction.setDate(date);

                Category category  = categoryList.get(random.nextInt(categoryList.size()));

                transaction.setCategory(category);
                transaction.setPrice(120.0f);
                transaction.setNote("Note " + j);

                transactionArrayList.add(transaction);
            }
        }
        endTime = System.nanoTime();
        myRealm.commitTransaction();

        duration = (endTime - startTime);

        long milli = (duration/1000000);
        long second = (milli/1000);
        float minutes = (second/ 60.0f);

        Log.d("REALM", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");

        exportDatabase();
    }

    public void exportDatabase() {

        File exportRealmFile = null;
        try {
            // get or create an "export.realm" file
            exportRealmFile = new File(this.getExternalCacheDir(), "export.realm");

            // if "export.realm" already exists, delete
            exportRealmFile.delete();

            // copy current realm to "export.realm"
            myRealm.writeCopyTo(exportRealmFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
        myRealm.close();

        // init email intent and add export.realm as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, "YOUR MAIL");
        intent.putExtra(Intent.EXTRA_SUBJECT, "YOUR SUBJECT");
        intent.putExtra(Intent.EXTRA_TEXT, "YOUR TEXT");
        Uri u = Uri.fromFile(exportRealmFile);
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        startActivity(Intent.createChooser(intent, "YOUR CHOOSER TITLE"));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displayView(item.getItemId());

        return true;
    }

    private void displayView(final int viewId){
        //Close drawer first
        drawer.closeDrawer(GravityCompat.START);

        //Creates a 250 millisecond delay to remove lag when drawer is closing
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = null;
                String title = getString(R.string.app_name);

                switch (viewId) {
                    case R.id.nav_calendar:
                        fragment = calendarFragment;
                        title = "Entry";
                        break;
                    case R.id.nav_category:
                        fragment = categoryFragment;
                        title = "Category";
                        break;
                    case R.id.nav_overview:
                        fragment = overviewFragment;
                        title = "Overview";
                        break;
                    case R.id.nav_share:
                        fragment = shareFragment;
                        title = "Share";
                        break;
                    case R.id.nav_send:
                        fragment = sendFragment;
                        title = "Send";
                        break;
                }

                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.contentFrame, fragment);
                    ft.commit();
                }

                //set the toolbar title
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
            }
        }, 250);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Fragment listeners
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCalendarInteraction(String value){

    }

    @Override
    public void nextMonth(){
        Log.d("MAIN ACTIVITY", "next value : ");

    }

    @Override
    public void previousMonth(){
        Log.d("MAIN ACTIVITY", "previous value : ");

    }

    @Override
    public void onOverviewInteraction(String value){

    }

    @Override
    public void onSendInteraction(String value){

    }

    @Override
    public void onShareInteraction(String value){

    }

}
