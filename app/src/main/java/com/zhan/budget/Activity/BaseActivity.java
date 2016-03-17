package com.zhan.budget.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.zhan.budget.Util.Util;

import io.realm.Realm;

/**
 * Base activity created to be extended by every activity that uses Realm in this application.
 * This class handles the closing and starting of realms.
 *
 * @author Zhan H. Yap
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    protected Realm myRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.onActivityCreateSetTheme(this);
        setContentView(getActivityLayout());
        init();

    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart");
        resumeRealm();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
        resumeRealm();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
        closeRealm();
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
        closeRealm();
    }

    /**
     * Every Activity has to inflate a layout in the onCreate method. Added this method to
     * avoid duplicate all the inflate code in every activity. You only have to return the layout to
     * inflate in this method when extends BaseActivity.
     */
    protected abstract int getActivityLayout();

    /**
     * Every Activity should override this function as it should be where other initialization
     * occurs once only in the lifecycle.
     * Note: I would put init in the onStart function but it will call multiple times when the user
     * comes back into the activity which is unnecessary.
     */
    protected void init(){
        resumeRealm();
    }

    public void resumeRealm(){
        if(myRealm == null || myRealm.isClosed()){
            myRealm = Realm.getDefaultInstance();
            Log.d(TAG, "resumeRealm");
        }
    }

    /**
     * Close Realm if possible
     */
    public void closeRealm(){
        if(myRealm != null && !myRealm.isClosed()){
            myRealm.close();
            Log.d(TAG, "closeRealm");
        }
    }
}
