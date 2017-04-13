package com.zhan.budget;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.BudgetType;
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
    private static final String TAG = "MyApplication";

    private static MyApplication instance;

    private int CURRENT_THEME = ThemeUtil.THEME_LIGHT;

    private Realm myRealm;
    private RealmConfiguration realmConfig;

    @Override
    public void onCreate(){
        super.onCreate();

        instance = this;

        CURRENT_THEME = BudgetPreference.getCurrentTheme(instance); Log.d(TAG, "MyApplication, Theme : "+CURRENT_THEME);



        realmConfig = new RealmConfiguration.Builder(this)
                .name(Constants.REALM_NAME)
                .schemaVersion(3)
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
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
                                                //This means this object should be removed.
                                                //Add to list.

                                                //Have to create a copy as realm dont allow us to touch un-managed realm outside of this loop
                                                DynamicRealmObject drobj = realm.createObject("Location");
                                                drobj.setString("name", obj.getString("name"));
                                                drobj.setString("color", obj.getString("color"));

                                                locationList.add(drobj);
                                            }catch(RealmPrimaryKeyConstraintException e){
                                                Log.d(TAG, "There already exist a Location : "+correctName);
                                            }
                                        }
                                    });

                            //Step 2 : Replace Transaction's location with correct location object (with correct name format)
                            schema.get("Transaction")
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            DynamicRealmObject location = obj.getObject("location");

                                            if(location != null){
                                                String locationName = location.getString("name").trim();
                                                String tempLocationName = location.getString("name_tmp").trim();

                                                //If transaction's location's name_tmp doesnt match name
                                                //Then we need to change the Transaction's old location to match the name_tmp
                                                if(!locationName.equals(tempLocationName)){
                                                    for(int i = 0; i < locationList.size(); i++){
                                                        if(locationList.get(i).getString("name").trim().equals(tempLocationName)){
                                                            obj.setObject("location", locationList.get(i));
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });

                            //Step 3 : Now delete the name_tmp field in Location.
                            // Find those location with the wrong name format
                            // Remove the location with incorrect name format as well.
                            // (ie: those not in locationList) by adding false to isNew field
                            schema.get("Location")
                                    .removeField("name_tmp")
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            for(int i = 0 ; i < locationList.size(); i++){
                                                //If after converting to lowercase and is equal and
                                                //not the same obj, delete it by settings isNew to false
                                                // {Location: name="costco"} => delete
                                                // {Location: name="COSTCO"} => delete
                                                // {Location: name="COstco"} => delete
                                                // {Location: name="Costco"} => dont delete
                                                if(locationList.get(i).getString("name").trim().equalsIgnoreCase(obj.getString("name"))){
                                                    if(!obj.equals(locationList.get(i))){
                                                        obj.deleteFromRealm();
                                                    }
                                                }
                                            }
                                        }
                                    });

                            oldVersion++;
                        }

                        Log.d(TAG, "old version :"+oldVersion);

                        listenToRealmDBChanges();
                    }
                })
                .build();
        Realm.setDefaultConfiguration(realmConfig);

        BudgetPreference.resetRealmCache(this);

        //JobManager.create(this).addJobCreator(new CustomJobCreator());

       // listenToRealmDBChanges();
    }

    private void listenToRealmDBChanges(){
        Log.d(TAG, "start listening to realm changes");
        myRealm = Realm.getDefaultInstance();
        myRealm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                element.removeChangeListener(this);
                Log.d(TAG,"Theres a change in DB in REalm");

                if(BudgetPreference.getAllowAutoBackup(getApplicationContext())){
                    Log.d(TAG,"backing up now");

                    DataBackup.backUpData(getApplicationContext());
                }
            }
        });
    }

    public void resetRealm(){
        if(myRealm != null){
            myRealm.beginTransaction();
            myRealm.deleteAll();
            myRealm.commitTransaction();
        }
    }

    public void createDefaultRealmData(){
        myRealm.beginTransaction();

        //First time usage
        ArrayList<Category> categoryList = new ArrayList<>();
        ArrayList<Account> accountList = new ArrayList<>();
        ArrayList<Location> locationList = new ArrayList<>();

        String[] tempCategoryNameList = new String[]{"Breakfast", "Lunch", "Dinner", "Snacks", "Drink", "Rent", "Travel", "Car", "Shopping", "Necessity", "Utilities", "Bill", "Groceries"};
        int[] tempCategoryColorList = new int[]{R.color.lemon, R.color.orange, R.color.pumpkin, R.color.alizarin, R.color.cream_can, R.color.midnight_blue, R.color.peter_river, R.color.turquoise, R.color.wisteria, R.color.jordy_blue, R.color.concrete, R.color.emerald, R.color.gossip};
        int[] tempCategoryIconList = new int[]{R.drawable.c_food, R.drawable.c_food, R.drawable.c_food, R.drawable.c_food, R.drawable.c_cafe, R.drawable.c_house, R.drawable.c_airplane, R.drawable.c_car, R.drawable.c_shirt, R.drawable.c_etc, R.drawable.c_utilities, R.drawable.c_bill, R.drawable.c_groceries};

        //create expense category
        for (int i = 0; i < tempCategoryNameList.length; i++) {
            Category c = myRealm.createObject(Category.class);
            c.setId(Util.generateUUID());
            c.setName(tempCategoryNameList[i]);
            c.setColor(getResources().getString(tempCategoryColorList[i]));
            c.setIcon(getResources().getResourceEntryName(tempCategoryIconList[i]));
            c.setBudget(100.0f + (i/5));
            c.setType(BudgetType.EXPENSE.toString());
            c.setCost(0);
            c.setIndex(i);

            categoryList.add(c);
        }

        String[] tempCategoryIncomeNameList = new String[]{"Salary", "Other"};
        int[] tempCategoryIncomeColorList = new int[]{R.color.light_wisteria, R.color.harbor_rat};
        int[] tempCategoryIncomeIconList = new int[]{R.drawable.c_bill, R.drawable.c_etc};

        //create income category
        for (int i = 0; i < tempCategoryIncomeNameList.length; i++) {
            Category c = myRealm.createObject(Category.class);
            c.setId(Util.generateUUID());
            c.setName(tempCategoryIncomeNameList[i]);
            c.setColor(getResources().getString(tempCategoryIncomeColorList[i]));
            c.setIcon(getResources().getResourceEntryName(tempCategoryIncomeIconList[i]));
            c.setBudget(0);
            c.setType(BudgetType.INCOME.toString());
            c.setCost(0);
            c.setIndex(i);

            categoryList.add(c);
        }

        //Create default accounts
        String[] tempAccountList = new String[]{"Credit Card","Debit Card", "Cash"};
        int[] tempAccountColorList = new int[]{R.color.amethyst, R.color.asbestos, R.color.belize_hole};
        for(int i = 0 ; i < tempAccountList.length; i++){
            Account account = myRealm.createObject(Account.class);
            account.setId(Util.generateUUID());
            account.setName(tempAccountList[i]);
            account.setIsDefault((i == 0));
            account.setColor(getResources().getString(tempAccountColorList[i]));
            accountList.add(account);
        }

/*
        //Create fake locations
        String[] locationTempList = new String[] {"Belgium", "France", "Italy", "Germany", "Spain", "USA", "Canada", "Brazil", "Norway", "England"};
        for(int i = 0; i < locationTempList.length; i++){
            Location location = myRealm.createObject(Location.class);
            location.setName(locationTempList[i]);
            location.setAmount(0);
            location.setColor(Colors.getRandomColorString(instance));
            locationList.add(location);
        }

        //Create fake transactions
        Date startDate = DateUtil.convertStringToDate(instance, "2017-01-01");
        Date endDate = DateUtil.convertStringToDate(instance, "2017-04-11");

        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);


        String dayType;

        for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
            Random random = new Random();
            int rd = random.nextInt(categoryList.size());
            int rda = random.nextInt(accountList.size());
            int ll = random.nextInt(locationList.size());

            //if(date.before(new Date())){
                dayType = DayType.COMPLETED.toString();
            //}else{
            //    dayType = DayType.SCHEDULED.toString();
            //}

            //Create random transactions per day
            for (int j = 0; j < 1000; j++) {
                Transaction transaction = myRealm.createObject(Transaction.class);
                transaction.setId(Util.generateUUID());
                transaction.setDate(date);
                transaction.setDayType(dayType);
                transaction.setLocation(locationList.get(ll));

                Account account = accountList.get(rda);

                Category category = categoryList.get(rd);
                if(category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                    transaction.setPrice(-120.0f + (rd * 0.5f));
                }else{
                    transaction.setPrice(Math.abs(-120.0f + (rd * 0.5f)));
                }

                transaction.setAccount(account);
                transaction.setCategory(category);
                transaction.setNote("Note " + j + " for " + DateUtil.convertDateToString(instance,date));
            }
        }
*/

        myRealm.commitTransaction();
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
