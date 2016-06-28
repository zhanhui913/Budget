package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zhan.budget.Adapter.CategoryGrid.IconCategoryRecyclerAdapter;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.View.SpacesItemDecoration;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IconPickerCategoryFragment.OnIconPickerCategoryFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class IconPickerCategoryFragment extends BaseFragment
    implements IconCategoryRecyclerAdapter.OnCategoryGridAdapterInteractionListener{

    private static final int NUM_COLUMNS = 5;

    private OnIconPickerCategoryFragmentInteractionListener mListener;

    private static final String ARG_1 = "selectedCategoryIcon";
    private static final String ARG_2 = "selectedCategoryColor";

    private List<Category> categoryIconList;
    private RecyclerView iconCategoryGridView;
    private IconCategoryRecyclerAdapter iconCategoryGridAdapter;

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
        return R.layout.fragment_category_grid;
    }

    @Override
    protected void init(){
        categoryIconList = CategoryUtil.getListOfUniqueIcons(getContext());

        selectedCategoryIcon = getArguments().getString(ARG_1);
        selectedColor = getArguments().getString(ARG_2);

        for(int i = 0; i < categoryIconList.size(); i++){
            if(categoryIconList.get(i).getIcon().equalsIgnoreCase(selectedCategoryIcon)){
                categoryIconList.get(i).setSelected(true);
                break;
            }
        }

        iconCategoryGridView = (RecyclerView) view.findViewById(R.id.categoryGrid);
        iconCategoryGridView.setLayoutManager(new GridLayoutManager(getContext(), NUM_COLUMNS));

        //Add padding
        iconCategoryGridView.addItemDecoration(new SpacesItemDecoration(getContext(), R.dimen.grid_view_horizontal_offset, R.dimen.grid_view_vertical_offset));

        iconCategoryGridAdapter = new IconCategoryRecyclerAdapter(this, categoryIconList, selectedColor);
        iconCategoryGridView.setAdapter(iconCategoryGridAdapter);
        Log.d("ICON_PICKER_CATEGORY", "init");
    }

    @Override
    public void onClick(int position){
        for(int i = 0; i < categoryIconList.size(); i++){
            categoryIconList.get(i).setSelected(false);
        }
        categoryIconList.get(position).setSelected(true);

        iconCategoryGridAdapter.setCategoryList(categoryIconList);

        mListener.onIconCategoryClick(categoryIconList.get(position).getIcon());
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
