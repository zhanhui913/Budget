package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhan.budget.Model.CategoryIconColor;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.library.CircularView;
import com.zhan.percentview.Model.Slice;
import com.zhan.percentview.PercentView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InfoFragment.OnShareInteractionListener} interface
 * to handle interaction events.
 */
public class InfoFragment extends Fragment {

    private OnShareInteractionListener mListener;

    private View view;
    private PercentView percentView;
    private CircularView circularView;

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
        percentView = (PercentView) view.findViewById(R.id.percentView);

        List<CategoryIconColor> categoryColorList = CategoryUtil.getListOfCategoryColors(getContext());

        List<Slice> sliceList = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            Slice s = new Slice();
            s.setColor(categoryColorList.get(i).getColor());
            s.setWeight((i * 2) + 1);
            sliceList.add(s);
        }
        percentView.setSliceList(sliceList);

        circularView = (CircularView)view.findViewById(R.id.categoryIcon);
        circularView.setCircleColor("#ff8e44ad");
        circularView.setText("Z");
        circularView.setTextColor("#ff00ff00");
        circularView.setTextSizeInDP(50);

        int rid = CategoryUtil.getIconID(getContext(), "c_camera");
        circularView.setIconResource(rid);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShareInteractionListener) {
            mListener = (OnShareInteractionListener) context;
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
    public interface OnShareInteractionListener {
        void onShareInteraction(String value);
    }
}
