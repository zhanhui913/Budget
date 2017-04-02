package com.zhan.budget;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.DataBackup;
import com.zhan.budget.Util.ThemeUtil;
import com.zhan.budget.Util.Util;

import java.util.ArrayList;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

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
                .schemaVersion(3)
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        // DynamicRealm exposes an editable schema
                        final RealmSchema schema = realm.getSchema();

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

                        //migration to version 3
                        if(oldVersion == 2){
                            //Change Transaction's price and Category's budget from float to double
                            schema.get("Transaction")
                                    .addField("price_tmp", double.class)
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            //Take the value from price column and add it to price_tmp
                                            obj.setDouble("price_tmp", obj.getFloat("price"));
                                        }
                                    })
                                    .removeField("price")
                                    .renameField("price_tmp","price");

                            schema.get("Category")
                                    .addField("budget_tmp", double.class)
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            //Take the value from budget column and add it to budget_tmp
                                            obj.setDouble("budget_tmp", obj.getFloat("budget"));
                                        }
                                    })
                                    .removeField("budget")
                                    .renameField("budget_tmp","budget");

                            final List<DynamicRealmObject> locationList = new ArrayList<>();

                            //Step 1 : Add new column that has the correct location name format
                            schema.get("Location")
                                    .addField("name_tmp", String.class)
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            String correctName = Util.capsFirstWord(obj.getString("name"));

                                            try{
                                                //Wont have any problem with primary key exception
                                                //as this is a temp column with no primary key
                                                //attribute
                                                obj.setString("name_tmp", correctName);

                                                //Try to change primary key attribute now, if there
                                                //already exist one, it will get caught by exception.
                                                obj.setString("name", correctName);

                                                //If reached here, that means we successfully change primary key.
                                                //Add to list.
                                                locationList.add(obj);
                                            }catch(RealmPrimaryKeyConstraintException e){
                                                Log.d("HELP", "There already exist a Location : "+correctName);
                                            }
                                        }
                                    });

                            //Step 2 : Replace Transaction's location with correct location object (with correct name format)
                            schema.get("Transaction")
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            DynamicRealmObject location = obj.getObject("location");

                                            String locationName = location.getString("name");
                                            String tempLocationName = location.getString("name_tmp");

                                            //If transaction's location's name_tmp doesnt match name
                                            //Then we need to change the Transaction's old location to match the name_tmp
                                            if(!locationName.equals(tempLocationName)){
                                                for(int i = 0; i < locationList.size(); i++){
                                                    if(locationList.get(i).getString("name").equals(tempLocationName)){
                                                        obj.setObject("location", locationList.get(i));
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });

                            final List<DynamicRealmObject> locationToDeleteList = new ArrayList<>();

                            //Step 3 : Now delete the name_tmp field in Location.
                            // Find those location with the wrong name format
                            schema.get("Location")
                                    .removeField("name_tmp")
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            for(int i = 0 ; i < locationList.size(); i++){
                                                //If after converting to lowercase and is equal and
                                                //not the same obj, delete it.
                                                // {Location: name="costco"} => delete
                                                // {Location: name="COSTCO"} => delete
                                                // {Location: name="COstco"} => delete
                                                // {Location: name="Costco"} => dont delete
                                                if(locationList.get(i).getString("name").equalsIgnoreCase(obj.getString("name"))){
                                                    if(!obj.equals(locationList.get(i))){
                                                        Log.d("HELP","Adding "+obj+" to the delete list");
                                                        locationToDeleteList.add(obj);
                                                        //obj.deleteFromRealm();
                                                    }
                                                }
                                            }



                                        }
                                    });

                            //Step 4 : Remove the location with incorrect name format as well.
                            // (ie: those not in locationList)
                            ///realm.beginTransaction();
                            realm.where("Location").findAllAsync().addChangeListener(new RealmChangeListener<RealmResults<DynamicRealmObject>>() {
                                @Override
                                public void onChange(RealmResults<DynamicRealmObject> element) {
                                    element.removeChangeListener(this);

                                    for(int i = 0 ; i < element.size(); i++){
                                        for(int k = 0; k < locationToDeleteList.size(); k++){
                                            if(element.get(i).getString("name").equals(locationToDeleteList.get(k).getString("name"))){
                                                Log.d("HELP","Remove "+element.get(i).getString("name")+" from realm list");
                                                element.get(i).deleteFromRealm();
                                            }
                                        }
                                    }
                                }
                            });

                            //realm.commitTransaction();


                            oldVersion++;
                        }

                        Log.d("MY_APP", "old version :"+oldVersion);
                    }
                })
                .build();
        Realm.setDefaultConfiguration(config);

        BudgetPreference.resetRealmCache(this);

        //JobManager.create(this).addJobCreator(new CustomJobCreator());

        Log.d("HELP", "start listening to realm changes");
        listenToRealmDBChanges();

    }

    private void listenToRealmDBChanges(){
        final Realm myRealm = Realm.getDefaultInstance();
        myRealm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                Log.d("HELP","Theres a change in DB in REalm");

                if(BudgetPreference.getAllowAutoBackup(getApplicationContext())){
                    Log.d("HELP","backing up now");

                    DataBackup.backUpData(getApplicationContext());
                }
            }
        });
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
