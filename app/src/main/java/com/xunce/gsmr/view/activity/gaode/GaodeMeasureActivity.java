package com.xunce.gsmr.view.activity.gaode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.model.MarkerIconCons;
import com.xunce.gsmr.model.event.GaoDeDrawMapDataEvent;
import com.xunce.gsmr.model.gaodemap.GaodeRailWayHolder;
import com.xunce.gsmr.util.view.ViewHelper;
import com.xunce.gsmr.view.style.TransparentStyle;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 高德地图测量Activity
 * Created by ssthouse on 2015/9/16.
 */
public class GaodeMeasureActivity extends GaodeBaseActivity {
    /**
     * 接收的数据
     */
    private static final String EXTRA_LATLNG = "extra_latlng";
    private LatLng latLng;

    /**
     * 标记点数据
     */
    private List<Marker> markerList = new ArrayList<>();
    private List<LatLng> pointList = new ArrayList<>();
    private Polyline polyline;
    //折线对象
    private PolylineOptions polylineOptions = new PolylineOptions();

    /**
     * View
     */
    private TextView tvLength;
    private float zoomlevel;
    private GaodeRailWayHolder railWayHolder;

    /**
     * 启动当前Activity
     *
     * @param activity
     * @param latLng
     */
    public static void start(Activity activity, LatLng latLng,float zoom) {
        Intent intent = new Intent(activity, GaodeMeasureActivity.class);
        if (latLng == null) {
            return;
        }
        intent.putExtra(Constant.EXTRA_KEY_LATITUDE, latLng.latitude);
        intent.putExtra(Constant.EXTRA_KEY_LONGITUDE, latLng.longitude);
        intent.putExtra(Constant.EXTRA_KEY_ZOOM,zoom);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaode_measure);
        TransparentStyle.setTransparentStyle(this, R.color.color_primary);
        super.init(savedInstanceState);
        //接收数据
        Intent intent = getIntent();
        latLng = new LatLng(intent.getDoubleExtra(Constant.EXTRA_KEY_LATITUDE, 0),
                intent.getDoubleExtra(Constant.EXTRA_KEY_LONGITUDE, 0));
        zoomlevel = intent.getFloatExtra(Constant.EXTRA_KEY_ZOOM,0);
        //初始化View
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        ViewHelper.initActionBar(this, getSupportActionBar(), "测距");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //开启定位
        super.initLocate();
        //将地图移动到目标点
        getaMap().moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        getaMap().moveCamera(CameraUpdateFactory.zoomTo(zoomlevel));

        EventBus.getDefault().register(this);

        //view---和点击事件
        tvLength = (TextView) findViewById(R.id.id_tv_length);
        findViewById(R.id.id_ib_locate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GaodeMeasureActivity.super.animateToMyLocation();
            }
        });
        findViewById(R.id.btn_zoom_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getaMap().moveCamera(CameraUpdateFactory.zoomIn());
            }
        });
        findViewById(R.id.btn_zoom_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getaMap().moveCamera(CameraUpdateFactory.zoomOut());
            }
        });
        //地图触控事件
        getaMap().setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //添加坐标点
                pointList.add(latLng);
                addMarker(latLng);
                //重新计算总长
                updateLength();
                //重画
                //redraw();
            }
        });
    }

    private void addMarker(LatLng latLng) {
        //将点击位置存入List
        MarkerOptions options = new MarkerOptions().icon(MarkerIconCons.descriptorRed).position(latLng);
        if (markerList.size()>1) {
            //将最后一个改为蓝色
            markerList.get(markerList.size() - 1).setIcon(MarkerIconCons.descriptorBlue);
        }
        //添加新的点
        markerList.add((getaMap().addMarker(options)));

        reDrawLine();

    }

    private void reDrawLine() {
        if (polyline ==null){
            polylineOptions = new PolylineOptions()
                    .width(15)
                    .color(Color.BLUE)
                    .addAll(pointList);
            polyline=getaMap().addPolyline(polylineOptions);
        }else {
            polyline.remove();
            polylineOptions = new PolylineOptions()
                    .width(15)
                    .color(Color.BLUE)
                    .addAll(pointList);
            polyline = getaMap().addPolyline(polylineOptions);
        }
    }

    /**
     * 重画地图上的点
     */
    private void redraw() {
        //清除marker--显示
        getaMap().clear();
        //画出线
        if (pointList.size() > 1) {
            polylineOptions = new PolylineOptions()
                    .width(15)
                    .color(Color.BLUE)
                    .addAll(pointList);
            polyline = getaMap().addPolyline(polylineOptions);
        }
        //画出标记点
        for (int i = 0; i < pointList.size(); i++) {
            if (i == 0 || i == pointList.size() - 1) {
                MarkerOptions markerOptions = new MarkerOptions().position(pointList.get(i))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.icon_marker_red))
                        .zIndex(9).draggable(true);
                getaMap().addMarker(markerOptions);
            } else {
                MarkerOptions markerOptions = new MarkerOptions().position(pointList.get(i))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.icon_marker_blue))
                        .zIndex(9).draggable(true);
                getaMap().addMarker(markerOptions);
            }
        }
    }

    /**
     * 画出PrjEditActivity上已有的地图数据
     *
     * @param gaoDeDrawMapDataEvent
     */
    public void onEventMainThread(GaoDeDrawMapDataEvent gaoDeDrawMapDataEvent) {
        //复制一份holder到当前activity
        if (gaoDeDrawMapDataEvent.getRailWayHolder() != null) {
            railWayHolder = gaoDeDrawMapDataEvent.getRailWayHolder();
        }
        if (railWayHolder != null) {
            railWayHolder.forcedrawLine(getaMap());
        }
    }

    /**
     * 更新总长
     */
    private void updateLength() {
        double length = 0;
        for (int i = 0; i < pointList.size() - 1; i++) {
            double gap = AMapUtils.calculateLineDistance(pointList.get(i), pointList.get(i + 1));
            length += gap;
        }
        if (length != 0) {
            int result = (int) length;
            tvLength.setText(result + "m");
        } else {
            tvLength.setText(0 + "m");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_measure, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_action_back_one_point:
                if (pointList.size() != 0) {
                    removeLastMarker();
                    pointList.remove(pointList.size() - 1);
                    reDrawLine();
                    updateLength();
                    //redraw();
                }
                break;
            case R.id.id_action_delete_all:
                updateLength();
                pointList.clear();
                reDrawLine();
                //redraw();
                removeAllMarker();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeAllMarker() {
        for (Marker marker : markerList) {
            marker.remove();
        }
        markerList.clear();
        polyline.remove();
        polyline.getPoints().clear();
    }

    private void removeLastMarker() {
        markerList.get(markerList.size() -1).remove();
        markerList.remove(markerList.size() -1);
        if (markerList.size() >0)
        markerList.get(markerList.size() -1).setIcon(MarkerIconCons.descriptorRed);
        polyline.getPoints().remove(polyline.getPoints().size() -1);
    }
}
