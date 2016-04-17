package com.xunce.gsmr.model.baidumap.graph;

/**
 * Created by Xingw on 2016/4/14.
 */
public class Point {
    /**
     * 经纬度
     */
    private double longitude;
    private double latitude;

    /**
     * 构造方法
     * @param longitude
     * @param latitude
     */
    public Point(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    //getter----and----setter-----------------------------------------
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
