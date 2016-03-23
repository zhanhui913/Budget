package com.zhan.budget.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.realm.Realm;

/**
 * Base fragment created to be extended by every fragment that uses Realm in this application.
 * This class handles the closing and starting of realms.
 *
 * @author Zhan H. Yap
 */
public abstract class BaseFragment extends Fragment {

    private static String TAG = "BaseFragment";

    protected Realm myRealm;
    protected View view;
    protected Fragment instance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        view = inflater.inflate(getFragmentLayout(), container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
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
     * Every fragment has to inflate a layout in the onCreateView method. Added this method to
     * avoid duplicate all the inflate code in every fragment. You only have to return the layout to
     * inflate in this method when extends BaseFragment.
     */
    protected abstract int getFragmentLayout();

    /**
     * Every fragment should override this function as it should be where other initialization
     * occurs once only in the lifecycle.
     * Note: I would put init in the onStart function but it will call multiple times when the user
     * comes back into the fragment which is unnecessary.
     */
    protected void init(){
        instance = this;
        resumeRealm();
    }

    public void resumeRealm(){
        if(myRealm == null || myRealm.isClosed()){
            myRealm = Realm.getDefaultInstance();
            Log.d(TAG, "----- RESUME REALM -----");
        }
    }

    /**
     * Close Realm if possible
     */
    public void closeRealm(){
        if(myRealm != null && !myRealm.isClosed()){
            myRealm.close();
            Log.d(TAG, "----- CLOSE REALM -----");
        }
    }
}
