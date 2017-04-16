package com.zhan.budget.Activity.Settings.Info;

import com.zhan.budget.Adapter.AttributionRecyclerAdapter;
import com.zhan.budget.Model.Attribution;
import com.zhan.budget.R;

import java.util.ArrayList;

/**
 * Created by Zhan on 2017-04-15.
 */

public class InfoDeveloper extends BaseInfoActivity
        implements AttributionRecyclerAdapter.OnOpenSourceInteractionListener{

    @Override
    protected void generateList(){
        dataList = new ArrayList<>();

        String[] titles = new String[]{"Developer", "Graphic Designer"};
        String[] names = new String[]{"Zhan H. Yap", "Briana Aubrey"};
        String[] colors = new  String[]{ "#FF2980b9", "#FFe76558"};

        for(int i = 0; i < titles.length; i++){
            Attribution dev = new Attribution();
            dev.setName(names[i]);
            dev.setAuthor(titles[i]);
            dev.setColor(colors[i]);
            dataList.add(dev);
        }

        adapter.setAttributionList(dataList);
    }

    @Override
    protected String setToolbarTitle(){
        return getString(R.string.setting_title_developer);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter's interface
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClick(int position){
        //Do nothing
    }
}
