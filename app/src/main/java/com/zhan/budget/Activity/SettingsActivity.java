package com.zhan.budget.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.SettingFragment;
import com.zhan.budget.R;

public class SettingsActivity extends BaseActivity {

    /**
     * Note : This activity doesnt call "getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, fragment).commit()"
     * because there should only ever have 1 fragment in this activity and that fragment is defined
     * in content_settings.xml
     */

    private SettingFragment settingFragment;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_settings;
    }

    @Override
    protected void init(){
        createToolbar();

        settingFragment = new SettingFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, settingFragment).commit();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(getString(R.string.title_activity_settings));
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            // Permission was granted!
            /*if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
                //settingFragment.backUpData();
            }else*/ if(requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
                settingFragment.restore();
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_CSV){
                settingFragment.exportCSVSort();
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE){
                settingFragment.sendRealmData();
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_AUTO_EXTERNAL_STORAGE){
                settingFragment.turnOnAutoUpdateSwitch();
                settingFragment.backUpData();
            }
        }else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
            boolean showRationale = true;
/*
            if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }else*/ if(requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_CSV){
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE){
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if(showRationale){
                // Permission was denied without checking the check box "Never ask again"
                Log.d("SETTINGS", "permission denied without never ask again");

                /*if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
                    //settingFragment.requestFilePermissionToWrite();
                }*/

                if(requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
                    settingFragment.requestFilePermissionToRead();
                }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_CSV){
                    settingFragment.requestFilePermissionToWriteCSV();
                }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE){
                    settingFragment.requestFilePermissionToAccess();
                }else if(requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_AUTO_EXTERNAL_STORAGE){
                    settingFragment.turnOffAutoUpdateSwitch();
                    settingFragment.requestFilePermissionToWriteAutoBackup();
                }
            }else{
                // Permission was denied while checking the check box "Never ask again"
                Log.d("SETTINGS", "permission denied with never ask again");
            }
        }
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }
}
