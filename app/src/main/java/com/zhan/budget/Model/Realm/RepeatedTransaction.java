package com.zhan.budget.Model.Realm;

import org.parceler.Parcel;

import io.realm.RealmObject;
import io.realm.RepeatedTransactionRealmProxy;

@Parcel(implementations = {RepeatedTransactionRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {RepeatedTransaction.class})
public class RepeatedTransaction extends RealmObject {

    private String id;
    private Transaction transaction;
    private int repeatUnit;
    private String repeatType;

    public RepeatedTransaction(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public int getRepeatUnit() {
        return repeatUnit;
    }

    public void setRepeatUnit(int repeatUnit) {
        this.repeatUnit = repeatUnit;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }
}
