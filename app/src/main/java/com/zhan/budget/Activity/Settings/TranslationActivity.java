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

public class TranslationActivity extends BaseActivity
        implements AttributionRecyclerAdapter.OnOpenSourceInteractionListener{

    private Toolbar toolbar;
    private List<Attribution> translationList;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_translation;
    }

    @Override
    protected void init(){
        createTranslationList();
        createToolbar();
        addListeners();
    }

    private void createTranslationList(){
        translationList = new ArrayList<>();

        String[] translationLanguage = new String[]{"English", "Spanish", "French"};
        String[] translationAuthor = new String[]{"Zhan H. Yap", "Fernando de la Cruz Soto", "TAIWO Azeez Abiodun"};
        String[] translationColors = new String[]{"#FF2980b9", "#FFc0392b", "#FF7f8c8d"};

        for(int i = 0; i < translationLanguage.length; i++){
            Attribution translation = new Attribution();
            translation.setName(translationLanguage[i]);
            translation.setAuthor(translationAuthor[i]);
            translation.setColor(translationColors[i]);
            translationList.add(translation);
        }

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.translationListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AttributionRecyclerAdapter adapter = new AttributionRecyclerAdapter(this, translationList);
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
            getSupportActionBar().setTitle(R.string.setting_title_translation);
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
        //Do nothing
    }

}
