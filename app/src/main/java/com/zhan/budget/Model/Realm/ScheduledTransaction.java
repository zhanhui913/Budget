package com.zhan.budget.Model.Realm;

import org.parceler.Parcel;

import io.realm.RealmObject;
import io.realm.ScheduledTransactionRealmProxy;
import io.realm.annotations.PrimaryKey;

@Parcel(implementations = {ScheduledTransactionRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {ScheduledTransaction.class})
public class ScheduledTransaction extends RealmObject {

    @PrimaryKey
    private String id;
    private Transaction transaction;
    private int repeatUnit;
    private String repeatType;

    public ScheduledTransaction() {
    }

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Realm object check equality in terms of property that isnt ignored
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkEquals(ScheduledTransaction other) {
        if(!id.equalsIgnoreCase(other.getId())) return false;
        if(other.getTransaction() != null && transaction != null){
            if(!transaction.checkEquals(other.getTransaction())) return false;
        }

        if(repeatUnit != other.getRepeatUnit()) return false;
        if(!repeatType.equalsIgnoreCase(other.getRepeatType())) return false;

        return true;
    }
}
