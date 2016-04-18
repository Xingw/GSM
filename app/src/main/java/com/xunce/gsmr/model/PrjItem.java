package com.xunce.gsmr.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.xunce.gsmr.util.DBHelper;

import io.realm.Realm;

/**
 * Created by Xingw on 2016/3/26.
 */
public class PrjItem implements Parcelable {
    private String prjName;
    private String dbLocation;
    private String creationTime;

    public PrjItem(String prjName, String dbLocation, String creationTime) {
        super();
        this.prjName = prjName;
        this.dbLocation = dbLocation;
        this.creationTime = creationTime;
    }

    public PrjItem() {
        super();
    }

//    public List<MarkerItem> getMarkerOnDbList(){
//        return new Select().from(MarkerItem.class)
//                .where("prjName = "+ "'"+prjName+"'")
//                .execute();
//    }


//    /**
//     * 删除一个PrjItem的所有数据
//     */
//    public void deletePrj(Context context){
//        //首先要判断Preference中保存的是不是当前工程
//        //如果是要删除Preference
//        if(PreferenceHelper.getInstance(context).getLastEditPrjName(context).equals(getPrjName())){
//            PreferenceHelper.getInstance(context).deleteLastEditPrjName(context);
//        }
//        realm.beginTransaction();
//        this.removeFromRealm();
//        realm.commitTransaction();
//    }

    protected PrjItem(Parcel in) {
        prjName = in.readString();
        dbLocation = in.readString();
        creationTime = in.readString();
    }

    public static final Creator<PrjItem> CREATOR = new Creator<PrjItem>() {
        @Override
        public PrjItem createFromParcel(Parcel in) {
            return new PrjItem(in);
        }

        @Override
        public PrjItem[] newArray(int size) {
            return new PrjItem[size];
        }
    };

    //getter-----------and---------------setter---------
    public String getPrjName() {
        return prjName;
    }

    public void setPrjName(String prjName) {
        this.prjName = prjName;
    }

    public String getDbLocation() {
        return dbLocation;
    }

    public void setDbLocation(String dbLocation) {
        this.dbLocation = dbLocation;
    }

    public String getCreationTime() {
        return creationTime;
    }
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(prjName);
        dest.writeString(dbLocation);
        dest.writeString(creationTime);
    }


    public PrjItemRealmObject toRealmObject(Realm realm) {
        return DBHelper.getPrjItemByName(realm,prjName);
    }
}
