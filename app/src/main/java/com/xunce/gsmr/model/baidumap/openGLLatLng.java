package com.xunce.gsmr.model.baidumap;

import android.graphics.Color;

import com.baidu.mapapi.model.LatLng;

import java.util.List;

/**
 * Created by Xingw on 2016/4/19.
 */
public class openGLLatLng {
    List<LatLng> latLngs;
    int color;

    public openGLLatLng(List<LatLng> latLngs, int color) {
        this.latLngs = latLngs;
        this.color = color;
    }

    public List<LatLng> getLatLngs() {
        return latLngs;
    }

    public void setLatLngs(List<LatLng> latLngs) {
        this.latLngs = latLngs;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
