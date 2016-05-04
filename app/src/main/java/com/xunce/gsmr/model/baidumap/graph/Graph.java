package com.xunce.gsmr.model.baidumap.graph;

import com.baidu.mapapi.map.BaiduMap;

/**
 * 地图绘图基类
 * Created by ssthouse on 2015/8/21.
 */
public abstract class Graph {
    protected String layerName;
    protected boolean show = true;
    public abstract void draw(BaiduMap baiduMap,boolean clear);

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public boolean isShow() {
        return show;
    }

}
