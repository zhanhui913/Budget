package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import com.zhan.budget.Etc.RequestCodes;
import com.zhan.budget.Fragment.TransactionFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.BudgetCurrency;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.Model.Realm.ScheduledTransaction;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Model.RepeatType;
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

    private static final String TAG = "TransactionInfoActivity";

    public static final String NEW_TRANSACTION = "New Transaction";

    public static final String EDIT_TRANSACTION_ITEM = "Edit Transaction Item";

    public static final String TRANSACTION_DATE = "Transaction Date";

    public static final String RESULT_TRANSACTION = "Result Transaction";

    public static final String HAS_CHANGED = "Has Changed";

    private boolean isNewTransaction = false;
    private Activity instance;
    private Toolbar toolbar;
    private Button button1,button2,button3,button4,button5,button6,button7,button8,button9,button0;
    private ImageButton buttonX;

    private ImageButton addNoteBtn, addAccountBtn, dateBtn, repeatBtn, locationBtn, changeCurrencyBtn;

    private TextView transactionCostView, transactionNameTextView, transactionCostCurrencyCodeText, currentPageTextView;

    private String priceString, noteString, locationString;

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

    private BudgetCurrency defaultCurrency;
    private BudgetCurrency currentCurrency;

    private HashSet<String> locationHash = new HashSet<>();

    //had to put this as  global because putting it as final would sometimes not allow me to put the location hash into its adapter
    private AutoCompleteTextView inputLocation;

    //Switch whenever theres a change in location
    private boolean newLocation = false;

    public static Intent createIntentForNewTransaction(Context context, Date date) {
        Intent intent = new Intent(context, TransactionInfoActivity.class);
        intent.putExtra(NEW_TRANSACTION, true);
        intent.putExtra(TRANSACTION_DATE, date);
        return intent;
    }

    public static Intent createIntentToEditTransaction(Context context, Transaction transaction) {
        Intent intent = new Intent(context, TransactionInfoActivity.class);
        intent.putExtra(NEW_TRANSACTION, false);

        Parcelable wrapped = Parcels.wrap(transaction);
        intent.putExtra(EDIT_TRANSACTION_ITEM, wrapped);

        return intent;
    }

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_transaction_info;
    }

    @Override
    protected void init(){
        instance = TransactionInfoActivity.this;

        //Get intents from caller activity
        isNewTransaction = (getIntent().getExtras()).getBoolean(NEW_TRANSACTION);

        if(!isNewTransaction) {
            editTransaction = Parcels.unwrap((getIntent().getExtras()).getParcelable(EDIT_TRANSACTION_ITEM));
            selectedDate = DateUtil.refreshDate(editTransaction.getDate());

            if(editTransaction.getCategory() != null){
                transactionIncomeFragment = TransactionFragment.newInstance(BudgetType.INCOME.toString(), editTransaction.getCategory().getId());
                transactionExpenseFragment = TransactionFragment.newInstance(BudgetType.EXPENSE.toString(), editTransaction.getCategory().getId());
            }else{
                transactionIncomeFragment = TransactionFragment.newInstance(BudgetType.INCOME.toString());
                transactionExpenseFragment = TransactionFragment.newInstance(BudgetType.EXPENSE.toString());
            }
        }else{
            selectedDate = DateUtil.refreshDate((Date)(getIntent().getSerializableExtra(TRANSACTION_DATE)));

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
        changeCurrencyBtn = (ImageButton)findViewById(R.id.changeCurrencyBtn);

        transactionCostView = (TextView)findViewById(R.id.transactionCostText);
        transactionNameTextView = (TextView)findViewById(R.id.transactionNameText);
        currentPageTextView = (TextView)findViewById(R.id.currentPageTitle);
        transactionCostCurrencyCodeText = (TextView)findViewById(R.id.transactionCostCurrencyCodeText);

        //default first page
        currentPage = BudgetType.EXPENSE;
        if(currentPage == BudgetType.EXPENSE){
            currentPageTextView.setText(R.string.category_expense);
        }else{
            currentPageTextView.setText(R.string.category_income);
        }

        viewPager = (ViewPager) findViewById(R.id.transactionViewPager);
        adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), transactionExpenseFragment, transactionIncomeFragment);
        viewPager.setAdapter(adapterViewPager);

        circleIndicator = (CircleIndicator) findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

        //If its edit mode
        if(!isNewTransaction){

            if(editTransaction.getNote() != null){
                Log.d("DEBUG","@@@@@"+editTransaction.getNote());
                noteString = editTransaction.getNote();
                transactionNameTextView.setText(noteString);
            }else{
                if(editTransaction.getCategory() != null){
                    transactionNameTextView.setText(editTransaction.getCategory().getName());
                }else{
                    transactionNameTextView.setText("");
                }
            }

            if(editTransaction.getLocation() != null){
                locationString = editTransaction.getLocation().getName();
            }

            //Check which category this transaction belongs to.
            //If its EXPENSE category, change page to EXPENSE view pager
            //If its INCOME category, change page to INCOME view pager
            if(editTransaction.getCategory() != null){
                if(editTransaction.getCategory().getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
                    viewPager.setCurrentItem(0);
                    currentPage = BudgetType.EXPENSE;
                }else if(editTransaction.getCategory().getType().equalsIgnoreCase(BudgetType.INCOME.toString())){
                    viewPager.setCurrentItem(1);
                    currentPage = BudgetType.INCOME;
                }
            }else{
                currentPage = BudgetType.EXPENSE;
            }

            getDefaultCurrency();
            currentCurrency = editTransaction.getCurrency();
            
            if(currentPage == BudgetType.EXPENSE){
                currentPageTextView.setText(R.string.category_expense);
            }else{
                currentPageTextView.setText(R.string.category_income);
            }

            priceString = CurrencyTextFormatter.formatFloat(editTransaction.getPrice(), currentCurrency);

            //Remove any extra un-needed signs
            priceString = CurrencyTextFormatter.stripCharacters(priceString, currentCurrency);

            Log.d("DEBUG", "---------->" + priceString);
            String appendString = (currentPage == BudgetType.EXPENSE) ? "-" : "";

            transactionCostView.setText(CurrencyTextFormatter.formatText(appendString+priceString, currentCurrency));

            updateConversion();

            Log.d("DEBUG", "price string is " + priceString + ", ->" + editTransaction.getPrice());
        }else{
            getDefaultCurrency();

            currentCurrency = defaultCurrency;

            priceString = "0";

            //Call one time to give priceStringWithDot the correct string format of 0.00
            removeDigit();
        }


        getAllLocations();
        createToolbar();
        addListeners();
        //createAccountDialog();
        checkAccountCount();
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
                getSupportActionBar().setTitle(getString(R.string.edit_transaction));
            }else{
                getSupportActionBar().setTitle(getString(R.string.add_transaction));
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

        changeCurrencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(SelectCurrencyActivity.createIntentToGetCurrency(getApplicationContext()), RequestCodes.SELECTED_CURRENCY);
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
                        transactionCostView.setText(CurrencyTextFormatter.formatText("-"+priceString, currentCurrency));

                        //If note is empty
                        if(!Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(noteString) && selectedExpenseCategory != null){
                            transactionNameTextView.setText(selectedExpenseCategory.getName());
                        }

                        currentPageTextView.setText(R.string.category_expense);

                        updateConversion();

                        break;
                    case 1:
                        currentPage = BudgetType.INCOME;
                        transactionCostView.setText(CurrencyTextFormatter.formatText(priceString, currentCurrency));

                        //If note is empty
                        if(!Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(noteString) && selectedIncomeCategory != null){
                            transactionNameTextView.setText(selectedIncomeCategory.getName());
                        }

                        currentPageTextView.setText(R.string.category_income);

                        updateConversion();

                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        transactionNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNoteDialog();
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

        monthTextView.setText(DateUtil.convertDateToStringFormat2(getApplicationContext(), new GregorianCalendar(year, month, date).getTime()));

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
                //return String.valueOf(defaultValue.toUpperCase());
                return DateUtil.getDayOfWeek(dayOfWeek).toUpperCase();
            }
        });

        calendarView.setStartDayOfTheWeek(BudgetPreference.getStartDay(this));

        calendarView.selectDate(tempDate);

        calendarView.setOnMonthChangeListener(new FlexibleCalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month, int direction) {
                monthTextView.setText(DateUtil.convertDateToStringFormat2(getApplicationContext(), new GregorianCalendar(year, month, 1).getTime()));
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
                monthTextView.setText(DateUtil.convertDateToStringFormat2(getApplicationContext(), selectedDate));
                calendarView.selectDate(selectedDate);
                calendarView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        AlertDialog.Builder dateAlertDialogBuilder = new AlertDialog.Builder(instance)
                .setView(dateDialogView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        selectedDate = tempDate;
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tempDate = selectedDate;
                        dialog.dismiss();
                        calendarView.selectDate(selectedDate);
                    }
                });

        dateDialog = dateAlertDialogBuilder.create();
    }

    private void checkAccountCount(){
        final Realm myRealm = Realm.getDefaultInstance(); BudgetPreference.addRealmCache(this);

        //Get list of accounts
        resultsAccount = myRealm.where(Account.class).findAllSortedAsync("isDefault", Sort.DESCENDING);
        resultsAccount.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> element) {
                element.removeChangeListener(this);
                Log.d("REALMZ1", "getAllAccounts closing  realm");

                createAccountDialog(myRealm.copyFromRealm(element));
            }
        });
    }

    private void createAccountDialog(List<Account> tempAccountList){
        AlertDialog.Builder accountAlertDialogBuilder;

        if(tempAccountList.size() > 0){
            View accountDialogView = View.inflate(instance, R.layout.alertdialog_number_picker, null);

            final ExtendedNumberPicker accountPicker = (ExtendedNumberPicker)accountDialogView.findViewById(R.id.numberPicker);

            TextView title = (TextView)accountDialogView.findViewById(R.id.title);
            title.setText(getString(R.string.account));

            accountNameList = new ArrayList<>();

            for (int i = 0; i < tempAccountList.size(); i++) {
                Log.d("ZHAP", i+"->"+tempAccountList.get(i).getName());
                accountNameList.add(tempAccountList.get(i).getName());
            }

            accountPicker.setMinValue(0);
            accountPicker.setMaxValue(accountNameList.size() - 1);
            accountPicker.setDisplayedValues(accountNameList.toArray(new String[0]));

            accountPicker.setWrapSelectorWheel(false);

            boolean doesTransactionHaveAccount = false;

            int pos = 0; //default is first item to be selected in the spinner
            if (!isNewTransaction) {
                for (int i = 0; i < tempAccountList.size(); i++) {
                    if (editTransaction.getAccount() != null) {
                        doesTransactionHaveAccount = true;
                        if (editTransaction.getAccount().getId().equalsIgnoreCase(tempAccountList.get(i).getId())) {
                            pos = i;
                            break;
                        }
                    }
                }
            }

            selectedAccountIndexInSpinner = pos;

            //if there is a default account
            boolean isThereDefaultAccount = false;

            for(int i = 0; i < tempAccountList.size(); i++){
                if(resultsAccount.get(i).isDefault()){
                    isThereDefaultAccount = true;
                    break;
                }
            }

            if(isThereDefaultAccount || doesTransactionHaveAccount){
                selectedAccount = tempAccountList.get(selectedAccountIndexInSpinner);
            }

            accountPicker.setValue(selectedAccountIndexInSpinner);

            accountAlertDialogBuilder = new AlertDialog.Builder(instance)
                    .setView(accountDialogView)
                    .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            selectedAccountIndexInSpinner = accountPicker.getValue();
                            selectedAccount = resultsAccount.get(selectedAccountIndexInSpinner);
                        }
                    })
                    .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Reset the selection back to previous
                            accountPicker.setValue(selectedAccountIndexInSpinner);
                            dialog.dismiss();
                        }
                    });
        }else{
            View accountDialogView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

            TextView title = (TextView)accountDialogView.findViewById(R.id.genericTitle);
            TextView message = (TextView)accountDialogView.findViewById(R.id.genericMessage);

            title.setText(getString(R.string.account));
            message.setText(getString(R.string.empty_account_selection));

            accountAlertDialogBuilder = new AlertDialog.Builder(instance)
                    .setView(accountDialogView)
                    .setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        }
                    });
        }

        accountDialog = accountAlertDialogBuilder.create();
    }

    private void createNoteDialog(){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic, null);

        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        title.setText(getString(R.string.add_note));
        input.setHint(getString(R.string.note));
        input.setText(noteString);

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        noteString = input.getText().toString();

                        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(noteString)){
                            transactionNameTextView.setText(noteString);
                        }else{
                            //set name back on category selected and which page
                            if(currentPage == BudgetType.EXPENSE){
                                transactionNameTextView.setText(selectedExpenseCategory.getName());
                            }else if(currentPage == BudgetType.INCOME){
                                transactionNameTextView.setText(selectedIncomeCategory.getName());
                            }
                        }

                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
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

        RealmResults<Location> locationRealmResults = myRealm.where(Location.class).findAllAsync();
        locationRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Location>>() {
            @Override
            public void onChange(RealmResults<Location> element) {
                element.removeChangeListener(this);

                for(int i = 0; i < element.size(); i++){
                    locationHash.add(element.get(i).getName());
                }
                //Toast.makeText(getBaseContext(), "There are "+locationHash.size()+" unique locations on init", Toast.LENGTH_SHORT).show();

                myRealm.close(); BudgetPreference.removeRealmCache(getBaseContext());
            }
        });
    }

    private void createLocationDialog(){
        //real one
        String[] locationArray = locationHash.toArray(new String[locationHash.size()]);

        View promptView = View.inflate(instance, R.layout.alertdialog_generic_autocomplete, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        title.setText(getString(R.string.add_location));

        inputLocation = (AutoCompleteTextView) promptView.findViewById(R.id.genericAutoCompleteEditText);
        inputLocation.setHint(getString(R.string.location));
        inputLocation.setText(locationString);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locationArray);
        inputLocation.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        locationString = inputLocation.getText().toString().trim();

                        if(editTransaction != null && editTransaction.getLocation() != null){
                            if(!inputLocation.getText().toString().equalsIgnoreCase(editTransaction.getLocation().getName())){
                                newLocation = true;
                            }
                        }else{
                            newLocation = true;
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
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
        final String[] values= {getString(R.string.days), getString(R.string.weeks), getString(R.string.months)};

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
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
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
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog noteDialog = builder.create();
        noteDialog.show();
    }

    private void getDefaultCurrency(){
        final Realm myRealm = Realm.getDefaultInstance();

        defaultCurrency = myRealm.where(BudgetCurrency.class).equalTo("isDefault", true).findFirst();
        if(defaultCurrency == null){
            defaultCurrency = new BudgetCurrency();
            defaultCurrency.setCurrencyCode(Constants.DEFAULT_CURRENCY_CODE);
            defaultCurrency.setCurrencyName(Constants.DEFAULT_CURRENCY_NAME);
        }

        Toast.makeText(getApplicationContext(), "default currency : "+defaultCurrency.getCurrencyName(), Toast.LENGTH_LONG).show();
        transactionCostCurrencyCodeText.setText(defaultCurrency.getCurrencyCode());
        myRealm.close();
    }

    private void addDigitToTextView(int digit){
        if(priceString.length() < CurrencyTextFormatter.MAX_RAW_INPUT_LENGTH){
            priceString += digit;

            String appendString = (currentPage == BudgetType.EXPENSE) ? "-" : "";
            transactionCostView.setText(CurrencyTextFormatter.formatText(appendString+priceString, currentCurrency));

            updateConversion();
        }else {
            Util.createSnackbar(getApplicationContext(), toolbar, getString(R.string.price_too_long));
        }
    }

    private void removeDigit(){
        if (priceString != null && priceString.length() >= 1) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }

        String appendString = (currentPage == BudgetType.EXPENSE) ? "-" : "";
        transactionCostView.setText(CurrencyTextFormatter.formatText(appendString+priceString, currentCurrency));

        updateConversion();
    }

    /**
     * Updates the secondary cost that represents the conversion rate of the selected currency and
     * the default currency
     */
    private void updateConversion(){
        if(currentCurrency.checkEquals(defaultCurrency)){
            transactionCostCurrencyCodeText.setVisibility(View.GONE);
        }else{
            transactionCostCurrencyCodeText.setVisibility(View.VISIBLE);
            float f = CurrencyTextFormatter.formatCurrency(priceString);
            float  afterConversion = CurrencyTextFormatter.convertCurrency(f, currentCurrency);

            afterConversion = (currentPage == BudgetType.EXPENSE) ? -afterConversion : afterConversion;
            transactionCostCurrencyCodeText.setText(CurrencyTextFormatter.formatFloat(afterConversion, defaultCurrency));
        }
    }

    private void createExchangeDialog(final BudgetCurrency selectedBudgetCurrency){
        View promptView = View.inflate(instance, R.layout.alertdialog_currency_rate, null);

        TextView selectedBudgetCurrencyHeader = (TextView) promptView.findViewById(R.id.selectedBudgetCurrencyHeader);
        TextView selectedBudgetCurrencyContent = (TextView) promptView.findViewById(R.id.selectedBudgetCurrencyContent);
        TextView defaultBudgetCurrency = (TextView) promptView.findViewById(R.id.defaultBudgetCurrency);
        final EditText input = (EditText) promptView.findViewById(R.id.exchangeRateEditText);

        //If conversion is greater than 0, put its value into input, else leave it empty for user to put
        if(selectedBudgetCurrency.getRate() > 0){
            double inverseRate = 1 / selectedBudgetCurrency.getRate();
            input.setText(Double.toString(inverseRate));
        }

        selectedBudgetCurrencyHeader.setText(selectedBudgetCurrency.getCurrencyCode());
        selectedBudgetCurrencyContent.setText(selectedBudgetCurrency.getCurrencyCode());
        defaultBudgetCurrency.setText(defaultCurrency.getCurrencyCode());

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_continue), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        currentCurrency = selectedBudgetCurrency;

                        Toast.makeText(getApplicationContext(), " x: "+Double.valueOf(input.getText().toString()), Toast.LENGTH_SHORT).show();

                        double newRate = Double.valueOf(input.getText().toString());

                        //Update the exchange rate into realm
                        Realm myRealm = Realm.getDefaultInstance();
                        myRealm.beginTransaction();
                        BudgetCurrency cc = myRealm.where(BudgetCurrency.class).equalTo("currencyCode", selectedBudgetCurrency.getCurrencyCode()).findFirst();

                        //Reverse the inverse
                        cc.setRate(1 / newRate);
                        myRealm.commitTransaction();
                        myRealm.close();

                        //also need to update the rate in local
                        currentCurrency.setRate(1 / newRate);


                        updateConversion();

                        String appendString = (currentPage == BudgetType.EXPENSE) ? "-" : "";
                        transactionCostView.setText(CurrencyTextFormatter.formatText(appendString+priceString, currentCurrency));
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog exchangeDialog = builder.create();
        exchangeDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        exchangeDialog.show();
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

            Date now = DateUtil.refreshDate(new Date());

            //If its previous date or current
            if(selectedDate.before(now) || DateUtil.isSameDay(selectedDate, now)){
                transaction.setDayType(DayType.COMPLETED.toString());
            }else{
                transaction.setDayType(DayType.SCHEDULED.toString());
            }
        }

        if(newLocation){
            if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(locationString)){

                Location newLocationObject = new Location();
                newLocationObject.setName(locationString.trim());
                newLocationObject.setColor(Colors.getRandomColorString(getBaseContext()));
                transaction.setLocation(newLocationObject);

                Realm myRealm = Realm.getDefaultInstance();
                myRealm.beginTransaction();
                myRealm.copyToRealmOrUpdate(newLocationObject);
                myRealm.commitTransaction();
                myRealm.close();
            }else{
                transaction.setLocation(null);
            }
        }else{
            if(editTransaction != null){
                transaction.setLocation(editTransaction.getLocation());
            }
        }

        transaction.setNote(this.noteString);
        transaction.setDate(DateUtil.formatDate(getApplicationContext(), selectedDate));
        transaction.setAccount(selectedAccount);
        transaction.setCurrency(currentCurrency);
        transaction.setRate(currentCurrency.getRate()); 

        if(currentPage == BudgetType.EXPENSE){
            transaction.setPrice(-CurrencyTextFormatter.formatCurrency(priceString));
            transaction.setCategory(selectedExpenseCategory);
        }else{
            transaction.setPrice(CurrencyTextFormatter.formatCurrency(priceString));
            transaction.setCategory(selectedIncomeCategory);
        }

        Parcelable wrapped = Parcels.wrap(transaction);
        intent.putExtra(RESULT_TRANSACTION, wrapped);

        //Check if any value changed
        if(editTransaction != null){
            if(editTransaction.checkEquals(transaction)){
                intent.putExtra(HAS_CHANGED, false);
            }else{
                intent.putExtra(HAS_CHANGED, true);
            }
        }

        addNewOrEditTransaction(transaction);

        if(isScheduledTransaction){
            addScheduleTransaction(scheduledTransaction, transaction);
        }

        setResult(RESULT_OK, intent);

        finish();
    }

    /**
     * The function that will be called after user either adds or edit a scheduled transaction.
     * @param scheduledTransaction The new scheduled transaction information.
     * @param localTransaction The transaction that the scheduled transaction is based on.
     */
    private void addScheduleTransaction(ScheduledTransaction scheduledTransaction, Transaction localTransaction){
        if(scheduledTransaction != null && scheduledTransaction.getRepeatUnit() != 0){

            Realm myRealm = Realm.getDefaultInstance();
            myRealm.beginTransaction();

            //Keep copy of these value to add back at the end
            Date origDate = localTransaction.getDate();
            String origID = localTransaction.getId();

            scheduledTransaction.setTransaction(localTransaction);
            myRealm.copyToRealmOrUpdate(scheduledTransaction);
            myRealm.commitTransaction();

            Log.d(TAG, "----------- Parceler Result ----------");
            Log.d(TAG, "scheduled transaction id :" + scheduledTransaction.getId());
            Log.d(TAG, "scheduled transaction unit :" + scheduledTransaction.getRepeatUnit() + ", type :" + scheduledTransaction.getRepeatType());
            Log.d(TAG, "transaction note :" + scheduledTransaction.getTransaction().getNote() + ", cost :" + scheduledTransaction.getTransaction().getPrice());
            Log.i(TAG, "----------- Parceler Result ----------");

            localTransaction.setDayType(DayType.SCHEDULED.toString());
            Date nextDate = localTransaction.getDate();

            for(int i = 0; i < 10; i++){
                myRealm.beginTransaction();

                if(scheduledTransaction.getRepeatType().equalsIgnoreCase(RepeatType.DAYS.toString())){
                    nextDate = DateUtil.getDateWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                }else if(scheduledTransaction.getRepeatType().equalsIgnoreCase(RepeatType.WEEKS.toString())){
                    nextDate = DateUtil.getWeekWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                }else{
                    nextDate = DateUtil.getMonthWithDirection(nextDate, scheduledTransaction.getRepeatUnit());
                }

                localTransaction.setId(Util.generateUUID());
                localTransaction.setDate(nextDate);

                Log.d(TAG, i + "-> " + DateUtil.convertDateToStringFormat5(getApplicationContext(), nextDate));
                myRealm.copyToRealmOrUpdate(localTransaction);
                myRealm.commitTransaction();
            }

            //Put back orig value
            //This is so that the localTransaction object in the intent can remain the same
            //So that when this returns to CalendarFragment, it will point to the correct date
            //which should be the starting date, not the end date of the scheduled transactions.
            myRealm.beginTransaction();
            localTransaction.setId(origID);
            localTransaction.setDate(origDate);

            Date now = DateUtil.refreshDate(new Date());

            if(localTransaction.getDate().before(now) || DateUtil.isSameDay(now, localTransaction.getDate())){
                localTransaction.setDayType(DayType.COMPLETED.toString());
            }else{
                localTransaction.setDayType(DayType.SCHEDULED.toString());
            }

            myRealm.copyToRealmOrUpdate(localTransaction);
            myRealm.commitTransaction();

            myRealm.close();
        }
    }

    /**
     * The function that will be called after user either adds or edit a transaction.
     * @param newOrEditTransaction The new transaction information.
     */
    private void addNewOrEditTransaction(final Transaction newOrEditTransaction){
        Log.d(TAG, "----------- Parceler Result ----------");
        Log.d(TAG, "transaction id :"+newOrEditTransaction.getId());
        Log.d(TAG, "transaction note :" + newOrEditTransaction.getNote() + ", cost :" + newOrEditTransaction.getPrice());
        Log.d(TAG, "transaction daytype :" + newOrEditTransaction.getDayType() + ", date :" + newOrEditTransaction.getDate());

        if(newOrEditTransaction.getCategory() != null){
            Log.d(TAG, "category name :" + newOrEditTransaction.getCategory().getName() + ", id:" + newOrEditTransaction.getCategory().getId());
            Log.d(TAG, "category type :" + newOrEditTransaction.getCategory().getType());
        }else{
            Log.d(TAG, "category null");
        }

        if(newOrEditTransaction.getAccount() != null){
            Log.d(TAG, "account id : " + newOrEditTransaction.getAccount().getId());
            Log.d(TAG, "account name : " + newOrEditTransaction.getAccount().getName());
        }else{
            Log.d(TAG, "account is null");
        }
        Log.d(TAG, "----------- Parceler Result ----------");

        Realm myRealm = Realm.getDefaultInstance();
        myRealm.beginTransaction();
        myRealm.copyToRealmOrUpdate(newOrEditTransaction);
        myRealm.commitTransaction();
        myRealm.close();
    }

    /**
     * If there is no Category selected, a dialog will popup to remind the user.
     */
    private void notificationForCategory(BudgetType type){
        View promptView = View.inflate(getBaseContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.category));
        message.setText(String.format(getString(R.string.category_selected_warning), currentPage.toString()));

        new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Etc
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.getExtras() != null) {
            if(requestCode == RequestCodes.SELECTED_CURRENCY){
                Toast.makeText(instance, "selected currency : "+currentCurrency.getCurrencyCode(), Toast.LENGTH_SHORT).show();
                createExchangeDialog((BudgetCurrency) Parcels.unwrap(data.getExtras().getParcelable(SelectCurrencyActivity.RESULT_CURRENCY)));
            }
        }
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
            if((currentPage == BudgetType.EXPENSE && selectedExpenseCategory != null) || (currentPage == BudgetType.INCOME && selectedIncomeCategory != null)){
                save();
            }else{
                notificationForCategory(currentPage);
            }

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

        if(currentPage == BudgetType.EXPENSE && !Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(noteString)){
            transactionNameTextView.setText(category.getName());
        }
    }

    @Override
    public void onCategoryIncomeClick(Category category){
        selectedIncomeCategory = category;
        Log.d("TRAN", "selected income category is "+selectedIncomeCategory.getName());

        if(currentPage == BudgetType.INCOME && !Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(noteString)){
            transactionNameTextView.setText(category.getName());
        }
    }

}
