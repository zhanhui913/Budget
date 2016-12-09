package com.zhan.budget.Activity.Settings;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.BaseActivity;
import com.zhan.budget.Adapter.AttributionRecyclerAdapter;
import com.zhan.budget.Model.Attribution;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.List;

public class DeveloperActivity extends BaseActivity
    implements AttributionRecyclerAdapter.OnOpenSourceInteractionListener{

        private Toolbar toolbar;
        private List<Attribution> developerList;

        @Override
        protected int getActivityLayout(){
            return R.layout.activity_developer;
        }

        @Override
        protected void init(){
            createOpenSourceList();
            createToolbar();
            addListeners();
        }

    private void createOpenSourceList(){
        developerList = new ArrayList<>();

        String[] titles = new String[]{"Developer", "Graphic Designer"};
        String[] names = new String[]{"Zhan H. Yap", "Briana Aubrey"};
        String[] colors = new  String[]{ "#FF2980b9", "#FFe76558"};

        for(int i = 0; i < titles.length; i++){
            Attribution dev = new Attribution();
            dev.setName(names[i]);
            dev.setAuthor(titles[i]);
            dev.setColor(colors[i]);
            developerList.add(dev);
        }

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.developerListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AttributionRecyclerAdapter adapter = new AttributionRecyclerAdapter(this, developerList);
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
            getSupportActionBar().setTitle(R.string.setting_title_developer);
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

    @Override
    public void onClick(int position){
        //Intent i = new Intent(Intent.ACTION_VIEW);
        //i.setData(Uri.parse(String.format(gitUrl, openSourceList.get(position).getAuthor(), openSourceList.get(position).getName())));
        //startActivity(i);
    }

}
