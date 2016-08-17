package com.zhan.budget;

import android.app.Application;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Util.BudgetPreference;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Zhan on 16-06-02.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name(Constants.REALM_NAME)
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);

        BudgetPreference.resetRealmCache(this);
    }
}
