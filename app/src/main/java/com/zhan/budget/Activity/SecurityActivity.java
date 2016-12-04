package com.zhan.budget.Activity;

import android.support.annotation.IdRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.zhan.budget.Fragment.Security.FingerprintFragment;
import com.zhan.budget.Fragment.Security.PatternFragment;
import com.zhan.budget.Fragment.Security.PinFragment;
import com.zhan.budget.Model.SecurityType;
import com.zhan.budget.R;

import static com.zhan.budget.R.styleable.BottomBar;

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


        BottomBar bottomBar = (BottomBar) findViewById(R.id.securityBottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_pin) {
                    replaceFragment(SecurityType.PIN);
                }else if(tabId == R.id.tab_pattern){

                    replaceFragment(SecurityType.PATTERN);
                }else if(tabId == R.id.tab_fingerprint){

                    replaceFragment(SecurityType.FINGERPRINT);
                }
            }
        });
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
        patternFragment = new PatternFragment();
        fingerprintFragment = new FingerprintFragment();
    }

    private void replaceFragment(SecurityType type){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(type == SecurityType.PIN){
            ft.replace(R.id.contentFrame, pinFragment);
        }else if(type == SecurityType.PATTERN){
            ft.replace(R.id.contentFrame, patternFragment);
        }else if(type == SecurityType.FINGERPRINT){
            ft.replace(R.id.contentFrame, fingerprintFragment);
        }
        ft.commit();
    }
}
