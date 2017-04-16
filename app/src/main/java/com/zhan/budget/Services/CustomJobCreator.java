package com.zhan.budget.Services;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by zhanyap on 2016-12-23.
 */

public class CustomJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case CurrencyJob.TAG:
                return new CurrencyJob();
            default:
                return null;
        }
    }
}
