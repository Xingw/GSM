package com.xunce.gsmr.view.activity.baidu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.model.MarkerItem;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.model.PrjItemRealmObject;
import com.xunce.gsmr.model.baidumap.BaiduRailWayHolder;
import com.xunce.gsmr.model.event.BaiduDrawMapDataEvent;
import com.xunce.gsmr.model.event.MarkerInfoSaveEvent;
import com.xunce.gsmr.util.DBHelper;
import com.xunce.gsmr.util.gps.LocateHelper;
import com.xunce.gsmr.util.gps.MapHelper;
import com.xunce.gsmr.util.gps.MarkerHelper;
import com.xunce.gsmr.util.gps.PositionUtil;
import com.xunce.gsmr.util.view.ToastHelper;
import com.xunce.gsmr.util.view.ViewHelper;
import com.xunce.gsmr.view.activity.gaode.MarkerInfoEditActivity;
import com.xunce.gsmr.view.style.TransparentStyle;

import de.greenrobot.event.EventBus;

/**
 * 用于选址的Activity---开启当前的Acitcvity需要传递一个MarkerItem
 * -----如果MarkerItem数据不是0-0就定位到传入的位置--修改选址
 * -----如果MarkerItem数据是0-0,定位到自己当前的位置--新建选址
 * Created by ssthouse on 2015/7/17.
 */
public class BaiduMarkerActivity extends AppCompatActivity {
    private static final String TAG = "BaiduMarkerActivity";

    /**
     * 开启本Activity需要的数据
     */
    private MarkerItem markerItem;
    private int requestCode;

    /**
     * 地图View
     */
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    /**
     * 定位相关
     */
    private LocationClient mLocClient;
    //用于Activity开启时的第一次定位
    private boolean isFistIn = true;
    private LatLng currentLatLng;

    //经纬度的输入框
    private EditText etLatitude, etLongitude;
    //定位按钮
    private ImageButton ibLocate;
    //数据库地址
    private String dbPath;
    private float zoomlevel;
    private BaiduRailWayHolder railWayHolder = null;

    //定位监听器---每秒触发
    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            //更新我的位置
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
    };


    /**
     * 启动当前Activity的静态方法
     * @param activity
     * @param markerItem
     * @param requestCode
     */
    public static void start(Activity activity, MarkerItem markerItem, String dbPath,LatLng latLng,float zoom, int requestCode) {
        Intent intent = new Intent(activity, BaiduMarkerActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_MARKER_ITEM, markerItem);
        intent.putExtra(Constant.EXTRA_KEY_REQUEST_CODE, requestCode);
        intent.putExtra(Constant.EXTRA_KEY_ZOOM, zoom);
        //之前在prjEditAc的界面中心坐标
        intent.putExtra(Constant.EXTRA_KEY_LATITUDE, latLng.latitude);
        intent.putExtra(Constant.EXTRA_KEY_LONGITUDE, latLng.longitude);
        intent.putExtra(Constant.EXTRA_KEY_DBPATH,dbPath);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_baidu_mark);
        TransparentStyle.setTransparentStyle(this, R.color.color_primary);


        //获取数据
        MarkerItem wrongItem = (MarkerItem) getIntent()
                .getSerializableExtra(Constant.EXTRA_KEY_MARKER_ITEM);
        dbPath = (String) getIntent().getSerializableExtra(Constant.EXTRA_KEY_DBPATH);
        markerItem = DBHelper.getMarkerItemInDB(dbPath,wrongItem.getMarkerId());
        requestCode = getIntent().getIntExtra(Constant.EXTRA_KEY_REQUEST_CODE,
                BaiduPrjEditActivity.REQUEST_CODE_MARKER_ACTIVITY);
        zoomlevel = getIntent().getFloatExtra(Constant.EXTRA_KEY_ZOOM,15);

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        //初始化定位---设置
        LocateHelper.initLocationClient(this, mLocClient);
        //开启定位
        mLocClient.start();

        //初始化视图
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        ViewHelper.initActionBar(this, getSupportActionBar(), "选址");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMapView = (MapView) findViewById(R.id.id_map_view);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        UiSettings uiSettings = mBaiduMap.getUiSettings();
        //开启指南针
        uiSettings.setCompassEnabled(true);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));


        //经纬度输入
        etLatitude = (EditText) findViewById(R.id.id_et_latitude);
        etLongitude = (EditText) findViewById(R.id.id_et_longitude);
        //如果是编辑---定位到编辑的点
        if (markerItem != null && markerItem.getLatitude() != 0 && markerItem.getLongitude() != 0) {
            MapHelper.animateToPoint(mBaiduMap,markerItem.getBaiduLatLng());
            etLatitude.setText(""+markerItem.getLatitude());
            etLongitude.setText(""+markerItem.getLongitude());
        }else {
            markerItem = new MarkerItem();
            SQLiteDatabase db = DBHelper.openDatabase(dbPath);
            DBHelper.insertMarkerItem(db,markerItem);
//            animateToMyLocation();
            Intent intent = getIntent();
            LatLng latLng = new LatLng(intent.getDoubleExtra(Constant.EXTRA_KEY_LATITUDE, 0),
                    intent.getDoubleExtra(Constant.EXTRA_KEY_LONGITUDE, 0));
            MapHelper.animateToPoint(mBaiduMap,latLng);
            etLatitude.setText(""+getIntent().getDoubleExtra(Constant.EXTRA_KEY_LATITUDE, Constant.LATITUDE_DEFAULT));
            etLongitude.setText(""+getIntent().getDoubleExtra(Constant.EXTRA_KEY_LONGITUDE, Constant.LONGITUDE_DEFAULT));
        }
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(zoomlevel));
        //地图状态变化监听---用于监听选取的Marker位置
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //更新输入框经纬度数据
                LatLng latlng = mBaiduMap.getMapStatus().target;
                etLatitude.setText(latlng.latitude + "");
                etLongitude.setText(latlng.longitude + "");
            }
        });

        //确认按钮---设置GCJ的坐标
        findViewById(R.id.id_btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MarkerHelper.isDataValid(etLatitude, etLongitude)) {
                    //首先获取百度地图的数据
                    LatLng bdLatLng = new LatLng(MarkerHelper.getLatitude(etLatitude),
                            MarkerHelper.getLongitude(etLongitude));
                    //将高德地图数据传入
                    markerItem.changeData(PositionUtil.bd_2_gaode_latlng(bdLatLng),dbPath);
                    //设置返回值
                    setResult(Constant.RESULT_CODE_OK);
                    //退出
                    finish();
                } else {
                    ToastHelper.showSnack(BaiduMarkerActivity.this, etLatitude, "请选择有效数据");
                }
            }
        });

        //定位按钮
        ibLocate = (ImageButton) findViewById(R.id.id_ib_locate);
        ibLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locate();
            }
        });
    }

    /**
     * 根据BDLocation定位
     */
    private void locate() {
        if (currentLatLng == null) {
            return;
        }
        MapHelper.animateToPoint(mBaiduMap, currentLatLng);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_mark_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.id_action_load_digital_file:
//                break;
            case R.id.id_action_delete:
                if (requestCode == BaiduPrjEditActivity.REQUEST_CODE_MARKER_EDIT_ACTIVITY) {
                    showMakesureDeleteDialog(this, markerItem);
                }
                break;
            //编辑文本信息
            case R.id.id_action_edit_info:
                MarkerInfoEditActivity.start(this, markerItem);
                break;
            case android.R.id.home:
                if (requestCode ==Constant.REQUEST_CODE_NAVI_MAP)return true;
                if (requestCode == BaiduPrjEditActivity.REQUEST_CODE_MARKER_EDIT_ACTIVITY) {
                    finish();
                    return true;
                }
//                markerItem.delete();
                setResult(Constant.RESULT_CODE_OK);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMakesureDeleteDialog(Context context, final MarkerItem markerItem) {
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(context).
                inflate(R.layout.dialog_delete_makesure, null);
        Button confirmButton = (Button) llPrjName.findViewById(R.id.tv_input_confirm);
        Button cancelButton = (Button) llPrjName.findViewById(R.id.tv_input_cancel);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog dialog = dialogBuilder
                .setView(llPrjName)
                .create();
        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        };
        View.OnClickListener confirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerItem.delete(dbPath);
                setResult(Constant.RESULT_CODE_OK);
                dialog.dismiss();
                finish();
            }
        };
        confirmButton.setOnClickListener(confirmListener);
        cancelButton.setOnClickListener(cancelListener);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (requestCode ==Constant.REQUEST_CODE_NAVI_MAP)return;
        if (requestCode == BaiduPrjEditActivity.REQUEST_CODE_MARKER_EDIT_ACTIVITY) {
            finish();
            return;
        }
        //如果直接想返回---需要删除提前在数据库中保存的数据
        markerItem.delete(dbPath);
        setResult(Constant.RESULT_CODE_OK);
        super.onBackPressed();
    }

    //生命周期----------------------------------------------
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    /**
     * 文本编辑的回调方法
     *
     * @param event
     */
    public void onEventMainThread(MarkerInfoSaveEvent event) {
        if (event != null) {
            //更新文本文件的数据
            markerItem.setDeviceType(event.getMarkerItem().getDeviceType());
            markerItem.setKilometerMark(event.getMarkerItem().getKilometerMark());
            markerItem.setSideDirection(event.getMarkerItem().getSideDirection());
            markerItem.setDistanceToRail(event.getMarkerItem().getDistanceToRail());
            markerItem.setTowerType(event.getMarkerItem().getTowerType());
            markerItem.setTowerHeight(event.getMarkerItem().getTowerHeight());
            markerItem.setAntennaDirection1(event.getMarkerItem().getAntennaDirection1());
            markerItem.setAntennaDirection2(event.getMarkerItem().getAntennaDirection2());
            markerItem.setAntennaDirection3(event.getMarkerItem().getAntennaDirection3());
            markerItem.setAntennaDirection4(event.getMarkerItem().getAntennaDirection4());
            markerItem.save(dbPath);
        }
    }

    /**
     * 画出PrjEditActivity上已有的地图数据
     *
     * @param baiduDrawMapDataEvent
     */
    public void onEventMainThread(BaiduDrawMapDataEvent baiduDrawMapDataEvent) {
        //复制一份holder到当前activity
        if (baiduDrawMapDataEvent.getBaiduRailWayHolder() != null) {
            railWayHolder = baiduDrawMapDataEvent.getBaiduRailWayHolder();
        }
        if (railWayHolder != null) {
            railWayHolder.forcedraw(mBaiduMap);
        }
    }
}
