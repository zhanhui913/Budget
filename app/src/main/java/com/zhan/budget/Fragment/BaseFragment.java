package com.zhan.budget.Fragment;

import android.support.v4.app.Fragment;
import android.util.Log;

import io.realm.Realm;

/**
 * Created by Zhan on 16-03-08.
 */
public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    private Realm myRealm;

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart");
        resumeRealm();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
        resumeRealm();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
        closeRealm();
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop");
        closeRealm();
    }
/*
    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
*/
    public void resumeRealm(){
        if(myRealm == null || myRealm.isClosed()){
            myRealm = Realm.getDefaultInstance();
            Log.i(TAG, "resumeRealm");
        }
    }

    public void closeRealm(){
        if(myRealm != null && !myRealm.isClosed()){
            myRealm.close();
            Log.i(TAG, "closeRealm");
        }
    }
}
