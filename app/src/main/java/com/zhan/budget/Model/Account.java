package com.zhan.budget.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Zhan on 16-01-23.
 */
public class Account extends RealmObject{

    @PrimaryKey
    private String id;

    private String name;

    public Account(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
