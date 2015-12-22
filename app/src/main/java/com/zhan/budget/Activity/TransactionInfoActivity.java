package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

public class TransactionInfoActivity extends AppCompatActivity {

    private boolean isEditMode = false;
    private Activity instance;
    private Toolbar toolbar;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_info);

        instance = TransactionInfoActivity.this;

        //Get intents from caller activity
        isEditMode = (getIntent().getExtras()).getBoolean(Constants.REQUEST_NEW_TRANSACTION);

        init();
    }

    /**
     * Perform all initializations here.
     */
    private void init(){
        editText = (EditText) findViewById(R.id.editText);

        createToolbar();
        addListeners();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white);

        if(getSupportActionBar() != null){
            if(isEditMode){
                getSupportActionBar().setTitle("Edit Transaction");
            }else{
                getSupportActionBar().setTitle("Add Transaction");
            }
        }
    }

    private void addListeners(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmLeave();
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        confirmLeave();
    }

    /**
     * Bring dialog up when user wants to leave prematurely without saving.
     */
    private void confirmLeave(){
        String message = "message";
        String negative = "Discard changes";

        if(!isEditMode){
            message = "You sure you want to discard transaction";
            negative = "Discard";
        }

        new AlertDialog.Builder(instance)
                .setTitle("Cancel")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Keep Editing", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close the dialog box and do nothing
                        dialog.cancel();
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close current activity
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * Clears focus from all edit text and hides soft keyboard
     */
    private void clearAllFocus(){
        //name.clearFocus();
        //question.clearFocus();
        //answer.clearFocus();
       // description.clearFocus();
        Util.hideSoftKeyboard(instance);
    }

    private void save(){
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Menu
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.formSaveBtn) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
