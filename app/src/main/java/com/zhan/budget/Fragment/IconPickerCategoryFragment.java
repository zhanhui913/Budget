package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import com.zhan.budget.Adapter.IconCategoryGridAdapter;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.circularview.CircularView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IconPickerCategoryFragment.OnIconPickerCategoryFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class IconPickerCategoryFragment extends Fragment {

    private OnIconPickerCategoryFragmentInteractionListener mListener;

    private View view;

    private List<Integer> iconList;
    private GridView iconCategoryGridView;
    private IconCategoryGridAdapter iconCategoryGridAdapter;

    private int selectedColor;
    private int selectedCategoryIcon;

    public IconPickerCategoryFragment() {
        // Required empty public constructor
    }

    public void setSelectedCategoryIcon(int selectedCategoryIcon){
        this.selectedCategoryIcon = selectedCategoryIcon;
        Log.d("ICON_FRAGMENT", "1) selected icon is "+selectedCategoryIcon);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_icon_picker_category, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
        listenToIconGridView();
        addListeners();
    }

    private void init(){
        iconList = CategoryUtil.getListOfUniqueIcon(getContext());
        iconCategoryGridView = (GridView) view.findViewById(R.id.iconGrid);
        iconCategoryGridAdapter = new IconCategoryGridAdapter(getContext(), iconList, selectedColor);
        iconCategoryGridView.setAdapter(iconCategoryGridAdapter);
        Log.d("ICON_PICKER_CATEGORY", "init");
    }

    private void listenToIconGridView(){
        iconCategoryGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        int k=0;
                        for(int i = 0; i < iconList.size(); i++){
                            if(iconList.get(i) == selectedCategoryIcon){
                                Log.d("COLOR_FRAGMENT", "found icon at index:"+i);
                                k = i;
                            }
                        }

                        ViewGroup gridChild = (ViewGroup) iconCategoryGridView.getChildAt(k);
                        CircularView cv = (CircularView) gridChild.findViewById(R.id.categoryIcon);
                        cv.setStrokeColor(R.color.darkgray);

                        // unregister listener (this is important)
                        iconCategoryGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }

    private void addListeners(){
        iconCategoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View childView = parent.getChildAt(i);
                    CircularView ccv = (CircularView) (childView.findViewById(R.id.categoryIcon));
                    ccv.setStrokeColor(R.color.transparent);

                    if (i == position) {
                        ccv.setStrokeColor(R.color.darkgray);
                    }
                }

                mListener.onIconCategoryClick(iconList.get(position));
            }
        });
    }

    public void updateColor(int color){
        Log.d("ICON_PICKER_CATEGORY", "updating color to "+color);
        selectedColor = color;

        if(iconCategoryGridAdapter != null){
            iconCategoryGridAdapter.updateColor(selectedColor);
        }else{
            Log.d("ICON_PICKER_CATEGORY", "iconCategoryGridAdapter is null");
        }
    }

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
        void onIconCategoryClick(int icon);
    }
}
