package com.zhan.budget.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
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
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.ThemeUtil;
import com.zhan.budget.Util.Util;
import com.zhan.circleindicator.CircleIndicator;
import com.zhan.library.CircularView;

import org.parceler.Parcels;

public class CategoryInfoActivity extends BaseActivity implements
        ColorPickerCategoryFragment.OnColorPickerCategoryFragmentInteractionListener,
        IconPickerCategoryFragment.OnIconPickerCategoryFragmentInteractionListener{

    private Toolbar toolbar;
    private TextView categoryNameTextView, categoryBudgetTextView;
    private ImageButton deleteCategoryBtn, changeBudgetBtn, changeNameBtn;

    private CircularView categoryCircularView;

    private Category category;
    private boolean isNewCategory;
    private String priceString = "";

    private TwoPageViewPager adapterViewPager;
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;

    //Fragments
    private ColorPickerCategoryFragment colorPickerCategoryFragment;
    private IconPickerCategoryFragment iconPickerCategoryFragment;

    //Selected color
    private String selectedColor;

    //Selected icon
    private String selectedIcon;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_category_info;
    }

    @Override
    protected void init(){
        super.init();

        isNewCategory = (getIntent().getExtras()).getBoolean(Constants.REQUEST_NEW_CATEGORY);

        if(!isNewCategory){
            category = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_EDIT_CATEGORY));
        }else{
            //Give default category values
            category = new Category();
            category.setId(Util.generateUUID());
            category.setColor(CategoryUtil.getDefaultCategoryColor(getApplicationContext()));
            category.setIcon(CategoryUtil.getDefaultCategoryIcon(getApplicationContext()));

            //Default is expense for now, need to change later when there are tabs in the category fragment
            category.setType(BudgetType.EXPENSE.toString());
        }

        colorPickerCategoryFragment = ColorPickerCategoryFragment.newInstance(category.getColor());
        iconPickerCategoryFragment = IconPickerCategoryFragment.newInstance(category.getIcon(), category.getColor());

        viewPager = (ViewPager) findViewById(R.id.categoryViewPager);
        adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), colorPickerCategoryFragment, iconPickerCategoryFragment);
        viewPager.setAdapter(adapterViewPager);

        circleIndicator = (CircleIndicator) findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

        categoryNameTextView = (TextView) findViewById(R.id.categoryNameTextView);
        categoryBudgetTextView = (TextView) findViewById(R.id.categoryBudgetTextView);

        categoryNameTextView.setText(category.getName());
        categoryBudgetTextView.setText(CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE));

        changeNameBtn = (ImageButton) findViewById(R.id.changeNameBtn);
        deleteCategoryBtn = (ImageButton) findViewById(R.id.deleteCategoryBtn);
        changeBudgetBtn = (ImageButton) findViewById(R.id.changeBudgetBtn);

        if(isNewCategory){
            deleteCategoryBtn.setVisibility(View.GONE);
        }

        //default color selected
        selectedColor = category.getColor();

        //default icon selected
        selectedIcon = category.getIcon();

        initCategoryCircularView();
        createToolbar();
        addListeners();
    }

    private void initCategoryCircularView(){
        categoryCircularView = (CircularView) findViewById(R.id.categoryCircularView);
        categoryCircularView.setCircleColor(category.getColor());

        categoryCircularView.setIconResource(CategoryUtil.getIconID(getApplicationContext(), category.getIcon()));
        if(ThemeUtil.getCurrentTheme() == ThemeUtil.THEME_LIGHT) {
            categoryCircularView.setIconColor(R.color.day);
        }else{
            categoryCircularView.setIconColor(R.color.night);
        }
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
            if(isNewCategory){
                getSupportActionBar().setTitle("Add Category");
            }else{
                getSupportActionBar().setTitle("Edit Category");
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
        input.setHint("Category");

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

        priceString = "";

        title.setText("Change Budget");
        budgetTextView.setText(CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE));

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        category.setBudget(CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE));
                        categoryBudgetTextView.setText(CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE));
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
        Toast.makeText(CategoryInfoActivity.this, "set text:"+digit, Toast.LENGTH_SHORT).show();
        priceString += digit;
        textView.setText(CurrencyTextFormatter.formatText(priceString, Constants.BUDGET_LOCALE));
    }

    private void removeDigit(TextView textView){
        if (priceString != null && priceString.length() >= 1) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }
        textView.setText(CurrencyTextFormatter.formatText(priceString, Constants.BUDGET_LOCALE));
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

        Category c;
/*        if(!isNewCategory){
            c = myRealm.where(Category.class).equalTo("id", category.getId()).findFirst();
            myRealm.beginTransaction();
            c.setName(categoryNameTextView.getText().toString());
            c.setIcon(selectedIcon);
            c.setColor(selectedColor);
            c.setBudget(category.getBudget());
            c.setCost(category.getCost());
            myRealm.commitTransaction();
        }else{
            c = myRealm.createObject(Category.class);
            c.setId(category.getId());
            c.setName(categoryNameTextView.getText().toString());
            c.setIcon(selectedIcon);
            c.setColor(selectedColor);
            c.setBudget(category.getBudget());
            c.setCost(category.getCost());
        }
*/

        if(!isNewCategory){
            c = myRealm.where(Category.class).equalTo("id", category.getId()).findFirst();
            myRealm.beginTransaction();
        }
        else{
            myRealm.beginTransaction();
            c = myRealm.createObject(Category.class);
            c.setId(category.getId());
        }

        c.setName(categoryNameTextView.getText().toString());
        c.setIcon(selectedIcon);
        c.setColor(selectedColor);
        c.setBudget(category.getBudget());
        c.setCost(category.getCost());
        c.setType(category.getType());
        myRealm.commitTransaction();

        Log.d("CATEGORY_INFO_ACTIVITY", "-----Results-----");
        Log.d("CATEGORY_INFO_ACTIVITY", "Category name : "+c.getName());
        Log.d("CATEGORY_INFO_ACTIVITY", "id : " + c.getId());
        Log.d("CATEGORY_INFO_ACTIVITY", "budget : " + c.getBudget());
        Log.d("CATEGORY_INFO_ACTIVITY", "type : " + c.getType());
        Log.d("CATEGORY_INFO_ACTIVITY", "color : " + c.getColor());
        Log.d("CATEGORY_INFO_ACTIVITY", "icon : " + c.getIcon());
        Log.d("CATEGORY_INFO_ACTIVITY", "-----Results-----");


        Parcelable wrapped = Parcels.wrap(c);

        intent.putExtra(Constants.RESULT_EDIT_CATEGORY, wrapped);
        setResult(RESULT_OK, intent);

        finish();
    }

    private void updateCategoryColor(){
        categoryCircularView.setCircleColor(selectedColor);
        iconPickerCategoryFragment.updateColor(selectedColor);
    }

    @Override
    public void onBackPressed() {
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
    public void onColorCategoryClick(String color){
        Log.d("CATEGORY_INFO", "click on color : "+color);
        selectedColor = color;
        updateCategoryColor();
    }

    @Override
    public void onIconCategoryClick(String icon){
        Log.d("CATEGORY_INFO", "click on icon : "+icon);
        selectedIcon = icon;
        categoryCircularView.setIconResource(CategoryUtil.getIconID(getApplicationContext(), icon));
    }
}
