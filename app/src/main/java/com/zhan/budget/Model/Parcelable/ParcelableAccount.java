package com.zhan.budget.Model.Parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import com.zhan.budget.Model.Account;

/**
 * Created by Zhan on 16-01-24.
 */
public class ParcelableAccount implements Parcelable{

    private String id;
    private String name;

    public ParcelableAccount(){

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

    public void convertAccountToParcelable(Account account){
        this.id = account.getId();
        this.name = account.getName();
    }

    public Account convertParcelableToAccount(){
        Account account = new Account();
        account.setId(this.id);
        account.setName(this.name);
        return account;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Parcelable
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(id);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<ParcelableAccount> CREATOR = new Parcelable.Creator<ParcelableAccount>() {

        @Override
        public ParcelableAccount createFromParcel(Parcel source) {
            return new ParcelableAccount(source);
        }

        @Override
        public ParcelableAccount[] newArray(int size) {
            return new ParcelableAccount[size];
        }
    };

    private ParcelableAccount(Parcel in){
        id = in.readString();
        name = in.readString();
    }
}
