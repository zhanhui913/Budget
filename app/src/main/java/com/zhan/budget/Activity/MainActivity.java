package com.zhan.budget.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Fragment.AccountFragment;
import com.zhan.budget.Fragment.CalendarFragment;
import com.zhan.budget.Fragment.CategoryFragment1;
import com.zhan.budget.Fragment.LocationFragment;
import com.zhan.budget.Fragment.MonthReportFragment;
import com.zhan.budget.Fragment.RateFragment;
import com.zhan.budget.MyApplication;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.Tutorial;

import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarFragment.OnCalendarInteractionListener,
        CategoryFragment1.OnCategoryInteractionListener,
        MonthReportFragment.OnMonthlyInteractionListener,
        AccountFragment.OnAccountInteractionListener,
        LocationFragment.OnLocationInteractionListener{

    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    private CalendarFragment calendarFragment;
    private CategoryFragment1 categoryFragment;
    private MonthReportFragment monthReportFragment;
    private AccountFragment accountFragment;
    private RateFragment rateFragment;
    private LocationFragment locationFragment;

    private int savedTheme;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_main;
    }

    private void createFragments(){
        calendarFragment = new CalendarFragment();
        categoryFragment = new CategoryFragment1();
        monthReportFragment = new MonthReportFragment();
        accountFragment = new AccountFragment();

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

        getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, calendarFragment).commit();

        //set 1st fragment (Calendar) in navigation drawer
        navigationView.getMenu().getItem(0).setChecked(true);

        //first time => onboard
        if(BudgetPreference.getFirstTime(this)){
            Intent mainAct = new Intent(this, MaterialTutorialActivity.class);
            mainAct.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS, Tutorial.getTutorialPages(this));
            startActivity(mainAct);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.SETTINGS) {
            //Only restart activity if theme is different
            if(savedTheme != MyApplication.getInstance(getApplicationContext()).getBudgetTheme()){
                Intent intent = getIntent();
                //overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                //overridePendingTransition(0, 0);
                startActivity(intent);
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
                        savedTheme = MyApplication.getInstance(getApplicationContext()).getBudgetTheme();
                        startActivityForResult(SettingsActivity.createIntent(getApplicationContext()), RequestCodes.SETTINGS);
                        break;
                    /*
                    case R.id.nav_rate:
                        fragment = rateFragment;
                        title = "Rate";
                        break;*/
                }

                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, fragment).commit();
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
