package com.zhan.budget.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.AccountFragment;
import com.zhan.budget.Fragment.CalendarFragment;
import com.zhan.budget.Fragment.CategoryFragment;
import com.zhan.budget.Fragment.LocationFragment;
import com.zhan.budget.Fragment.MonthReportFragment;
import com.zhan.budget.Fragment.RateFragment;
import com.zhan.budget.Fragment.SettingFragment;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.Util;

import java.util.ArrayList;

import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarFragment.OnCalendarInteractionListener,
        CategoryFragment.OnCategoryInteractionListener,
        MonthReportFragment.OnMonthlyInteractionListener,
        AccountFragment.OnAccountInteractionListener,
        LocationFragment.OnLocationInteractionListener{

    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    private CalendarFragment calendarFragment;
    private CategoryFragment categoryFragment;
    private MonthReportFragment monthReportFragment;
    private AccountFragment accountFragment;
    //private InfoFragment infoFragment;
    private RateFragment rateFragment;
    private SettingFragment settingFragment;
    private LocationFragment locationFragment;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_main;
    }

    private void createFragments(){
        calendarFragment = new CalendarFragment();
        categoryFragment = new CategoryFragment();
        monthReportFragment = new MonthReportFragment();
        accountFragment = new AccountFragment();
        settingFragment = new SettingFragment();
        //infoFragment = new InfoFragment();
        rateFragment = new RateFragment();
        locationFragment = new LocationFragment();
    }

    @Override
    protected void init(){
        createFragments();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if(navigationView != null){
            navigationView.setNavigationItemSelectedListener(this);
        }

        //Load calendarFragment first
        getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, calendarFragment).commit();

        //set first fragment as default in navigation drawer
        navigationView.getMenu().getItem(0).setChecked(true);

        if(BudgetPreference.getFirstTime(getBaseContext())){
            loadTutorials();
        }
    }

    private void loadTutorials(){
        Intent mainAct = new Intent(getBaseContext(), MaterialTutorialActivity.class);
        mainAct.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS, getTourPages(getBaseContext()));
        startActivity(mainAct);
    }

    private ArrayList<TutorialItem> getTourPages(Context context){
        TutorialItem page1 = new TutorialItem(
                "Calendar",
                "View all transactions for a specific date.",
                R.color.colorPrimary,
                R.drawable.screen1);

        TutorialItem page2 = new TutorialItem(
                "Theme",
                "Change between light and dark mode.",
                R.color.colorPrimary,
                R.drawable.screen2);

        TutorialItem page3 = new TutorialItem(
                "Approve",
                "Approve, un-approve, or delete a transaction by swiping left.",
                R.color.colorPrimary,
                R.drawable.screen3);

        TutorialItem page4 = new TutorialItem(
                "Add new Transaction",
                "Create or edit a transaction.",
                R.color.colorPrimary,
                R.drawable.screen4);

        TutorialItem page5 = new TutorialItem(
                "Budget",
                "Compare your current spending with your budget.",
                R.color.colorPrimary,
                R.drawable.screen5);

        TutorialItem page6 = new TutorialItem(
                "Account",
                "View how much you spent on all accounts for a month.",
                R.color.colorPrimary,
                R.drawable.screen6);

        TutorialItem page7 = new TutorialItem(
                "Location",
                "View all locations that are in the transaction for a month",
                R.color.colorPrimary,
                R.drawable.screen7);

        TutorialItem page8 = new TutorialItem(
                "Monthly Overview",
                "View how much you spent and earn each month for the whole year.",
                R.color.colorPrimary,
                R.drawable.screen8);

        TutorialItem page9 = new TutorialItem(
                "Percentage",
                "View how much you spent on a category relative to other categories.",
                R.color.colorPrimary,
                R.drawable.screen9);

        TutorialItem page10 = new TutorialItem(
                "View all",
                "View all transactions for a specific Account, Category, Location during a month.",
                R.color.colorPrimary,
                R.drawable.screen10);

        ArrayList<TutorialItem> tourItems = new ArrayList<>();
        tourItems.add(page1);
        tourItems.add(page2);
        tourItems.add(page3);
        tourItems.add(page4);
        tourItems.add(page5);
        tourItems.add(page6);
        tourItems.add(page7);
        tourItems.add(page8);
        tourItems.add(page9);
        tourItems.add(page10);

        return tourItems;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            // Permission was granted!
            if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
                settingFragment.backUpData();
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
                settingFragment.restore();
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_CSV){
                settingFragment.exportCSVSort();
            }
        }else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
            boolean showRationale = true;

            if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_CSV){
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if(showRationale){
                // Permission was denied without checking the check box "Never ask again"
                Log.d("SETTINGS", "permission denied without never ask again");

                if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
                    settingFragment.requestFilePermissionToWrite();
                }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
                    settingFragment.requestFilePermissionToRead();
                }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_CSV){
                    settingFragment.requestFilePermissionToWriteCSV();
                }
            }else{
                // Permission was denied while checking the check box "Never ask again"
                Log.d("SETTINGS", "permission denied with never ask again");
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer  != null){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
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
                        break;
                    case R.id.nav_location:
                        fragment = locationFragment;
                        break;
                    case R.id.nav_setting:
                        fragment = settingFragment;
                        title = "Setting";
                        break;
                    /*case R.id.nav_info:
                        fragment = infoFragment;
                        title = "Info";
                        break;*/
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
                    if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(title)) {
                        getSupportActionBar().setTitle(title);
                    }
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
    public void updateToolbar(String date){
        //set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(date);
        }
    }
}
