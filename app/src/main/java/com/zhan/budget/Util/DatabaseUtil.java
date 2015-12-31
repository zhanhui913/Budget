package com.zhan.budget.Util;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.zhan.budget.Database.Database;

/**
 * Created by zhanyap on 2015-12-29.
 */
public final class DatabaseUtil {

    private static Database sqlhelper = null;
    private static SQLiteDatabase database = null;
    private static Context context = null;

    /** Prevents Instances */
    private DatabaseUtil(){};

    /**
     * Initiates the Database for access
     * @param context Application context
     */
    public static void initiate(Context context){
        if (sqlhelper == null)
            sqlhelper = new Database(context);

        if (DatabaseUtil.context == null)
            DatabaseUtil.context = context;
    }

    /**
     * Opens the database for reading
     * @throws SQLException if the database cannot be opened for reading
     */
    public static void openReadable() throws SQLException {
        if (database == null)
            database = sqlhelper.getReadableDatabase();
    }

    /**
     * Opens the database for writing
     * Defaults to Foreign Keys Constraint ON
     * @throws SQLException if the database cannot be opened for writing
     */
    public static void openWritable() throws SQLException{
        if ((database == null)? true : database.isReadOnly()) {
            openWritable(true);
        }
    }

    /**
     * Opens the database for writing
     * @param foreignKeys State of Foreign Keys Constraint, true = ON, false = OFF
     * @throws SQLException if the database cannot be opened for writing
     */
    public static void openWritable(boolean foreignKeys) throws SQLException{
        database = sqlhelper.getWritableDatabase();
        if (foreignKeys) {
            database.execSQL("PRAGMA foreign_keys = ON;");
        } else {
            database.execSQL("PRAGMA foreign_keys = OFF;");
        }
    }

    /**
     * Closes the database
     */
    public static void close(){
        if (database != null){
            database.close();
            database = null;
        }
        if (sqlhelper != null){
            sqlhelper.close();
            sqlhelper = null;
        }
    }
}
