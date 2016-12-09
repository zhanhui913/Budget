package com.zhan.budget.Activity.Settings;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhan.budget.Activity.BaseActivity;
import com.zhan.budget.BuildConfig;
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
        TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
        versionNumber.setText(String.format(getString(R.string.version), BuildConfig.VERSION_NAME));

        createToolbar();
        createOpenSource();
        createDeveloper();
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
            getSupportActionBar().setTitle(R.string.setting_title_about);
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

        if(openSourceCV != null){
            openSourceCV.setCircleColor(R.color.belize_hole);
            openSourceCV.setIconColor(Colors.getHexColorFromAttr(this, R.attr.themeColor));
            openSourceCV.setIconResource(R.drawable.svg_ic_code);
        }

        if(openSourceBtn != null) {
            openSourceBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent openSource = new Intent(getApplicationContext(), OpenSourceActivity.class);
                    startActivity(openSource);
                }
            });
        }
    }

    private void createDeveloper(){
        ViewGroup developerBtn = (ViewGroup) findViewById(R.id.developerBtn);
        CircularView developerCV = (CircularView) findViewById(R.id.developerCV);

        if(developerCV != null){
            developerCV.setCircleColor(R.color.nephritis);
            developerCV.setIconColor(Colors.getHexColorFromAttr(this, R.attr.themeColor));
            developerCV.setIconResource(R.drawable.svg_ic_code);
        }

        if(developerBtn != null) {
            developerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent dev = new Intent(getApplicationContext(), DeveloperActivity.class);
                    startActivity(dev);
                }
            });
        }
    }

    private void createTranslation(){
        ViewGroup translationBtn = (ViewGroup) findViewById(R.id.translationBtn);
        CircularView translationCV = (CircularView) findViewById(R.id.translationCV);

        if(translationCV != null){
            translationCV.setCircleColor(R.color.pomegranate);
            translationCV.setIconColor(Colors.getHexColorFromAttr(this, R.attr.themeColor));
            translationCV.setIconResource(R.drawable.svg_ic_code);
        }

        if(translationBtn != null){
            translationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent translation = new Intent(getApplicationContext(), TranslationActivity.class);
                    startActivity(translation);
                }
            });
        }
    }
}
