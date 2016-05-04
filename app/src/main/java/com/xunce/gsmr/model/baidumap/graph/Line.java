package com.xunce.gsmr.model.baidumap.graph;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.xunce.gsmr.model.baidumap.openGLLatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * 地图上的直线
 * Created by ssthouse on 2015/7/30.
 */
public class Line extends Graph {
    private static final String TAG = "Line";

    //直线的参数
    private static int lineColor =  0xAAFF0000;
    private static int lineWidth = 10;

    private LatLng latLngBegin;

    private LatLng latLngEnd;

    private Overlay polyline;


    public Line(LatLng latLngBegin, LatLng latLngEnd) {
        this.latLngBegin = latLngBegin;
        this.latLngEnd = latLngEnd;
    }

    public Line(LatLng latLngBegin, LatLng latLngEnd,String layerName) {
        this.latLngBegin = latLngBegin;
        this.latLngEnd = latLngEnd;
        this.layerName = layerName;
    }


    @Override
    public void draw(BaiduMap baiduMap,boolean clear) {
        if (show) {
            if (clear){
                // 添加折线
                List<LatLng> points = new ArrayList<>();
                points.add(latLngBegin);
                points.add(latLngEnd);
                PolylineOptions ooPolyline = new PolylineOptions()
                        .width(lineWidth)
                        .color(lineColor)
                        .points(points);
                polyline = baiduMap.addOverlay(ooPolyline);
                return;
            }
            if (polyline == null) {
                // 添加折线
                List<LatLng> points = new ArrayList<>();
                points.add(latLngBegin);
                points.add(latLngEnd);
                PolylineOptions ooPolyline = new PolylineOptions()
                        .width(lineWidth)
                        .color(lineColor)
                        .points(points);
                polyline = baiduMap.addOverlay(ooPolyline);
            } else {
                polyline.setVisible(true);
            }
        }
    }

    /**
     * openGL的方式绘制
     * @param openglLatLng
     */
    public void draw(List<openGLLatLng> openglLatLng) {
        // 添加折线
        List<LatLng> points = new ArrayList<>();
        points.add(latLngBegin);
        points.add(latLngEnd);
//            ooPolyline = new PolylineOptions()
//                    .width(lineWidth)
//                    .color(lineColor)
//                    .points(points);
//            openglLatLng.addOverlay(ooPolyline);
        openglLatLng.add(new openGLLatLng(points,lineColor));
    }

    public void hide(){
        if (polyline !=null)
        polyline.setVisible(false);
    }

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

    public void setShow(boolean show) {
        this.show = show;
        if (!show)
            hide();
    }

    public void forcedraw(BaiduMap baiduMap) {
        List<LatLng> points = new ArrayList<>();
        points.add(latLngBegin);
        points.add(latLngEnd);
        PolylineOptions ooPolyline = new PolylineOptions()
                .width(lineWidth)
                .color(lineColor)
                .points(points);
        baiduMap.addOverlay(ooPolyline);
    }
}
