package com.zhan.budget.Activity.Settings;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.zhan.budget.Activity.BaseActivity;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.Date;
import java.util.GregorianCalendar;

public class PrivacyPolicyActivity extends BaseActivity {

    Date privacyDate;

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

        //April 4, 2017
        //Gregorian uses month starts at 0 for January.
        Date lastUpdatedDate = new GregorianCalendar(2017, 3, 4).getTime();

        TextView privacyDate = (TextView)findViewById(R.id.privacyDate);
        privacyDate.setText(String.format(getString(R.string.privacy_last_updated_date), DateUtil.convertDateToStringFormat5(this, lastUpdatedDate)));
    }
}
