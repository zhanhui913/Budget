package com.zhan.budget;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Util.BudgetPreference;

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

    @Override
    public void onCreate(){
        super.onCreate();
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
    }
}
