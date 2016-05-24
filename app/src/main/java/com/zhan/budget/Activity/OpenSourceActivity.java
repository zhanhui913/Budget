package com.zhan.budget.Activity;

import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zhan.budget.R;

public class OpenSourceActivity extends BaseActivity {

    private Toolbar toolbar;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_open_source;
    }

    @Override
    protected void init(){
        createToolbar();
        addListeners();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Budget");
        }
    }

    private void addListeners(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
