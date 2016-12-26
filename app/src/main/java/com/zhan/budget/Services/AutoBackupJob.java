package com.zhan.budget.Services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.DataBackup;
import com.zhan.budget.Util.DateUtil;

import java.util.Date;

/**
 * Created by zhanyap on 2016-12-23.
 */

public class AutoBackupJob extends Job {

    public static final String TAG = "autoBackupJob";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        // run your job here
        Log.d(TAG, "----------------> starting auto backup here");

        String dateString = DateUtil.convertDateToStringFormat7(getContext(), new Date());
        BudgetPreference.setLastBackup(getContext(), dateString);

        if(DataBackup.backUpData()){
            return Result.SUCCESS;
        }else {
            return Result.RESCHEDULE;
        }
    }

    public static int scheduleJob() {
        return new JobRequest.Builder(AutoBackupJob.TAG)
                .setPeriodic(JobRequest.MIN_INTERVAL, JobRequest.MIN_FLEX)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .setPersisted(true)
                .build()
                .schedule();
    }


}
