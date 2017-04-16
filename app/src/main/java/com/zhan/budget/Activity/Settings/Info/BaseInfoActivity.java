package com.zhan.budget.Activity.Settings.Info;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.BaseActivity;
import com.zhan.budget.Adapter.AttributionRecyclerAdapter;
import com.zhan.budget.Model.Attribution;
import com.zhan.budget.R;

import java.util.List;

/**
 * Created by Zhan on 2017-04-15.
 */

public abstract class BaseInfoActivity extends BaseActivity {

    private Toolbar toolbar;
    protected List<Attribution> dataList;
    protected Activity instance;
    protected AttributionRecyclerAdapter adapter;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_info_base;
    }

    @Override
    protected void init(){
        instance = this;

        createToolbar();
        addListeners();
        setupList();
        generateList();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(setToolbarTitle());
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

    private void setupList(){
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.dataListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AttributionRecyclerAdapter(this, dataList);
        recyclerView.setAdapter(adapter);

        //Add divider
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Functions that subclass needs to implement
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void generateList();

    protected abstract String setToolbarTitle();
}
