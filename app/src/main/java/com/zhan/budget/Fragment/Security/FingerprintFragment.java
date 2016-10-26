package com.zhan.budget.Fragment.Security;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.zhan.budget.Fragment.BaseFragment;
import com.zhan.budget.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FingerprintFragment extends BaseFragment {

    private static final String TAG = "FingerprintFragment";


    public FingerprintFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_security_fingerprint;
    }

    @Override
    protected void init(){

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Lifecycle
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
      /*  if (context instanceof CalendarFragment.OnCalendarInteractionListener) {
            mListener = (CalendarFragment.OnCalendarInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCalendarInteractionListener");
        }
*/
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
       // mListener = null;
        Log.d(TAG, "onDetach");
    }

}
