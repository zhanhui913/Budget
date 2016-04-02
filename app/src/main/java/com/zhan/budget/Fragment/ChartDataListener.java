package com.zhan.budget.Fragment;

import com.zhan.budget.Model.Realm.Category;

import java.util.List;

/**
 * Created by zhanyap on 2016-04-02.
 */
public interface ChartDataListener {
    void setData(List<Category> categoryList);
}
