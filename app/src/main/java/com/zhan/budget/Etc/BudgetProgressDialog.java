package com.zhan.budget.Etc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.zhan.budget.R;

/**
 * Created by Zhan on 16-06-20.
 */
public class BudgetProgressDialog extends ProgressDialog {

    private RoundCornerProgressBar progressBar;
    private TextView titleTextView;

    public static ProgressDialog ctor(Context context) {
        BudgetProgressDialog dialog = new BudgetProgressDialog(context);
        //dialog.setIndeterminate(true);
        //dialog.setCancelable(false);
        return dialog;
    }

    public BudgetProgressDialog(Context context){
        super(context);
    }

    public BudgetProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertdialog_progressbar);

        titleTextView = (TextView) findViewById(R.id.genericTitle);
        progressBar = (RoundCornerProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    }

    @Override
    public void setTitle(CharSequence title){
        titleTextView.setText(""+title);
    }

    @Override
    public void setProgress(int value){
        progressBar.setProgress(value);
    }

    @Override
    public void setMax(int max){
        progressBar.setMax(max);
    }

    @Override
    public void show(){
        super.show();
    }

    @Override
    public void dismiss(){
        super.dismiss();
    }
}
