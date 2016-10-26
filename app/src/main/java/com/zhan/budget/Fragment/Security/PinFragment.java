package com.zhan.budget.Fragment.Security;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.zhan.budget.Fragment.BaseFragment;
import com.zhan.budget.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PinFragment extends BaseFragment {


    private static final String TAG = "PinFragment";

    private  PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;

    public PinFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_security_pin;
    }

    @Override
    protected void init(){
        mPinLockView = (PinLockView) view.findViewById(R.id.pin_lock_view);
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {

            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        });

        mIndicatorDots = (IndicatorDots) view.findViewById(R.id.indicator_dots);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       /* if (context instanceof CalendarFragment.OnCalendarInteractionListener) {
            mListener = (CalendarFragment.OnCalendarInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCalendarInteractionListener");
        }*/

        Log.d(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
       // mListener = null;
        Log.d(TAG, "onDetach");
    }

}
