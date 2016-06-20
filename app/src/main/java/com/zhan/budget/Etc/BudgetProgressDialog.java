package com.zhan.budget.Etc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhan.budget.R;

/**
 * Created by Zhan on 16-06-20.
 */
public class BudgetProgressDialog extends ProgressDialog {

    private ProgressBar progressBar;
    private TextView title;

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
        setContentView(R.layout.budget_progress_dialog);

        title = (TextView) findViewById(R.id.genericTitle);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    public void setTitle(String value){
        title.setText(value);
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
