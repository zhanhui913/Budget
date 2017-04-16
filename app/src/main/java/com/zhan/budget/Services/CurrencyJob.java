package com.zhan.budget.Services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

/**
 * Created by zhanyap on 2016-12-23.
 */

public class CurrencyJob extends Job {
    public static final String TAG = "currencyJob";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        // run your job here
        Log.d(TAG, "----------------> starting currency converting here");
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(CurrencyJob.TAG)
                .setExecutionWindow(30_000L, 40_000L)
                .build()
                .schedule();
    }
}
