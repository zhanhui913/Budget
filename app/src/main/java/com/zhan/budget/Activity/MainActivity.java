package com.zhan.budget.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.zhan.budget.Database.Database;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.CalendarFragment;
import com.zhan.budget.Fragment.CategoryFragment;
import com.zhan.budget.Fragment.OverviewFragment;
import com.zhan.budget.Fragment.SendFragment;
import com.zhan.budget.Fragment.ShareFragment;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.Random;

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
    Database db;

    private CalendarFragment calendarFragment;
    private CategoryFragment categoryFragment;
    private OverviewFragment overviewFragment;
    private ShareFragment shareFragment;
    private SendFragment sendFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFragments();
        init();
    }

    private void init(){
        activity = MainActivity.this;

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
        boolean defaultIsFirstTime = true;
        boolean isFirstTIme = sharedPreferences.getBoolean(Constants.FIRST_TIME, defaultIsFirstTime);

        if(isFirstTIme){
            createDefaultCategory();

            //set Constants.FIRST_TIME shared preferences to false
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.FIRST_TIME, false);
            editor.apply();
        }

        Toast.makeText(activity, "IsFirstTime: "+isFirstTIme, Toast.LENGTH_SHORT).show();
    }

    private void createFragments(){
        calendarFragment = new CalendarFragment();
        categoryFragment = new CategoryFragment();
        overviewFragment = new OverviewFragment();
        shareFragment = new ShareFragment();
        sendFragment = new SendFragment();
    }

    private void createDefaultCategory(){
        openDatabase();

        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("ASYNC", "preparing transaction to create default categoroes");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String[] tempCategoryNameList = new String[]{"Breakfast","Lunch","Dinner", "Snacks","Drink","Rent","Travel", "Shopping","Necessity","Utilities","Bill","Groceries"};
                String[] tempCategoryColorList = new String[]{"F1C40F","E67E22","D35400", "F2784B","FDE3A7","6C7A89","19B5FE", "BF55EC","E26A6A","81CFE0","26A65B","BFBFBF"};
                int[] tempCategoryIconList = new int[]{0,1,2,3,4,5,6,7,8,9,10,11};

                final ArrayList<Category> tempCategoryArrayList = new ArrayList<>();

                //create category first
                for(int i = 0; i < tempCategoryNameList.length; i++){
                    Category c = new Category();
                    c.setName(tempCategoryNameList[i]);
                    c.setColor("#"+tempCategoryColorList[i]);
                    c.setIcon(tempCategoryIconList[i]);

                    Random random = new Random();

                    float budget = random.nextFloat() * 100.0f;

                    c.setBudget(budget);
                    c.setCost(0);

                    tempCategoryArrayList.add(c);

                    long categoryID = db.createCategory(c);

                    if(categoryID == -1){
                        Log.e("ZHAN", "db.createCategory returned -1");
                        continue;
                    }
                    c.setId((int)categoryID);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                Log.d("ASYNC", "done creating default categories");

                db.exportDB();
            }
        };

        loader.execute();
    }

    public void openDatabase(){
        if(db == null) {
            db = new Database(activity);
        }
    }

    public void closeDatabase(){
        if(db != null){
            db.close();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        closeDatabase();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        openDatabase();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
