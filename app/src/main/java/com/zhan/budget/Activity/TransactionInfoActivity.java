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
    private ActionMode actionMode;
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
        /*editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (actionMode == null) {
                    actionMode = toolbar.startActionMode(callback);
                }
            }
        });*/
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
    // Action Mode
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private ActionMode.Callback callback = new ActionMode.Callback() {
        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.action_mode_done, menu);

            actionMode = mode;

            return true;
        }

        // Called when the user click share item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.formSaveBtn:
                    // Action picked, so close the Contextual Action Bar(CAB)
                    mode.finish();

                    return true;
                default:
                    return false;
            }
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            clearAllFocus();
            actionMode = null;
        }
    };

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
