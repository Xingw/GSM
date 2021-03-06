package com.xunce.gsmr.view.activity.baidu;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.xunce.gsmr.R;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.model.baidumap.BaiduRailWayHolder;
import com.xunce.gsmr.model.baidumap.MarkerHolder;
import com.xunce.gsmr.model.baidumap.openGLLatLng;
import com.xunce.gsmr.model.event.BaiduFragmentInitFinishEvent;
import com.xunce.gsmr.util.gps.GPSUtil;
import com.xunce.gsmr.util.gps.MapHelper;
import com.xunce.gsmr.util.gps.PositionUtil;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import de.greenrobot.event.EventBus;

/**
 * 包含百度地图的一些组件
 * 简化Activity代码
 * Created by ssthouse on 2015/9/13.
 */
public class BaiduMapFragment extends Fragment implements BaiduMap.OnMapDrawFrameCallback {
    private static final String TAG = "BaiduMapFragment";

    /**
     * 上下文
     */
    private Context context;

    /**
     * Fragment的layout
     */
    private View layout;

    /**
     * 控制的百度地图
     */
    private MapView mapView;
    private BaiduMap baiduMap;
    //地图显示的InfoWindow
    private InfoWindow infoWindow;
    private LinearLayout llInfoWindow;

    /**
     * 控制的Project数据
     */
    private PrjItem prjItem;

    /**
     * 控制地图上的Marker
     */
    private MarkerHolder markerHolder;

    /**
     * 控制定位
     */
    private LocationClient locationClient;
    //当前获取的定位位置
    private BDLocation currentBDLocation;

    /**
     * 是否首次进入
     */
    private boolean isFistIn = true;

    /**
     * 公里标VIew
     */
    private LinearLayout llPosition;
    private EditText etPosition;
    private boolean isPositionShowed = false;

    /**
     * 用于绘制openGL图像
     */
    private List<openGLLatLng> latLngPolygon = new ArrayList<>();
    private boolean cleanlatLng = false;
    private LocationManager locationManager;

    /**
     * 获取Instance
     *
     * @param bundle
     * @return
     */
    public static BaiduMapFragment getInstance(Bundle bundle) {
        BaiduMapFragment baiduMapFragment = new BaiduMapFragment();
        baiduMapFragment.setArguments(bundle);
        return baiduMapFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_baidu_prj_edit_baidu, null);
        //初始化数据
        mapView = (MapView) layout.findViewById(R.id.id_map_view);
        context = getActivity();
        prjItem = (PrjItem) getArguments().getParcelable("prjItem");

        //正式初始化
        init();
        initOpenGLDraw();
        return layout;
    }

    /**
     * 初始化OpenGL绘制
     */
    private void initOpenGLDraw() {
        baiduMap.setOnMapDrawFrameCallback(this);
    }

    /**
     * 正式初始化
     */
    private void init() {
        //初始化数据
        this.baiduMap = mapView.getMap();
        //初始化BaiduMap
        initBaiduMap();
        //初始化marker控制器
        markerHolder = new MarkerHolder(getActivity(), prjItem, baiduMap);
        //初始化定位控制器
        initLocationClient();
    }

    /**
     * 初始化BaiduMap
     */
    private void initBaiduMap() {
        baiduMap.setMyLocationEnabled(true);
        MapHelper.animateZoom(baiduMap, 15);
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
        //隐藏缩放控件
        mapView.showZoomControls(false);
        //初始化InfoWindow内容
        llInfoWindow = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.view_info_window, null);

        //marker的点击事件
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                //如果点的是已经选中了的Marker---变回未选中状态
                if (marker == markerHolder.getCurrentMarker()) {
                    markerHolder.clearSelection();
                    baiduMap.hideInfoWindow();
                } else {
                    markerHolder.setAll2Blue();
                    marker.setIcon(MarkerHolder.descriptorRed);
                    //选中了Marker
                    markerHolder.setCurrentMarker(marker);
                    //弹出InfoWindow
                    showInfoWindow(marker.getPosition());
                }
                return true;
            }
        });

        //Fragment初始化完成事件
        EventBus.getDefault().post(new BaiduFragmentInitFinishEvent());
    }

    /**
     * 显示公里标输入框
     */
    private void showLlPosition() {
        isPositionShowed = true;
        llPosition.setVisibility(View.VISIBLE);
        llPosition.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pop_up));
    }

    /**
     * 隐藏公里标输入框
     */
    private void hideLlPosition() {
        isPositionShowed = false;
        llPosition.startAnimation(AnimationUtils.loadAnimation(context, R.anim.drop_down));
        llPosition.setVisibility(View.GONE);
    }

    /**
     * 显示InfoWindow
     *
     * @param latLng
     */
    public void showInfoWindow(LatLng latLng) {
        infoWindow = new InfoWindow(llInfoWindow, latLng, -47);
        baiduMap.showInfoWindow(infoWindow);
    }

    public void hideInfoWindow() {
        baiduMap.hideInfoWindow();
    }

    public LatLng getCurrentMarkerLatLng() {
        return markerHolder.getCurrentMarker().getPosition();
    }

    /**
     * 初始化LocationClient
     */
    public void initLocationClient() {
        //设置Options
        if (PreferenceHelper.getInstance(context).getIsWifiLocateMode(context)) {
            if (locationManager!=null){
                locationManager.removeUpdates(GPSlocationListener);
                locationManager = null;
            }
            //创建client
            locationClient = new LocationClient(context);
            final LocationClientOption locateOptions = new LocationClientOption();
            locateOptions.setCoorType("bd09ll");    //返回的定位结果是百度经纬度,默认值gcj02
            locateOptions.setScanSpan(1000);        //设置发起定位请求的间隔时间为5000ms
            locateOptions.setIsNeedAddress(true);   //返回的定位结果包含地址信息
            locateOptions.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
            locationClient.setLocOption(locateOptions);
            //注册监听事件
            locationClient.registerLocationListener(new BDLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    if (bdLocation != null) {
                        //如果是第一次获取到数据----将地图定位到改点
                        currentBDLocation = bdLocation;
                        //更新我的位置
                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(currentBDLocation.getRadius())
                                .latitude(currentBDLocation.getLatitude())
                                .longitude(currentBDLocation.getLongitude()).build();
                        baiduMap.setMyLocationData(locData);
                        //如果是第一次进入---地图定位到我的位置
                        if (isFistIn) {
                            locate();
                            isFistIn = false;
                        }
                    }
                }
            });
            locateOptions.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            //启动定位
            locationClient.start();
        } else {
            if (locationClient !=null){
                locationClient.stop();
            }
            locationManager = GPSUtil.getCORSLocationManager(getActivity());
            if (locationManager == null) return;
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                LatLng latLng = PositionUtil.Gps84_To_bd09(location.getLatitude(),location
                        .getLongitude());
                location.setLongitude(latLng.longitude);
                location.setLatitude(latLng.latitude);
                currentBDLocation = new BDLocation();
                currentBDLocation.setLatitude(location.getLatitude());
                currentBDLocation.setLongitude(location.getLongitude());
                currentBDLocation.setAltitude(location.getAltitude());
                currentBDLocation.setDirection(location.getBearing());
                animateToPoint(new LatLng(currentBDLocation.getLatitude(),currentBDLocation
                        .getLongitude()));
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000, (float) 0.01,
                    GPSlocationListener);
        }
    }

    private LocationListener GPSlocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location!=null){
                LatLng latLng = PositionUtil.Gps84_To_bd09(location.getLatitude(),location
                        .getLongitude());
                location.setLongitude(latLng.longitude);
                location.setLatitude(latLng.latitude);
                currentBDLocation = new BDLocation();
                currentBDLocation.setLatitude(location.getLatitude());
                currentBDLocation.setLongitude(location.getLongitude());
                currentBDLocation.setAltitude(location.getAltitude());
                currentBDLocation.setDirection(location.getBearing());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    /**
     * 定位到当前接收到的定位点
     */
    public void locate() {
        if (currentBDLocation != null) {
            //更新我的位置
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(currentBDLocation.getRadius())
                    .latitude(currentBDLocation.getLatitude())
                    .longitude(currentBDLocation.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
            //更新地图中心点
            LatLng ll = new LatLng(currentBDLocation.getLatitude(),
                    currentBDLocation.getLongitude());
            MapHelper.animateToPoint(baiduMap, ll);
        }else {
            ToastHelper.show(getActivity(),"未获取到定位数据");
        }
    }

    /**
     * 加载marker
     */
    public void loadMarker(PrjItem prjItem) {
        //MarkerHolder模块
        if (markerHolder == null) {
            markerHolder = new MarkerHolder(getActivity(), prjItem, baiduMap);
        } else {
            markerHolder.initMarkerList();
        }
    }

    /**
     * 动画聚焦到一个点
     *
     * @param latLng
     */
    public void animateToPoint(LatLng latLng) {
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        baiduMap.animateMapStatus(u);
    }

    /**
     * 动画放大
     *
     * @param zoomLevel
     */
    public void animateZoom(int zoomLevel) {
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(zoomLevel);
        baiduMap.animateMapStatus(u);
    }

    /**
     * 加载Marker图标
     */
    public void loadMarker() {
        markerHolder.initMarkerList();
    }

    public LatLng getTarget() {
        return baiduMap.getMapStatus().target;
    }

    //getter---and---setter--------------------------------------------
    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public MarkerHolder getMarkerHolder() {
        return markerHolder;
    }

    public void setMarkerHolder(MarkerHolder markerHolder) {
        this.markerHolder = markerHolder;
    }

    public BaiduMap getBaiduMap() {
        return baiduMap;
    }

    public void setBaiduMap(BaiduMap baiduMap) {
        this.baiduMap = baiduMap;
    }

    public List<openGLLatLng> getLatLngPolygon() {
        return latLngPolygon;
    }

    public void setLatLngPolygon(List<openGLLatLng> latLngPolygon) {
        this.latLngPolygon = latLngPolygon;
    }

    //--------------生命周期--------------------------------------------
    public void pause() {
        if (mapView != null) {
            mapView.onPause();
        }
    }

    public void resume() {
        if (mapView != null) {
            mapView.onResume();
        }
    }

    public void destory() {
        if (mapView != null) {
            mapView.onDestroy();
        }
        if (locationClient != null) {
            locationClient.stop();
        }
    }

    //OpenGL绘制部分******************************************************************
    public void onMapDrawFrame(GL10 gl, MapStatus drawingMapStatus) {
        if (baiduMap.getProjection() != null) {
            if (latLngPolygon.size() > 0)
                for (openGLLatLng openGLLatLng : latLngPolygon) {
                    FloatBuffer vertexBuffer = calPolylinePoint(drawingMapStatus, openGLLatLng.getLatLngs());
                    drawPolyline(gl, openGLLatLng.getColor(), vertexBuffer, 6, openGLLatLng.getLatLngs().size(),
                            drawingMapStatus);
                }
        }
        if (cleanlatLng){
            latLngPolygon.clear();
            cleanlatLng = false;
        }
    }

    private void drawPolyline(GL10 gl, int color, FloatBuffer lineVertexBuffer,
                              float lineWidth, int pointSize, MapStatus drawingMapStatus) {
        gl.glEnable(GL10.GL_BLEND);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        float colorA = Color.alpha(color) / 255f;
        float colorR = Color.red(color) / 255f;
        float colorG = Color.green(color) / 255f;
        float colorB = Color.blue(color) / 255f;

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVertexBuffer);
        gl.glColor4f(colorR, colorG, colorB, colorA);
        gl.glLineWidth(lineWidth);
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, pointSize);

        gl.glDisable(GL10.GL_BLEND);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    public FloatBuffer calPolylinePoint(MapStatus mspStatus, List<LatLng> latLng) {
        FloatBuffer vertexBuffer;
        float[] vertexs;
        PointF[] polyPoints = new PointF[latLng.size()];
        vertexs = new float[3 * latLng.size()];
        int i = 0;
        for (LatLng xy : latLng) {
            polyPoints[i] = baiduMap.getProjection().toOpenGLLocation(xy,
                    mspStatus);
            vertexs[i * 3] = polyPoints[i].x;
            vertexs[i * 3 + 1] = polyPoints[i].y;
            vertexs[i * 3 + 2] = 0.0f;
            i++;
        }
        vertexBuffer = makeFloatBuffer(vertexs);
        return vertexBuffer;
    }

    private FloatBuffer makeFloatBuffer(float[] fs) {
        ByteBuffer bb = ByteBuffer.allocateDirect(fs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(fs);
        fb.position(0);
        return fb;
    }
    //OpenGL绘制部分结束***************************************************************************

    public void hideAll() {
        cleanlatLng = true;
        baiduMap.clear();
        loadMarker(prjItem);
    }

    public void  hideText(){
        baiduMap.clear();
        loadMarker(prjItem);
    }
}
