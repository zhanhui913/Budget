package com.zhan.budget.Activity.Settings.Info;

import android.content.Intent;
import android.net.Uri;

import com.zhan.budget.Adapter.AttributionRecyclerAdapter;
import com.zhan.budget.Model.Attribution;
import com.zhan.budget.R;

import java.util.ArrayList;

/**
 * Created by Zhan on 2017-04-15.
 */

public class InfoOpenSource extends BaseInfoActivity
        implements AttributionRecyclerAdapter.OnOpenSourceInteractionListener{

    private String gitUrl = "https://github.com/%s/%s";

    @Override
    protected void generateList(){
        dataList = new ArrayList<>();

        String[] titles = new String[]{"Realm-Java", "CircularViewAndroid", "FlexibleCalendar", "AndroidSwipeLayout", "Android-Ultra-Pull-To-Refresh", "Android-RoundCornerProgressBar", "SmoothProgressBar","Parceler"  , "MPAndroidChart", "Android-PdfMyXml", "MaterialIntroTutorial", "RecyclerView-FlexibleDivider", "Okhttp"   , "Android-job"};
        String[] authors = new String[]{"realm"    , "zhanhui913"         , "p-v"             , "daimajia"          , "liaohuqiu"                    , "akexorcist"                    , "castorflex"       ,"johncarl81", "PhilJay"       , "se-bastiaan"     , "riggaroo"             , "yqritc"                      , "square"   , "evernote"};
        String[] colors = new  String[]{"#FFf39c12", "#FF2980b9"          , "#970019"         , "#FF2ecc71"         , "#FFf5e16e"                    , "#FFc0392b"                     , "#FFbe90d4"        ,"#FF2ecc71" , "#FFecc62c"     , "#FF89c4f4"       , "#FF87d37c"            , "#FFe76558"                   , "#FFf39c12", "#03A9F4"};

        for(int i = 0; i < titles.length; i++){
            Attribution os = new Attribution();
            os.setName(titles[i]);
            os.setAuthor(authors[i]);
            os.setColor(colors[i]);
            dataList.add(os);
        }

        adapter.setAttributionList(dataList);
    }

    @Override
    protected String setToolbarTitle(){
        return getString(R.string.setting_title_open_source);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Adapter's interface
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClick(int position){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(String.format(gitUrl, dataList.get(position).getAuthor(), dataList.get(position).getName())));
        startActivity(i);
    }
}