package com.zhan.budget;

import android.app.Application;

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
                    }
                })
                .build();
        Realm.setDefaultConfiguration(config);

        BudgetPreference.resetRealmCache(this);
    }
}
