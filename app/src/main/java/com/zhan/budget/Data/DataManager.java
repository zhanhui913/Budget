package com.zhan.budget.Data;

import com.zhan.budget.Data.Prefs.PreferenceHelper;
import com.zhan.budget.Data.Realm.RealmHelper;

/**
 * Created by zhanyap on 2017-04-19.
 */

public interface DataManager extends RealmHelper, RealmHelper.LoadTransactionsForDayCallback, PreferenceHelper{
}
