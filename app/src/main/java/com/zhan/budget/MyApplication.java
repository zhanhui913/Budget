package com.zhan.budget;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Services.CustomJobCreator;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.ThemeUtil;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Zhan on 16-06-02.
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    private int CURRENT_THEME = ThemeUtil.THEME_LIGHT;

    @Override
    public void onCreate(){
        super.onCreate();

        instance = this;

        CURRENT_THEME = BudgetPreference.getCurrentTheme(instance); Log.d("GOD", "MyApplication, Theme : "+CURRENT_THEME);

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name(Constants.REALM_NAME)
                .schemaVersion(2)
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        // DynamicRealm exposes an editable schema
                        RealmSchema schema = realm.getSchema();

                        //migrate to version 2
                        if(oldVersion == 1){
                            schema.get("Category")
                                    .addField("isText",boolean.class)
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            //Sets all empty values to false
                                            obj.set("isText", false);
                                        }
                                    });

                            oldVersion++;
                        }


                        Toast.makeText(MyApplication.this, "a) It looks like you're at version "+oldVersion, Toast.LENGTH_SHORT).show();
                        Log.d("MY_APP", "old version :"+oldVersion);
/*
                        //migrate to version 3

                        //need to go through all transaction and apply selected default budget currency (if selected from here)
                        //maybe ask default currency from here

                        if(oldVersion == 2){
                             schema.get("Transaction")
                                     .addField("currency", BudgetCurrency.class)
                                     .transform(new RealmObjectSchema.Function() {
                                         @Override
                                         public void apply(DynamicRealmObject obj) {
                                             //Sets all empty values to USD
                                             BudgetCurrency usdCurrency = new BudgetCurrency();
                                             usdCurrency.setCurrencyCode(Constants.DEFAULT_CURRENCY_CODE);
                                             usdCurrency.setCurrencyName(Constants.DEFAULT_CURRENCY_NAME);
                                             usdCurrency.setDefault(true);

                                             obj.set("currency",usdCurrency);
                                         }
                                     });
                             Toast.makeText(MyApplication.this, "b) It looks like you were at version 2, now at 3", Toast.LENGTH_SHORT).show();
                             oldVersion++;
                         }*/
                    }
                })
                .build();
        Realm.setDefaultConfiguration(config);

        BudgetPreference.resetRealmCache(this);


        JobManager.create(this).addJobCreator(new CustomJobCreator());
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public static MyApplication getInstance(Context context) {
        return context != null ? (MyApplication) context.getApplicationContext() : instance;
    }

    public int getBudgetTheme() {
        return CURRENT_THEME;
    }

    public void setBudgetTheme(@NonNull int theme) {
        CURRENT_THEME = theme;
        BudgetPreference.setCurrentTheme(instance, CURRENT_THEME);
    }

}
