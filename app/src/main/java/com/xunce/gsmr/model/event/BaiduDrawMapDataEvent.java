package com.xunce.gsmr.model.event;

import com.xunce.gsmr.model.baidumap.BaiduRailWayHolder;

/**
 * Created by Xingw on 2016/4/17.
 */
public class BaiduDrawMapDataEvent {
    private BaiduRailWayHolder baiduRailWayHolder;

    public BaiduDrawMapDataEvent(BaiduRailWayHolder baiduRailWayHolder) {
        this.baiduRailWayHolder = baiduRailWayHolder;
    }

    public BaiduRailWayHolder getBaiduRailWayHolder() {
        return baiduRailWayHolder;
    }

    public void setBaiduRailWayHolder(BaiduRailWayHolder baiduRailWayHolder) {
        this.baiduRailWayHolder = baiduRailWayHolder;
    }
}
