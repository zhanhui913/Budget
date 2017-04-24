package com.zhan.budget.Data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zhan.budget.Util.Util;

/**
 * Enables injection of mock implementations for
 * {@link DataManager} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    public static AppDataManager provideAppDataManager(@NonNull Context context) {
        Util.checkNotNull(context);
        return AppDataManager.getInstance(context);
    }
}
