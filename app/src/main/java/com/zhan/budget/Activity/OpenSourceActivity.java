package com.zhan.budget.Activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Adapter.OpenSourceRecyclerAdapter;
import com.zhan.budget.Adapter.TransactionRecyclerAdapter;
import com.zhan.budget.Model.OpenSource;
import com.zhan.budget.R;
import com.zhan.budget.Util.Colors;

import java.util.ArrayList;
import java.util.List;

public class OpenSourceActivity extends BaseActivity {

    private Toolbar toolbar;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_open_source;
    }

    @Override
    protected void init(){
        createOpenSourceList();
        createToolbar();
        addListeners();
    }

    private void createOpenSourceList(){
        List<OpenSource> openSourceList = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            OpenSource os = new OpenSource();
            os.setName("Open source "+i);
            os.setColor(Colors.getRandomColorString(this));
            openSourceList.add(os);
        }

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.openSourceListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        OpenSourceRecyclerAdapter adapter = new OpenSourceRecyclerAdapter(this, openSourceList);
        recyclerView.setAdapter(adapter);

        //Add divider
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .marginResId(R.dimen.left_padding_divider, R.dimen.right_padding_divider)
                        .build());
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
            getSupportActionBar().setTitle("Budget");
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
}
