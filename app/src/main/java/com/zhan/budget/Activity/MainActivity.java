package com.zhan.budget.Activity;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.AccountFragment;
import com.zhan.budget.Fragment.CalendarFragment;
import com.zhan.budget.Fragment.CategoryFragment;
import com.zhan.budget.Fragment.OverviewFragment;
import com.zhan.budget.Fragment.SendFragment;
import com.zhan.budget.Fragment.ShareFragment;
import com.zhan.budget.Model.Account;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;

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
    private AccountFragment accountFragment;
    private ShareFragment shareFragment;
    private SendFragment sendFragment;

    private ArrayList<Category> categoryList = new ArrayList<>();
    private ArrayList<Account> accountList = new ArrayList<>();

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
        accountFragment = new AccountFragment();
        shareFragment = new ShareFragment();
        sendFragment = new SendFragment();
    }

    private void init(){
        activity = MainActivity.this;

        RealmConfiguration config = new RealmConfiguration.Builder(getApplicationContext())
                .name(Constants.REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);

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
            Toast.makeText(getApplicationContext(), "first time", Toast.LENGTH_SHORT).show();
            createFakeTransactions();

            //set Constants.FIRST_TIME shared preferences to false
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.FIRST_TIME, false);
            editor.apply();
        }
    }

    long startTime,endTime,duration;
    private void createFakeTransactions(){
        Realm realm = Realm.getDefaultInstance();

        final ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                String[] tempCategoryNameList = new String[]{"Breakfast", "Lunch", "Dinner", "Snacks", "Drink", "Rent", "Travel", "Car", "Shopping", "Necessity", "Utilities", "Bill", "Groceries"};
                String[] tempCategoryColorList = new String[]{"F1C40F", "E67E22", "D35400", "F2784B", "FDE3A7", "6C7A89", "19B5FE", "16A085", "BF55EC", "E26A6A", "81CFE0", "26A65B", "BFBFBF"};
                int[] tempCategoryIconList = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

                //create expense category
                for (int i = 0; i < tempCategoryNameList.length; i++) {
                    Category c = bgRealm.createObject(Category.class);
                    c.setId(Util.generateUUID());
                    c.setName(tempCategoryNameList[i]);
                    c.setColor("#" + tempCategoryColorList[i]);
                    c.setIcon(tempCategoryIconList[i]);
                    c.setBudget(100.0f + (i/5));
                    c.setType(BudgetType.EXPENSE.toString());
                    c.setCost(0);

                    categoryList.add(c);
                }

                String[] tempCategoryIncomeNameList = new String[]{"Salary", "Other"};
                String[] tempCategoryIncomeColorList = new String[]{"8E44AD", "34495E"};
                int[] tempCategoryIncomeIconList = new int[]{11, 9};
                //create income category
                for (int i = 0; i < tempCategoryIncomeNameList.length; i++) {
                    Category c = bgRealm.createObject(Category.class);
                    c.setId(Util.generateUUID());
                    c.setName(tempCategoryIncomeNameList[i]);
                    c.setColor("#" + tempCategoryIncomeColorList[i]);
                    c.setIcon(tempCategoryIncomeIconList[i]);
                    c.setBudget(0);
                    c.setType(BudgetType.INCOME.toString());
                    c.setCost(0);

                    categoryList.add(c);
                }

                //Create default accounts
                String[] tempAccountList = new String[]{"Credit Card","Debit Card", "Cash"};
                for(int i = 0; i < tempAccountList.length; i++){
                    Account account = bgRealm.createObject(Account.class);
                    account.setId(Util.generateUUID());
                    account.setName(tempAccountList[i]);

                    accountList.add(account);
                }

                //Create fake transactions
                Date startDate = Util.convertStringToDate("2014-12-01");
                Date endDate = Util.convertStringToDate("2016-02-01");

                Calendar start = Calendar.getInstance();
                start.setTime(startDate);
                Calendar end = Calendar.getInstance();
                end.setTime(endDate);

                startTime = System.nanoTime();

                for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
                    Random random = new Random();
                    int rd = random.nextInt(categoryList.size());

                    int rda = random.nextInt(accountList.size());

                    //Create random transactions per day
                    for (int j = 0; j < rd; j++) {
                        Transaction transaction = bgRealm.createObject(Transaction.class);
                        transaction.setId(Util.generateUUID());
                        transaction.setDate(date);

                        Account account = accountList.get(rda);

                        Category category = categoryList.get(rd);

                        transaction.setAccount(account);
                        transaction.setCategory(category);
                        transaction.setPrice(-120.0f + (rd * 0.5f));
                        transaction.setNote("Note " + j + " for "+Util.convertDateToString(date));

                        transactionArrayList.add(transaction);
                    }
                }
            }
        }, new Realm.Transaction.Callback() {
            @Override
            public void onSuccess() {
                endTime = System.nanoTime();

                duration = (endTime - startTime);

                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);

                Log.d("REALM", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }

            @Override
            public void onError(Exception e) {
                // transaction is automatically rolled-back, do any cleanup here
                e.printStackTrace();
            }
        });
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
                    case R.id.nav_account:
                        fragment = accountFragment;
                        title = "Account";
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
        }, 300);
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
