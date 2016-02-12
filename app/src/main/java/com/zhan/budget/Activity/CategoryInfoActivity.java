package com.zhan.budget.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.ColorPickerCategoryFragment;
import com.zhan.budget.Fragment.IconPickerCategoryFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.circleindicator.CircleIndicator;
import com.zhan.circularview.CircularView;

import org.parceler.Parcels;

import java.util.Locale;

import io.realm.Realm;

public class CategoryInfoActivity extends AppCompatActivity implements
        ColorPickerCategoryFragment.OnColorPickerCategoryFragmentInteractionListener,
        IconPickerCategoryFragment.OnIconPickerCategoryFragmentInteractionListener{

    private Toolbar toolbar;
    private TextView categoryNameTextView, categoryBudgetTextView;
    private ImageButton deleteCategoryBtn, changeBudgetBtn, changeNameBtn;

    private CircularView categoryCircularView;

    private Category category;
    private boolean isEditMode;
    private String priceString = "";

    private TwoPageViewPager adapterViewPager;
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;

    //Fragments
    private ColorPickerCategoryFragment colorPickerCategoryFragment;
    private IconPickerCategoryFragment iconPickerCategoryFragment;

    //Selected color
    private int selectedColor;

    //Selected icon
    private int selectedIcon;

    private Realm myRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_info);

        category = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_EDIT_CATEGORY));
        isEditMode = (getIntent().getExtras()).getBoolean(Constants.REQUEST_NEW_CATEGORY);

        Log.d("CATEGORY_INFO", "this category type is "+category.getType());

        init();
        initCategoryCircularView();
        createToolbar();
        addListeners();
    }

    private void init(){
        myRealm = Realm.getDefaultInstance();

        colorPickerCategoryFragment = new ColorPickerCategoryFragment();
        colorPickerCategoryFragment.setSelectedCategoryColor(category.getColor());

        iconPickerCategoryFragment = new IconPickerCategoryFragment();
        iconPickerCategoryFragment.updateColor(category.getColor()); //set initial color from category
        iconPickerCategoryFragment.setSelectedCategoryIcon(category.getIcon());

        viewPager = (ViewPager) findViewById(R.id.categoryViewPager);
        adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), colorPickerCategoryFragment, iconPickerCategoryFragment);
        viewPager.setAdapter(adapterViewPager);

        circleIndicator = (CircleIndicator) findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

        categoryNameTextView = (TextView) findViewById(R.id.categoryNameTextView);
        categoryBudgetTextView = (TextView) findViewById(R.id.categoryBudgetTextView);

        categoryNameTextView.setText(category.getName());
        categoryBudgetTextView.setText("$"+category.getBudget());

        priceString = Float.toString(category.getBudget());

        changeNameBtn = (ImageButton) findViewById(R.id.changeNameBtn);
        deleteCategoryBtn = (ImageButton) findViewById(R.id.deleteCategoryBtn);
        changeBudgetBtn = (ImageButton) findViewById(R.id.changeBudgetBtn);




        runTest();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // test
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////


    private void runTest(){
        createCurrency(Locale.CANADA);
        createCurrency(Locale.JAPAN);
        createCurrency(Locale.KOREA);
        createCurrency(Locale.FRANCE);
        createCurrency(Locale.US);
        createCurrency(Locale.UK);
    }

    private void createCurrency(Locale locale){
        Log.d("CURRENCY", "----- TEST -----");

        String currency = "";
        for(int i = 0; i < 8; i++){
            if(i == 6){
                currency += 0;
            }else{
                currency += i;
            }

            Log.d("CURRENCY", locale+" "+currency+" => "+CurrencyTextFormatter.formatText(currency, locale)+" -> float:"+CurrencyTextFormatter.formatCurrency(currency, locale));
        }

        Log.d("CURRENCY", "----- END TEST -----");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // end of test
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initCategoryCircularView(){
        categoryCircularView = (CircularView) findViewById(R.id.categoryCircularView);
        categoryCircularView.setCircleColor(category.getColor());

        categoryCircularView.setIconDrawable(ResourcesCompat.getDrawable(getResources(),
                category.getIcon(), getTheme()));
        categoryCircularView.setIconColor(R.color.white);
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
            if(isEditMode){
                getSupportActionBar().setTitle("Edit Category");

            }else{
                getSupportActionBar().setTitle("Add Category");
            }
        }
    }

    private void addListeners(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeRealm();
                finish();
            }
        });

        changeNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }
        });

        deleteCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        changeBudgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Change budget Category ", Toast.LENGTH_SHORT).show();
                changeBudget();
            }
        });
    }

    private void changeName(){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.alertdialog_generic, null);

        TextView genericTitle = (TextView) promptView.findViewById(R.id.genericTitle);
        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);

        genericTitle.setText("Category Name");
        input.setText(categoryNameTextView.getText());

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        categoryNameTextView.setText(input.getText().toString());
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void changeBudget(){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.alertdialog_number_pad, null);

        TextView title = (TextView) promptView.findViewById(R.id.numberPadTitle);
        final TextView budgetTextView = (TextView) promptView.findViewById(R.id.numericTextView);

        title.setText("Change Budget");
        budgetTextView.setText(CurrencyTextFormatter.formatText(priceString, Locale.CANADA));

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();

        Button button1 = (Button) promptView.findViewById(R.id.number1);
        Button button2 = (Button) promptView.findViewById(R.id.number2);
        Button button3 = (Button) promptView.findViewById(R.id.number3);
        Button button4 = (Button) promptView.findViewById(R.id.number4);
        Button button5 = (Button) promptView.findViewById(R.id.number5);
        Button button6 = (Button) promptView.findViewById(R.id.number6);
        Button button7 = (Button) promptView.findViewById(R.id.number7);
        Button button8 = (Button) promptView.findViewById(R.id.number8);
        Button button9 = (Button) promptView.findViewById(R.id.number9);
        Button button0 = (Button) promptView.findViewById(R.id.number0);
        ImageButton buttonX = (ImageButton) promptView.findViewById(R.id.numberX);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 1);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 2);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 3);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 4);
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 5);
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 6);
            }
        });

        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 7);
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 8);
            }
        });

        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 9);
            }
        });

        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDigitToTextView(budgetTextView, 0);
            }
        });

        buttonX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDigit(budgetTextView);
            }
        });
    }

    private void addDigitToTextView(TextView textView, int digit){
       /*
        priceString += digit;
        StringBuilder cashAmountBuilder = new StringBuilder(priceString);

        while (cashAmountBuilder.length() < 3) {
            cashAmountBuilder.insert(0, '0');
        }

        cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');
        priceStringWithDot = cashAmountBuilder.toString();

        String appendString = (category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString()))?"-$":"+$";
        textView.setText(appendString + priceStringWithDot);
        */



        priceString += digit;
        textView.setText(CurrencyTextFormatter.formatText(priceString, Locale.CANADA));
    }

    private void removeDigit(TextView textView){
        /*
        if (priceString != null && priceString.length() >= 1) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }

        StringBuilder cashAmountBuilder = new StringBuilder(priceString);

        while (cashAmountBuilder.length() < 3) {
            cashAmountBuilder.insert(0, '0');
        }

        cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');
        priceStringWithDot = cashAmountBuilder.toString();

        String appendString = (category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString()))?"-$":"+$";
        textView.setText(appendString + priceStringWithDot);
        */

        if (priceString != null && priceString.length() >= 1) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }
        textView.setText(CurrencyTextFormatter.formatText(priceString, Locale.CANADA));
    }

    private void confirmDelete(){
        new AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "DELETE...", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void save(){
        Intent intent = new Intent();

        Category c = myRealm.where(Category.class).equalTo("id", category.getId()).findFirst();
        myRealm.beginTransaction();
        c.setName(categoryNameTextView.getText().toString());
        c.setIcon(selectedIcon);
        c.setColor(selectedColor);

        if(category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString())){
            c.setCost(-CurrencyTextFormatter.formatCurrency(priceString, Locale.CANADA));
        }else{
            c.setCost(CurrencyTextFormatter.formatCurrency(priceString, Locale.CANADA));
        }

        myRealm.commitTransaction();

        Parcelable wrapped = Parcels.wrap(c);

        intent.putExtra(Constants.RESULT_EDIT_CATEGORY,wrapped);
        setResult(RESULT_OK, intent);

        closeRealm();
        finish();
    }

    private void updateCategoryColor(){
        categoryCircularView.setCircleColor(selectedColor);
        iconPickerCategoryFragment.updateColor(selectedColor);
    }

    @Override
    public void onBackPressed() {
        closeRealm();
        finish();
    }

    private void closeRealm(){
        if(!myRealm.isClosed()){
            myRealm.close();
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
    public void onColorCategoryClick(int color){
        Log.d("CATEGORY_INFO", "click on color : "+color);
        selectedColor = color;
        updateCategoryColor();
    }

    @Override
    public void onIconCategoryClick(int icon){
        Log.d("CATEGORY_INFO", "click on icon : "+icon);
        selectedIcon = icon;
        categoryCircularView.setIconDrawable(ResourcesCompat.getDrawable(getResources(),
                icon, getTheme()));
    }
}
