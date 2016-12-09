package com.zhan.budget.Activity.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.zhan.budget.Activity.BaseActivity;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;
import com.zhan.library.CircularView;

public class AboutActivity extends BaseActivity {

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_about;
    }

    @Override
    protected void init(){
        createToolbar();
        createOpenSource();
        createTranslation();
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
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createOpenSource(){
        ViewGroup openSourceBtn = (ViewGroup) findViewById(R.id.openSourceBtn);
        CircularView openSourceCV = (CircularView) findViewById(R.id.openSourceCV);

        openSourceCV.setCircleColor(R.color.jordy_blue);
        openSourceCV.setIconColor(Colors.getHexColorFromAttr(getApplicationContext(), R.attr.themeColor));
        openSourceCV.setIconResource(R.drawable.svg_ic_code);

        openSourceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openSource = new Intent(getApplicationContext(), OpenSourceActivity.class);
                startActivity(openSource);
            }
        });
    }

    private void createTranslation(){
        ViewGroup translationBtn = (ViewGroup) findViewById(R.id.translationBtn);
        CircularView translationCV = (CircularView) findViewById(R.id.translationCV);

        translationCV.setCircleColor(R.color.carrot);
        translationCV.setIconColor(Colors.getHexColorFromAttr(getApplicationContext(), R.attr.themeColor));
        translationCV.setIconResource(R.drawable.svg_ic_code);

        translationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent about = new Intent(getApplicationContext(), AboutActivity.class);
                //startActivity(about);
            }
        });
    }
}
