package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zhan.budget.Adapter.CategoryGrid.ColorCategoryRecyclerAdapter;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnColorPickerCategoryFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ColorPickerCategoryFragment extends BaseFragment
        implements ColorCategoryRecyclerAdapter.OnCircularViewAdapterInteractionListener{

    private OnColorPickerCategoryFragmentInteractionListener mListener;

    private static final String ARG_1 = "selectedCategoryColor";

    private RecyclerView colorCategoryGridView;
    private ColorCategoryRecyclerAdapter colorCategoryGridAdapter;

    private String selectedCategoryColor;

    private List<Category> categoryColorList;

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
        categoryColorList = CategoryUtil.getListOfCategoryColors(getContext());

        selectedCategoryColor = getArguments().getString(ARG_1);

        for(int i = 0; i < categoryColorList.size(); i++){
            if(categoryColorList.get(i).getColor().equalsIgnoreCase(selectedCategoryColor)){
                categoryColorList.get(i).setSelected(true);
                Log.d("COLOR_FRAGMENT", "found it at "+i);
                break;
            }
        }

        colorCategoryGridView = (RecyclerView) view.findViewById(R.id.colorGrid);
        colorCategoryGridView.setLayoutManager(new GridLayoutManager(getContext(), 5));

        colorCategoryGridAdapter = new ColorCategoryRecyclerAdapter(this, categoryColorList);
        colorCategoryGridView.setAdapter(colorCategoryGridAdapter);
    }

    @Override
    public void onClick(int position){
        for(int i = 0; i < categoryColorList.size(); i++){
            categoryColorList.get(i).setSelected(false);
        }
        categoryColorList.get(position).setSelected(true);

        colorCategoryGridAdapter.setCategoryList(categoryColorList);
        mListener.onColorCategoryClick(categoryColorList.get(position).getColor());
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
