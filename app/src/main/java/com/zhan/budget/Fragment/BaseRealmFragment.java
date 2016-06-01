package com.zhan.budget.Fragment;

import android.util.Log;

import com.zhan.budget.Util.BudgetPreference;

import io.realm.Realm;

/**
 * Base fragment created to be extended by every fragment that uses Realm in this application.
 * This class handles the closing and starting of realms.
 *
 * @author Zhan H. Yap
 */
public abstract class BaseRealmFragment extends BaseFragment {
    private static String TAG = "BaseRealmFragment";

    protected Realm myRealm;

    @Override
    public void onStart(){
        resumeRealm();
        super.onStart();
        Log.d(TAG, "onStart");
    }


    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
        closeRealm();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        closeRealm();
    }

    protected void resumeRealm(){
        myRealm = Realm.getDefaultInstance();
        BudgetPreference.addRealmCache(getContext());
        Log.d(TAG, "----- RESUME REALM -----");
    }

    /**
     * Close Realm if possible
     */
    protected void closeRealm(){
        myRealm.close();
        BudgetPreference.removeRealmCache(getContext());
        Log.d(TAG, "----- CLOSE REALM -----");
    }

    @Override
    protected void init() {
        resumeRealm();
    }
}
