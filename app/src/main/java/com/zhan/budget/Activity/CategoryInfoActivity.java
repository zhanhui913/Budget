package com.zhan.budget.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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

    public static final String NEW_CATEGORY = "New Category";

    public static final String NEW_CATEGORY_TYPE = "New Category Type";

    public static final String EDIT_CATEGORY_ITEM = "Edit Category Item";

    public static final String RESULT_CATEGORY = "Result Category";

    private Activity instance;
    private Toolbar toolbar;
    private TextView currentPageTextView, categoryNameTextView, categoryBudgetTextView;
    private ImageButton deleteCategoryBtn, changeBudgetBtn, changeNameBtn;
    private ToggleButton toggleBtn;
    private ViewPager viewPager;
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

    public static Intent createIntentForNewCategory(Context context, BudgetType type){
        Intent intent = new Intent(context, CategoryInfoActivity.class);
        intent.putExtra(NEW_CATEGORY, true);
        intent.putExtra(NEW_CATEGORY_TYPE, type.toString());
        return intent;
    }

    public static Intent createIntentToEditCategory(Context context, Category category){
        Intent intent = new Intent(context, CategoryInfoActivity.class);
        intent.putExtra(NEW_CATEGORY, false);

        Parcelable wrapped = Parcels.wrap(category);
        intent.putExtra(EDIT_CATEGORY_ITEM, wrapped);

        return intent;
    }

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_category_info;
    }

    @Override
    protected void init(){
        instance = this;

        isNewCategory = (getIntent().getExtras()).getBoolean(NEW_CATEGORY);

        if(!isNewCategory){
            category = Parcels.unwrap((getIntent().getExtras()).getParcelable(EDIT_CATEGORY_ITEM));
        }else{
            //Give default category values
            category = new Category();
            category.setId(Util.generateUUID());
            category.setColor(CategoryUtil.getDefaultCategoryColor(this));
            category.setIcon(CategoryUtil.getDefaultCategoryIcon(this));
            category.setType(getIntent().getExtras().getString(NEW_CATEGORY_TYPE));
            category.setText(false); //default use icon
        }

        colorPickerCategoryFragment = ColorPickerCategoryFragment.newInstance(category.getColor());
        iconPickerCategoryFragment = IconPickerCategoryFragment.newInstance(category.getIcon(), category.getColor());

        viewPager = (ViewPager) findViewById(R.id.categoryViewPager);
        TwoPageViewPager adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(), colorPickerCategoryFragment, iconPickerCategoryFragment);
        viewPager.setAdapter(adapterViewPager);

        CircleIndicator circleIndicator = (CircleIndicator) findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

        currentPageTextView = (TextView)findViewById(R.id.currentPageTitle);
        categoryNameTextView = (TextView) findViewById(R.id.categoryNameTextView);
        categoryBudgetTextView = (TextView) findViewById(R.id.categoryBudgetTextView);

        //default first page
        currentPageTextView.setText(R.string.color);

        categoryNameTextView.setText(category.getName());
        categoryBudgetTextView.setText(CurrencyTextFormatter.formatDouble(category.getBudget()));

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

        categoryNameTextView.setText(category.getName());
        categoryBudgetTextView.setText(CurrencyTextFormatter.formatDouble(category.getBudget()));

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
        categoryCircularView.setStrokeColor(category.getColor());

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
                getSupportActionBar().setTitle(category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString()) ? getString(R.string.new_category_expense) : getString(R.string.new_category_income));
            }else{
                getSupportActionBar().setTitle(category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString()) ? getString(R.string.edit_category_expense) : getString(R.string.edit_category_income));
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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        currentPageTextView.setText(R.string.color);
                        break;
                    case 1:
                        currentPageTextView.setText(R.string.icon);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void changeName(){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_edittext, null);

        TextView genericTitle = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);

        genericTitle.setText(getString(R.string.name));
        input.setText(categoryNameTextView.getText());
        input.setHint(getString(R.string.category));

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        categoryNameTextView.setText(input.getText().toString().trim());
                        category.setName(input.getText().toString().trim());

                        if(isCurrentCircularText){ //if the current toggle is text
                            changeCircularViewToText(input.getText().toString().trim());
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

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        final TextView budgetTextView = (TextView) promptView.findViewById(R.id.numericTextView);

        priceString = CurrencyTextFormatter.formatDouble(category.getBudget());

        //Remove any extra un-needed signs
        priceString = CurrencyTextFormatter.stripCharacters(priceString);

        title.setText(getString(R.string.budget));
        budgetTextView.setText(CurrencyTextFormatter.formatDouble(category.getBudget()));

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        category.setBudget(CurrencyTextFormatter.formatCurrency(priceString));
                        categoryBudgetTextView.setText(CurrencyTextFormatter.formatDouble(category.getBudget()));
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
        if(priceString.length() < CurrencyTextFormatter.MAX_RAW_INPUT_LENGTH) {
            priceString += digit;
            textView.setText(CurrencyTextFormatter.formatText(priceString));
        }
    }

    private void removeDigit(TextView textView){
        if (priceString != null && priceString.length() >= 1) {
            priceString = priceString.substring(0, priceString.length() - 1);
        }

        textView.setText(CurrencyTextFormatter.formatText(priceString));
    }

    private void confirmDelete(){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(R.string.warning_delete_category);

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();

                        Realm myRealm = Realm.getDefaultInstance();
                        Category cat = myRealm.where(Category.class).equalTo("id", category.getId()).findFirst();
                        myRealm.beginTransaction();
                        cat.deleteFromRealm();
                        myRealm.commitTransaction();
                        myRealm.close();

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

        if(categoryRealmResults.size() > 0){
            Log.d("ZHAP", "size :"+categoryRealmResults.size());
            Log.d("ZHAP", "Highest category index for " + category.getType() + " is " + categoryRealmResults.get(categoryRealmResults.size() - 1).getIndex());
            nextIndexCategory = categoryRealmResults.get(categoryRealmResults.size() - 1).getIndex() + 1;
        }else{
            nextIndexCategory = 0;
        }

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
        double cost = c.getCost();

        Category carbonCopy = myRealm.copyFromRealm(c);
        carbonCopy.setCost(cost);
        Parcelable wrapped = Parcels.wrap(carbonCopy);
        myRealm.close();  BudgetPreference.removeRealmCache(this);

        intent.putExtra(RESULT_CATEGORY, wrapped);

        setResult(RESULT_OK, intent);

        finish();
    }

    private void updateCategoryColor(){
        categoryCircularView.setCircleColor(selectedColor);
        categoryCircularView.setStrokeColor(selectedColor);
        iconPickerCategoryFragment.updateColor(selectedColor);
    }

    private void changeCircularViewToText(String value){
        if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(value)){
            categoryCircularView.setText(""+Util.getFirstCharacterFromString(categoryNameTextView.getText().toString().toUpperCase().trim()));
        }
        categoryCircularView.setIconResource(0);
    }

    private void changeCircularViewToIcon(){
        categoryCircularView.setText("");
        categoryCircularView.setIconResource(catRes);
    }

    /**
     * If there is no Category name, a dialog will popup to remind the user.
     */
    private void notificationForCategory(){
        View promptView = View.inflate(getBaseContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(R.string.category);
        message.setText(R.string.warning_category_valid_name);

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
            if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(category.getName())){
                getLatestIndexForCategory();
                save();
            }else{
                notificationForCategory();
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
