package com.zhan.budget.Activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import io.realm.BaseRealm;

public class SettingsCategory extends BaseRealmActivity {

    private  Toolbar toolbar;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_settings_category;
    }

    @Override
    protected void init(){
        super.init();

        createToolbar();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
