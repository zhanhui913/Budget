package com.zhan.budget.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhan.budget.Model.MetaMission;
import com.zhan.budget.R;

import java.util.List;


/**
 * Created by zhanyap on 15-08-19.
 */
public class MissionListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<MetaMission> listMission;

    public MissionListAdapter(Activity activity, List<MetaMission> listMission) {
        this.activity = activity;
        this.listMission = listMission;
    }

    @Override
    public int getCount() {
        return this.listMission.size();
    }

    @Override
    public Object getItem(int location) {
        return listMission.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.mission_item, null);

        TextView name = (TextView) convertView.findViewById(R.id.missionName);
        TextView date = (TextView) convertView.findViewById(R.id.missionDescription);

        // getting mission data for the row
        MetaMission ms = listMission.get(position);

        // Name
        name.setText(ms.getName());

        // Description
        date.setText(ms.getDescription());

        return convertView;
    }
}
