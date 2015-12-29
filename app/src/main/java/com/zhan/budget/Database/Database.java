package com.zhan.budget.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.Util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


/**
 * Created by Zhan on 2015-12-29.
 */
public class Database extends SQLiteOpenHelper{
    //Logcat ag
    private static final String LOG = "DATABASE";

    //Database name
    private static final String DATABASE_NAME = "BudgetDatabase";

    //Database version
    private static final int DATABASE_VERSION = 1;

    //Column names for CATEGORY Table
    private static final String TABLE_CATEGORY = "budget_category";
    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_TITLE = "title";
    private static final String CATEGORY_BUDGET = "budget";
    private static final String CATEGORY_COST = "cost";

    //Column names for TRANSACTION Table
    private static final String TABLE_TRANSACTION = "budget_transaction";
    private static final String TRANSACTION_ID = "id";
    private static final String TRANSACTION_CATEGORY_ID = "category_id";
    private static final String TRANSACTION_NOTE = "note";
    private static final String TRANSACTION_DATE = "date";
    private static final String TRANSACTION_PRICE = "price";

    //Database creation sql statement for CATEGORY Table
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + " (" +
            CATEGORY_ID + " INTEGER PRIMARY KEY," +
            CATEGORY_TITLE + " TEXT," +
            CATEGORY_BUDGET + " REAL," +
            CATEGORY_COST + " REAL);";

    //Database creation sql statement for TRANSACTION Table
    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE " + TABLE_TRANSACTION + " (" +
            TRANSACTION_ID + " INTEGER PRIMARY KEY," +
            TRANSACTION_CATEGORY_ID + " INTEGER," +
            TRANSACTION_NOTE + " TEXT," +
            TRANSACTION_DATE + " TEXT," +
            TRANSACTION_PRICE + " REAL,"+
            "FOREIGN KEY (" + TRANSACTION_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + CATEGORY_ID + "));";

    private SQLiteDatabase sqLiteDatabase;

    public Database(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //Create required tables
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_TRANSACTION);
        sqLiteDatabase = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.w(LOG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        //On upgrade drop older tables (drop table with most dependencies first)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);

        //Create new tables
        onCreate(db);
    }
/*
    public boolean isOpen(){
        return sqLiteDatabase.isOpen();
    }*/

    //----------------------------------------------------------------------------------------------
    //
    // CRUD Operations for CATEGORY Table
    //
    //----------------------------------------------------------------------------------------------

    /**
     * Insert a Category into the database
     * @param category The new Category to be inserted
     */
    public void createCategory(Category category){
        //Get references to writable db
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(CATEGORY_ID, category.getId()); //Testing if auto increment works
        values.put(CATEGORY_TITLE, category.getName());
        values.put(CATEGORY_BUDGET, category.getBudget());
        values.put(CATEGORY_COST, category.getCost());

        db.insert(TABLE_CATEGORY, null, values);

        db.close();
        Log.d(TABLE_CATEGORY, "create Category " + category.toString());
        exportDB();
    }

    /**
     * Get the Category with the corresponding id from the database
     * @param id The id of the Category
     * @return Category
     */
    public Category getCategoryById(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        String[] TOUR_COLUMNS = {CATEGORY_ID,CATEGORY_TITLE, CATEGORY_BUDGET, CATEGORY_COST};

        Cursor cursor = db.query(TABLE_CATEGORY,     //Table
                TOUR_COLUMNS,                        //Column names
                " id = ?",                           // Selections
                new String[]{String.valueOf(id)},    // selections argument
                null,                                // group by
                null,                                // having
                null,                                // order by
                null);                               // limit

        Category category = new Category();
        if(!(cursor.moveToFirst()) || (cursor.getCount()==0)){
            Log.d(TABLE_CATEGORY,"GetCategory returns empty for id = "+id);
        }else{
            Log.d(TABLE_CATEGORY, "GetCategory returns something for id = " + id);

            category.setId(cursor.getInt(0));
            category.setName(cursor.getString(1));
            category.setBudget(cursor.getFloat(2));
            category.setCost(cursor.getFloat(3));
        }

        cursor.close();
        db.close();
        Log.d(TABLE_CATEGORY,"get Category "+category.toString());
        return category;
    }

    /**
     * Get the Category with the corresponding title from the database
     * @param title The title of the Category
     * @return Category
     */
    public Category getCategoryByTitle(String title){
        SQLiteDatabase db = this.getWritableDatabase();

        String[] TOUR_COLUMNS = {CATEGORY_ID,CATEGORY_TITLE};

        Cursor cursor = db.query(TABLE_CATEGORY,     //Table
                TOUR_COLUMNS,                        //Column names
                " title = ?",                        // Selections
                new String[]{title},    // selections argument
                null,                                // group by
                null,                                // having
                null,                                // order by
                null);                               // limit

        Category category = new Category();
        if(!(cursor.moveToFirst()) || (cursor.getCount()==0)){
            Log.d(TABLE_CATEGORY,"GetCategory returns empty for title = "+title);
        }else{
            Log.d(TABLE_CATEGORY,"GetCategory returns something for title = "+title);

            category.setId(cursor.getInt(0));
            category.setName(cursor.getString(1));
            category.setBudget(cursor.getFloat(2));
            category.setCost(cursor.getFloat(3));
        }

        cursor.close();
        db.close();
        Log.d(TABLE_CATEGORY,"get Category "+category.toString());
        return category;
    }

    /**
     * Get all Categories from the database
     * @return ArrayList<Category>
     */
    public ArrayList<Category> getAllCategory(){
        ArrayList<Category> categories = new ArrayList<Category>();

        String query = "SELECT * FROM " + TABLE_CATEGORY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query,null);

        Category category = null;
        if(cursor.moveToFirst()){
            do {
                category = new Category();
                category.setId(cursor.getInt(0));
                category.setName(cursor.getString(1));
                category.setBudget(cursor.getFloat(2));
                category.setCost(cursor.getFloat(3));

                //Add category to arraylist
                categories.add(category);
                Log.d(TABLE_CATEGORY, "adding 1 category");
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        Log.d(TABLE_CATEGORY,"get all categories ");
        return categories;
    }

    /**
     * Update the Category with the corresponding new Category into the database
     * @param category The new Category
     * @return The number of rows affected
     */
    public int updateCategory(Category category){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_ID,category.getId());
        values.put(CATEGORY_TITLE,category.getName());
        values.put(CATEGORY_BUDGET, category.getBudget());
        values.put(CATEGORY_COST, category.getCost());

        int i = db.update(TABLE_CATEGORY,   // Table
                values,                 // Column/value
                CATEGORY_ID + " = ?",         // Selections
                new String[] {String.valueOf(category.getId())}); //Select i

        db.close();
        Log.d(TABLE_CATEGORY, "updating category " + category.toString());
        exportDB();
        return i;
    }

    /**
     * Delete the corresponding Category from the database
     * @param category The Category to be deleted
     */
    public void deleteCategory(Category category){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_CATEGORY,
                CATEGORY_ID + " = ?",
                new String[]{String.valueOf(category.getId())});

        db.close();

        Log.d(TABLE_CATEGORY, "deleting category " + category.toString());
        exportDB();
    }

    //----------------------------------------------------------------------------------------------
    //
    // CRUD Operations for Transaction Table
    //
    //----------------------------------------------------------------------------------------------

    /**
     * Insert a Transaction into the database
     * @param transaction The new Transaction to be inserted
     */
    public void createTransaction(Transaction transaction){
        //Get references to writable db
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(TRANSACTION_ID, item.getId()); //Testing if auto increment works
        values.put(TRANSACTION_CATEGORY_ID, transaction.getCategory().getId());
        values.put(TRANSACTION_NOTE, transaction.getNote());
        values.put(TRANSACTION_DATE, transaction.getDate().toString());
        values.put(TRANSACTION_PRICE, transaction.getPrice());

        db.insert(TABLE_TRANSACTION, null, values);

        db.close();
        Log.d(TABLE_TRANSACTION, "create Item " + transaction.toString());

        exportDB();
    }

    /**
     * Get the Transaction with the corresponding id from the database
     * @param id The id of the Transaction
     * @return Trasaction
     */
    public Transaction getTransaction(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        String[] TOUR_COLUMNS = {TRANSACTION_ID,TRANSACTION_CATEGORY_ID,TRANSACTION_NOTE,TRANSACTION_DATE,TRANSACTION_PRICE};

        Cursor cursor = db.query(TABLE_TRANSACTION,  //Table
                TOUR_COLUMNS,                        //Column names
                " id = ?",                           // Selections
                new String[]{String.valueOf(id)},    // selections argument
                null,                                // group by
                null,                                // having
                null,                                // order by
                null);                               // limit

        Transaction transaction = new Transaction();
        if(!(cursor.moveToFirst()) || (cursor.getCount()==0)){
            Log.d(TABLE_TRANSACTION,"GetItem returns empty for id = "+id);
        }else{
            Log.d(TABLE_TRANSACTION, "GetItem returns something for id = " + id);

            transaction.setId(cursor.getInt(0));
            transaction.setCategory(getCategoryById(cursor.getInt(1)));
            transaction.setNote(cursor.getString(2));
            transaction.setDate(Util.parseDate(cursor.getString(3)));
            transaction.setPrice(cursor.getFloat(4));
        }

        cursor.close();
        db.close();
        Log.d(TABLE_TRANSACTION,"got Transaction "+transaction.toString());
        return transaction;
    }

    /**
     * Get all Transactions from the database
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransaction(){
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();

        String query = "SELECT * FROM " + TABLE_TRANSACTION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query,null);

        Transaction transaction = null;
        if(cursor.moveToFirst()){
            do {
                transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setCategory(getCategoryById(cursor.getInt(1)));
                transaction.setNote(cursor.getString(2));
                transaction.setDate(Util.parseDate(cursor.getString(3)));
                transaction.setPrice(cursor.getFloat(4));

                //Add Transaction to arraylist
                transactions.add(transaction);
                Log.d(TABLE_TRANSACTION, "adding 1 transaction");
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        Log.d(TABLE_TRANSACTION,"get all transaction "+transactions.toString());
        return transactions;
    }

    /**
     * Update the Transaction with the corresponding new Transaction into the database
     * @param transaction The new Transaction
     * @return The number of rows affected
     */
    public int updateTransaction(Transaction transaction){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRANSACTION_ID,transaction.getId());
        values.put(TRANSACTION_CATEGORY_ID, transaction.getCategory().getId());
        values.put(TRANSACTION_NOTE, transaction.getNote());
        values.put(TRANSACTION_DATE, transaction.getDate().toString());
        values.put(TRANSACTION_PRICE, transaction.getPrice());

        int i = db.update(TABLE_TRANSACTION,   // Table
                values,                        // Column/value
                TRANSACTION_ID + " = ?",              // Selections
                new String[] {String.valueOf(transaction.getId())}); //Select i

        db.close();
        Log.d(TABLE_TRANSACTION, "updating transaction " + transaction.toString());
        exportDB();
        return i;
    }

    /**
     * Delete the corresponding Transaction from the database
     * @param transaction The Transaction to be deleted
     */
    public void deleteTransaction(Transaction transaction){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_TRANSACTION,
                TRANSACTION_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())});

        db.close();

        Log.d(TABLE_TRANSACTION, "deleting transaction " + transaction.toString());
        exportDB();
    }

    //----------------------------------------------------------------------------------------------
    //
    // ETC functions
    //
    //----------------------------------------------------------------------------------------------

/* NOT SURE IF STILL NEEDED
    public Date convertStringToDate(String stringDate){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyy");
        Date date = null;

        try{
            date = formatter.parse(stringDate);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return date;
    }

    public String convertDateToString(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        String stringDate = formatter.format(date);

        return stringDate;
    }
*/

    private void importDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                createDirectory();

                String currentDBPath = "//data//" + "com.zhan.budget" + "//databases//" + DATABASE_NAME;
                String backupDBPath = "BudgetDatabase.sqlite"; // From SD directory.
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) { Log.d(LOG, "can write");
                createDirectory();

                String currentDBPath = "//data//" + "com.zhan.budget" + "//databases//" + DATABASE_NAME;
                String backupDBPath = "Budget/BudgetDatabase.sqlite";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDirectory(){
        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Budget");
        directory.mkdirs();
    }
}
