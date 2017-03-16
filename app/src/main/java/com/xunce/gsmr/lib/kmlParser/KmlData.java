package com.xunce.gsmr.lib.kmlParser;

import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TextOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.xunce.gsmr.util.gps.PositionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Kml文件解析出的数据
 * Created by ssthouse on 2015/11/25.
 */
public class KmlData {
    private String name;
    private String longitude;
    private String latitude;
    private String styleUrl;

    public static final String POLY_STYLE = "#polystyle";
    public static final String TEXT_STYLE = "#msn_shaded_dot";

    /**
     * 蝴蝶形状的所有点
     */
    private List<GpsPoint> pointList = new ArrayList<>();


    /**
     * 在地图上画出多边形
     *
     * @param amap
     */
    public void draw(AMap amap) {
        if (styleUrl.equals(POLY_STYLE)) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.width(8)
                    .color(Color.GREEN);
            for (GpsPoint polyCoordinates : pointList) {
                polylineOptions.add(polyCoordinates.getLatLng());
            }
            amap.addPolyline(polylineOptions);
        } else if (styleUrl.equals(TEXT_STYLE)) {
            TextOptions textOptions = new TextOptions();
            textOptions.fontSize(12)
                    .position(pointList.get(0).getLatLng())
                    .fontColor(Color.BLACK)
                    .text(name);
            amap.addText(textOptions);
        }
    }

        /**
         * 在地图上画出多边形
         *
         * @param baidumap
         */
    public void draw(BaiduMap baidumap) {
        if (styleUrl.equals(POLY_STYLE)) {
            com.baidu.mapapi.map.PolylineOptions polylineOptions = new com.baidu.mapapi.map.PolylineOptions();
            polylineOptions.width(8)
                    .color(Color.GREEN);
            List<LatLng> Latlnglist = new ArrayList<>();
            for (GpsPoint polyCoordinates : pointList) {
                Latlnglist.add(polyCoordinates.getBaiduLatLng());
            }
            polylineOptions.points(Latlnglist);
            baidumap.addOverlay(polylineOptions);
        } else if (styleUrl.equals(TEXT_STYLE)) {
            com.baidu.mapapi.map.TextOptions textOptions = new com.baidu.mapapi.map.TextOptions();
            textOptions.fontSize(12)
                    .position(pointList.get(0).getBaiduLatLng())
                    .fontColor(Color.BLACK)
                    .text(name);
            baidumap.addOverlay(textOptions);
        }
        //查看所有数据点
//        for (GpsPoint polyCoordinates : pointList) {
//            Timber.e(polyCoordinates + "\n");
//        }
    }

    @Override
    public String toString() {
        return name + ":" + longitude + ":" + latitude + "\n" + "我有的点数目为:\t" + pointList.size();
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStyleUrl() {
        return styleUrl;
    }

    public void setStyleUrl(String styleUrl) {
        this.styleUrl = styleUrl;
    }

    public List<GpsPoint> getPointList() {
        return pointList;
    }

    public void setPointList(List<GpsPoint> pointList) {
        this.pointList = pointList;
    }

    public com.baidu.mapapi.model.LatLng getBaiduLatLng(){
        return PositionUtil.Gps84_To_bd09(Double.parseDouble(getLatitude()),Double.parseDouble(getLongitude()));
    }

    public com.amap.api.maps.model.LatLng getGaodeLatLng(){
        return PositionUtil.gps84_To_Gcj02(Double.parseDouble(getLatitude()),Double.parseDouble(getLongitude()));
    }
}
