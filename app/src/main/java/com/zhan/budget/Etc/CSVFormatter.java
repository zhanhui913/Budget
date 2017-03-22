package com.zhan.budget.Etc;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
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
    private RoundCornerProgressBar mDialog;
    private AlertDialog alertDialog;
    private TextView percentTextView, progressTextView;

    private OnCSVInteractionListener mListener;

    //Delimiter used in CSV file
    final String COMMA_DELIMITER = ",";
    final String NEW_LINE_SEPARATOR = "\n";

    //CSV file header
    final String FILE_HEADER = "Type, Date, Note, Category, Price, Account, Location, Completed?";

    private File csvFile;


    public CSVFormatter(Context context, List<Transaction> transactionList, File csvFile){
        this.context = context;
        this.transactionList = transactionList;
        this.csvFile = csvFile;

        //Option 1
        View promptView = View.inflate(context, R.layout.alertdialog_progressbar, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        title.setText(R.string.dialog_title_csv);

        mDialog = (RoundCornerProgressBar) promptView.findViewById(R.id.progressBar);
        mDialog.setMax(transactionList.size());
        mDialog.setProgressColor(ContextCompat.getColor(context, R.color.colorPrimary));

        percentTextView = (TextView)promptView.findViewById(R.id.percentTextView);
        progressTextView = (TextView)promptView.findViewById(R.id.progressTextView);

        percentTextView.setText(String.format(context.getString(R.string.dialog_progress_percent), 0));
        progressTextView.setText(String.format(context.getString(R.string.dialog_progress_total), 0, transactionList.size()));

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(promptView)
                .setNegativeButton(context.getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        CSVFormatter.this.cancel(true);

                        if(mListener != null){
                            mListener.onCompleteCSV(false);
                        }
                    }
                });

        alertDialog = builder.create();
        alertDialog.show();
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
                if(transactionList.get(i).getCategory() != null){
                    fileWriter.append(Util.checkNull(transactionList.get(i).getCategory().getType()));
                }
                fileWriter.append(COMMA_DELIMITER);
                if(transactionList.get(i).getDate() != null){
                    fileWriter.append(Util.checkNull(DateUtil.convertDateToStringFormat5(context, transactionList.get(i).getDate())));
                }
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(transactionList.get(i).getNote()));
                fileWriter.append(COMMA_DELIMITER);
                if(transactionList.get(i).getCategory() != null){
                    fileWriter.append(Util.checkNull(transactionList.get(i).getCategory().getName()));
                }
                fileWriter.append(COMMA_DELIMITER);

                //Need to remove commas from $1,000 for example
                fileWriter.append(Util.checkNull(CurrencyTextFormatter.formatFloat(transactionList.get(i).getPrice())).replace(",",""));
                fileWriter.append(COMMA_DELIMITER);
                if(transactionList.get(i).getAccount() != null){
                    fileWriter.append(Util.checkNull(transactionList.get(i).getAccount().getName()));
                }
                fileWriter.append(COMMA_DELIMITER);
                if(transactionList.get(i).getLocation() != null){
                    fileWriter.append(Util.checkNull(transactionList.get(i).getLocation().getName()));
                }
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(Util.checkNull(transactionList.get(i).getDayType()));
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
        percentTextView.setText(String.format(context.getString(R.string.dialog_progress_percent), Math.round((progress[0] / (float)transactionList.size()) * 100)));
        progressTextView.setText(String.format(context.getString(R.string.dialog_progress_total), progress[0], transactionList.size()));
    }

    @Override
    protected void onPostExecute(Boolean result) {
        alertDialog.dismiss();

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
