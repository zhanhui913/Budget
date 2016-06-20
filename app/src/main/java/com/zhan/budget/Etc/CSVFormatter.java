package com.zhan.budget.Etc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by Zhan on 16-06-20.
 */
public class CSVFormatter extends AsyncTask<Void, Integer,  Boolean> {

    private Context context;
    private List<Transaction> transactionList;
    private final ProgressDialog mDialog;
    private OnCSVInteractionListener mListener;

    //Delimiter used in CSV file
    final String COMMA_DELIMITER = ",";
    final String NEW_LINE_SEPARATOR = "\n";

    //CSV file header
    final String FILE_HEADER = "Type, Date, Note, Category, Price, Account, Location, Completed?";

    //File root = Environment.getExternalStorageDirectory();
    //final File csvFile = new File(root, Constants.CSV_NAME);

    private File csvFile;

    public CSVFormatter(Context context, List<Transaction> transactionList, File csvFile){
        this.context = context;
        this.transactionList = transactionList;
        this.csvFile = csvFile;

        mDialog = new ProgressDialog(context);
        mDialog.setMax(transactionList.size());
        mDialog.setMessage("CSVing....");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0); //start at 0
        mDialog.setCancelable(false);
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CSVFormatter.this.cancel(true);

                if(mListener != null){
                    mListener.onCompleteCSV(false);
                }
            }
        });
        mDialog.show();
    }

    public void setCSVInteraction(OnCSVInteractionListener mListener){
        this.mListener = mListener;
    }

    long startTime, endTime, duration;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("SETTINGS_FRAGMENT", "preparing to write " + transactionList.size() + " entries into csv");
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
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
                fileWriter.append(Util.checkNull(transactionList.get(i).getCategory().getType().toString()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(DateUtil.convertDateToStringFormat5(transactionList.get(i).getDate())));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(transactionList.get(i).getNote()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(transactionList.get(i).getCategory().getName()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(CurrencyTextFormatter.formatFloat(transactionList.get(i).getPrice(), Constants.BUDGET_LOCALE)));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(transactionList.get(i).getAccount().getName()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(transactionList.get(i).getLocation()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(transactionList.get(i).getDayType().toString()));
                fileWriter.append(NEW_LINE_SEPARATOR);

                publishProgress(i);
            }
            Log.d("SETTINGS_FRAGMENT", "CSV file was created successfully");
            return true;
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

        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... progress){
        mDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();

        if(mListener != null){
            mListener.onCompleteCSV(result);
        }

        endTime = System.nanoTime();
        duration = (endTime - startTime);
        long milli = (duration / 1000000);
        long second = (milli / 1000);
        float minutes = (second / 60.0f);
        Log.d("SETTINGS_FRAGMENT", "took " + milli + " milliseconds -> " + second + " seconds -> " + minutes + " minutes");
    }

    public interface OnCSVInteractionListener{
        void onCompleteCSV(boolean value);
    }
}
