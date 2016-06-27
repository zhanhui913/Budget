package com.zhan.budget.Fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhan.budget.Activity.Settings.OpenSourceActivity;
import com.zhan.budget.Activity.Settings.SettingsAccount;
import com.zhan.budget.Activity.Settings.SettingsCategory;
import com.zhan.budget.Activity.Settings.SettingsLocation;
import com.zhan.budget.BuildConfig;
import com.zhan.budget.Etc.CSVFormatter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.ThemeUtil;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.ExtendedNumberPicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends BaseFragment {

    private static final String TAG = "SettingFragment";

    private ViewGroup themeBtn, firstDayBtn, categoryOrderBtn, defaultAccountBtn, locationBtn, backupBtn, openLicenseBtn, emailBtn;
    private TextView themeContent, firstDayContent, backupContent, versionNumber;

    private TextView  restoreBackupBtn ,resetBtn, exportCSVBtn, tourBtn, faqBtn;

    //
    private static int CURRENT_THEME;

    //CSV
    private List<Transaction> transactionList;
    private RealmResults<Transaction> transactionResults;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_setting;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        themeBtn = (ViewGroup) view.findViewById(R.id.themeBtn);
        themeContent = (TextView) view.findViewById(R.id.themeContent);

        firstDayBtn = (ViewGroup) view.findViewById(R.id.firstDayBtn);
        firstDayContent = (TextView) view.findViewById(R.id.firstDayContent);

        categoryOrderBtn = (ViewGroup) view.findViewById(R.id.categoryOrderBtn);

        defaultAccountBtn = (ViewGroup) view.findViewById(R.id.defaultAccountBtn);

        locationBtn = (ViewGroup) view.findViewById(R.id.locationBtn);

        backupBtn = (ViewGroup) view.findViewById(R.id.backupBtn);
        backupContent = (TextView) view.findViewById(R.id.backupContent);

        restoreBackupBtn = (TextView)view.findViewById(R.id.restoreBackupBtn);
        resetBtn = (TextView) view.findViewById(R.id.resetDataBtn);
        exportCSVBtn = (TextView) view.findViewById(R.id.exportCSVBtn);
        emailBtn = (ViewGroup) view.findViewById(R.id.emailBtn);
        tourBtn = (TextView) view.findViewById(R.id.tourBtn);
        faqBtn = (TextView) view.findViewById(R.id.faqBtn);
        openLicenseBtn = (ViewGroup) view.findViewById(R.id.openSourceBtn);

        versionNumber = (TextView) view.findViewById(R.id.appVersionTextId);

        //Set theme
        CURRENT_THEME = BudgetPreference.getCurrentTheme(getContext());
        themeContent.setText((CURRENT_THEME == ThemeUtil.THEME_DARK ? "Night Mode" : "Day Mode"));

        //Set start day
        int startDay = BudgetPreference.getStartDay(getContext());
        firstDayContent.setText(startDay == Calendar.SUNDAY ? "Sunday" : "Monday");

        //Set default account
        /*if(BudgetPreference.getDefaultAccount(getContext()).equalsIgnoreCase("NA")){
            defaultAccountContent.setText("Credit Card");
        }else {
            defaultAccountContent.setText(BudgetPreference.getDefaultAccount(getContext()));
        }*/

        //Set last backup
        updateLastBackupInfo(BudgetPreference.getLastBackup(getContext()));

        //set version number
        versionNumber.setText("v"+BuildConfig.VERSION_NAME);

        addListeners();
    }

    private void addListeners(){
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                themeContent.setText((CURRENT_THEME == ThemeUtil.THEME_DARK ? "Night Mode": "Day Mode"));
                ThemeUtil.changeToTheme(getActivity(), (CURRENT_THEME == ThemeUtil.THEME_DARK ? ThemeUtil.THEME_LIGHT : ThemeUtil.THEME_DARK));
            }
        });

        firstDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int startDay = BudgetPreference.getStartDay(getContext());

                if(startDay == Calendar.SUNDAY){
                    firstDayContent.setText("Monday");
                    BudgetPreference.setMondayStartDay(getContext());
                }else{
                    firstDayContent.setText("Sunday");
                    BudgetPreference.setSundayStartDay(getContext());
                }
            }
        });

        categoryOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsCategory = new Intent(getContext(), SettingsCategory.class);
                startActivity(settingsCategory);
            }
        });

        defaultAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getAccountList();
                Intent settingsAccount = new Intent(getContext(), SettingsAccount.class);
                startActivity(settingsAccount);
            }
        });

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsLocation = new Intent(getContext(), SettingsLocation.class);
                startActivity(settingsLocation);
            }
        });

        backupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionToCreateBackup();
            }
        });

        restoreBackupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionToRestoreBackup();
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
            }
        });

        exportCSVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFilePermissionToWriteCSV();
            }
        });

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email();
            }
        });

        tourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTutorials();
            }
        });

        faqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        openLicenseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openSource = new Intent(getContext(), OpenSourceActivity.class);
                startActivity(openSource);
            }
        });
    }


    private void sendRealmData() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                if(createDirectory()){ Log.d("FILE", "can write file");
                    String currentDBPath = "//data//" + "com.zhan.budget" + "//files//" + Constants.REALM_NAME;
                    String backupDBPath = "Budget/" + Constants.REALM_NAME;
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    File exportRealmFile = new File(Environment.getExternalStorageDirectory().toString() + "/Budget/" + Constants.REALM_NAME);
                    email(exportRealmFile, "Realm Data", "This is a realm data");
                }else{
                    Log.d("FILE","cannot write file");
                    Util.createSnackbar(getContext(), getView(), "Fail to retrieve file.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Default Account
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
/*
    private void getAccountList(){
        Toast.makeText(getContext(), "click on default account_popup", Toast.LENGTH_SHORT).show();

        final Realm myRealm = Realm.getDefaultInstance();
        final RealmResults<Account> accountResults = myRealm.where(Account.class).findAllAsync();
        accountResults.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                element.removeChangeListener(this);
                openAccountPopupMenu(myRealm.copyFromRealm(element));
                myRealm.close();
            }
        });
    }

    private void openAccountPopupMenu(final List<Account> accountList){
        //Creating the instance of PopupMenu
        final PopupMenu popup = new PopupMenu(getContext(), defaultAccountBtn);

        for (int i = 0; i < accountList.size(); i++) {
            popup.getMenu().add(0,0,i, accountList.get(i).getName());
        }

        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.account_popup, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(getContext(), "You Clicked : " + item.getTitle()+" at index "+item.getOrder(), Toast.LENGTH_SHORT).show();
                Log.d("TITLE", "from account list : "+accountList.get(item.getOrder()).getName());
                setDefaultAccount(accountList.get(item.getOrder()));
                return true;
            }
        });

        popup.show();
    }

    private void setDefaultAccount(final Account defaultAccount){
        final Realm myRealm = Realm.getDefaultInstance();

        final RealmResults<Account> accounts = myRealm.where(Account.class).findAllAsync();
        accounts.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                element.removeChangeListener(this);

                myRealm.beginTransaction();

                for(int i = 0; i < accounts.size(); i++){
                    if(defaultAccount.getId().equalsIgnoreCase(element.get(i).getId())){
                        //myRealm.copyToRealmOrUpdate(accounts.get(i).setIsDefault(true));

                        element.get(i).setIsDefault(true);
                    }else{
                        //myRealm.copyToRealmOrUpdate();
                        element.get(i).setIsDefault(false);
                    }
                }

                myRealm.commitTransaction();
                myRealm.close();

                //BudgetPreference.setDefaultAccount(getContext(), defaultAccount.getName());
                //defaultAccountContent.setText(defaultAccount.getName());
            }
        });
    }
*/
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Backup Data
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private File DOWNLOAD_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private final String EXPORT_REALM_FILE_NAME = "Backup_Budget.realm";
    private final String IMPORT_REALM_FILE_NAME = Constants.REALM_NAME;

    private void checkPermissionToCreateBackup(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //STORAGE permission has not been granted
            requestFilePermissionToWrite();
        }else{
            backUpData();
        }
    }

    private void checkPermissionToRestoreBackup(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //STORAGE permission has not been granted
            requestFilePermissionToRead();
        }else{
            restore();
        }
    }

    public void requestFilePermissionToWrite(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            new AlertDialog.Builder(getContext())
                    .setTitle("Permission denied")
                    .setMessage("Without this permission the app is unable to create a backup data.")
                    .setPositiveButton("Re-try", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    })
                    .setNegativeButton("Deny", null)
                    .create()
                    .show();

        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void requestFilePermissionToRead(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            new AlertDialog.Builder(getContext())
                    .setTitle("Permission denied")
                    .setMessage("Without this permission the app is unable to restore the backup data.")
                    .setPositiveButton("Re-try", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    })
                    .setNegativeButton("Deny", null)
                    .create()
                    .show();

        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    public void backUpData(){
        try{
            //create a backup file
            File exportRealmFile = new File(DOWNLOAD_DIRECTORY, EXPORT_REALM_FILE_NAME);

            //If backup file already exist, delete it
            exportRealmFile.delete();

            //Copy current realm to backup file
            Realm myRealm = Realm.getDefaultInstance();
            myRealm.writeCopyTo(exportRealmFile);
            myRealm.close();

            String dateString = DateUtil.convertDateToStringFormat7(new Date());
            updateLastBackupInfo(dateString);
            BudgetPreference.setLastBackup(getContext(), dateString);

            Util.createSnackbar(getContext(), getView(), "Backup data successful.");

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void restore(){
        String restoreFilePath = DOWNLOAD_DIRECTORY + "/" + EXPORT_REALM_FILE_NAME;
        copyBundleRealmFile(restoreFilePath, IMPORT_REALM_FILE_NAME);
    }

    private String copyBundleRealmFile(String oldFilePath, String outFileName){
        if(new File(oldFilePath).exists()){
            try{
                File file = new File(getContext().getFilesDir(), outFileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                FileInputStream inputStream = new FileInputStream(new File(oldFilePath));

                byte[] buf = new byte[1024];
                int bytesRead;
                while((bytesRead = inputStream.read(buf)) > 0){
                    outputStream.write(buf, 0 , bytesRead);
                }
                outputStream.close();

                Util.createSnackbar(getContext(), getView(), "Restore data successful.");

                return file.getAbsolutePath();
            }catch(IOException e){
                e.printStackTrace();
            }
        }else{
            Util.createSnackbar(getContext(), getView(), "Restore data failed as backup file doesn't exist.");
        }

        return null;
    }

    private void updateLastBackupInfo(String value){
        backupContent.setText("last backup : "+value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Export CSV
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private enum sortCSV{
        DATE,
        CATEGORY,
        ACCOUNT,
        LOCATION
    }

    private List<String> sortListType;

    public void requestFilePermissionToWriteCSV(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            new AlertDialog.Builder(getContext())
                    .setTitle("Permission denied")
                    .setMessage("Without this permission the app is unable to create the CSV.")
                    .setPositiveButton("Re-try", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_WRITE_CSV);
                        }
                    })
                    .setNegativeButton("Deny", null)
                    .create()
                    .show();

        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_WRITE_CSV);
        }
    }

    public void exportCSVSort(){
        sortListType = new ArrayList<>();
        sortListType.add(sortCSV.DATE.toString());
        sortListType.add(sortCSV.CATEGORY.toString());
        sortListType.add(sortCSV.ACCOUNT.toString());
        sortListType.add(sortCSV.LOCATION.toString());

        View sortDialogView = View.inflate(getContext(), R.layout.alertdialog_number_picker, null);

        TextView title = (TextView)sortDialogView.findViewById(R.id.title);
        title.setText("Sort by");

        final ExtendedNumberPicker sortPicker = (ExtendedNumberPicker)sortDialogView.findViewById(R.id.numberPicker);

        sortPicker.setMinValue(0);
        sortPicker.setMaxValue(sortListType.size() - 1);
        sortPicker.setDisplayedValues(sortListType.toArray(new String[0]));
        sortPicker.setWrapSelectorWheel(false);
        sortPicker.setValue(0); //set default

        AlertDialog.Builder sortAlertDialogBuilder = new AlertDialog.Builder(getContext())
                .setView(sortDialogView)
                .setPositiveButton("SELECT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String selectedString = sortListType.get(sortPicker.getValue()).toString();
                        getTransactionListForCSV(selectedString);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Reset the selection back to previous
                        dialog.dismiss();
                    }
                });

        AlertDialog sortDialog = sortAlertDialogBuilder.create();
        sortDialog.show();
    }

    private void getTransactionListForCSV(final String sortType){
        final Realm myRealm = Realm.getDefaultInstance();
        transactionList = new ArrayList<>();
        transactionResults = myRealm.where(Transaction.class).findAllAsync();
        transactionResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);
                transactionList = myRealm.copyFromRealm(element);

                if(sortType.equalsIgnoreCase(sortCSV.DATE.toString())){
                    sortByDate();
                }else if(sortType.equalsIgnoreCase(sortCSV.CATEGORY.toString())){
                    sortByCategory();
                } else if(sortType.equalsIgnoreCase(sortCSV.LOCATION.toString())){
                    sortByLocation();
                }else if(sortType.equalsIgnoreCase(sortCSV.ACCOUNT.toString())){
                    sortByAccount();
                }

                myRealm.close();
            }
        });
    }

    private void sortByDate(){
        Collections.sort(transactionList, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                int t = t1.getDate().compareTo(t2.getDate());

                //If Date is the same, then compare by Category
                if(t == 0){
                    t = t1.getCategory().getName().compareToIgnoreCase(t2.getCategory().getName());
                }

                //If Category name is the same, then compare by price
                if(t == 0){
                    t = (int)t1.getPrice() - (int)t2.getPrice();
                }

                return t;
            }
        });

        exportCSV(sortCSV.DATE);
    }

    private void sortByLocation(){
        Collections.sort(transactionList, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                int t = 0;
                if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(t1.getLocation().getName()) &&
                        Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(t2.getLocation().getName())){
                    t = t1.getLocation().getName().compareToIgnoreCase(t2.getLocation().getName());
                }

                //If Location is the same or doesnt exist, then compare by Category
                if(t == 0){
                    t = t1.getCategory().getName().compareToIgnoreCase(t2.getCategory().getName());
                }

                //If Category name is the same, then compare by date
                if(t == 0){
                    t = t1.getDate().compareTo(t2.getDate());
                }

                //If date is the same, then compare by price
                if(t == 0){
                    t = (int)t1.getPrice() - (int)t2.getPrice();
                }

                return t;
            }
        });

        exportCSV(sortCSV.LOCATION);
    }

    private void sortByCategory(){
        Collections.sort(transactionList, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                int t = t1.getCategory().getName().compareToIgnoreCase(t2.getCategory().getName());

                //If Category name is the same, then compare by date
                if(t == 0){
                    t = t1.getDate().compareTo(t2.getDate());
                }

                //If date is the same, then compare by price
                if(t == 0){
                    t = (int)t1.getPrice() - (int)t2.getPrice();
                }

                return t;
            }
        });

        exportCSV(sortCSV.CATEGORY);
    }

    private void sortByAccount(){
        Collections.sort(transactionList, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                int t = t1.getAccount().getName().compareToIgnoreCase(t2.getAccount().getName());

                //If Account name is the same, then compare by Category
                if(t == 0){
                    t = t1.getCategory().getName().compareToIgnoreCase(t2.getCategory().getName());
                }

                //If Category name is the same, then compare by date
                if(t == 0){
                    t = t1.getDate().compareTo(t2.getDate());
                }

                //If date is the same, then compare by price
                if(t == 0){
                    t = (int)t1.getPrice() - (int)t2.getPrice();
                }

                return t;
            }
        });

        exportCSV(sortCSV.ACCOUNT);
    }

    private void exportCSV(final sortCSV sortType){
        String csvFileName = Constants.NAME + "_" + sortType.toString() + "_" + (DateUtil.convertDateToStringFormat6(new Date())) + Constants.CSV_END;

        final File csvFile = new File(DOWNLOAD_DIRECTORY, csvFileName);

        CSVFormatter csvFormatter = new CSVFormatter(getContext(), transactionList, csvFile);
        csvFormatter.setCSVInteraction(new CSVFormatter.OnCSVInteractionListener() {
            @Override
            public void onCompleteCSV(boolean value) {
                if(value){
                    email(csvFile, "CSV", "This CSV is sorted by "+sortType.toString() );
                }else{
                    Util.createSnackbar(getContext(), getView(), "CSV creation failed.");
                }
            }
        });
        csvFormatter.execute();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Reset Data
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void resetData(){
        View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText("Confirm Delete");
        message.setText("Resetting data will remove all data you've entered, are you sure you want to reset?");

        new AlertDialog.Builder(getActivity())
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Util.createSnackbar(getContext(), getView(), "Resetting...");

                        BudgetPreference.resetFirstTime(getContext());

                        RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                                .name(Constants.REALM_NAME)
                                .deleteRealmIfMigrationNeeded()
                                .schemaVersion(1)
                                .build();

                        Realm.deleteRealm(config);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Tutorial
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadTutorials(){
        Intent mainAct = new Intent(getContext(), MaterialTutorialActivity.class);
        mainAct.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS, getTourPages(getContext()));
        startActivityForResult(mainAct, 3218);
    }

    private ArrayList<TutorialItem> getTourPages(Context context){
        TutorialItem page1 = new TutorialItem(
                "Calendar",
                "View all transactions for a specific date.",
                R.color.colorPrimary,
                R.drawable.screen1);

        TutorialItem page2 = new TutorialItem(
                "Theme",
                "Change between light and dark mode.",
                R.color.colorPrimary,
                R.drawable.screen2);

        TutorialItem page3 = new TutorialItem(
                "Approve",
                "Approve, un-approve, or delete a transaction by swiping left.",
                R.color.colorPrimary,
                R.drawable.screen3);

        TutorialItem page4 = new TutorialItem(
                "Add new Transaction",
                "Create or edit a transaction.",
                R.color.colorPrimary,
                R.drawable.screen4);

        TutorialItem page5 = new TutorialItem(
                "Budget",
                "Compare your current spending with your budget.",
                R.color.colorPrimary,
                R.drawable.screen5);

        TutorialItem page6 = new TutorialItem(
                "Account",
                "View how much you spent on all accounts for a month.",
                R.color.colorPrimary,
                R.drawable.screen6);

        TutorialItem page7 = new TutorialItem(
                "Location",
                "View all locations that are in the transaction for a month",
                R.color.colorPrimary,
                R.drawable.screen7);

        TutorialItem page8 = new TutorialItem(
                "Monthly Overview",
                "View how much you spent and earn each month for the whole year.",
                R.color.colorPrimary,
                R.drawable.screen8);

        TutorialItem page9 = new TutorialItem(
                "Percentage",
                "View how much you spent on a category relative to other categories.",
                R.color.colorPrimary,
                R.drawable.screen9);

        TutorialItem page10 = new TutorialItem(
                "View all",
                "View all transactions for a specific Account, Category, Location during a month.",
                R.color.colorPrimary,
                R.drawable.screen10);

        ArrayList<TutorialItem> tourItems = new ArrayList<>();
        tourItems.add(page1);
        tourItems.add(page2);
        tourItems.add(page3);
        tourItems.add(page4);
        tourItems.add(page5);
        tourItems.add(page6);
        tourItems.add(page7);
        tourItems.add(page8);
        tourItems.add(page9);
        tourItems.add(page10);

        return tourItems;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Etc
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean createDirectory(){
        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Budget");

        //If file doesnt exist
        if(!directory.exists()){
            return directory.mkdirs();
        }else{
            return true;
        }
    }

    public void email(File file, String subject, String body) {
        // init email intent and add file as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        Uri u = Uri.fromFile(file);
        Log.d("REALM", " u : "+u.getPath());
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        startActivity(Intent.createChooser(intent, "Send email"));
    }

    public void email(){
        // init email intent and add file as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.target_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");

        // start email intent
        startActivity(Intent.createChooser(intent, "Send email"));
    }

}
