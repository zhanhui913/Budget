package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.zhan.budget.Adapter.ColorCategoryGridAdapter;
import com.zhan.budget.Model.CategoryColor;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;

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

    private GridView colorCategoryGridView;
    private ColorCategoryGridAdapter colorCategoryGridAdapter;

    private int selectedCategoryColor;

    private List<CategoryColor> categoryColorList;

    public ColorPickerCategoryFragment() {
        // Required empty public constructor
    }

    public void setSelectedCategoryColor(int selectedCategoryColor){
        this.selectedCategoryColor = selectedCategoryColor;
        Log.d("COLOR_FRAGMENT", "1) selected color is "+selectedCategoryColor);
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
        categoryColorList = CategoryUtil.getListOfCategoryColors(getContext());

        for(int i = 0; i < categoryColorList.size(); i++){
            if(categoryColorList.get(i).getColor() == selectedCategoryColor){
                categoryColorList.get(i).setIsSelected(true);
            }
        }

        colorCategoryGridView = (GridView) view.findViewById(R.id.colorGrid);
        colorCategoryGridAdapter = new ColorCategoryGridAdapter(getContext(), categoryColorList);
        colorCategoryGridView.setAdapter(colorCategoryGridAdapter);
    }

    private void addListeners(){
        colorCategoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i < categoryColorList.size(); i++){
                    categoryColorList.get(i).setIsSelected(false);
                }

                categoryColorList.get(position).setIsSelected(true);
                colorCategoryGridAdapter.notifyDataSetChanged();

                mListener.onColorCategoryClick(categoryColorList.get(position).getColor());
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
        void onColorCategoryClick(int color);
    }
}
