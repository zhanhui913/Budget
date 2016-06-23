package com.zhan.budget.Etc;

import android.content.Context;

import com.zhan.budget.Model.Location;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;

/**
 * Created by zhanyap on 2016-06-23.
 */
public final class LocationRealmManager {

    public enum Status{
        ADD,    //If a new tuple has been added
        REMOVE, //If a tuple has been removed
        UPDATE, //If a tuple has its count value updated
        FAIL    //Failed
    }

    private LocationRealmManager(){}

    /**
     * Add a location to Realm, checks if a new Location tuple is needed or just update its count.
     * @param context Context
     * @param locationName new location name
     * @param mListener LocationRealmManagerInteractionListener
     */
    public static void addLocation(final Context context, final String locationName, final LocationRealmManagerInteractionListener mListener){
        final Realm myRealm = Realm.getDefaultInstance();
        final RealmResults<Location> locationRealmResults = myRealm.where(Location.class).findAllSortedAsync("name");
        locationRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Location>>() {
            @Override
            public void onChange(RealmResults<Location> element) {
                element.removeChangeListener(this);

                boolean foundExistingLocation = false;

                myRealm.beginTransaction();
                for(int i = 0; i < locationRealmResults.size(); i++){
                    if(locationRealmResults.get(i).getName().equalsIgnoreCase(locationName)){
                        foundExistingLocation = true;
                        locationRealmResults.get(i).setAmount(locationRealmResults.get(i).getAmount() + 1);
                        myRealm.commitTransaction();
                        myRealm.close();
                        mListener.onResult(Status.UPDATE);
                    }
                }

                if(!foundExistingLocation){
                    Location newLocation = new Location();
                    newLocation.setId(Util.generateUUID());
                    newLocation.setName(locationName);
                    newLocation.setAmount(1);
                    newLocation.setColor(Colors.getRandomColorString(context));
                    myRealm.copyToRealm(newLocation);
                    myRealm.commitTransaction();
                    myRealm.close();
                    mListener.onResult(Status.ADD);
                }
            }
        });
    }

    /**
     * Remove a location from Realm; true if successfully remove, false if not.
     * False when location value doesnt exist in table.
     * @param context Context
     * @param locationName location name to delete
     * @param mListener LocationRealmManagerInteractionListener
     */
    public static void removeLocation(final Context context, final String locationName, final LocationRealmManagerInteractionListener mListener){
        final Realm myRealm = Realm.getDefaultInstance();
        final Location locationRealmResult = myRealm.where(Location.class).equalTo("name", locationName).findFirstAsync();
        locationRealmResult.addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel element) {
                locationRealmResult.removeChangeListener(this);

                if(locationRealmResult.isLoaded()){ //not sure if this if statement is needed
                    if(locationRealmResult.isValid()){ //found one

                        myRealm.beginTransaction();

                        //check its count value
                        if(locationRealmResult.getAmount() > 1){
                            //Update its count by removing 1
                            locationRealmResult.setAmount(locationRealmResult.getAmount() - 1);
                            myRealm.commitTransaction();
                            myRealm.close();
                            mListener.onResult(Status.UPDATE);
                        }else{
                            //Remove the tuple completely
                            locationRealmResult.deleteFromRealm();
                            myRealm.commitTransaction();
                            myRealm.close();
                            mListener.onResult(Status.REMOVE);
                        }
                    }else{ //found none
                        mListener.onResult(Status.FAIL);
                    }
                }
            }
        });
    }


    public interface LocationRealmManagerInteractionListener{
        void onResult(Status result);
    }
}
