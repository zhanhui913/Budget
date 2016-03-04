package com.zhan.budget.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import com.zhan.budget.Fragment.MonthReportFragment;
import com.zhan.budget.Fragment.RateFragment;
import com.zhan.budget.Fragment.SettingFragment;
import com.zhan.budget.Fragment.InfoFragment;
import com.zhan.budget.Model.Account;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarFragment.OnCalendarInteractionListener,
        CategoryFragment.OnCategoryInteractionListener,
        MonthReportFragment.OnOverviewInteractionListener,
        InfoFragment.OnShareInteractionListener{

    MainActivity activity;
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    private CalendarFragment calendarFragment;
    private CategoryFragment categoryFragment;
    private MonthReportFragment monthReportFragment;
    private AccountFragment accountFragment;
    private InfoFragment infoFragment;
    private RateFragment rateFragment;
    private SettingFragment settingFragment;

    private ArrayList<Category> categoryList = new ArrayList<>();
    private ArrayList<Account> accountList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFragments();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //STORAGE permission has not been granted
            requestFilePermission();
        }

        init();
    }

    private void createFragments(){
        calendarFragment = new CalendarFragment();
        categoryFragment = new CategoryFragment();
        monthReportFragment = new MonthReportFragment();
        accountFragment = new AccountFragment();
        settingFragment = new SettingFragment();
        infoFragment = new InfoFragment();
        rateFragment = new RateFragment();
    }

    private void requestFilePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            new AlertDialog.Builder(this)
                    .setTitle("Permission denied")
                    .setMessage("You need to allow access to storage in order to create a backup of the database.")
                    .setPositiveButton("Re-try", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    })
                    .setNegativeButton("I'm sure", null)
                    .create()
                    .show();

        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // storage-related task you need to do.
                    Toast.makeText(getApplicationContext(), "YAY", Toast.LENGTH_SHORT).show();
                } else if(grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if(showRationale){
                        //Permission was denied without checking the check box "Never ask again"
                        Util.Write("permission denied without never ask again");
                        requestFilePermission();
                    }else{
                        //Permission was denied while checking the check box "Never ask again"
                        Util.Write("permission denied with never ask again");
                    }

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "BOO", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //code to load my fragment
        getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, calendarFragment).commit();

        //set first fragment as default
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
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                String[] tempCategoryNameList = new String[]{"Breakfast", "Lunch", "Dinner", "Snacks", "Drink", "Rent", "Travel", "Car", "Shopping", "Necessity", "Utilities", "Bill", "Groceries"};
                int[] tempCategoryColorList = new int[]{R.color.lemon, R.color.orange, R.color.pumpkin, R.color.alizarin, R.color.cream_can, R.color.midnight_blue, R.color.peter_river, R.color.turquoise, R.color.wisteria, R.color.jordy_blue, R.color.concrete, R.color.emerald, R.color.gossip};
                int[] tempCategoryIconList = new int[]{R.drawable.c_food, R.drawable.c_food, R.drawable.c_food, R.drawable.c_food, R.drawable.c_cafe, R.drawable.c_house, R.drawable.c_airplane, R.drawable.c_car, R.drawable.c_shirt, R.drawable.c_etc, R.drawable.c_utilities, R.drawable.c_bill, R.drawable.c_groceries};

                //create expense category
                for (int i = 0; i < tempCategoryNameList.length; i++) {
                    Category c = bgRealm.createObject(Category.class);
                    c.setId(Util.generateUUID());
                    c.setName(tempCategoryNameList[i]);
                    c.setColor(getApplicationContext().getResources().getString(tempCategoryColorList[i]));
                    c.setIcon(getApplicationContext().getResources().getResourceEntryName(tempCategoryIconList[i]));
                    c.setBudget(100.0f + (i/5));
                    c.setType(BudgetType.EXPENSE.toString());
                    c.setCost(0);

                    categoryList.add(c);
                }

                String[] tempCategoryIncomeNameList = new String[]{"Salary", "Other"};
                int[] tempCategoryIncomeColorList = new int[]{R.color.light_wisteria, R.color.harbor_rat};
                int[] tempCategoryIncomeIconList = new int[]{R.drawable.c_bill, R.drawable.c_etc};

                //create income category
                for (int i = 0; i < tempCategoryIncomeNameList.length; i++) {
                    Category c = bgRealm.createObject(Category.class);
                    c.setId(Util.generateUUID());
                    c.setName(tempCategoryIncomeNameList[i]);
                    c.setColor(getApplicationContext().getResources().getString(tempCategoryIncomeColorList[i]));
                    c.setIcon(getApplicationContext().getResources().getResourceEntryName(tempCategoryIncomeIconList[i]));
                    c.setBudget(0);
                    c.setType(BudgetType.INCOME.toString());
                    c.setCost(0);

                    categoryList.add(c);
                }

                //Create default accounts
                String[] tempAccountList = new String[]{"Credit Card","Debit Card", "Cash"};
                for(int i = 0 ; i < tempAccountList.length; i++){
                    Account account = bgRealm.createObject(Account.class);
                    account.setId(Util.generateUUID());
                    account.setName(tempAccountList[i]);
                    accountList.add(account);
                }

                //Create fake transactions
                Date startDate = DateUtil.convertStringToDate("2016-03-01");
                Date endDate = DateUtil.convertStringToDate("2016-04-01");

                Calendar start = Calendar.getInstance();
                start.setTime(startDate);
                Calendar end = Calendar.getInstance();
                end.setTime(endDate);

                startTime = System.nanoTime();

                String dayType;

                for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
                    Random random = new Random();
                    int rd = random.nextInt(categoryList.size());
                    int rda = random.nextInt(accountList.size());


                    if(DateUtil.getDaysFromDate(date) <= DateUtil.getDaysFromDate(new Date())){
                        dayType = DayType.COMPLETED.toString();
                    }else{
                        dayType = DayType.SCHEDULED.toString();
                    }

                    //Create random transactions per day
                    for (int j = 0; j < rd; j++) {
                        Transaction transaction = bgRealm.createObject(Transaction.class);
                        transaction.setId(Util.generateUUID());
                        transaction.setDate(date);
                        transaction.setDayType(dayType);

                        Account account = accountList.get(rda);

                        Category category = categoryList.get(rd);

                        transaction.setAccount(account);
                        transaction.setCategory(category);
                        transaction.setPrice(-120.0f + (rd * 0.5f));
                        transaction.setNote("Note " + j + " for " + DateUtil.convertDateToString(date));
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

                realm.close();
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

        //Creates a 300 millisecond delay to remove lag when drawer is closing
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = null;
                String title = "";

                switch (viewId) {
                    case R.id.nav_calendar:
                        fragment = calendarFragment;
                        break;
                    case R.id.nav_category:
                        fragment = categoryFragment;
                        break;
                    case R.id.nav_overview:
                        fragment = monthReportFragment;
                        break;
                    case R.id.nav_account:
                        fragment = accountFragment;
                        title = "Account";
                        break;
                    case R.id.nav_setting:
                        fragment = settingFragment;
                        title = "Setting";
                        break;
                    case R.id.nav_info:
                        fragment = infoFragment;
                        title = "Info";
                        break;
                    case R.id.nav_rate:
                        fragment = rateFragment;
                        title = "Rate";
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
    public void updateToolbar(Date date){
        //set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(DateUtil.convertDateToStringFormat2(date));
        }
    }

    @Override
    public void onOverviewInteraction(String value){

    }

    @Override
    public void onShareInteraction(String value){

    }
}
