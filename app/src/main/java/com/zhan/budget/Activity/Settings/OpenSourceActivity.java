package com.zhan.budget.Activity.Settings;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhan.budget.Activity.BaseActivity;
import com.zhan.budget.Adapter.OpenSourceRecyclerAdapter;
import com.zhan.budget.Model.OpenSource;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.List;

public class OpenSourceActivity extends BaseActivity
        implements OpenSourceRecyclerAdapter.OnOpenSourceInteractionListener{

    private Toolbar toolbar;
    private List<OpenSource> openSourceList;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_open_source;
    }

    @Override
    protected void init(){

        //Remove date icon and cost text view in the right
        ImageView dateIcon = (ImageView)findViewById(R.id.dateIcon);
        TextView totalCost = (TextView)findViewById(R.id.totalCostTextView);
        if(dateIcon != null){
            dateIcon.setVisibility(View.GONE);
        }
        if(totalCost != null){
            totalCost.setVisibility(View.GONE);
        }

        //Set author
        TextView name = (TextView)findViewById(R.id.dateTextView);
        if(name != null){
            name.setText(getString(R.string.author));
        }

        createOpenSourceList();
        createToolbar();
        addListeners();
    }

    private void createOpenSourceList(){
        openSourceList = new ArrayList<>();

        String[] titles = new String[]{"Realm-Java", "CircularViewAndroid", "FlexibleCalendar", "AndroidSwipeLayout", "Android-Ultra-Pull-To-Refresh", "Android-RoundCornerProgressBar", "Parceler", "SmoothProgressBar", "WilliamChart", "MPAndroidChart", "Android-PdfMyXml", "MaterialIntroTutorial", "RecyclerView-FlexibleDivider"};
        String[] authors = new String[]{"realm", "zhanhui913", "p-v", "daimajia", "liaohuqiu", "akexorcist", "johncarl81", "castorflex", "diogobernardino", "PhilJay", "se-bastiaan", "riggaroo", "yqritc"};
        String[] colors = new  String[]{"#FFf39c12", "#FF2980b9", "#970019", "#FF2ecc71", "#FFf5e16e", "#FFc0392b", "#FF2ecc71", "#FFbe90d4", "#FF7f8c8d", "#FFecc62c", "#FF89c4f4", "#FF87d37c", "#FFe76558"};

        for(int i = 0; i < titles.length; i++){
            OpenSource os = new OpenSource();
            os.setName(titles[i]);
            os.setAuthor(authors[i]);
            os.setColor(colors[i]);
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
            getSupportActionBar().setTitle(getString(R.string.app_name));
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
        String url = "https://github.com/" + openSourceList.get(position).getAuthor() + "/" + openSourceList.get(position).getName();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
