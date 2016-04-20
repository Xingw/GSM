package com.xunce.gsmr.model.gaodemap.graph;

import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.xunce.gsmr.util.gps.PositionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 矢量图形
 * Created by ssthouse on 2015/10/16.
 */
public class Vector extends BaseGraph {
    private static final int POLYLINE_WIDTH = 6;
    private boolean show = true;
    /**
     * 当前矢量的名称
     */
    private String layername;

    /**
     * 一个矢量的所有点
     */
    private List<Point> pointList = new ArrayList<>();

    /**
     * 画在地图上的数据
     */
    private PolylineOptions polylineOptions;
    private Polyline polyline;

    /**
     * 传入name的构造方法
     *
     * @param layername
     */
    public Vector(String layername) {
        this.layername = layername;
    }
    /**
     * 初始化需要画在地图上的数据
     */
    public void initPolylineOptions() {
        polylineOptions = new PolylineOptions();
        polylineOptions.width(POLYLINE_WIDTH).color(Color.BLUE);
        //添加点
        for (int i = 0; i < pointList.size(); i++) {
            Point point = pointList.get(i);
            polylineOptions.add(PositionUtil.gps84_To_Gcj02(point.getLatitude(), point.getLongitude()));
        }
        //判断需不需要改变颜色
        //L.log(TAG, "name:\t" + name);
        if (layername != null && layername.contains("Railway")) {
            //L.log(TAG, "我改变了颜色");
            polylineOptions.color(Color.RED);
            polylineOptions.width(POLYLINE_WIDTH * 2);
        }
    }

    @Override
    public void draw(AMap aMap) {
        if (show) {
            if (polylineOptions == null) {
                initPolylineOptions();
                polyline = aMap.addPolyline(polylineOptions);
                return;
            }
            if (polyline == null) {
                polyline = aMap.addPolyline(polylineOptions);
            } else {
                polyline.setVisible(true);
            }
        }
    }

    /**
     * 强制重画
     */
    public void forceDraw(AMap aMap) {
        if (polylineOptions == null) {
            initPolylineOptions();
        }
        aMap.addPolyline(polylineOptions);

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

    public String getName() {
        return layername;
    }

    public void setName(String name) {
        this.layername = name;
    }

    public List<Point> getPointList() {
        return pointList;
    }

    public void setPointList(List<Point> pointList) {
        this.pointList = pointList;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public String getLayerName() {
        return layername;
    }

    public void setShow(boolean show) {
        this.show = show;
        if (show ==false){
            hide();
        }
    }
}
