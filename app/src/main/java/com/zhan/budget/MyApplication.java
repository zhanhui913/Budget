package com.zhan.budget;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.zhan.budget.Etc.Constants;
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

                            //Combine Locations with same name
                            //Step 1 : Add new column that matches name
                            //Step 2 : Take values from that column and add it back to name

                            final List<DynamicRealmObject> locList = new ArrayList<>();

                            schema.get("Location")
                                    .addField("name_tmp", String.class)
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {



                                            String oldName = obj.getString("name");

                                            Log.d("HELP", "trying to change : "+oldName+"  to "+Util.capsFirstWord(oldName));

                                            try{
                                                //Wont have any problem with primary key exception
                                                //as this is a temp column with no primary key
                                                //attribute
                                                obj.setString("name_tmp", Util.capsFirstWord(oldName));

                                                //Try to change primary key attribute now
                                                obj.setString("name", Util.capsFirstWord(oldName));

                                                //If reached here, successfully change primary key
                                                //Add to list
                                                locList.add(obj);
                                            }catch(RealmPrimaryKeyConstraintException e){
                                                Log.d("HELP", "There already exist a Location : "+oldName);
                                            }
                                        }
                                    });

                            Log.d("HELP", "-------------");
                            Log.d("HELP", "There are "+locList.size()+" loc in list");
                            for(int i  = 0; i < locList.size();i++){
                                Log.d("HELP", i+") "+locList.get(i));
                            }
                            schema.get("Transaction")
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            DynamicRealmObject cat = obj.getObject("category"); //debug
                                            String catName = cat.getString("name"); //debug

                                            DynamicRealmObject location = obj.getObject("location");

                                            String locationName = location.getString("name");
                                            String tempLocationName = location.getString("name_tmp");

                                            Log.d("HELP", "Transaction ("+catName+"), location : "+locationName+", tmp = "+tempLocationName);

                                            //If transaction's location's name_tmp doesnt match name
                                            //Then we need to change the Transaction's location to match the name_tmp
                                            if(!locationName.equals(tempLocationName)){
                                                for(int i = 0; i < locList.size(); i++){
                                                    if(locList.get(i).getString("name").equals(tempLocationName)){
                                                        obj.setObject("location", locList.get(i));

                                                        Log.d("HELP", "New Transaction ("+catName+"), location : "+obj.getObject("location").getString("name")+", tmp = "+obj.getObject("location").getString("name_tmp"));

                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });
                            Log.d("HELP", "-------------");

                            //Now delete the name_tmp field in Location
                            schema.get("Location").removeField("name_tmp");



                            oldVersion++;
                        }

                        Toast.makeText(MyApplication.this, "a) It looks like you're at version "+oldVersion, Toast.LENGTH_SHORT).show();
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
