package com.zhan.budget.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.zhan.budget.Adapter.MonthReportGridAdapter;
import com.zhan.budget.Model.Parcelable.ParcelableMonthReport;
import com.zhan.budget.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OverviewFragment.OnOverviewInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverviewFragment extends Fragment {

    private OnOverviewInteractionListener mListener;
    private View view;

    private List<ParcelableMonthReport> monthReportList;
    private GridView monthReportGridView;
    private MonthReportGridAdapter monthReportGridAdapter;

    public OverviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OverviewFragment.
     */
    public static OverviewFragment newInstance() {
        OverviewFragment fragment = new OverviewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_overview, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init(){
        monthReportList = new ArrayList<>();
        monthReportGridView = (GridView) view.findViewById(R.id.monthReportGridView);
        monthReportGridAdapter = new MonthReportGridAdapter(getActivity(), monthReportList);
        monthReportGridView.setAdapter(monthReportGridAdapter);

        putFakeData();
    }

    private void putFakeData(){
        Log.d("OVERVIEW_FRAGMENT"," put fake data");
        for(int i = 0; i < 12; i++){

            ParcelableMonthReport pm = new ParcelableMonthReport();
            pm.setMonth(new Date());
            pm.setChangeCost(-50f);
            pm.setCostThisMonth(100);

            monthReportList.add(pm);
            Log.d("OVERVIEW_FRAGMENT", "adding "+i);
        }
        Log.d("OVERVIEW_FRAGMENT", "NotifyData change");
        monthReportGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOverviewInteractionListener) {
            mListener = (OnOverviewInteractionListener) context;
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
    public interface OnOverviewInteractionListener {
        void onOverviewInteraction(String value);
    }
}
