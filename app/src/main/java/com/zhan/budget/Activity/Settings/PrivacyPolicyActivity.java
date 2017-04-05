package com.zhan.budget.Activity.Settings;

import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zhan.budget.Activity.BaseActivity;
import com.zhan.budget.R;

public class PrivacyPolicyActivity extends BaseActivity {

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_privacy_policy;
    }

    @Override
    protected void init(){
        createToolbar();
    }
    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(R.string.settings_title_privacy);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
