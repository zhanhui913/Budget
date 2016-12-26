package com.zhan.budget.Util;

import android.os.Environment;

import com.zhan.budget.Etc.Constants;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;

/**
 * Created by zhanyap on 2016-12-23.
 */

public class DataBackup {
    private static File DOWNLOAD_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static final String EXPORT_REALM_FILE_NAME = "Backup_Budget.realm";
    public static final String IMPORT_REALM_FILE_NAME = Constants.REALM_NAME;

    public DataBackup(){}

    public static boolean backUpData(){
        try{
            //create a backup file
            File exportRealmFile = new File(DOWNLOAD_DIRECTORY, EXPORT_REALM_FILE_NAME);

            //If backup file already exist, delete it
            exportRealmFile.delete();

            //Copy current realm to backup file
            Realm myRealm = Realm.getDefaultInstance();
            myRealm.writeCopyTo(exportRealmFile);
            myRealm.close();

            return true;
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }
}
