package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.p_v.flexiblecalendar.view.BaseCellView;
import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.TransactionFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Location;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.ScheduledTransaction;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;
import com.zhan.budget.View.ExtendedNumberPicker;
import com.zhan.budget.View.RectangleCellView;
import com.zhan.circleindicator.CircleIndicator;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class TransactionInfoActivity extends BaseActivity implements
        TransactionFragment.OnTransactionFragmentInteractionListener{

    private boolean isNewTransaction = false;
    private Activity instance;
    private Toolbar toolbar;
    private Button button1,button2,button3,button4,button5,button6,button7,button8,button9,button0;
    private ImageButton buttonX;

    private ImageButton addNoteBtn, addAccountBtn, dateBtn, repeatBtn, locationBtn;

    private TextView transactionCostView;

    private String priceString, noteString, locationString;
    private Location location;

    private TransactionFragment transactionExpenseFragment, transactionIncomeFragment;

    private Date selectedDate;
    private Date tempDate;

    private TwoPageViewPager adapterViewPager;
    private ViewPager viewPager;

    private Category selectedExpenseCategory;
    private Category selectedIncomeCategory;

    private CircleIndicator circleIndicator;

    private Account selectedAccount;
    private int selectedAccountIndexInSpinner;

    private BudgetType currentPage; //Determines if the current page is in expense or income page

    private List<String> accountNameList;
    private RealmResults<Account> resultsAccount;

    //Alert dialog for account
    private AlertDialog accountDialog;

    //Alert dialog for date
    private AlertDialog dateDialog;
    private TextView monthTextView;

    private Transaction editTransaction;
    private Boolean isScheduledTransaction = false; //default is false
    private ScheduledTransaction scheduledTransaction;

    private HashSet<String> locationHash = new HashSet<>();

    //had to put this as  global because putting it as final would sometimes not allow me to put the location hash into its adapter
    private AutoCompleteTextView inputLocation;

    //Switch whenever theres a change in location
    private boolean newLocation = false;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_transaction_info;
    }

    @Override
    protected void init(){
        instance = TransactionInfoActivity.this;

        //Get intents from caller activity
        isNewTransaction = (getIntent().getExtras()).getBoolean(Constants.REQUEST_NEW_TRANSACTION);
        selectedDate = DateUtil.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_NEW_TRANSACTION_DATE));

        if(!isNewTransaction){
            editTransaction = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_EDIT_TRANSACTION));
        }

        if(!isNewTransaction) {
            transactionIncomeFragment = TransactionFragment.newInstance(BudgetType.INCOME.toString(), editTransaction.getCategory().getId());
            transactionExpenseFragment = TransactionFragment.newInstance(BudgetType.EXPENSE.toString(), editTransaction.getCategory().getId());
        }else{
            transactionIncomeFragment = TransactionFragment.newInstance(BudgetType.INCOME.toString());
            transactionExpenseFragment = TransactionFragment.newInstance(BudgetType.EXPENSE.toString());
        }

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
        buttonX = (ImageButton)findViewById(R.id.numberX);

        addNoteBtn = (ImageButton)findViewById(R.id.addNoteBtn);
        addAccountBtn = (ImageButton)findViewById(R.id.addAccountBtn);
        dateBtn = (ImageButton)findViewById(R.id.dateBtn);
        repeatBtn = (ImageButton)findViewById(R.id.repeatBtn);
        locationBtn = (ImageButton)findViewById(R.id.addLocationBtn);

        transactionCostView = (TextView)findViewById(R.id.transactionCostText);

        //default first page
        currentPage = BudgetType.EXPENSE;

        viewPager = (ViewPager) findViewById(R.id.transactionViewPager);
        adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), transactionExpenseFragment, transactionIncomeFragment);
        viewPager.setAdapter(adapterViewPager);

        circleIndicator = (CircleIndicator) findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

        priceString = "0";

        //Call one time to give priceStringWithDot the correct string format of 0.00
        removeDigit();

        //If its edit mode
        if(!isNewTransaction){

            if(editTransaction.getNote() != null){
                Log.d("DEBUG","@@@@@"+editTransaction.getNote());
                noteString = editTransaction.getNote();
            }

            if(editTransaction.getLocation() != null){
                locationString = editTransaction.getLocation().getName();
            }

            //Check which category this transaction belongs to.
            //If its EXPENSE category, change page to EXPENSE view pager
            //If its INCOME category, change page to INCOME view pager
            if(editTransaction.getCategory().getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                viewPager.setCurrentItem(0);
                currentPage = BudgetType.EXPENSE;
            }else if(editTransaction.getCategory().getType().equalsIgnoreCase(BudgetType.INCOME.toString())){
                viewPager.setCurrentItem(1);
                currentPage = BudgetType.INCOME;
            }

            priceString = CurrencyTextFormatter.formatFloat(editTransaction.getPrice(), Constants.BUDGET_LOCALE);

            //Remove any extra un-needed signs
            priceString = CurrencyTextFormatter.stripCharacters(priceString);

            Log.d("DEBUG", "---------->" + priceString);
            String appendString = (currentPage == BudgetType.EXPENSE) ? "-" : "";

            transactionCostView.setText(CurrencyTextFormatter.formatText(appendString+priceString, Constants.BUDGET_LOCALE));

            Log.d("DEBUG", "price string is " + priceString + ", ->" + editTransaction.getPrice());
        }

        getAllLocations();
        createToolbar();
        addListeners();
        createAccountDialog();
        createDateDialog();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_clear);

        if(getSupportActionBar() != null){
            if(!isNewTransaction){
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

        addAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountDialog.show();
            }
        });

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog.show();
            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRepeatDialog();
            }
        });

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLocationDialog();
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
                        //float ss = CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE);
                        //transactionCostView.setText("" + CurrencyTextFormatter.formatFloat(ss, Constants.BUDGET_LOCALE));
                        transactionCostView.setText(CurrencyTextFormatter.formatText("-"+priceString, Constants.BUDGET_LOCALE));
                        break;
                    case 1:
                        currentPage = BudgetType.INCOME;
                        //float ss1 = CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE);
                        //transactionCostView.setText("" + CurrencyTextFormatter.formatFloat(Math.abs(ss1), Constants.BUDGET_LOCALE));
                        transactionCostView.setText(CurrencyTextFormatter.formatText(priceString, Constants.BUDGET_LOCALE));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void createDateDialog(){
        View dateDialogView = View.inflate(instance, R.layout.alertdialog_date, null);

        monthTextView = (TextView) dateDialogView.findViewById(R.id.alertdialogMonthTextView);
        final FlexibleCalendarView calendarView = (FlexibleCalendarView) dateDialogView.findViewById(R.id.alertdialogCalendarView);

        int year = DateUtil.getYearFromDate(selectedDate);
        int month = DateUtil.getMonthFromDate(selectedDate);
        int date = DateUtil.getDateFromDate(selectedDate);

        tempDate = selectedDate;

        monthTextView.setText(DateUtil.convertDateToStringFormat2(new GregorianCalendar(year, month, date).getTime()));

        calendarView.setCalendarView(new FlexibleCalendarView.CalendarView() {
            @Override
            public BaseCellView getCellView(int position, View convertView, ViewGroup parent, @BaseCellView.CellType int cellType) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    cellView = (BaseCellView) View.inflate(instance, R.layout.calendar_date_cell_view, null);
                }

                if (cellType == BaseCellView.TODAY) {
                    cellView.setTextColor(ContextCompat.getColor(instance, R.color.colorPrimary));
                } else if (cellType == BaseCellView.SELECTED_TODAY) {
                    cellView.setTextColor(Colors.getColorFromAttr(instance, R.attr.themeColor));
                }

                return cellView;
            }

            @Override
            public BaseCellView getWeekdayCellView(int position, View convertView, ViewGroup parent) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    cellView = (RectangleCellView) View.inflate(instance, R.layout.calendar_week_cell_view, null);
                }
                return cellView;
            }

            @Override
            public String getDayOfWeekDisplayValue(int dayOfWeek, String defaultValue) {
                return String.valueOf(defaultValue.toUpperCase());
            }
        });

        calendarView.setStartDayOfTheWeek(BudgetPreference.getStartDay(this));

        calendarView.selectDate(tempDate);

        calendarView.setOnMonthChangeListener(new FlexibleCalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month, int direction) {
                monthTextView.setText(DateUtil.convertDateToStringFormat2(new GregorianCalendar(year, month, 1).getTime()));
            }
        });

        calendarView.setOnDateClickListener(new FlexibleCalendarView.OnDateClickListener() {
            @Override
            public void onDateClick(int year, int month, int day) {
                tempDate = new GregorianCalendar(year, month, day).getTime();
            }
        });

        //When the calendar view is done being drawn, move the display to the selectedDate
        calendarView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                monthTextView.setText(DateUtil.convertDateToStringFormat2(selectedDate));
                calendarView.selectDate(selectedDate);
                calendarView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        AlertDialog.Builder dateAlertDialogBuilder = new AlertDialog.Builder(instance)
                .setView(dateDialogView)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        selectedDate = tempDate;
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tempDate = selectedDate;
                        dialog.dismiss();
                        calendarView.selectDate(selectedDate);
                    }
                });

        dateDialog = dateAlertDialogBuilder.create();
    }

    private void createAccountDialog(){
        View accountDialogView = View.inflate(instance, R.layout.alertdialog_number_picker, null);

        final ExtendedNumberPicker accountPicker = (ExtendedNumberPicker)accountDialogView.findViewById(R.id.numberPicker);

        TextView title = (TextView)accountDialogView.findViewById(R.id.title);
        title.setText("Select Account");

        accountNameList = new ArrayList<>();

        final Realm myRealm = Realm.getDefaultInstance(); BudgetPreference.addRealmCache(this);

        //Get list of accounts
        resultsAccount = myRealm.where(Account.class).findAllSortedAsync("isDefault", Sort.DESCENDING);
        resultsAccount.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                element.removeChangeListener(this);
                Log.d("REALMZ1", "getAllAccounts closing  realm");


                for (int i = 0; i < element.size(); i++) {
                    Log.d("ZHAP", i+"->"+element.get(i).getName());
                    accountNameList.add(element.get(i).getName());
                }

                //Swap the default account to be in index 0 (for nameList and realmList)
                //Collections.swap(accountNameList, 0, defaultAccountIndex);
                //Collections.swap(resultsAccount, 0 , defaultAccountIndex);

                accountPicker.setMinValue(0);

                if (accountNameList.size() > 0) {
                    accountPicker.setMaxValue(accountNameList.size() - 1);
                }
                accountPicker.setDisplayedValues(accountNameList.toArray(new String[0]));
                accountPicker.setWrapSelectorWheel(false);

                int pos = 0; //default is first item to be selected in the spinner
                if (!isNewTransaction) {
                    for (int i = 0; i < element.size(); i++) {
                        if (editTransaction.getAccount() != null) {
                            if (editTransaction.getAccount().getId().equalsIgnoreCase(element.get(i).getId())) {
                                pos = i;
                                break;
                            }
                        }
                    }
                }

                selectedAccountIndexInSpinner = pos;
                selectedAccount = myRealm.copyFromRealm(element.get(pos));

                accountPicker.setValue(pos);

                myRealm.close(); BudgetPreference.removeRealmCache(getBaseContext());
            }
        });

        AlertDialog.Builder accountAlertDialogBuilder = new AlertDialog.Builder(instance)
                .setView(accountDialogView)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        selectedAccountIndexInSpinner = accountPicker.getValue();
                        selectedAccount = resultsAccount.get(selectedAccountIndexInSpinner);
                        //Toast.makeText(getApplicationContext(), "Selected account is "+selectedAccount.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Reset the selection back to previous
                        accountPicker.setValue(selectedAccountIndexInSpinner);
                        dialog.dismiss();
                    }
                });

        accountDialog = accountAlertDialogBuilder.create();
    }

    private void createNoteDialog(){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic, null);

        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        title.setText("Add Note");
        input.setHint("Note");
        input.setText(noteString);

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        noteString = input.getText().toString();
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
    }

    private void getAllLocations(){
        final Realm myRealm = Realm.getDefaultInstance(); BudgetPreference.addRealmCache(this);

        //Toast.makeText(getBaseContext(), "Trying to get all locations", Toast.LENGTH_SHORT).show();

        RealmResults<Location> locationRealmResults = myRealm.where(Location.class).findAllAsync();
        locationRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Location>>() {
            @Override
            public void onChange(RealmResults<Location> element) {
                element.removeChangeListener(this);

                for(int i = 0; i < element.size(); i++){
                    locationHash.add(element.get(i).getName());
                }
                Toast.makeText(getBaseContext(), "There are "+locationHash.size()+" unique locations on init", Toast.LENGTH_SHORT).show();

                myRealm.close(); BudgetPreference.removeRealmCache(getBaseContext());
            }
        });
    }

    private void createLocationDialog(){
        //real one
        String[] locationArray = locationHash.toArray(new String[locationHash.size()]);

        View promptView = View.inflate(instance, R.layout.alertdialog_generic_autocomplete, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        title.setText("Add Location");

        inputLocation = (AutoCompleteTextView) promptView.findViewById(R.id.genericAutoCompleteEditText);
        inputLocation.setHint("Location");
        inputLocation.setText(locationString);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locationArray);
        inputLocation.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        locationString = inputLocation.getText().toString();

                        if(editTransaction.getLocation() != null){
                            if(!inputLocation.getText().toString().equalsIgnoreCase(editTransaction.getLocation().getName())){
                                newLocation = true;
                            }
                        }else{
                            newLocation = true;
                        }
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
    }

    private void createRepeatDialog(){
        View promptView = View.inflate(instance, R.layout.alertdialog_repeat, null);

        final ExtendedNumberPicker quantityNumberPicker = (ExtendedNumberPicker)promptView.findViewById(R.id.quantityNumberPicker);
        quantityNumberPicker.setMaxValue(50);
        quantityNumberPicker.setMinValue(0);
        quantityNumberPicker.setWrapSelectorWheel(true);

        final ExtendedNumberPicker repeatNumberPicker = (ExtendedNumberPicker)promptView.findViewById(R.id.repeatNumberPicker);

        //Initializing a new string array with elements
        final String[] values= {"days", "weeks", "months"};

        //Populate NumberPicker values from String array values
        //Set the minimum value of NumberPicker
        repeatNumberPicker.setMinValue(0); //from array first value

        //Specify the maximum value/number of NumberPicker
        repeatNumberPicker.setMaxValue(values.length - 1); //to array last value

        //Specify the NumberPicker data source as array elements
        repeatNumberPicker.setDisplayedValues(values);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        repeatNumberPicker.setWrapSelectorWheel(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (quantityNumberPicker.getValue() == 0) {
                            isScheduledTransaction = false;
                        } else {
                            isScheduledTransaction = true;

                            scheduledTransaction = new ScheduledTransaction();
                            scheduledTransaction.setId(Util.generateUUID());
                            scheduledTransaction.setRepeatUnit(quantityNumberPicker.getValue());
                            scheduledTransaction.setRepeatType(values[repeatNumberPicker.getValue()]);
                            //Set the scheduledTransaction's transaction property in save function
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog noteDialog = builder.create();
        noteDialog.show();
    }

    private void addDigitToTextView(int digit){
        priceString += digit;

        String appendString = (currentPage == BudgetType.EXPENSE) ? "-" : "";
        //transactionCostView.setText(""+CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE));

        transactionCostView.setText(CurrencyTextFormatter.formatText(appendString+priceString, Constants.BUDGET_LOCALE));
    }

    private void removeDigit(){
        if (priceString != null && priceString.length() >= 1) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }

        String appendString = (currentPage == BudgetType.EXPENSE) ? "-" : "";
        //transactionCostView.setText(""+CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE));

        transactionCostView.setText(CurrencyTextFormatter.formatText(appendString+priceString, Constants.BUDGET_LOCALE));
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void save(){
        Intent intent = new Intent();

        Transaction transaction = new Transaction();

        if(!isNewTransaction){
            transaction.setId(editTransaction.getId());
            transaction.setDayType(editTransaction.getDayType());
        }else{
            transaction.setId(Util.generateUUID());

            if(!selectedDate.before(new Date())){
                transaction.setDayType(DayType.COMPLETED.toString());
            }else{
                transaction.setDayType(DayType.SCHEDULED.toString());
            }
        }

        if(newLocation){
            if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(locationString)){
                Location newLocationObject = new Location();
                newLocationObject.setId(Util.generateUUID());
                newLocationObject.setName(locationString);
                newLocationObject.setColor(Colors.getRandomColorString(getBaseContext()));
                transaction.setLocation(newLocationObject);
            }else{
                transaction.setLocation(null);
            }
        }else{
            transaction.setLocation(editTransaction.getLocation());
        }

        transaction.setNote(this.noteString);
        transaction.setDate(DateUtil.formatDate(selectedDate));
        transaction.setAccount(selectedAccount);

        if(currentPage == BudgetType.EXPENSE){
            transaction.setPrice(-CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE));
            transaction.setCategory(selectedExpenseCategory);
        }else{
            transaction.setPrice(CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE));
            transaction.setCategory(selectedIncomeCategory);
        }

        ScheduledTransaction sT = new ScheduledTransaction();
        if(isScheduledTransaction){
            Log.d("isScheduledTransaction", "adding scheduled transaction");
            scheduledTransaction.setTransaction(transaction);

            sT.setId(Util.generateUUID());
            sT.setRepeatUnit(scheduledTransaction.getRepeatUnit());
            sT.setRepeatType(scheduledTransaction.getRepeatType());
            //setting transaction in the schedule transaction in the caller fragment
        }else{
            Log.d("isScheduledTransaction", "not adding scheduled transaction");
        }
        Parcelable scheduledTransactionWrapped = Parcels.wrap(sT);

        Log.d("DEBUG", "===========> ("+CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE)+") , string = "+priceString);

        Parcelable wrapped = Parcels.wrap(transaction);

        if(!isNewTransaction){
            intent.putExtra(Constants.RESULT_EDIT_TRANSACTION, wrapped);
        }else{
            intent.putExtra(Constants.RESULT_NEW_TRANSACTION, wrapped);
        }

        if(isScheduledTransaction) {
            intent.putExtra(Constants.RESULT_SCHEDULE_TRANSACTION, scheduledTransactionWrapped);
        }

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
        selectedExpenseCategory = category;
        Log.d("TRAN", "selected expense category is "+selectedExpenseCategory.getName());
    }

    @Override
    public void onCategoryIncomeClick(Category category){
        selectedIncomeCategory = category;
        Log.d("TRAN", "selected income category is "+selectedIncomeCategory.getName());
    }

}
