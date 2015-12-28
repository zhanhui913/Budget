package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

public class TransactionInfoActivity extends AppCompatActivity {

    private boolean isEditMode = false;
    private Activity instance;
    private Toolbar toolbar;
    private Button button1,button2,button3,button4,button5,button6,button7,button8,button9,buttonDot,button0,buttonX;
    private TextView transactionCostView;
    private String priceString;


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
        button1 = (Button)findViewById(R.id.number1);
        button2 = (Button)findViewById(R.id.number2);
        button3 = (Button)findViewById(R.id.number3);
        button4 = (Button)findViewById(R.id.number4);
        button5 = (Button)findViewById(R.id.number5);
        button6 = (Button)findViewById(R.id.number6);
        button7 = (Button)findViewById(R.id.number7);
        button8 = (Button)findViewById(R.id.number8);
        button9 = (Button)findViewById(R.id.number9);
        buttonDot = (Button)findViewById(R.id.numberDot);
        button0 = (Button)findViewById(R.id.number0);
        buttonX = (Button)findViewById(R.id.numberX);

        transactionCostView = (TextView)findViewById(R.id.transactionCostText);

        priceString = "";

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
                finish();
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(1);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(2);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(3);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(4);
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(5);
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(6);
            }
        });

        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(7);
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(8);
            }
        });

        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(9);
            }
        });

        buttonDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(0);
            }
        });

        buttonX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDigit();
            }
        });

        //transactionCostView.addTextChangedListener(tw);


    }

    TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().matches("^\\$(\\d{1,3}(\\,\\d{3})*|(\\d+))(\\.\\d{2})?$")) {
                String userInput = "" + s.toString().replaceAll("[^\\d]", "");
                StringBuilder cashAmountBuilder = new StringBuilder(userInput);

                while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                    cashAmountBuilder.deleteCharAt(0);
                }
                while (cashAmountBuilder.length() < 3) {
                    cashAmountBuilder.insert(0, '0');
                }
                cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');

                transactionCostView.removeTextChangedListener(this);
                transactionCostView.setText(cashAmountBuilder.toString());

                transactionCostView.setTextKeepState("$" + cashAmountBuilder.toString());
                Selection.setSelection(transactionCostView.getEditableText(), cashAmountBuilder.toString().length() + 1);

                transactionCostView.addTextChangedListener(this);
            }
        }
    };

    private void addDigitToTextView(int digit){
        //transactionCostView.setText(transactionCostView.getText() + "" +digit);
        priceString += digit;
        StringBuilder cashAmountBuilder = new StringBuilder(priceString);

        while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
            cashAmountBuilder.deleteCharAt(0);
        }
        while (cashAmountBuilder.length() < 3) {
            cashAmountBuilder.insert(0, '0');
        }

        cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');
        transactionCostView.setText("$"+cashAmountBuilder.toString());

    }

    private void addDot(){

    }

    private void removeDigit(){
        if (priceString != null && priceString.length() >= 1) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }

        StringBuilder cashAmountBuilder = new StringBuilder(priceString);

        while (cashAmountBuilder.length() < 3) {
            cashAmountBuilder.insert(0, '0');
        }

        cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');
        transactionCostView.setText("$"+cashAmountBuilder.toString());

    }

    @Override
    public void onBackPressed() {
        finish();
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
