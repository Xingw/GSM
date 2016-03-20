package com.xunce.gsmr.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.util.DBHelper;
import com.xunce.gsmr.util.preference.PreferenceHelper;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import io.realm.RealmObject;

/**
 * 单个工程Item
 * Created by ssthouse on 2015/7/18.
 */
public class PrjItem extends RealmObject{
    private static final String TAG = "PrjItem";

    private String prjName;

    private String dbLocation;


    public PrjItem(String prjName) {
        super();
        this.prjName = prjName;
    }

    public PrjItem(String prjName, String dbLocation) {
        super();
        this.prjName = prjName;
        this.dbLocation = dbLocation;
    }

    public PrjItem() {
        super();
    }

//    public List<MarkerItem> getMarkerItemList(){
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

}
