package com.zhan.budget.Fragment;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Model.RepeatType;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;
import com.zhan.budget.Util.Util;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class AsyncFragment extends BaseRealmFragment {


    private static final String TAG = "AsyncFragment";

    private RealmAsyncTask realmAsyncTask;

    private TextView indicator;
    private Button startBtn;

    public AsyncFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_async;
    }

    @Override
    protected void init(){ Log.d(TAG, "init");
        super.init();

        startBtn = (Button)view.findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setClickable(false);
                fetchLargeItems();
            }
        });

        indicator = (TextView)view.findViewById(R.id.indicator);
    }

    private void fetchLargeItems(){
        realmAsyncTask = myRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                Transaction tt = new Transaction();
                tt.setDayType(DayType.SCHEDULED.toString());

                int numRepeats = DateUtil.getNumberRepeatInYear(1, "days", 5);

                Date nextDate = DateUtil.refreshDate(new Date());

                for(int i = 0; i < numRepeats; i++){

                    nextDate = DateUtil.getDateWithDirection(nextDate, 1);

                    tt.setPrice(913.00);
                    tt.setId(Util.generateUUID());
                    tt.setDate(nextDate);
                    Log.d(TAG, i+") "+tt.toString());
                    bgRealm.copyToRealmOrUpdate(tt);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // Transaction was a success.
                indicator.setText("SUCCESS");
                startBtn.setClickable(true);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
                indicator.setText("FAILED");
                startBtn.setClickable(true);
            }
        });
    }

    @Override
    public void onStop(){
        if (realmAsyncTask != null && !realmAsyncTask.isCancelled()) {
            realmAsyncTask.cancel();
        }

        super.onStop();
    }
}
