package com.zhan.budget.Fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Etc.Constants;
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
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends BaseFragment {

    private static final String TAG = "SettingFragment";

    private ViewGroup themeBtn, firstDayBtn, backupBtn;
    private TextView themeContent, firstDayContent, backupContent;

    private TextView  resetBtn, exportCSVBtn, emailBtn, tourBtn, faqBtn;

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

        backupBtn = (ViewGroup) view.findViewById(R.id.backupBtn);
        backupContent = (TextView) view.findViewById(R.id.backupContent);

        resetBtn = (TextView) view.findViewById(R.id.resetDataBtn);
        exportCSVBtn = (TextView) view.findViewById(R.id.exportCSVBtn);
        emailBtn = (TextView) view.findViewById(R.id.emailBtn);
        tourBtn = (TextView) view.findViewById(R.id.tourBtn);
        faqBtn = (TextView) view.findViewById(R.id.faqBtn);

        CURRENT_THEME = BudgetPreference.getCurrentTheme(getContext());
        themeContent.setText((CURRENT_THEME == ThemeUtil.THEME_DARK ? "Night Mode" : "Day Mode"));

        int startDay = BudgetPreference.getStartDay(getContext());
        firstDayContent.setText(startDay == Calendar.SUNDAY ? "Sunday" : "Monday");

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
        final Realm myRealm = Realm.getDefaultInstance();
        transactionList = new ArrayList<>();
        transactionResults = myRealm.where(Transaction.class).findAllSortedAsync("date", Sort.ASCENDING);
        transactionResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                transactionResults.removeChangeListener(this);
                transactionList = myRealm.copyFromRealm(transactionResults);
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
