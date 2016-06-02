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

import com.zhan.budget.Adapter.IconCategoryGridAdapter;
import com.zhan.budget.Model.CategoryIconColor;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IconPickerCategoryFragment.OnIconPickerCategoryFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class IconPickerCategoryFragment extends BaseFragment {

    private OnIconPickerCategoryFragmentInteractionListener mListener;

    private static final String ARG_1 = "selectedCategoryIcon";
    private static final String ARG_2 = "selectedCategoryColor";

    private List<CategoryIconColor> categoryIconColorList;
    private GridView iconCategoryGridView;
    private IconCategoryGridAdapter iconCategoryGridAdapter;

    private String selectedColor;
    private String selectedCategoryIcon;

    public IconPickerCategoryFragment() {
        // Required empty public constructor
    }

    public static IconPickerCategoryFragment newInstance(String selectedCategoryIcon, String selectedColor) {
        IconPickerCategoryFragment fragment = new IconPickerCategoryFragment();

        Bundle args = new Bundle();
        args.putString(ARG_1, selectedCategoryIcon);
        args.putString(ARG_2, selectedColor);
        fragment.setArguments(args);

        Log.d("ICON_FRAGMENT", "1) selected icon is " + selectedCategoryIcon);
        Log.d("ICON_FRAGMENT", "1) selected color is "+selectedColor);

        return fragment;
    }

    public void setSelectedCategoryIcon(String selectedCategoryIcon){
        this.selectedCategoryIcon = selectedCategoryIcon;
        Log.d("ICON_FRAGMENT", "1) selected icon is "+selectedCategoryIcon);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_icon_picker_category;
    }

    @Override
    protected void init(){
        categoryIconColorList = CategoryUtil.getListOfUniqueIcons(getContext());

        selectedCategoryIcon = getArguments().getString(ARG_1);
        selectedColor = getArguments().getString(ARG_2);

        for(int i = 0; i < categoryIconColorList.size(); i++){
            if(categoryIconColorList.get(i).getIcon().equalsIgnoreCase(selectedCategoryIcon)){
                categoryIconColorList.get(i).setIsSelected(true);
                break;
            }
        }

        iconCategoryGridView = (GridView) view.findViewById(R.id.iconGrid);
        iconCategoryGridAdapter = new IconCategoryGridAdapter(getContext(), categoryIconColorList, selectedColor);
        iconCategoryGridView.setAdapter(iconCategoryGridAdapter);
        Log.d("ICON_PICKER_CATEGORY", "init");

        addListeners();
    }

    private void addListeners(){
        iconCategoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i < categoryIconColorList.size(); i++){
                    categoryIconColorList.get(i).setIsSelected(false);
                }

                categoryIconColorList.get(position).setIsSelected(true);
                iconCategoryGridAdapter.notifyDataSetChanged();

                mListener.onIconCategoryClick(categoryIconColorList.get(position).getIcon());
            }
        });
    }

    public void updateColor(String color){
        Log.d("ICON_PICKER_CATEGORY", "updating color to "+color);
        selectedColor = color;

        if(iconCategoryGridAdapter != null){
            iconCategoryGridAdapter.updateColor(selectedColor);
        }else{
            Log.d("ICON_PICKER_CATEGORY", "iconCategoryGridAdapter is null");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIconPickerCategoryFragmentInteractionListener) {
            mListener = (OnIconPickerCategoryFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
    public interface OnIconPickerCategoryFragmentInteractionListener {
        void onIconCategoryClick(String icon);
    }
}
