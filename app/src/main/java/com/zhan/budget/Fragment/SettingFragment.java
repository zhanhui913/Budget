package com.zhan.budget.Fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Activity.OpenSourceActivity;
import com.zhan.budget.Activity.SettingsAccount;
import com.zhan.budget.Activity.SettingsCategory;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.ThemeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends BaseFragment {

    private static final String TAG = "SettingFragment";

    private ViewGroup themeBtn, firstDayBtn, categoryOrderBtn, defaultAccountBtn, backupBtn;
    private TextView themeContent, firstDayContent, backupContent;

    private TextView  resetBtn, exportCSVBtn, emailBtn, tourBtn, faqBtn, openLicenseBtn;

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

        backupBtn = (ViewGroup) view.findViewById(R.id.backupBtn);
        backupContent = (TextView) view.findViewById(R.id.backupContent);

        resetBtn = (TextView) view.findViewById(R.id.resetDataBtn);
        exportCSVBtn = (TextView) view.findViewById(R.id.exportCSVBtn);
        emailBtn = (TextView) view.findViewById(R.id.emailBtn);
        tourBtn = (TextView) view.findViewById(R.id.tourBtn);
        faqBtn = (TextView) view.findViewById(R.id.faqBtn);
        openLicenseBtn = (TextView) view.findViewById(R.id.openSourceBtn);

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

        addListeners();
    }

    private void addListeners(){
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "them button click", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "change category info", Toast.LENGTH_SHORT).show();

                Intent settingsCategory = new Intent(getContext(), SettingsCategory.class);
                startActivity(settingsCategory);
            }
        });

        defaultAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getAccountList();

                Toast.makeText(getContext(), "change account info", Toast.LENGTH_SHORT).show();

                Intent settingsAccount = new Intent(getContext(), SettingsAccount.class);
                startActivity(settingsAccount);
            }
        });

        backupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "reset", Toast.LENGTH_SHORT).show();
                resetData();
            }
        });

        exportCSVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "csv", Toast.LENGTH_SHORT).show();
                getTransactionListForCSV();
            }
        });

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "email", Toast.LENGTH_SHORT).show();
                sendRealmData();
            }
        });

        tourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "tour", Toast.LENGTH_SHORT).show();
                loadTutorials();
            }
        });

        faqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "faq", Toast.LENGTH_SHORT).show();
            }
        });

        openLicenseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "open source", Toast.LENGTH_SHORT).show();
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
                    email(exportRealmFile);
                }else{
                    Log.d("FILE","cannot write file");
                    Toast.makeText(getContext(), "Fail to retrieve file.", Toast.LENGTH_SHORT).show();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Backup Data
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkPermission(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //STORAGE permission has not been granted
            requestFilePermission();
        }else{
            Toast.makeText(getContext(), "backup successful", Toast.LENGTH_SHORT).show();
            backUpData();
        }
    }

    public void requestFilePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            new AlertDialog.Builder(getContext())
                    .setTitle("Permission denied")
                    //.setMessage("You need to allow access to storage in order to create a backup of the database.")
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
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void backUpData(){
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            Date date = new Date();

            if (sd.canWrite()) {
                if(createDirectory()){ Log.d("FILE", "can write file");
                    String currentDBPath = "//data//" + "com.zhan.budget" + "//files//" + Constants.REALM_NAME;
                    String backupDBPath = "Budget/" + Constants.NAME + "_" + DateUtil.convertDateToStringFormat6(date) + ".realm";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    String dateString = DateUtil.convertDateToStringFormat7(date);

                    Log.d("FILE", "backupFile : "+Environment.getExternalStorageDirectory().toString() + backupDBPath+" -> "+dateString);

                    updateLastBackupInfo(dateString);
                    BudgetPreference.setLastBackup(getContext(), dateString);
                }else{
                    Log.d("FILE","cannot write file");
                    Toast.makeText(getContext(), "Fail to backup data at "+DateUtil.convertDateToStringFormat7(date), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLastBackupInfo(String value){
        backupContent.setText("last backup : "+value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Export CSV
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void getTransactionListForCSV(){
        final Realm myRealm = Realm.getDefaultInstance();
        transactionList = new ArrayList<>();
        transactionResults = myRealm.where(Transaction.class).findAllSortedAsync("date", Sort.ASCENDING);
        transactionResults.addChangeListener(new RealmChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> element) {
                element.removeChangeListener(this);
                transactionList = myRealm.copyFromRealm(element);
                myRealm.close();
                exportCSV();
            }
        });
    }

    private void exportCSV(){
        //Delimiter used in CSV file
        final String COMMA_DELIMITER = ",";
        final String NEW_LINE_SEPARATOR = "\n";

        //CSV file header
        final String FILE_HEADER = "Type, Date, Note, Category, Price, Account";

        File root = Environment.getExternalStorageDirectory();
        final File csvFile = new File(root, Constants.CSV_NAME);

        final AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

            long startTime, endTime, duration;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("SETTINGS_FRAGMENT", "preparing to write " + transactionList.size() + " entries into csv");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                startTime = System.nanoTime();

                FileWriter fileWriter = null;

                try{
                    fileWriter = new FileWriter(csvFile);

                    //Write the CSV file header
                    fileWriter.append(FILE_HEADER);

                    //Add a new line separator after the header
                    fileWriter.append(NEW_LINE_SEPARATOR);

                    //Write a new transaction object to the csv file
                    for(int i = 0; i < transactionList.size(); i++){
                        fileWriter.append(transactionList.get(i).getCategory().getType().toString());
                        fileWriter.append(COMMA_DELIMITER);
                        fileWriter.append(DateUtil.convertDateToStringFormat5(transactionList.get(i).getDate()));
                        fileWriter.append(COMMA_DELIMITER);
                        fileWriter.append(transactionList.get(i).getNote());
                        fileWriter.append(COMMA_DELIMITER);
                        fileWriter.append(transactionList.get(i).getCategory().getName());
                        fileWriter.append(COMMA_DELIMITER);
                        fileWriter.append(""+transactionList.get(i).getPrice());
                        fileWriter.append(COMMA_DELIMITER);
                        fileWriter.append(transactionList.get(i).getAccount().getName());
                        fileWriter.append(NEW_LINE_SEPARATOR);
                    }
                    Log.d("SETTINGS_FRAGMENT", "CSV file was created successfully");
                }catch(Exception e) {
                    e.printStackTrace();
                }finally{
                    try{
                        fileWriter.flush();
                        fileWriter.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);

                email(csvFile);

                endTime = System.nanoTime();
                duration = (endTime - startTime);
                long milli = (duration / 1000000);
                long second = (milli / 1000);
                float minutes = (second / 60.0f);
                Log.d("SETTINGS_FRAGMENT", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
            }
        };
        loader.execute();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Reset Data
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void resetData(){
        // get alertdialog_generic_message.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText("Confirm Delete");
        message.setText("Resetting data will remove all data you've entered, are you sure you want to reset?");

        new AlertDialog.Builder(getActivity())
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getContext(), "RESETTING...", Toast.LENGTH_SHORT).show();

                        BudgetPreference.resetFirstTime(getContext());

                        RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                                .name(Constants.REALM_NAME)
                                .deleteRealmIfMigrationNeeded()
                                .schemaVersion(1)
                                .build();


                        //Manually delete realm file
                        Realm myRealm = Realm.getDefaultInstance();
                        String path = myRealm.getPath();
                        myRealm.close();
                        File file = new File(path);
                        file.delete();

                        //Realm.deleteRealm(config);
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
                "Title",
                "Subtitle",
                R.color.colorPrimary,
                R.drawable.c_airplane,
                R.drawable.background);

        TutorialItem page2 = new TutorialItem(
                "Title 2",
                "Subtitle 2",
                R.color.midnight_blue,
                R.drawable.c_android,
                R.drawable.background);



        ArrayList<TutorialItem> tourItems = new ArrayList<>();
        tourItems.add(page1);
        tourItems.add(page2);


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

    public void email(File file) {
        // init email intent and add file as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, "YOUR MAIL");
        intent.putExtra(Intent.EXTRA_SUBJECT, "YOUR SUBJECT");
        intent.putExtra(Intent.EXTRA_TEXT, "YOUR TEXT");
        Uri u = Uri.fromFile(file);
        Log.d("REALM", " u : "+u.getPath());
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        startActivity(Intent.createChooser(intent, "YOUR CHOOSER TITLE"));
    }

}
