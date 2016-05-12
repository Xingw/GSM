package com.xunce.gsmr.view.activity.gaode;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.xunce.gsmr.R;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.model.gaodemap.MarkerHolder;
import com.xunce.gsmr.util.gps.GPSUtil;
import com.xunce.gsmr.util.gps.LocateHelper;
import com.xunce.gsmr.util.gps.PositionUtil;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;

/**
 * 必须有一个R.id.id_map的高德地图控件
 * 必须被调用init()方法
 * 高德基础地图Activity
 * Created by ssthouse on 2015/9/14.
 */
public class GaodeBaseActivity extends AppCompatActivity {

    /**
     * 地图
     */
    private AMap aMap;
    private MapView mapView;
    /**
     * 定位回调
     */
    private LocationSource.OnLocationChangedListener mListener;
    /**
     * 定位管理器
     */
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    /**
     * 保存当前位置
     */
    private AMapLocation currentAMapLocation;

    /**
     * 地图上的标记点管理器
     */
    private MarkerHolder markerHolder;
    private LocationManager locationManager;
    private boolean Firstin = false;


    /**
     * 初始化AMap对象
     */
    public void init(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.id_map_view);

        // 此方法必须重写
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            UiSettings mUiSettings = getaMap().getUiSettings();//隐藏放大缩小按钮
            mUiSettings.setZoomControlsEnabled(false);
        }
    }

    /**
     * 加载marker
     */
    public void loadMarker(PrjItem prjItem) {
            //MarkerHolder模块
            if (markerHolder == null) {
                markerHolder = new MarkerHolder(this, prjItem, getaMap());
            } else {
                markerHolder.initMarker();
            }
    }

    /**
     * 地图中心移动到一个点
     *
     * @param latLng
     */
    public void animateToPoint(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.changeLatLng(latLng);
        aMap.animateCamera(cameraUpdate);
    }

    /**
     * 改变方向
     *
     * @param bearing
     */
    public void changeBearing(float bearing) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.changeBearing(bearing);
        aMap.animateCamera(cameraUpdate);
    }

    public void animateToPoint(LatLng latLng,float zoom) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.changeLatLng(latLng);
        aMap.animateCamera(cameraUpdate);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }
    /**
     * 定位到我的位置
     */
    public void animateToMyLocation() {
        if (currentAMapLocation == null) {
            ToastHelper.show(this, "尚未获得定位信息");
            return;
        }
        LatLng latLng = new LatLng(currentAMapLocation.getLatitude(),
                currentAMapLocation.getLongitude());
        animateToPoint(latLng);
        //changeBearing(currentAMapLocation.getBearing());
        //显示小蓝点
        mListener.onLocationChanged(currentAMapLocation);
    }

    //定位相关-------------------------------------------------------------

    /**
     * 获取定位得到的location
     */
    private AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                currentAMapLocation = aMapLocation;
                //Timber.e("我收到了一条定位...\t" + "定位结果为:\t" + aMapLocation.getErrorCode());
                if (Firstin){
                    animateToMyLocation();
                    Firstin=false;
                }
            }
        }
    };

    /**
     * 显示定位
     */
    public void initLocate() {
        locationClient = new AMapLocationClient(this);
        locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setInterval(1000);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
        locationClient.setLocationOption(locationOption);
        // 设置定位模式
        if(PreferenceHelper.getInstance(this).getIsWifiLocateMode(this)) {
            //开启定位
            locationClient.startLocation();
        }else{
            locationManager = GPSUtil.getCORSLocationManager(this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                LatLng latLng = PositionUtil.gps84_To_Gcj02(location.getLatitude(),location
                        .getLongitude());
                location.setLongitude(latLng.longitude);
                location.setLatitude(latLng.latitude);
                currentAMapLocation = new AMapLocation(location);
                animateToPoint(new LatLng(currentAMapLocation.getLatitude(),currentAMapLocation
                        .getLongitude()));
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000, (float) 0.01,
                    GPSlocationListener);
        }

        //设置定位UI
        aMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                if (onLocationChangedListener != null) {
                    mListener = onLocationChangedListener;
                }
            }

            @Override
            public void deactivate() {

            }
        });
        aMap.getUiSettings().setMyLocationButtonEnabled(false); // 是否显示默认的定位按钮
        aMap.setMyLocationEnabled(true);// 是否可触 发定位并显示定位层
    }

    /**
     * 改变定位方式
     */
    public void changeLocateMode(){
        if( PreferenceHelper.getInstance(this).getIsWifiLocateMode(this)){
            if (locationManager!=null){
                locationManager.removeUpdates(GPSlocationListener);
                locationManager = null;
            }
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationClient.startLocation();
        }else{
            if (locationClient !=null){
                locationClient.stopLocation();
            }
            locationManager = GPSUtil.getCORSLocationManager(this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000, (float) 0.01,
                    GPSlocationListener);
            //locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode
            //        .Device_Sensors);
        }

    }

    private LocationListener GPSlocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location!=null){
                LatLng latLng = PositionUtil.gps84_To_Gcj02(location.getLatitude(),location
                        .getLongitude());
                location.setLongitude(latLng.longitude);
                location.setLatitude(latLng.latitude);
                currentAMapLocation = new AMapLocation(location);
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
    //生命周期---------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    //getter----and----setter--------------------------------------
    public AMap getaMap() {
        return aMap;
    }

    public void setaMap(AMap aMap) {
        this.aMap = aMap;
    }

    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public MarkerHolder getMarkerHolder() {
        return markerHolder;
    }
}
