package com.zhan.budget.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.TextView;

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
import com.zhan.budget.View.ExtendedNumberPicker;

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

        // remove for now in v1.0.0
        //rateFragment = new RateFragment();
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

        if(getIntent().getExtras() != null){
            //This will be true if its coming from Settings (ie: called from SettingsFragment during a change theme)
            boolean isFromSettings = (getIntent().getExtras()).getBoolean(Constants.REQUEST_CHANGE_THEME);

            if(isFromSettings){
                getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, settingFragment).commit();

                //set 5th fragment (Settings) in navigation drawer
                navigationView.getMenu().getItem(5).setChecked(true);
            }else{
                getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, calendarFragment).commit();

                //set 1st fragment (Calendar) in navigation drawer
                navigationView.getMenu().getItem(0).setChecked(true);
            }
        }else{
            getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, calendarFragment).commit();

            //set 1st fragment (Calendar) in navigation drawer
            navigationView.getMenu().getItem(0).setChecked(true);
        }

        if(BudgetPreference.getFirstTime(getBaseContext())){
            askForCurrencyActivity();
        }
    }

    private int selectedCurrencyIndex = 0;
    private String selectedCurrency = "";
    private String[] currencyList;
    //Alert dialog for currency
    private AlertDialog currencyDialog;

    private void askForCurrency(){
        View accountDialogView = View.inflate(this, R.layout.alertdialog_number_picker, null);

        final ExtendedNumberPicker currencyPicker = (ExtendedNumberPicker)accountDialogView.findViewById(R.id.numberPicker);

        TextView title = (TextView)accountDialogView.findViewById(R.id.title);
        title.setText("Select BudgetCurrency");


        currencyList = new String[]{"AUD", "CAD", "USD"};


        currencyPicker.setMinValue(0);
        if(currencyList.length > 0){
            currencyPicker.setMaxValue(currencyList.length - 1);
            currencyPicker.setDisplayedValues(currencyList);
            currencyPicker.setWrapSelectorWheel(false);
            currencyPicker.setValue(selectedCurrencyIndex);
        }



        AlertDialog.Builder accountAlertDialogBuilder = new AlertDialog.Builder(this)
                .setView(accountDialogView)
                .setCancelable(false)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        selectedCurrencyIndex = currencyPicker.getValue();
                        selectedCurrency = currencyList[selectedCurrencyIndex];
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Reset the selection back to previous
                        currencyPicker.setValue(selectedCurrencyIndex);
                        dialog.dismiss();
                    }
                });

        currencyDialog = accountAlertDialogBuilder.create();
        currencyDialog.show();
    }

    private void askForCurrencyActivity(){
        Intent currencyIntent = new Intent(getApplicationContext(), SelectCurrencyActivity.class);
        currencyIntent.putExtra(Constants.REQUEST_CURRENCY_IN_SETTINGS, false);
        currencyIntent.putExtra(Constants.REQUEST_DEFAULT_CURRENCY, true);
        startActivity(currencyIntent);
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
                        break;
                    case R.id.nav_rate:
                        fragment = rateFragment;
                        title = "Rate";
                        break;*/
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
