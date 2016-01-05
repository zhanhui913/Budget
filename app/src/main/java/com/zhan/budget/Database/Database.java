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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Created by Zhan on 2015-12-29.
 */
public class Database extends SQLiteOpenHelper{
    //Logcat ag
    private static final String LOG = "DATABASE";

    //Database name
    private static final String DATABASE_NAME = "BudgetDatabase.db";

    //Database version
    private static final int DATABASE_VERSION = 1;

    //Column names for CATEGORY Table
    private static final String TABLE_CATEGORY = "budget_category";
    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_TITLE = "title";
    private static final String CATEGORY_BUDGET = "budget";
    private static final String CATEGORY_COST = "cost";
    private static final String CATEGORY_COLOR = "color";
    private static final String CATEGORY_ICON = "icon";

    //Column names for TRANSACTION Table
    private static final String TABLE_TRANSACTION = "budget_transaction";
    private static final String TRANSACTION_ID = "id";
    private static final String TRANSACTION_CATEGORY_ID = "category_id";
    private static final String TRANSACTION_NOTE = "note";
    private static final String TRANSACTION_DATE = "date";
    private static final String TRANSACTION_PRICE = "price";

    //Database creation sql statement for CATEGORY Table
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + " (" +
            CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CATEGORY_TITLE + " TEXT," +
            CATEGORY_BUDGET + " REAL," +
            CATEGORY_COST + " REAL," +
            CATEGORY_COLOR + " TEXT," +
            CATEGORY_ICON + " INTEGER);";

    //Database creation sql statement for TRANSACTION Table
    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE  " + TABLE_TRANSACTION + " (" +
            TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            TRANSACTION_CATEGORY_ID + " INTEGER," +
            TRANSACTION_NOTE + " TEXT," +
            TRANSACTION_DATE + " TEXT," +
            TRANSACTION_PRICE + " REAL,"+
            "FOREIGN KEY (" + TRANSACTION_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + CATEGORY_ID + "));";

    //Index on the date of the TRANSACTION Table
    private static final String INDEX_TABLE_TRANSACTION = "CREATE INDEX dateIndex ON "+ TABLE_TRANSACTION + "(" + TRANSACTION_DATE + ");";

    public Database(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //Create required tables
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_TRANSACTION);
        db.execSQL(INDEX_TABLE_TRANSACTION);
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
    public long createCategory(Category category){
        //Get references to writable db
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CATEGORY_TITLE, category.getName());
        values.put(CATEGORY_BUDGET, category.getBudget());
        values.put(CATEGORY_COST, category.getCost());
        values.put(CATEGORY_COLOR, category.getColor());
        values.put(CATEGORY_ICON, category.getIcon());

        long id = db.insert(TABLE_CATEGORY, null, values);

        db.close();
        //Log.d(TABLE_CATEGORY, "create Category " + category.getName());

        return id;
    }

    /**
     * Get the Category with the corresponding id from the database
     * @param id The id of the Category
     * @return Category
     */
    public Category getCategoryById(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        String[] TOUR_COLUMNS = {CATEGORY_ID,CATEGORY_TITLE, CATEGORY_BUDGET, CATEGORY_COST, CATEGORY_COLOR, CATEGORY_ICON};

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
            //Log.d(TABLE_CATEGORY,"GetCategory returns empty for id = "+id);
        }else{
            //Log.d(TABLE_CATEGORY, "GetCategory returns something for id = " + id);

            category.setId(cursor.getInt(0));
            category.setName(cursor.getString(1));
            category.setBudget(cursor.getFloat(2));
            category.setCost(cursor.getFloat(3));
            category.setColor(cursor.getString(4));
            category.setIcon(cursor.getInt(5));
        }

        cursor.close();
        db.close();
        return category;
    }

    /**
     * Get all Categories from the database
     * @return ArrayList<Category>
     */
    public ArrayList<Category> getAllCategory(){
        ArrayList<Category> categories = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_CATEGORY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query,null);

        Category category;
        if(cursor.moveToFirst()){
            do {
                category = new Category();
                category.setId(cursor.getInt(0));
                category.setName(cursor.getString(1));
                category.setBudget(cursor.getFloat(2));
                category.setCost(cursor.getFloat(3));
                category.setColor(cursor.getString(4));
                category.setIcon(cursor.getInt(5));

                //Add category to arraylist
                categories.add(category);
                //Log.d(TABLE_CATEGORY, "adding 1 category");
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
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
        values.put(CATEGORY_COLOR, category.getColor());
        values.put(CATEGORY_ICON, category.getIcon());

        int i = db.update(TABLE_CATEGORY,   // Table
                values,                 // Column/value
                CATEGORY_ID + " = ?",         // Selections
                new String[] {String.valueOf(category.getId())}); //Select i

        db.close();
        //Log.d(TABLE_CATEGORY, "updating category " + category.toString());
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

        //Log.d(TABLE_CATEGORY, "deleting category " + category.toString());
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
    public long createTransaction(Transaction transaction){
        //Get references to writable db
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_CATEGORY_ID, transaction.getCategory().getId());
        values.put(TRANSACTION_NOTE, transaction.getNote());
        values.put(TRANSACTION_DATE, Util.convertDateToString(transaction.getDate()));
        values.put(TRANSACTION_PRICE, transaction.getPrice());

        db.beginTransaction();
        long transactionId = db.insert(TABLE_TRANSACTION, null, values);
        db.endTransaction();

        db.close();
        //Log.d(TABLE_TRANSACTION, "create Transaction " + transaction.toString());
        return transactionId;
    }

    /**
     * Insert a Transaction into the database
     * @param transactionList The new Transaction List to be inserted
     */
    public void createBulkTransaction(ArrayList<Transaction> transactionList){
        //Get references to writable db
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        for(int i = 0; i < transactionList.size(); i++){
            ContentValues values = new ContentValues();
            values.put(TRANSACTION_CATEGORY_ID, transactionList.get(i).getCategory().getId());
            values.put(TRANSACTION_NOTE, transactionList.get(i).getNote());
            values.put(TRANSACTION_DATE, Util.convertDateToString(transactionList.get(i).getDate()));
            values.put(TRANSACTION_PRICE, transactionList.get(i).getPrice());

            db.insert(TABLE_TRANSACTION, null, values);
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        //Log.d(TABLE_TRANSACTION, "create Transaction list ");
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
                " " + TRANSACTION_ID + " = ?",       // Selections
                new String[]{String.valueOf(id)},    // selections argument
                null,                                // group by
                null,                                // having
                null,                                // order by
                null);                               // limit

        Transaction transaction = new Transaction();
        if(!(cursor.moveToFirst()) || (cursor.getCount()==0)){
            //Log.d(TABLE_TRANSACTION,"GetTransaction returns empty for id = "+id);
        }else{
            //Log.d(TABLE_TRANSACTION, "GetTransaction returns something for id = " + id);

            transaction.setId(cursor.getInt(0));
            transaction.setCategory(getCategoryById(cursor.getInt(1)));
            transaction.setNote(cursor.getString(2));
            transaction.setDate(Util.convertStringToDate(cursor.getString(3)));
            transaction.setPrice(cursor.getFloat(4));
        }

        cursor.close();
        db.close();
        return transaction;
    }

    /**
     * Get all Transactions from the database
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransaction(){
        return getAllTransaction(false);
    }

    /**
     * Get all Transactions from the database
     * @param unique Unique dates in list of Transactions (default false)
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransaction(boolean unique){
        ArrayList<Transaction> transactions = new ArrayList<>();

        String[] TOUR_COLUMNS = {"*"};

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTION, //Table
                TOUR_COLUMNS,                       //Column names
                null,                              // Selections
                null,                               // selections argument
                (unique)?TRANSACTION_DATE:null,     // group by
                null,                               // having
                null,                               // order by
                null);                              // limit

        Transaction transaction;
        if(cursor.moveToFirst()){
            do {
                transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setCategory(getCategoryById(cursor.getInt(1)));
                transaction.setNote(cursor.getString(2));
                transaction.setDate(Util.convertStringToDate(cursor.getString(3)));
                transaction.setPrice(cursor.getFloat(4));

                //Add Transaction to arraylist
                transactions.add(transaction);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactions;
    }

    /**
     * Get all Transactions that have the date
     * @param date The current date
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransactionInDate(Date date){
        ArrayList<Transaction> transactions = new ArrayList<>();

        String[] TOUR_COLUMNS = {TRANSACTION_ID,TRANSACTION_CATEGORY_ID,TRANSACTION_NOTE,TRANSACTION_DATE,TRANSACTION_PRICE};

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTION,           //Table
                TOUR_COLUMNS,                                 //Column names
                " " + TRANSACTION_DATE + " = ?",              // Selections
                new String[]{Util.convertDateToString(date)}, // selections argument
                null,                                // group by
                null,                                // having
                null,                                // order by
                null);                               // limit

        Transaction transaction;
        if(cursor.moveToFirst()){
            do {
                //Log.d(TABLE_TRANSACTION, "GetAllTransactionsInDate  date = " + Util.convertDateToString(date)+" -> "+cursor.getString(2));
                transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setCategory(getCategoryById(cursor.getInt(1)));
                transaction.setNote(cursor.getString(2));
                transaction.setDate(Util.convertStringToDate(cursor.getString(3)));
                transaction.setPrice(cursor.getFloat(4));

                //Add Transaction to arraylist
                transactions.add(transaction);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactions;
    }

    /**
     * Get all Transactions in the month of the year
     * @param date The current date
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransactionInMonth(Date date){
        return getAllTransactionInMonth(date, false);
    }

    /**
     * Get all Transactions in the month of the year
     * @param date The current date
     * @param unique Unique dates in list of Transactions (default false)
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransactionInMonth(Date date, boolean unique){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        return getAllTransactionInMonth(year, month, unique);
    }

    /**
     * Get all Transactions in the month of the year
     * @param month The current month (0 = January, 11 = December)
     * @param year The current year
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransactionInMonth(int year, int month){
        return getAllTransactionInMonth(month, year, false);
    }

    /**
     * Get all Transactions in the month of the year
     * @param month The current month (0 = January, 11 = December)
     * @param year The current year
     * @param unique Unique dates in list of Transactions (default false)
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransactionInMonth(int year, int month, boolean unique){
        ArrayList<Transaction> transactions = new ArrayList<>();

        String[] TOUR_COLUMNS = {TRANSACTION_ID,TRANSACTION_CATEGORY_ID,TRANSACTION_NOTE,TRANSACTION_DATE,TRANSACTION_PRICE};

        String beginMonth = Util.convertDateToString((new GregorianCalendar(year, month, 1)).getTime());

        //If this is December, the next month needs to be the following year's January
        if(month == 11){
            month = 0;
            year++;
        }

        String endMonth = Util.convertDateToString((new GregorianCalendar(year, month, 1)).getTime());

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTION,           //Table
                TOUR_COLUMNS,                                 //Column names
                " " + TRANSACTION_DATE + " BETWEEN ? AND ?",  // Selections
                new String[]{beginMonth, endMonth}, // selections argument
                (unique)?TRANSACTION_DATE:null,     // group by
                null,                               // having
                null,                               // order by
                null);                              // limit

        Transaction transaction;
        if(cursor.moveToFirst()){
            do {
                //Log.d(TABLE_TRANSACTION, "GetAllTransactionsForMonth  month = " + beginMonth+" -> "+cursor.getString(2));
                transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setCategory(getCategoryById(cursor.getInt(1)));
                transaction.setNote(cursor.getString(2));
                transaction.setDate(Util.convertStringToDate(cursor.getString(3)));
                transaction.setPrice(cursor.getFloat(4));

                //Add Transaction to arraylist
                transactions.add(transaction);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactions;
    }

    /**
     * Get all Transactions in the year
     * @param year The current year
     * @return ArrayList<Transaction>
     */
    public ArrayList<Transaction> getAllTransactionInYear(int year){
        ArrayList<Transaction> transactions = new ArrayList<>();

        String[] TOUR_COLUMNS = {TRANSACTION_ID,TRANSACTION_CATEGORY_ID,TRANSACTION_NOTE,TRANSACTION_DATE,TRANSACTION_PRICE};

        String beginYear = Util.convertDateToString((new GregorianCalendar(year, 0, 1)).getTime());
        String endYear = Util.convertDateToString((new GregorianCalendar(year + 1, 0, 1)).getTime());

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTION,           //Table
                TOUR_COLUMNS,                                 //Column names
                " " + TRANSACTION_DATE + " BETWEEN ? AND ?",  // Selections
                new String[]{beginYear, endYear}, // selections argument
                null,                                // group by
                null,                                // having
                null,                                // order by
                null);                               // limit

        Transaction transaction;
        if(cursor.moveToFirst()){
            do {
                //Log.d(TABLE_TRANSACTION, "GetAllTransactionsInYear  year = " + beginYear+" -> "+cursor.getString(2));
                transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setCategory(getCategoryById(cursor.getInt(1)));
                transaction.setNote(cursor.getString(2));
                transaction.setDate(Util.convertStringToDate(cursor.getString(3)));
                transaction.setPrice(cursor.getFloat(4));

                //Add Transaction to arraylist
                transactions.add(transaction);
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
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
        values.put(TRANSACTION_DATE, Util.convertDateToString(transaction.getDate()));
        values.put(TRANSACTION_PRICE, transaction.getPrice());

        int i = db.update(TABLE_TRANSACTION,   // Table
                values,                        // Column/value
                TRANSACTION_ID + " = ?",              // Selections
                new String[] {String.valueOf(transaction.getId())}); //Select i

        db.close();
        //Log.d(TABLE_TRANSACTION, "updating transaction " + transaction.toString());
        return i;
    }

    /**
     * Delete the corresponding Transaction from the database
     * @param transaction The Transaction to be deleted
     */
    public void deleteTransaction(Transaction transaction){
        SQLiteDatabase db = this.getWritableDatabase();

        //Log.d(TABLE_TRANSACTION, "deleting transaction " + transaction.getNote());

        db.delete(TABLE_TRANSACTION,
                TRANSACTION_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())});

        db.close();
    }

    //----------------------------------------------------------------------------------------------
    //
    // ETC functions
    //
    //----------------------------------------------------------------------------------------------

    public void importDB() {
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

    public void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                if(createDirectory()){ //Log.d("FILE","can write file");
                    String currentDBPath = "//data//" + "com.zhan.budget" + "//databases//" + DATABASE_NAME;
                    String backupDBPath = "Budget/" + DATABASE_NAME;
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }else{
                    Log.d("FILE","cannot write file");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean createDirectory(){
        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Budget");
        return directory.mkdirs();
    }
}
