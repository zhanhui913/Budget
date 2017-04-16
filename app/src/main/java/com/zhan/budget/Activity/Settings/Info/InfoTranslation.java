package com.zhan.budget.Activity.Settings.Info;

import com.zhan.budget.Adapter.AttributionRecyclerAdapter;
import com.zhan.budget.Model.Attribution;
import com.zhan.budget.R;

import java.util.ArrayList;

/**
 * Created by Zhan on 2017-04-15.
 */

public class InfoTranslation extends BaseInfoActivity
        implements AttributionRecyclerAdapter.OnOpenSourceInteractionListener{

    @Override
    protected void generateList(){
        dataList = new ArrayList<>();

        String[] translationLanguage = new String[]{"English"/*, "Spanish", "French"*/};
        String[] translationAuthor = new String[]{"Zhan H. Yap"/*, "Fernando de la Cruz Soto", "TAIWO Azeez Abiodun"*/};
        String[] translationColors = new String[]{"#FF2980b9"/*, "#FFc0392b", "#FF7f8c8d"*/};

        for(int i = 0; i < translationLanguage.length; i++){
            Attribution translation = new Attribution();
            translation.setName(translationLanguage[i]);
            translation.setAuthor(translationAuthor[i]);
            translation.setColor(translationColors[i]);
            dataList.add(translation);
        }

        adapter.setAttributionList(dataList);
    }

    @Override
    protected String setToolbarTitle(){
        return getString(R.string.setting_title_translation);
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
