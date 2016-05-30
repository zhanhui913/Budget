package com.zhan.budget.Activity;

import android.util.Log;

import io.realm.Realm;

/**
 * Base activity created to be extended by every activity that uses Realm in this application.
 * This class handles the closing and starting of realms.
 *
 * @author Zhan H. Yap
 */
public abstract class BaseRealmActivity extends BaseActivity {
    private static final String TAG = "BaseRealmActivity";

    protected Realm myRealm;

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    @Override
    protected void init(){
        resumeRealm();
    }

    protected void resumeRealm(){
        myRealm = Realm.getDefaultInstance();
        Log.d(TAG, "resumeRealm");
    }

    /**
     * Close Realm if possible
     */
    protected void closeRealm(){
        myRealm.close();
        Log.d(TAG, "closeRealm");
    }
}
