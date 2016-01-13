package com.zhan.budget.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Category;
import com.zhan.budget.R;

public class CategoryInfo extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton editCategoryFAB;
    private TextView categoryName, categoryBudget, categoryCost;
    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_info);

        init();
        addListeners();
    }

    private void init(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editCategoryFAB = (FloatingActionButton) findViewById(R.id.editCategoryFAB);
        categoryName = (TextView) findViewById(R.id.categoryName);
        categoryBudget = (TextView) findViewById(R.id.categoryBudget);
        categoryCost = (TextView) findViewById(R.id.categoryCost);

        category = (getIntent().getExtras()).getParcelable(Constants.REQUEST_EDIT_CATEGORY);

        categoryName.setText(category.getName());
        categoryBudget.setText("$" + category.getBudget());
        categoryCost.setText("$" + category.getCost());
    }

    private void addListeners(){
        editCategoryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
