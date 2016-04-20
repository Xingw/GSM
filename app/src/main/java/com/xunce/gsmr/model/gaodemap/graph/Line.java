package com.xunce.gsmr.model.gaodemap.graph;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

/**
 * 高德地图的Line Created by ssthouse on 2015/9/15.
 */
public class Line extends BaseGraph {
    //直线的参数
    private static int lineColor = 0xAAFF0000;
    private static int lineWidth = 10;
    private boolean show = true;
    /**
     * 坐标点
     */
    private LatLng latLngBegin;
    private LatLng latLngEnd;
    private String layerName;
    private Polyline polyline;

    /**
     * 构造方法
     *
     * @param latLngBegin
     * @param latLngEnd
     */
    public Line(LatLng latLngBegin, LatLng latLngEnd,String layerName) {
        this.latLngBegin = latLngBegin;
        this.latLngEnd = latLngEnd;
        this.layerName = layerName;
    }
    /**
     * 构造方法
     *
     * @param latLngBegin
     * @param latLngEnd
     */
    public Line(LatLng latLngBegin, LatLng latLngEnd) {
        this.latLngBegin = latLngBegin;
        this.latLngEnd = latLngEnd;
    }
    @Override
    public void draw(AMap aMap) {
        if (show) {
            if (polyline == null) {
                PolylineOptions options = new PolylineOptions();
                options.add(latLngBegin)
                        .add(latLngEnd)
                        .width(lineWidth)
                        .color(lineColor);
                polyline = aMap.addPolyline(options);
            } else {
                polyline.setVisible(true);
            }
        }
    }

    /**
     * 强制重画
     */
    public void forceDraw(AMap aMap) {
        PolylineOptions options = new PolylineOptions();
        options.add(latLngBegin)
                .add(latLngEnd)
                .width(lineWidth)
                .color(lineColor);
        aMap.addPolyline(options);

    }

    /**
     * 隐藏
     */
    public void hide() {
        if (polyline != null) {
            polyline.setVisible(false);
        }
    }

    /**
     * 销毁
     */
    public void destory() {
        if (polyline != null) {
            polyline.remove();
        }
    }

    @Override
    public String toString() {
        return "latitude:" + latLngBegin.latitude + "\t"
                + "longitude:" + latLngBegin.longitude + "\t"
                + "latitude:" + latLngEnd.latitude + "\t"
                + "longitude:" + latLngEnd.longitude + "\t";
    }

    //getter----and---setter------------------------------------------------
    public LatLng getLatLngBegin() {
        return latLngBegin;
    }

    public void setLatLngBegin(LatLng latLngBegin) {
        this.latLngBegin = latLngBegin;
    }

    public LatLng getLatLngEnd() {
        return latLngEnd;
    }

    public void setLatLngEnd(LatLng latLngEnd) {
        this.latLngEnd = latLngEnd;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setShow(boolean show) {
        this.show = show;
        if (show ==false){
            hide();
        }
    }
}
