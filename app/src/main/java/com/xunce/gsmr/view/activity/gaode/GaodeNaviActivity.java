package com.xunce.gsmr.view.activity.gaode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.PathPlanningStrategy;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Xingw on 2016/4/27.
 */
public class GaodeNaviActivity extends Activity {
    NaviLatLng endLatlng;
    NaviLatLng startLatlng;
    List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
    List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
    private MapView mapView;
    private AMap amap;
    private AMapNavi aMapNavi;
    private HashMap<Integer, RouteOverLay> routeOverlays = new HashMap<Integer, RouteOverLay>();
    private int routeIndex;
    private int[] routeIds;
    private TTSController ttsManager;
    private boolean chooseRouteSuccess;
    private boolean mapClickStartReady;
    private boolean mapClickEndReady;
    private Marker mStartMarker;
    private Marker mEndMarker;
    private boolean calculateSuccess;

    private EditText startPoint;
    private EditText endPoint;
    private boolean changeText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaode_navi);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        amap = mapView.getMap();
        LatLng latLng = new LatLng(getIntent().getDoubleExtra(Constant.EXTRA_KEY_LATITUDE,Constant.LATITUDE_DEFAULT)
                ,getIntent().getDoubleExtra(Constant.EXTRA_KEY_LONGITUDE,Constant.LONGITUDE_DEFAULT));
        amap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        AMapNaviListener listener = new AMapNaviListener() {
            @Override
            public void onInitNaviFailure() {

            }

            @Override
            public void onInitNaviSuccess() {

            }

            @Override
            public void onStartNavi(int i) {

            }

            @Override
            public void onTrafficStatusUpdate() {

            }

            @Override
            public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

            }

            @Override
            public void onGetNavigationText(int i, String s) {

            }

            @Override
            public void onEndEmulatorNavi() {

            }

            @Override
            public void onArriveDestination() {

            }

            @Override
            public void onCalculateRouteSuccess() {

            }

            @Override
            public void onCalculateRouteFailure(int i) {

            }

            @Override
            public void onReCalculateRouteForYaw() {

            }

            @Override
            public void onReCalculateRouteForTrafficJam() {

            }

            @Override
            public void onArrivedWayPoint(int i) {

            }

            @Override
            public void onGpsOpenStatus(boolean b) {

            }

            @Override
            public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

            }

            @Override
            public void onNaviInfoUpdate(NaviInfo naviInfo) {

            }

            @Override
            public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

            }

            @Override
            public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

            }

            @Override
            public void showCross(AMapNaviCross aMapNaviCross) {

            }

            @Override
            public void hideCross() {

            }

            @Override
            public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

            }

            @Override
            public void hideLaneInfo() {

            }

            @Override
            public void onCalculateMultipleRoutesSuccess(int[] ints) {
                //当且仅当，使用策略AMapNavi.DrivingMultipleRoutes时回调
                //单路径算路依然回调onCalculateRouteSuccess，不回调这个


                //你会获取路径ID数组
                routeIds = ints;
                for (int i = 0; i < routeIds.length; i++) {
                    //你可以通过对应的路径ID获得一条道路路径AMapNaviPath
                    AMapNaviPath path = (aMapNavi.getNaviPaths()).get(routeIds[i]);

                    //你可以通过这个AMapNaviPath生成一个RouteOverLay用于加在地图上
                    RouteOverLay routeOverLay = new RouteOverLay(amap, path, getBaseContext());
                    routeOverLay.setTrafficLine(true);
                    routeOverLay.addToMap();

                    routeOverlays.put(routeIds[i], routeOverLay);
                }

                routeOverlays.get(routeIds[0]).zoomToSpan();
                calculateSuccess = true;

            }

            @Override
            public void notifyParallelRoad(int i) {

            }

            @Override
            public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

            }

            @Override
            public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

            }

            @Override
            public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

            }
        };
        aMapNavi = AMapNavi.getInstance(getApplicationContext());
        aMapNavi.addAMapNaviListener(listener);


        ttsManager = TTSController.getInstance(getApplicationContext());
        ttsManager.init();
        ttsManager.startSpeaking();

        // 初始化Marker添加到地图
        mStartMarker = amap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.drawable.start))));
        mEndMarker = amap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.drawable.end))));

        startPoint = (EditText) findViewById(R.id.id_et_start);
        endPoint = (EditText) findViewById(R.id.id_et_end);
        startPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (changeText){
                    changeText = false;
                    return;
                }
                NaviLatLng latLng = parseEditText(s.toString());
                if (latLng == null) {
                    startPoint.setText("");
                    startLatlng = null;
                    changeText = true;
                } else {
                    startLatlng = latLng;
                    mStartMarker.setPosition(new LatLng(latLng.getLatitude(), latLng.getLongitude()));
                    startList.clear();
                    startList.add(startLatlng);
                    if (endLatlng != null) {
                        calculateRoute();
                    }
                }
            }
        });
        endPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (changeText){
                    changeText = false;
                    return;
                }
                NaviLatLng latLng = parseEditText(s.toString());
                if (latLng == null) {
                    endPoint.setText("");
                    endLatlng = null;
                    changeText = true;
                } else {
                    endLatlng = latLng;
                    mStartMarker.setPosition(new LatLng(latLng.getLatitude(), latLng.getLongitude()));
                    startList.clear();
                    startList.add(endLatlng);
                    if (startLatlng != null) {
                        calculateRoute();
                    }
                }
            }
        });
        amap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                for (RouteOverLay routeOverlay : routeOverlays.values()) {
                    routeOverlay.removeFromMap();
                }


                if (mapClickStartReady) {
                    startLatlng = new NaviLatLng(latLng.latitude, latLng.longitude);
                    mStartMarker.setPosition(latLng);
                    startList.clear();
                    startList.add(startLatlng);
                    startPoint.setText(startLatlng.getLatitude()+","+startLatlng.getLongitude());
                    changeText = true;
                    if (endLatlng != null) {
                        calculateRoute();
                    }
                }


                if (mapClickEndReady) {
                    endLatlng = new NaviLatLng(latLng.latitude, latLng.longitude);
                    mEndMarker.setPosition(latLng);
                    endList.clear();
                    endList.add(endLatlng);
                    endPoint.setText(endLatlng.getLatitude()+","+endLatlng.getLongitude());
                    changeText = true;
                    if (startLatlng != null) {
                        calculateRoute();
                    }
                }

                mapClickEndReady = false;
                mapClickStartReady = false;
            }
        });

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        aMapNavi.stopNavi();
        ttsManager.destroy();
        aMapNavi.destroy();
    }

    public void calculateRoute() {
        startList.add(startLatlng);
        endList.add(endLatlng);
        aMapNavi.calculateDriveRoute(startList, endList, null, PathPlanningStrategy.DRIVING_MULTIPLE_ROUTES);
    }

    public void changeRoute(View view) {
        if (!calculateSuccess) {
            Toast.makeText(this, "请先算路", Toast.LENGTH_SHORT).show();
            return;
        }

        if (routeIndex >= routeIds.length)
            routeIndex = 0;

        //突出选择的那条路
        for (RouteOverLay routeOverLay : routeOverlays.values()) {
            routeOverLay.setTransparency(0.7f);
        }
        routeOverlays.get(routeIds[routeIndex]).setTransparency(0);


        //必须告诉AMapNavi 你最后选择的哪条路
        aMapNavi.selectRouteId(routeIds[routeIndex]);
        Toast.makeText(this, "导航距离:" + (aMapNavi.getNaviPaths()).get(routeIds[routeIndex]).getAllLength() + "m" + "\n" + "导航时间:" + (aMapNavi.getNaviPaths()).get(routeIds[routeIndex]).getAllTime() + "s", Toast.LENGTH_SHORT).show();
        routeIndex++;

        chooseRouteSuccess = true;
    }

    public void goToEmulateActivity(View view) {
        if (chooseRouteSuccess && calculateSuccess) {
            //SimpleNaviActivity非常简单，就是startNavi而已（因为导航道路已在这个activity生成好）
            Intent intent = new Intent(this, SimpleNaviActivity.class);
            intent.putExtra("gps", false);
            startActivity(intent);
        } else {
            Toast.makeText(this, "请先算路，选路", Toast.LENGTH_SHORT).show();
        }
    }

    public void chooseStart(View view) {
        Toast.makeText(this, "请在地图上点选起点", Toast.LENGTH_SHORT).show();
        mapClickStartReady = true;
    }

    public void chooseEnd(View view) {
        Toast.makeText(this, "请在地图上点选终点", Toast.LENGTH_SHORT).show();
        mapClickEndReady = true;
    }

    public void goToGPSActivity(View view) {
        if (chooseRouteSuccess && calculateSuccess) {
            //SimpleNaviActivity非常简单，就是startNavi而已（因为导航道路已在这个activity生成好）
            Intent intent = new Intent(this, SimpleNaviActivity.class);
            intent.putExtra("gps", true);
            startActivity(intent);
        } else {
            Toast.makeText(this, "请先算路，选路", Toast.LENGTH_SHORT).show();
        }
    }

    private NaviLatLng parseEditText(String text) {
        try {
            double latD = Double.parseDouble(text.split(",")[0]);
            double lonD = Double.parseDouble(text.split(",")[1]);

            return new NaviLatLng(latD, lonD);
        } catch (Exception e) {
            Toast.makeText(this, "e:" + e, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "格式:[lat],[lon]", Toast.LENGTH_SHORT).show();
        }


        return null;
    }
}
