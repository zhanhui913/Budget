package com.zhan.budget.Activity;

import android.Manifest;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.AccountFragment;
import com.zhan.budget.Fragment.CalendarFragment;
import com.zhan.budget.Fragment.CategoryFragment;
import com.zhan.budget.Fragment.InfoFragment;
import com.zhan.budget.Fragment.LocationFragment;
import com.zhan.budget.Fragment.MonthReportFragment;
import com.zhan.budget.Fragment.RateFragment;
import com.zhan.budget.Fragment.SettingFragment;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import io.realm.Realm;
import io.realm.RealmConfiguration;

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
    private InfoFragment infoFragment;
    private RateFragment rateFragment;
    private SettingFragment settingFragment;
    private LocationFragment locationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initRealm();
        super.onCreate(savedInstanceState);
    }

    private void initRealm(){
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name(Constants.REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);
    }

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
        infoFragment = new InfoFragment();
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
        navigationView.setNavigationItemSelectedListener(this);

        //code to load my fragment
        getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, calendarFragment).commit();

        //set first fragment as default
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the storage-related task you need to do.
                    Toast.makeText(getApplicationContext(), "YAY", Toast.LENGTH_SHORT).show();
                    settingFragment.backUpData();
                } else if(grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if(showRationale){
                        //Permission was denied without checking the check box "Never ask again"
                        Log.d("SETTINGS", "permission denied without never ask again");
                        settingFragment.requestFilePermission();
                    }else{
                        //Permission was denied while checking the check box "Never ask again"
                        Log.d("SETTINGS", "permission denied with never ask again");
                    }

                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "BOO", Toast.LENGTH_SHORT).show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
                        title = "";
                        break;
                    case R.id.nav_category:
                        fragment = categoryFragment;
                        title = "";
                        break;
                    case R.id.nav_overview:
                        fragment = monthReportFragment;
                        title = "";
                        break;
                    case R.id.nav_account:
                        fragment = accountFragment;
                        title = "Account";
                        break;
                    case R.id.nav_location:
                        fragment = locationFragment;
                        title = "Location";
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
