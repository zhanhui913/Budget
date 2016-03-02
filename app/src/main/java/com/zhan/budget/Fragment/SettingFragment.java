package com.zhan.budget.Fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {

    private View view;
    private TextView backupBtn, resetBtn, exportCSVBtn, emailBtn, tourBtn, faqBtn;

    //CSV
    private List<Transaction> transactionList;
    private RealmResults<Transaction> transactionResults;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        addListeners();
    }

    private void init(){
        backupBtn = (TextView) view.findViewById(R.id.backupBtn);
        resetBtn = (TextView) view.findViewById(R.id.resetDataBtn);
        exportCSVBtn = (TextView) view.findViewById(R.id.exportCSVBtn);
        emailBtn = (TextView) view.findViewById(R.id.emailBtn);
        tourBtn = (TextView) view.findViewById(R.id.tourBtn);
        faqBtn = (TextView) view.findViewById(R.id.faqBtn);
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
                transactionList = myRealm.copyFromRealm(transactionResults);


                exportCSV();

                transactionResults.removeChangeListener(this);
                myRealm.close();
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
    // Etc
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
