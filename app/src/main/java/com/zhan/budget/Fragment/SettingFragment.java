package com.zhan.budget.Fragment;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.zhan.budget.Activity.SelectCurrencyActivity;
import com.zhan.budget.Activity.Settings.AboutActivity;
import com.zhan.budget.Activity.Settings.SettingsAccount;
import com.zhan.budget.Activity.Settings.SettingsCategory;
import com.zhan.budget.Activity.Settings.SettingsLocation;
import com.zhan.budget.BuildConfig;
import com.zhan.budget.Etc.CSVFormatter;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.DataBackup;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.ThemeUtil;
import com.zhan.budget.Util.Tutorial;
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
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends BaseFragment {

    private static final String TAG = "SettingFragment";

    private ViewGroup themeBtn, firstDayBtn, categoryOrderBtn, defaultAccountBtn, locationBtn, currencyBtn, restoreBackupBtn, resetBtn, exportCSVBtn, aboutBtn, ratingsBtn, emailBtn, tutorialBtn, faqBtn;
    private TextView themeContent, firstDayContent, backupContent, versionNumber;
    private Switch autoBackupSwitch;

    private BudgetCurrency currentCurrency;

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
    protected void init(){
        themeBtn = (ViewGroup) view.findViewById(R.id.themeBtn);
        themeContent = (TextView) view.findViewById(R.id.themeContent);

        firstDayBtn = (ViewGroup) view.findViewById(R.id.firstDayBtn);
        firstDayContent = (TextView) view.findViewById(R.id.firstDayContent);

        categoryOrderBtn = (ViewGroup) view.findViewById(R.id.categoryOrderBtn);

        defaultAccountBtn = (ViewGroup) view.findViewById(R.id.defaultAccountBtn);

        locationBtn = (ViewGroup) view.findViewById(R.id.locationBtn);

        currencyBtn = (ViewGroup) view.findViewById(R.id.currencyBtn);

        autoBackupSwitch = (Switch) view.findViewById(R.id.autoBackupSwitch);
        backupContent = (TextView) view.findViewById(R.id.backupContent);

        restoreBackupBtn = (ViewGroup)view.findViewById(R.id.restoreBackupBtn);

        resetBtn = (ViewGroup) view.findViewById(R.id.resetDataBtn);

        exportCSVBtn = (ViewGroup) view.findViewById(R.id.exportCSVBtn);

        ratingsBtn = (ViewGroup) view.findViewById(R.id.ratingBtn);

        emailBtn = (ViewGroup) view.findViewById(R.id.emailBtn);

        tutorialBtn = (ViewGroup) view.findViewById(R.id.tutorialBtn);

        faqBtn = (ViewGroup) view.findViewById(R.id.faqBtn);

        aboutBtn = (ViewGroup) view.findViewById(R.id.aboutBtn);

        versionNumber = (TextView) view.findViewById(R.id.appVersionTextId);

        ///////////////////////////////////
        //
        // Setting up
        //
        ///////////////////////////////////

        //Set theme
        CURRENT_THEME = BudgetPreference.getCurrentTheme(getContext());
        themeContent.setText(CURRENT_THEME == ThemeUtil.THEME_DARK ? R.string.setting_content_theme_night : R.string.setting_content_theme_day);

        //Set start day
        int startDay = BudgetPreference.getStartDay(getContext());
        firstDayContent.setText(startDay == Calendar.SUNDAY ? R.string.setting_content_day_sun : R.string.setting_content_day_mon);

        //Set auto backup
        boolean allowAutoBackup = BudgetPreference.getAllowAutoBackup(getContext());
        autoBackupSwitch.setChecked(allowAutoBackup);
        updateLastBackupInfo(BudgetPreference.getLastBackup(getContext()));

        //set version number
        versionNumber.setText(String.format(getString(R.string.version), BuildConfig.VERSION_NAME));

        addListeners();
        getDefaultCurrency();
    }

    private void addListeners(){
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                themeContent.setText((CURRENT_THEME == ThemeUtil.THEME_DARK ? R.string.setting_content_theme_night : R.string.setting_content_theme_day));
                ThemeUtil.changeToTheme(getActivity(), (CURRENT_THEME == ThemeUtil.THEME_DARK ? ThemeUtil.THEME_LIGHT : ThemeUtil.THEME_DARK));
            }
        });

        firstDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int startDay = BudgetPreference.getStartDay(getContext());

                if(startDay == Calendar.SUNDAY){
                    firstDayContent.setText(R.string.setting_content_day_mon);
                    BudgetPreference.setMondayStartDay(getContext());
                }else{
                    firstDayContent.setText(R.string.setting_content_day_sun);
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

        currencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(SelectCurrencyActivity.createIntentToSetDefaultCurrency(getContext()));
            }
        });

        autoBackupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BudgetPreference.setAllowAutoBackup(getContext(), isChecked);

                if(isChecked){
                    checkPermissionToAutoBackup();
                }
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

        ratingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rate();
            }
        });

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email();
            }
        });

        tutorialBtn.setOnClickListener(new View.OnClickListener() {
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

        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about = new Intent(getContext(), AboutActivity.class);
                startActivity(about);
            }
        });

        versionNumber.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                checkPermissionToAccessRealm();
                return false;
            }
        });
    }

    private void getDefaultCurrency(){
        final Realm myRealm = Realm.getDefaultInstance();

        currentCurrency = myRealm.where(BudgetCurrency.class).equalTo("isDefault", true).findFirst();
        if(currentCurrency == null){
            currentCurrency = new BudgetCurrency();
            currentCurrency.setCurrencyCode(SelectCurrencyActivity.DEFAULT_CURRENCY_CODE);
            currentCurrency.setCurrencyName(SelectCurrencyActivity.DEFAULT_CURRENCY_NAME);
        }else {
            currentCurrency = myRealm.copyFromRealm(currentCurrency);
        }

        myRealm.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Backup Data
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private File DOWNLOAD_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private final String EXPORT_REALM_FILE_NAME = "Backup_Budget.realm";
    private final String IMPORT_REALM_FILE_NAME = Constants.REALM_NAME;


    private void checkPermissionToAutoBackup(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //STORAGE permission has not been granted
            requestFilePermissionToWriteAutoBackup();
        }else{
            backUpData();
        }
    }

    public void requestFilePermissionToWriteAutoBackup(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

            TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
            TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

            title.setText(R.string.permission_denied);
            message.setText(R.string.permission_rationale_write_backup_data);

            new AlertDialog.Builder(getActivity())
                    .setView(promptView)
                    .setPositiveButton(R.string.permission_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestCodes.MY_PERMISSIONS_REQUEST_WRITE_AUTO_EXTERNAL_STORAGE);
                        }
                    })
                    .setNegativeButton(R.string.permission_deny, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            turnOffAutoUpdateSwitch();
                        }
                    })
                    .create()
                    .show();
        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestCodes.MY_PERMISSIONS_REQUEST_WRITE_AUTO_EXTERNAL_STORAGE);
        }
    }

    public void turnOffAutoUpdateSwitch(){
        if(autoBackupSwitch != null){
            autoBackupSwitch.setChecked(false);
        }
    }

    public void turnOnAutoUpdateSwitch(){
        if(autoBackupSwitch != null){
            autoBackupSwitch.setChecked(true);
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

    public void requestFilePermissionToRead(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

            TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
            TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

            title.setText(R.string.permission_denied);
            message.setText(R.string.permission_rationale_read_backup_data);

            new AlertDialog.Builder(getActivity())
                    .setView(promptView)
                    .setPositiveButton(R.string.permission_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RequestCodes.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    })
                    .setNegativeButton(R.string.permission_deny, null)
                    .create()
                    .show();

        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RequestCodes.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    public void backUpData(){
        if(DataBackup.backUpData(getContext())){
            String dateString = DateUtil.convertDateToStringFormat7(getContext(), new Date());
            updateLastBackupInfo(dateString);
            BudgetPreference.setLastBackup(getContext(), dateString);

            Util.createSnackbar(getContext(), getView(), getString(R.string.setting_content_backup_data_successful));
        }else{
            Util.createSnackbar(getContext(), getView(), getString(R.string.setting_content_backup_data_failed));
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

                Util.createSnackbar(getContext(), getView(), getString(R.string.setting_content_restore_data_successful));

                return file.getAbsolutePath();
            }catch(IOException e){
                e.printStackTrace();
            }
        }else{
            Util.createSnackbar(getContext(), getView(), getString(R.string.setting_content_restore_data_failed));
        }

        return null;
    }

    private void updateLastBackupInfo(String value){
        backupContent.setText(String.format(getString(R.string.setting_content_backup_data), value));
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

            View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

            TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
            TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

            title.setText(R.string.permission_denied);
            message.setText(R.string.permission_rationale_write_csv);

            new AlertDialog.Builder(getActivity())
                    .setView(promptView)
                    .setPositiveButton(R.string.permission_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestCodes.MY_PERMISSIONS_REQUEST_WRITE_CSV);
                        }
                    })
                    .setNegativeButton(R.string.permission_deny, null)
                    .create()
                    .show();

        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestCodes.MY_PERMISSIONS_REQUEST_WRITE_CSV);
        }
    }

    public void exportCSVSort(){
        sortListType = new ArrayList<>();
        sortListType.add(sortCSV.DATE.toString());
        sortListType.add(sortCSV.CATEGORY.toString());
        sortListType.add(sortCSV.ACCOUNT.toString());
        sortListType.add(sortCSV.LOCATION.toString());

        View sortDialogView = View.inflate(getContext(), R.layout.alertdialog_number_picker, null);

        TextView title = (TextView)sortDialogView.findViewById(R.id.alertdialogTitle);
        title.setText(R.string.setting_content_export_data_sort);

        final ExtendedNumberPicker sortPicker = (ExtendedNumberPicker)sortDialogView.findViewById(R.id.numberPicker);

        sortPicker.setMinValue(0);
        sortPicker.setMaxValue(sortListType.size() - 1);
        sortPicker.setDisplayedValues(sortListType.toArray(new String[0]));
        sortPicker.setWrapSelectorWheel(false);
        sortPicker.setValue(0); //set default

        AlertDialog.Builder sortAlertDialogBuilder = new AlertDialog.Builder(getContext())
                .setView(sortDialogView)
                .setPositiveButton(R.string.dialog_button_select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String selectedString = sortListType.get(sortPicker.getValue());
                        getTransactionListForCSV(selectedString);
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
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
        String csvFileName = Constants.NAME + "_" + sortType.toString() + "_" + (DateUtil.convertDateToStringFormat6(getContext(), new Date())) + Constants.CSV_END;

        final File csvFile = new File(DOWNLOAD_DIRECTORY, csvFileName);

        CSVFormatter csvFormatter = new CSVFormatter(getContext(), transactionList, currentCurrency, csvFile);
        csvFormatter.setCSVInteraction(new CSVFormatter.OnCSVInteractionListener() {
            @Override
            public void onCompleteCSV(boolean value) {
                if(value){
                    email(csvFile, getString(R.string.csv), String.format(getString(R.string.csv_success), sortType.toString()));
                }else{
                    Util.createSnackbar(getContext(), getView(), getString(R.string.csv_failed));
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

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(R.string.reset);
        message.setText(R.string.reset_data_message);

        new AlertDialog.Builder(getActivity())
                .setView(promptView)
                .setPositiveButton(R.string.dialog_button_reset, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Util.createSnackbar(getContext(), getView(), getString(R.string.resetting));

                        BudgetPreference.resetFirstTime(getContext());
                        BudgetPreference.resetFirstTimeCurrency(getContext());

                        RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                                .name(Constants.REALM_NAME)
                                .deleteRealmIfMigrationNeeded()
                                .schemaVersion(1)
                                .build();

                        Realm.deleteRealm(config);
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
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
    // Rate
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static String APP_STORE = "market://details?id=com.zhan.budget";

    private void rate(){
        try {
            Intent rate = new Intent(Intent.ACTION_VIEW);
            rate.setData(Uri.parse(APP_STORE));
            startActivity(rate);
        }catch(Exception e){
            Util.createSnackbar(getContext(), view, getString(R.string.ratings_failed));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Tutorial
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadTutorials(){
        Intent mainAct = new Intent(getContext(), MaterialTutorialActivity.class);
        mainAct.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS, Tutorial.getTutorialPages(getContext()));
        startActivity(mainAct);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Send Realm data
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkPermissionToAccessRealm(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //STORAGE permission has not been granted
            requestFilePermissionToAccess();
        }else{
            sendRealmData();
        }
    }

    public void requestFilePermissionToAccess(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            View promptView = View.inflate(getContext(), R.layout.alertdialog_generic_message, null);

            TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
            TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

            title.setText(R.string.permission_denied);
            message.setText(R.string.permission_rationale_access_data);

            new AlertDialog.Builder(getActivity())
                    .setView(promptView)
                    .setPositiveButton(R.string.permission_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RequestCodes.MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE);
                        }
                    })
                    .setNegativeButton(R.string.permission_deny, null)
                    .create()
                    .show();

        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RequestCodes.MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE);
        }
    }

    public void sendRealmData() {
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
                    email(exportRealmFile, getString(R.string.realm_data), getString(R.string.realm_message));
                }else{
                    Log.d("FILE","cannot write file");
                    Util.createSnackbar(getContext(), getView(), getString(R.string.realm_message_failed));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

    public void email(){
        // init email intent and add file as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.target_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_title));

        // start email intent
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Services
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


}
