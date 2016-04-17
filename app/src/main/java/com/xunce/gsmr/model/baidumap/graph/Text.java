package com.xunce.gsmr.model.baidumap.graph;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

/**
 * 地图上的文字
 * Created by ssthouse on 2015/7/30.
 */
public class Text extends Graph {
    private static final String TAG = "Text";

    //文字的参数
    private static int textColor = 0xFFFF00FF;
    private static int textBgColor = 0x00FF00FF;
    private static int textSize = 24;

    private LatLng latLng;
    private float rotate;
    private String content;
    private TextOptions ooText;

    public Text(LatLng latLng, float rotate, String content) {
        this.latLng = latLng;
        this.rotate = rotate;
        this.content = content;
    }

    public Text(LatLng latLng, String string) {
        this.latLng = latLng;
        this.content = string;
        this.rotate = 0;
    }

    @Override
    public void draw(BaiduMap baiduMap) {
        if(ooText == null){
            // 添加文字
            OverlayOptions ooText = new TextOptions()
                    .bgColor(textBgColor)
                    .fontSize(textSize)
                    .fontColor(textColor)
                    .text(content)
                    .rotate(-rotate)
                    .position(latLng);
            baiduMap.addOverlay(ooText);
        }else{
            ooText.visible(true);
        }
    }

    public void hide(){
        ooText.visible(false);
    }


    //getter----and---setter------------------------------------------------
    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getRotate() {
        return rotate;
    }
    public void setRotate(float rotate) {
        this.rotate = rotate;
    }
}
