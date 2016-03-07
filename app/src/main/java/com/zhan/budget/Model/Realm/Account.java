package com.zhan.budget.Model.Realm;

import org.parceler.Parcel;

import io.realm.AccountRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {AccountRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Account.class})
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
