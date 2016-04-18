package com.xunce.gsmr.model;

import com.xunce.gsmr.util.DBHelper;

import io.realm.RealmObject;

/**
 * 单个数据库工程Item 注意RealmObject类只能定义getter和setter
 * 并且不能跨线程使用Item 所以尽量使用PrjItem
 * 这个类用于取数据即可
 * Created by xingw on 2015/7/18.
 */
public class PrjItemRealmObject extends RealmObject{
    private String prjName;
    private String dbLocation;
    private String creationTime;

    public PrjItemRealmObject(String prjName, String dbLocation, String creationTime) {
        super();
        this.prjName = prjName;
        this.dbLocation = dbLocation;
        this.creationTime = creationTime;
    }

    public PrjItemRealmObject() {
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
}
