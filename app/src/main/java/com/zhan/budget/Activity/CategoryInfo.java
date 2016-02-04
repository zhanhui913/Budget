package com.zhan.budget.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zhan.budget.Adapter.TwoPageViewPager;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.ColorCategoryFragment;
import com.zhan.budget.Fragment.TransactionExpenseFragment;
import com.zhan.budget.Fragment.TransactionIncomeFragment;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.circleindicator.CircleIndicator;
import com.zhan.circularview.CircularView;

import org.parceler.Parcels;

public class CategoryInfo extends AppCompatActivity implements
        ColorCategoryFragment.OnColorCateogyrFragmentInteractionListener,
        TransactionExpenseFragment.OnTransactionExpenseFragmentInteractionListener,
        TransactionIncomeFragment.OnTransactionIncomeFragmentInteractionListener{

    private Toolbar toolbar;
    private TextView categoryName, categoryBudget, categoryCost;

    private CircularView categoryCircularView;

    private Category category;
    private boolean isEditMode;

    private TwoPageViewPager adapterViewPager;
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;

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

        viewPager = (ViewPager) findViewById(R.id.categoryViewPager);
        adapterViewPager = new TwoPageViewPager(getSupportFragmentManager(),
                new ColorCategoryFragment(),
                (category.getType().equalsIgnoreCase(BudgetType.EXPENSE.toString()))? new TransactionExpenseFragment(): new TransactionIncomeFragment());
        viewPager.setAdapter(adapterViewPager);

        circleIndicator = (CircleIndicator) findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

/*
        categoryName = (TextView) findViewById(R.id.categoryName);
        categoryBudget = (TextView) findViewById(R.id.categoryBudget);
        categoryCost = (TextView) findViewById(R.id.categoryCost);


        categoryName.setText(category.getName());
        categoryBudget.setText("$" + category.getBudget());
        categoryCost.setText("$" + category.getCost());
        */
    }

    private void initCategoryCircularView(){
        categoryCircularView = (CircularView) findViewById(R.id.categoryCircularView);
        categoryCircularView.setCircleColor(Color.parseColor(category.getColor()));

        categoryCircularView.setIconDrawable(ResourcesCompat.getDrawable(getResources(),
                CategoryUtil.getIconResourceId(category.getIcon()), getTheme()));
        categoryCircularView.setIconColor(Color.parseColor("#FFFFFF"));
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
                finish();
            }
        });
    }

    private void save(){
        Intent intent = new Intent();

        Category newCategory = category;
        newCategory.setCost(category.getCost()+ 1 );

        //intent.putExtra(Constants.RESULT_EDIT_CATEGORY, newCategory);
        setResult(RESULT_OK, intent);

        finish();
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
    public void onCategoryExpenseClick(Category category){
        //selectedExpenseCategory = category;
    }

    @Override
    public void onCategoryIncomeClick(Category category){
        //selectedIncomeCategory = category;
    }

    @Override
    public void onColorCategoryClick(String color){
        Log.d("CATEGORY_INFO", "click on color : "+color);
    }

}
