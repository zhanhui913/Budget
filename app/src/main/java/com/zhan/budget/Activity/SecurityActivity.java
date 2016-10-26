package com.zhan.budget.Activity;

import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zhan.budget.Fragment.Security.FingerprintFragment;
import com.zhan.budget.Fragment.Security.PatternFragment;
import com.zhan.budget.Fragment.Security.PinFragment;
import com.zhan.budget.R;

public class SecurityActivity extends BaseActivity {


    private Toolbar toolbar;
    private PinFragment pinFragment;
    private PatternFragment patternFragment;
    private FingerprintFragment fingerprintFragment;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_security;
    }

    @Override
    protected void init(){
        createToolbar();
        addListeners();
        createFragments();

        getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, pinFragment).commit();

    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_clear);

        if(getSupportActionBar() != null){
            /*if(isNewAccount){
                getSupportActionBar().setTitle("Add Account");
            }else{
                getSupportActionBar().setTitle("Edit Account");
            }*/
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

    private void createFragments(){
        pinFragment = new PinFragment();
    }
}
