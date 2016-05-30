package com.zhan.budget.Fragment;

import android.util.Log;

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
        super.onStart();
        Log.d(TAG, "onStart");
        resumeRealm();
    }
/*
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
    }*/

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
        closeRealm();
    }

    /**
     * Every fragment has to inflate a layout in the onCreateView method. Added this method to
     * avoid duplicate all the inflate code in every fragment. You only have to return the layout to
     * inflate in this method when extends BaseFragment.
     */
    protected abstract int getFragmentLayout();

    @Override
    protected void init(){
        resumeRealm();
    }

    protected void resumeRealm(){
        myRealm = Realm.getDefaultInstance();
        Log.d(TAG, "----- RESUME REALM -----");
    }

    /**
     * Close Realm if possible
     */
    protected void closeRealm(){
        myRealm.close();
        Log.d(TAG, "----- CLOSE REALM -----");
    }
}
