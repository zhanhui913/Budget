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
import com.zhan.budget.Model.CategoryIconColor;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnColorPickerCategoryFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ColorPickerCategoryFragment extends BaseFragment {

    private OnColorPickerCategoryFragmentInteractionListener mListener;

    private static final String ARG_1 = "selectedCategoryColor";

    private GridView colorCategoryGridView;
    private ColorCategoryGridAdapter colorCategoryGridAdapter;

    private String selectedCategoryColor;

    private List<CategoryIconColor> categoryIconColorList;

    public ColorPickerCategoryFragment() {
        // Required empty public constructor
    }

    public static ColorPickerCategoryFragment newInstance(String selectedCategoryColor) {
        ColorPickerCategoryFragment fragment = new ColorPickerCategoryFragment();

        Bundle args = new Bundle();
        args.putString(ARG_1, selectedCategoryColor);
        fragment.setArguments(args);

        Log.d("COLOR_FRAGMENT", "1) selected color is " + selectedCategoryColor);

        return fragment;
    }

    public void setSelectedCategoryColor(String selectedCategoryColor){
        this.selectedCategoryColor = selectedCategoryColor;
        Log.d("COLOR_FRAGMENT", "1) selected color is " + selectedCategoryColor);
    }

    @Override
    protected int getFragmentLayout() {
    return R.layout.fragment_color_picker_category;
}

    @Override
    protected void init(){
        categoryIconColorList = CategoryUtil.getListOfCategoryColors(getContext());

        selectedCategoryColor = getArguments().getString(ARG_1);

        for(int i = 0; i < categoryIconColorList.size(); i++){
            if(categoryIconColorList.get(i).getColor().equalsIgnoreCase(selectedCategoryColor)){
                categoryIconColorList.get(i).setIsSelected(true);
                Log.d("COLOR_FRAGMENT", "found it at "+i);
                break;
            }
        }

        colorCategoryGridView = (GridView) view.findViewById(R.id.colorGrid);
        colorCategoryGridAdapter = new ColorCategoryGridAdapter(getContext(), categoryIconColorList);
        colorCategoryGridView.setAdapter(colorCategoryGridAdapter);

        addListeners();
    }

    private void addListeners(){
        colorCategoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i < categoryIconColorList.size(); i++){
                    categoryIconColorList.get(i).setIsSelected(false);
                }

                categoryIconColorList.get(position).setIsSelected(true);
                colorCategoryGridAdapter.notifyDataSetChanged();

                mListener.onColorCategoryClick(categoryIconColorList.get(position).getColor());
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
