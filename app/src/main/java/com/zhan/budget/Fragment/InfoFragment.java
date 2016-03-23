package com.zhan.budget.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.zhan.budget.R;
import com.zhan.budget.View.TintLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {

    private View view;

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init(){
        final TintLayout tintLayout = (TintLayout) view.findViewById(R.id.tint_layout);
        SeekBar slider = (SeekBar) view.findViewById(R.id.tintSeekbar);
        tintLayout.setAngle(90);

        slider.setMax(360);
        slider.setProgress(90);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tintLayout.setAngle(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });






        final TintLayout tintLayout1 = (TintLayout) view.findViewById(R.id.tint_layout1);
        SeekBar slider1 = (SeekBar) view.findViewById(R.id.tintSeekbar1);
        tintLayout1.setAngle(90);

        slider1.setMax(360);
        slider1.setProgress(90);
        slider1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tintLayout1.setAngle(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
