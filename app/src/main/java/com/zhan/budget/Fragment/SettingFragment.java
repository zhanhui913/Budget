package com.zhan.budget.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.processor.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends BaseFragment {

    private static final String TAG = "SettingFragment";

    private TextView backupBtn, resetBtn, exportCSVBtn, emailBtn, tourBtn, faqBtn;

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
        super.init();

        backupBtn = (TextView) view.findViewById(R.id.backupBtn);
        resetBtn = (TextView) view.findViewById(R.id.resetDataBtn);
        exportCSVBtn = (TextView) view.findViewById(R.id.exportCSVBtn);
        emailBtn = (TextView) view.findViewById(R.id.emailBtn);
        tourBtn = (TextView) view.findViewById(R.id.tourBtn);
        faqBtn = (TextView) view.findViewById(R.id.faqBtn);

        addListeners();
    }

    private void addListeners(){
        backupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "backup", Toast.LENGTH_SHORT).show();
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
                Util.changeToTheme(getActivity(), Util.THEME_BLUE);
            }
        });

        faqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "faq", Toast.LENGTH_SHORT).show();
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Export CSV
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void getTransactionListForCSV(){
        transactionList = new ArrayList<>();
        transactionResults = myRealm.where(Transaction.class).findAllSortedAsync("date", Sort.ASCENDING);
        transactionResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                transactionList = myRealm.copyFromRealm(transactionResults);

                exportCSV();

                transactionResults.removeChangeListener(this);
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
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        //set Constants.FIRST_TIME shared preferences to true to reset it
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.FIRST_TIME, true);
        editor.apply();

        RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                .name(Constants.REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(1)
                .build();

        Realm.deleteRealm(config);
        Toast.makeText(getContext(), "Reset data", Toast.LENGTH_SHORT).show();

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
