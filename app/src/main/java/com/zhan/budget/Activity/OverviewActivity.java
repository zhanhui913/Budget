package com.zhan.budget.Activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.util.Date;

public class OverviewActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Date month;
private TextView dateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        //month = (getIntent().getExtras()).getSerializableExtra(Constants.REQUEST_NEW_OVERVIEW_MONTH);
        month = (Date)(getIntent().getExtras()).get(Constants.REQUEST_NEW_OVERVIEW_MONTH);

        init();
        createToolbar();
        addListeners();
    }

    private void init(){
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setText(Util.convertDateToStringFormat2(month));
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_navback);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Overview");
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
