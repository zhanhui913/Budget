package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhan.budget.Adapter.CategoryGridAdapter;
import com.zhan.budget.Database.Database;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.circularview.CircularView;

import java.util.ArrayList;
import java.util.Date;

public class TransactionInfoActivity extends AppCompatActivity {

    private boolean isEditMode = false;
    private Activity instance;
    private Toolbar toolbar;
    private Button button1,button2,button3,button4,button5,button6,button7,button8,button9,button0,buttonX;
    private ImageView addNoteBtn;
    private TextView transactionCostView;

    private String priceString, priceStringWithDot;
    private String noteString;

    private Database db; //shouldnt have db access here, category and transactions should be dealt with in the caller activity
    private Date selectedDate;

    private ArrayList<Category> categoryList;
    private GridView categoryGridView;
    private CategoryGridAdapter categoryGridAdapter;

    private Category selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_info);

        instance = TransactionInfoActivity.this;

        //Get intents from caller activity
        isEditMode = (getIntent().getExtras()).getBoolean(Constants.REQUEST_NEW_TRANSACTION);
        selectedDate = Util.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_NEW_TRANSACTION_DATE));

        init();
    }

    /**
     * Perform all initializations here.
     */
    private void init(){
        openDatabase();

        button1 = (Button)findViewById(R.id.number1);
        button2 = (Button)findViewById(R.id.number2);
        button3 = (Button)findViewById(R.id.number3);
        button4 = (Button)findViewById(R.id.number4);
        button5 = (Button)findViewById(R.id.number5);
        button6 = (Button)findViewById(R.id.number6);
        button7 = (Button)findViewById(R.id.number7);
        button8 = (Button)findViewById(R.id.number8);
        button9 = (Button)findViewById(R.id.number9);
        button0 = (Button)findViewById(R.id.number0);
        buttonX = (Button)findViewById(R.id.numberX);
        addNoteBtn = (ImageView)findViewById(R.id.addNoteBtn);

        transactionCostView = (TextView)findViewById(R.id.transactionCostText);

        categoryList = new ArrayList<>();
        categoryGridView = (GridView) findViewById(R.id.categoryGrid);
        categoryGridAdapter = new CategoryGridAdapter(this, categoryList);
        categoryGridView.setAdapter(categoryGridAdapter);

        priceString = priceStringWithDot = "";

        createToolbar();
        addListeners();
        populateCategoryExpense();
    }

    private void populateCategoryExpense(){
        AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("ASYNC", "preparing to get categories");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                categoryList = db.getAllCategoryByType(BudgetType.EXPENSE);
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);
                Log.d("ASYNC", "done getting categories");
                categoryGridAdapter.addAll(categoryList);

                //categoryGridAdapter.getView(0);

            }
        };
        loader.execute();
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

        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNoteDialog();
            }
        });

        //transactionCostView.addTextChangedListener(tw);

        categoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i < parent.getChildCount(); i++){
                    View childView = parent.getChildAt(i);
                    CircularView ccv = (CircularView)(childView.findViewById(R.id.categoryIcon));
                    ccv.setStrokeColor(getResources().getColor(android.R.color.transparent));
                }

                View childView = parent.getChildAt(position);
                CircularView ccv = (CircularView)(childView.findViewById(R.id.categoryIcon));
                ccv.setStrokeColor(getResources().getColor(R.color.darkgray));

                selectedCategory = categoryList.get(position);
            }
        });
    }

    private void createNoteDialog(){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(instance);

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.alertdialog_note_transaction, null);

        final EditText input = (EditText) promptView.findViewById(R.id.editTextNote);

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addNote(input.getText().toString());
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog noteDialog = builder.create();
        noteDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        noteDialog.show();

        input.requestFocus();
    }

    private void addNote(String note){
        this.noteString = note;
    }

/*
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
    };*/

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
        priceStringWithDot = cashAmountBuilder.toString();
        transactionCostView.setText("$" + cashAmountBuilder.toString());
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

        Transaction transaction = new Transaction();
        transaction.setNote(this.noteString);
        transaction.setPrice(Float.parseFloat(priceStringWithDot));
        transaction.setDate(Util.formatDate(selectedDate));
        transaction.setCategory(selectedCategory);

        intent.putExtra(Constants.RESULT_NEW_TRANSACTION, transaction);
        setResult(RESULT_OK, intent);

        finish();
    }

    public void openDatabase(){
        db = new Database(getApplicationContext());
    }

    public void closeDatabase(){
        db.close();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        closeDatabase();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Menu
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save, menu);
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
