package com.zhan.budget.Activity.Settings;

import android.content.Intent;
import android.net.Uri;
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

public class OpenSourceActivity extends BaseActivity
        implements AttributionRecyclerAdapter.OnOpenSourceInteractionListener{

    private Toolbar toolbar;
    private List<Attribution> openSourceList;
    private String gitUrl = "https://github.com/%s/%s";

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
        openSourceList = new ArrayList<>();

        String[] titles = new String[]{"Realm-Java", "CircularViewAndroid", "FlexibleCalendar", "AndroidSwipeLayout", "Android-Ultra-Pull-To-Refresh", "Android-RoundCornerProgressBar", "SmoothProgressBar","Parceler"  , "MPAndroidChart", "Android-PdfMyXml", "MaterialIntroTutorial", "RecyclerView-FlexibleDivider", "Okhttp"   , "Android-job"};
        String[] authors = new String[]{"realm"    , "zhanhui913"         , "p-v"             , "daimajia"          , "liaohuqiu"                    , "akexorcist"                    , "castorflex"       ,"johncarl81", "PhilJay"       , "se-bastiaan"     , "riggaroo"             , "yqritc"                      , "square"   , "evernote"};
        String[] colors = new  String[]{"#FFf39c12", "#FF2980b9"          , "#970019"         , "#FF2ecc71"         , "#FFf5e16e"                    , "#FFc0392b"                     , "#FFbe90d4"        ,"#FF2ecc71" , "#FFecc62c"     , "#FF89c4f4"       , "#FF87d37c"            , "#FFe76558"                   , "#FFf39c12", "#03A9F4"};

        for(int i = 0; i < titles.length; i++){
            Attribution os = new Attribution();
            os.setName(titles[i]);
            os.setAuthor(authors[i]);
            os.setColor(colors[i]);
            openSourceList.add(os);
        }

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.openSourceListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AttributionRecyclerAdapter adapter = new AttributionRecyclerAdapter(this, openSourceList);
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
            getSupportActionBar().setTitle(R.string.setting_title_open_source);
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
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(String.format(gitUrl, openSourceList.get(position).getAuthor(), openSourceList.get(position).getName())));
        startActivity(i);
    }
}
