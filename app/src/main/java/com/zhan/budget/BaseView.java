package com.zhan.budget;

import android.content.Context;

/**
 * Created by Zhan on 2017-04-16.
 */

public interface BaseView<T> {
    void setPresenter(T presenter);

    Context getContext();
}
