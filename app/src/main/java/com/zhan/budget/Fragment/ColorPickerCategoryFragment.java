package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.zhan.budget.Adapter.ColorCategoryGridAdapter;
import com.zhan.budget.R;
import com.zhan.circularview.CircularView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnColorPickerCategoryFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ColorPickerCategoryFragment extends Fragment {

    private OnColorPickerCategoryFragmentInteractionListener mListener;
    private View view;

    private List<Integer> colorList;
    private GridView colorCategoryGridView;
    private ColorCategoryGridAdapter colorCategoryGridAdapter;

    public ColorPickerCategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_color_picker_category, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
        addListeners();
    }

    private void init(){
        int[] colorArray = getResources().getIntArray(R.array.category_colors);

        colorList = new ArrayList<>();
        for(int i = 0; i < colorArray.length; i++){
            colorList.add(colorArray[i]);
        }

        colorCategoryGridView = (GridView) view.findViewById(R.id.colorGrid);
        colorCategoryGridAdapter = new ColorCategoryGridAdapter(getContext(), colorList);
        colorCategoryGridView.setAdapter(colorCategoryGridAdapter);
    }

    private void addListeners(){
        colorCategoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View childView = parent.getChildAt(i);
                    CircularView ccv = (CircularView) (childView.findViewById(R.id.categoryIcon));
                    ccv.setStrokeColor(ContextCompat.getColor(getActivity(), R.color.transparent));

                    if (i == position) {
                        ccv.setStrokeColor(ContextCompat.getColor(getActivity(), R.color.darkgray));
                    }
                }

                mListener.onColorCategoryClick(String.format("#%06X", 0xFFFFFF & colorList.get(position)));
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnColorPickerCategoryFragmentInteractionListener) {
            mListener = (OnColorPickerCategoryFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnColorCateogyrFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnColorPickerCategoryFragmentInteractionListener {
        void onColorCategoryClick(String color);
    }
}
