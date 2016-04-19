package com.xunce.gsmr.model.baidumap.graph;

import android.graphics.Color;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.xunce.gsmr.model.baidumap.graph.Point;
import com.xunce.gsmr.util.gps.PositionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xingw on 2016/4/14.
 */
public class Vector extends Graph{
    private static final int POLYLINE_WIDTH = 6;

    /**
     * 当前矢量的名称
     */
    private String name;

    /**
     * 一个矢量的所有点
     */
    private List<Point> pointList = new ArrayList<>();

    /**
     * 画在地图上的数据
     */
    private PolylineOptions polylineOptions;

    /**
     * 传入name的构造方法
     *
     * @param name
     */
    public Vector(String name) {
        this.name = name;
    }

    /**
     * 初始化需要画在地图上的数据
     */
    public void initPolylineOptions() {
        List<LatLng> latLngs = new ArrayList<>();

        //添加点
        for (int i = 0; i < pointList.size(); i++) {
            Point point = pointList.get(i);
            latLngs.add(PositionUtil.Gps84_To_bd09(point.getLatitude(), point.getLongitude()));
        }

        //判断需不需要改变颜色
        //L.log(TAG, "name:\t" + name);
        if (latLngs.size()>=2) {
            if (name != null && name.contains("Railway")) {
                //L.log(TAG, "我改变了颜色");\
                polylineOptions = new PolylineOptions().width(POLYLINE_WIDTH * 2).color(Color.RED).points(latLngs);
            } else {
                polylineOptions = new PolylineOptions().width(POLYLINE_WIDTH).color(Color.BLUE).points(latLngs);
            }
        }
    }

    /**
     * 隐藏
     */
    public void hide() {
        if (polylineOptions != null) {
            polylineOptions.visible(false);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Point> getPointList() {
        return pointList;
    }

    public void setPointList(List<Point> pointList) {
        this.pointList = pointList;
    }

    @Override
    public void draw(BaiduMap baiduMap) {
        if(polylineOptions == null){
            initPolylineOptions();
            baiduMap.addOverlay(polylineOptions);
        }else {
            polylineOptions.visible(true);
        }
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = polylineOptions;
    }
}
