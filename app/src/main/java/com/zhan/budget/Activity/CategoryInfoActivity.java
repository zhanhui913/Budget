package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Etc.CurrencyTextFormatter;
import com.zhan.budget.Fragment.ColorPickerCategoryFragment;
import com.zhan.budget.Fragment.IconPickerCategoryFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Util;
import com.zhan.circleindicator.CircleIndicator;
import com.zhan.library.CircularView;

import org.parceler.Parcels;

import io.realm.Realm;
import io.realm.RealmResults;

public class CategoryInfoActivity extends BaseActivity implements
        ColorPickerCategoryFragment.OnColorPickerCategoryFragmentInteractionListener,
        IconPickerCategoryFragment.OnIconPickerCategoryFragmentInteractionListener{

    private Activity instance;
    private Toolbar toolbar;
    private TextView categoryNameTextView, categoryBudgetTextView;
    private ImageButton deleteCategoryBtn, changeBudgetBtn, changeNameBtn;
    private ToggleButton toggleBtn;

    private CircularView categoryCircularView;

    private Category category;
    private boolean isNewCategory;
    private String priceString = "";

    //Fragments
    private ColorPickerCategoryFragment colorPickerCategoryFragment;
    private IconPickerCategoryFragment iconPickerCategoryFragment;

    //Selected color
    private String selectedColor;

    //Selected icon
    private String selectedIcon;

    private int catRes; //The res ID for category icon for circular view use
    private boolean isCurrentCircularText; //Is the current circular view using text or icon

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_category_info;
    }

    @Override
    protected void init(){
        instance = this;

        isNewCategory = (getIntent().getExtras()).getBoolean(Constants.REQUEST_NEW_CATEGORY);


        if(!isNewCategory){
            category = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_EDIT_CATEGORY));
        }else{
            //Give default category values
            category = new Category();
            category.setId(Util.generateUUID());
            category.setColor(CategoryUtil.getDefaultCategoryColor(this));
            category.setIcon(CategoryUtil.getDefaultCategoryIcon(this));
            category.setType(getIntent().getExtras().getString(Constants.REQUEST_NEW_CATEGORY_TYPE));
            category.setText(false); //default use icon
        }

        colorPickerCategoryFragment = ColorPickerCategoryFragment.newInstance(category.getColor());
        iconPickerCategoryFragment = IconPickerCategoryFragment.newInstance(category.getIcon(), category.getColor());

        ViewPager viewPager = (ViewPager) findViewById(R.id.categoryViewPager);
        TwoPageViewPager adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), colorPickerCategoryFragment, iconPickerCategoryFragment);
        viewPager.setAdapter(adapterViewPager);

        CircleIndicator circleIndicator = (CircleIndicator) findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

        categoryNameTextView = (TextView) findViewById(R.id.categoryNameTextView);
        categoryBudgetTextView = (TextView) findViewById(R.id.categoryBudgetTextView);

        categoryNameTextView.setText(category.getName());
        categoryBudgetTextView.setText(CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE));

        changeNameBtn = (ImageButton) findViewById(R.id.changeNameBtn);
        deleteCategoryBtn = (ImageButton) findViewById(R.id.deleteCategoryBtn);
        changeBudgetBtn = (ImageButton) findViewById(R.id.changeBudgetBtn);

        toggleBtn = (ToggleButton) findViewById(R.id.useTextToggle);

        if(isNewCategory){
            deleteCategoryBtn.setVisibility(View.GONE);
        }else{
            deleteCategoryBtn.setVisibility(View.VISIBLE);
        }

        //Income category has no need for budget
        if(category.getType().equalsIgnoreCase(BudgetType.INCOME.toString())){
            changeBudgetBtn.setVisibility(View.GONE);
            categoryBudgetTextView.setVisibility(View.GONE);
        }

        //default color selected
        selectedColor = category.getColor();

        //default icon selected
        selectedIcon = category.getIcon();

        initCategoryCircularView();
        createToolbar();
        addListeners();

        //Check if current category is using text or icon in its circular view
        if(category.isText()){
            isCurrentCircularText = true;
            changeCircularViewToText(categoryNameTextView.getText().toString());
        }else{
            isCurrentCircularText = false;
            changeCircularViewToIcon();
        }
    }

    private void initCategoryCircularView(){
        categoryCircularView = (CircularView) findViewById(R.id.categoryCircularView);
        categoryCircularView.setCircleColor(category.getColor());

        catRes = CategoryUtil.getIconID(this, category.getIcon());
        categoryCircularView.setIconResource(catRes);
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

        categoryNameTextView.setOnClickListener(new View.OnClickListener() {
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
                changeBudget();
            }
        });

        categoryBudgetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBudget();
            }
        });

        toggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCurrentCircularText = isChecked;

                if (isChecked) {
                    // The toggle is enabled (Use text)
                    changeCircularViewToText(categoryNameTextView.getText().toString());
                } else {
                    // The toggle is disabled (Use icon)
                    changeCircularViewToIcon();
                }
            }
        });
    }

    private void changeName(){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic, null);

        TextView genericTitle = (TextView) promptView.findViewById(R.id.genericTitle);
        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);

        genericTitle.setText("Category Name");
        input.setText(categoryNameTextView.getText());
        input.setHint("Category");

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        categoryNameTextView.setText(input.getText().toString());

                        if(isCurrentCircularText){ //if the current toggle is text
                            changeCircularViewToText(input.getText().toString());
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog nameDialog = builder.create();
        nameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        nameDialog.show();
    }

    private void changeBudget(){
        View promptView = View.inflate(instance, R.layout.alertdialog_number_pad, null);

        TextView title = (TextView) promptView.findViewById(R.id.numberPadTitle);
        final TextView budgetTextView = (TextView) promptView.findViewById(R.id.numericTextView);

        priceString = CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE);

        //Remove any extra un-needed signs
        priceString = CurrencyTextFormatter.stripCharacters(priceString);

        title.setText("Change Budget");
        budgetTextView.setText(CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE));

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        category.setBudget(CurrencyTextFormatter.formatCurrency(priceString, Constants.BUDGET_LOCALE));
                        categoryBudgetTextView.setText(CurrencyTextFormatter.formatFloat(category.getBudget(), Constants.BUDGET_LOCALE));
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
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
        //Toast.makeText(CategoryInfoActivity.this, "set text:"+digit, Toast.LENGTH_SHORT).show();
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
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(R.string.warning_delete_category);

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        Realm myRealm = Realm.getDefaultInstance();
                        Category cat = myRealm.where(Category.class).equalTo("id", category.getId()).findFirst();
                        myRealm.beginTransaction();
                        cat.deleteFromRealm();
                        myRealm.commitTransaction();
                        myRealm.close();

                        intent.putExtra(Constants.RESULT_DELETE_CATEGORY, true); //deleting category
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    int nextIndexCategory;
    private void getLatestIndexForCategory(){ Log.d("ZHAP", "trying to get latest index for new category for type :"+category.getType());
        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<Category> categoryRealmResults = myRealm.where(Category.class).equalTo("type", category.getType()).findAllSorted("index");
        Log.d("ZHAP", "size :"+categoryRealmResults.size());
        Log.d("ZHAP", "Highest category index for " + category.getType() + " is " + categoryRealmResults.get(categoryRealmResults.size() - 1).getIndex());
        nextIndexCategory = categoryRealmResults.get(categoryRealmResults.size() - 1).getIndex() + 1;
        myRealm.close();
    }

    private void save(){
        Intent intent = new Intent();

        Category c;

        Realm myRealm = Realm.getDefaultInstance();  BudgetPreference.addRealmCache(this);
        if(!isNewCategory){
            c = myRealm.where(Category.class).equalTo("id", category.getId()).findFirst();
            myRealm.beginTransaction();
        } else{
            myRealm.beginTransaction();
            c = myRealm.createObject(Category.class);
            c.setId(category.getId());
            c.setIndex(nextIndexCategory);
        }

        c.setName(categoryNameTextView.getText().toString());
        c.setIcon(selectedIcon);
        c.setColor(selectedColor);
        c.setBudget(category.getBudget());
        c.setCost(category.getCost());
        c.setType(category.getType());
        c.setText(isCurrentCircularText);
        myRealm.commitTransaction();

        Log.d("CATEGORY_INFO_ACTIVITY", "-----Results-----");
        Log.d("CATEGORY_INFO_ACTIVITY", "Category name : "+c.getName());
        Log.d("CATEGORY_INFO_ACTIVITY", "id : " + c.getId());
        Log.d("CATEGORY_INFO_ACTIVITY", "budget : " + c.getBudget());
        Log.d("CATEGORY_INFO_ACTIVITY", "type : " + c.getType());
        Log.d("CATEGORY_INFO_ACTIVITY", "color : " + c.getColor());
        Log.d("CATEGORY_INFO_ACTIVITY", "icon : " + c.getIcon());
        Log.d("CATEGORY_INFO_ACTIVITY", "cost : " + c.getCost());
        Log.d("CATEGORY_INFO_ACTIVITY", "-----Results-----");

        //Need to explicitly copy the value of cost since its property is ignored in the model.
        float cost = c.getCost();

        Category carbonCopy = myRealm.copyFromRealm(c);
        carbonCopy.setCost(cost);
        Parcelable wrapped = Parcels.wrap(carbonCopy);
        myRealm.close();  BudgetPreference.removeRealmCache(this);

        if(!isNewCategory){
            intent.putExtra(Constants.RESULT_EDIT_CATEGORY, wrapped);
        }else{
            intent.putExtra(Constants.RESULT_NEW_CATEGORY, wrapped);
        }

        setResult(RESULT_OK, intent);

        finish();
    }

    private void updateCategoryColor(){
        categoryCircularView.setCircleColor(selectedColor);
        iconPickerCategoryFragment.updateColor(selectedColor);
    }

    private void changeCircularViewToText(String value){
        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(value)){
            categoryCircularView.setText(""+Util.getFirstCharacterFromString(categoryNameTextView.getText().toString().toUpperCase()));
        }
        categoryCircularView.setIconResource(0);
    }

    private void changeCircularViewToIcon(){
        categoryCircularView.setText("");
        categoryCircularView.setIconResource(catRes);
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
            getLatestIndexForCategory();

            if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(categoryNameTextView.getText().toString())){
                save();
            }else{
                Util.createSnackbar(getBaseContext(), (View)categoryNameTextView.getParent(), "Please input a valid name for this category");
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
    public void onColorCategoryClick(String color){
        Log.d("CATEGORY_INFO", "click on color : "+color);
        selectedColor = color;
        updateCategoryColor();
    }

    @Override
    public void onIconCategoryClick(String icon){
        Log.d("CATEGORY_INFO", "click on icon : "+icon);
        selectedIcon = icon;

        catRes = CategoryUtil.getIconID(this, icon);
        if(!isCurrentCircularText){
            categoryCircularView.setIconResource(catRes);
        }
    }
}
