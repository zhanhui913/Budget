package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Adapter.TransactionViewPager;
import com.zhan.budget.Database.Database;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.TransactionExpenseFragment;
import com.zhan.budget.Fragment.TransactionIncomeFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;
import com.zhan.circleindicator.CircleIndicator;

import java.util.Date;

public class TransactionInfoActivity extends AppCompatActivity implements
        TransactionExpenseFragment.OnTransactionExpenseFragmentInteractionListener,
        TransactionIncomeFragment.OnTransactionIncomeFragmentInteractionListener{

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

    private TransactionViewPager adapterViewPager;
    private ViewPager viewPager;

    private Category selectedExpenseCategory;
    private Category selectedIncomeCategory;

    private CircleIndicator circleIndicator;
    private BudgetType currentPage; //Determines if the current page is in expense or income page

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

        //default first page
        currentPage = BudgetType.EXPENSE;

        viewPager = (ViewPager) findViewById(R.id.transactionViewPager);
        adapterViewPager = new TransactionViewPager(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        circleIndicator = (CircleIndicator) findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

        priceString = priceStringWithDot = "";

        //Call one time to give priceStringWithDot the correct string format of 0.00
        removeDigit();

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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        currentPage = BudgetType.EXPENSE;
                        transactionCostView.setText("-$" + priceStringWithDot);
                        break;
                    case 1:
                        currentPage = BudgetType.INCOME;
                        transactionCostView.setText("+$" + priceStringWithDot);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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

    private void addDigitToTextView(int digit){
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

        String appendString = (currentPage == BudgetType.EXPENSE)?"-$":"+$";
        transactionCostView.setText(appendString + priceStringWithDot);
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
        priceStringWithDot = cashAmountBuilder.toString();

        String appendString = (currentPage == BudgetType.EXPENSE)?"-$":"+$";
        transactionCostView.setText(appendString + priceStringWithDot);
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

    private void save(){
        Intent intent = new Intent();

        Transaction transaction = new Transaction();
        transaction.setNote(this.noteString);
        transaction.setPrice(Float.parseFloat(priceStringWithDot));
        transaction.setDate(Util.formatDate(selectedDate));

        if(currentPage == BudgetType.EXPENSE) {
            transaction.setCategory(selectedExpenseCategory);
        }else{
            transaction.setCategory(selectedIncomeCategory);
        }

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Fragment Listener
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCategoryExpenseClick(Category category){
        Toast.makeText(getApplicationContext(), "clicked on expense category : "+category.getName(), Toast.LENGTH_SHORT).show();
        selectedExpenseCategory = category;
    }

    @Override
    public void onCategoryIncomeClick(Category category){
        Toast.makeText(getApplicationContext(), "clicked on income category : "+category.getName(), Toast.LENGTH_SHORT).show();
        selectedIncomeCategory = category;
    }

}
