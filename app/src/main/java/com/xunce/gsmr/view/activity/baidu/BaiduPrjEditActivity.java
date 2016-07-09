package com.xunce.gsmr.view.activity.baidu;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.navi.model.NaviLatLng;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.orhanobut.logger.Logger;
import com.xunce.gsmr.Net.Update;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.kilometerMark.KilometerMark;
import com.xunce.gsmr.lib.kmlParser.KMLParser;
import com.xunce.gsmr.model.MarkerItem;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.model.SearchItem;
import com.xunce.gsmr.model.baidumap.BaiduMapCons;
import com.xunce.gsmr.model.baidumap.BaiduRailWayHolder;
import com.xunce.gsmr.model.event.BaiduDrawMapDataEvent;
import com.xunce.gsmr.model.event.BaiduFragmentInitFinishEvent;
import com.xunce.gsmr.model.event.ExcelXmlDataEvent;
import com.xunce.gsmr.model.event.KMLLoadFinishedEvent;
import com.xunce.gsmr.model.event.KilomarkerHolderPostEvent;
import com.xunce.gsmr.model.event.LocateModeChangeEvent;
import com.xunce.gsmr.model.event.MarkerEditEvent;
import com.xunce.gsmr.model.event.MarkerIconChangeEvent;
import com.xunce.gsmr.model.event.NaviInputEvent;
import com.xunce.gsmr.model.event.ProgressbarEvent;
import com.xunce.gsmr.util.FileHelper;
import com.xunce.gsmr.util.L;
import com.xunce.gsmr.util.gps.MapHelper;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;
import com.xunce.gsmr.util.view.ViewHelper;
import com.xunce.gsmr.view.activity.Naviinputpoint;
import com.xunce.gsmr.view.activity.PicGridActivity;
import com.xunce.gsmr.view.activity.PrjSelectActivity;
import com.xunce.gsmr.view.activity.SettingActivity;
import com.xunce.gsmr.view.style.TransparentStyle;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 开启时会接收到一个PrjItem---intent中
 */
public class BaiduPrjEditActivity extends AppCompatActivity {
    private static final String TAG = "BaiduPrjEditActivity";

    //Activity请求码
    public static final int REQUEST_CODE_ROUTE_ACTIVITY = 1000;
    public static final int REQUEST_CODE_MARKER_ACTIVITY = 1001;
    public static final int REQUEST_CODE_PICTURE_ACTIVITY = 1002;
    public static final int REQUEST_CODE_MARKER_EDIT_ACTIVITY = 1003;
    private static final int NAVI_FOOT = 100;
    private static final int NAVI_DRIVE = 101;
    private static final int NAVI_BUS = 102;

    /**
     * 地图总控制器
     */
    private BaiduMapFragment baiduMapFragment;

    /**
     * 用于点击两次退出
     */
    private long mExitTime;

    /**
     * 接收到的数据
     */
    private PrjItem prjItem;

    //菜单按钮展开
    private FloatingActionsMenu floatingActionsMenu_hide_left;
    private FloatingActionsMenu floatingActionsMenu_hide_up;
    private FloatingActionButton floatingActionButton_expand;
    private boolean expand = false;
    /**
     * 地图数据
     */
    private BaiduRailWayHolder railWayHolder;
    //地图模式选择
    private FloatingActionButton mapModeBtn;
    private static int ModeValue=Constant.MODE_MAP_2D;
    /**
     * 缩放控件
     */
    private LinearLayout zoomlayout;
    //地图数据显示
    private FloatingActionButton swMapDatabtn;
    private boolean isChecked = false;
    private boolean clear = false;
    public boolean isMapTextShowed = false;
    //公里标显示标志位
    private View llPosition;
    BaiduMap.OnMapStatusChangeListener listener;
    //进度条
    private View pbBlock;
    //进度条说明文字
    private TextView tvPbComment;
    //是否正在绘制
    private static boolean drawing = false;
    private TextView etStart;
    private TextView etEnd;
    private SearchItem startsearch = new SearchItem("我的位置","我的位置");
    private SearchItem endsearch;
    /**
     * 用于延时发送数据
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    EventBus.getDefault().post(new BaiduDrawMapDataEvent(railWayHolder));
                    break;
                case 1:
                    EventBus.getDefault().post(new KilomarkerHolderPostEvent(railWayHolder.getKilometerMarkHolder()));
                    break;
                case 2:
                    baiduMapFragment.animateToPoint(new LatLng(getIntent().getDoubleExtra(Constant.EXTRA_KEY_LATITUDE,Constant.LATITUDE_DEFAULT),getIntent().getDoubleExtra(Constant.EXTRA_KEY_LONGITUDE,Constant.LONGITUDE_DEFAULT)));
                    break;
            }
        }
    };
    /**
     * 用于图层选择
     */
    private static String[] layername;
    private static boolean[] layerboolean;
    private KMLParser kmlParser;


    /**
     * 用于更加方便的开启Activity
     * 后面几个参数可以用来传递-----放入intent 的数据
     *
     * @param activity
     */
    public static void start(Activity activity, PrjItem prjItem, Location location) {
        Intent intent = new Intent(activity, BaiduPrjEditActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_PRJ_ITEM, prjItem);
        if (location != null) {
            intent.putExtra(Constant.EXTRA_KEY_LONGITUDE, location.getLongitude());
            intent.putExtra(Constant.EXTRA_KEY_LATITUDE, location.getLatitude());
        }
        intent.putExtra(Constant.EXTRA_KEY_PRJ_ITEM, prjItem);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_prj_edit);
        //注册eventbus
        EventBus.getDefault().register(this);
        TransparentStyle.setTransparentStyle(this, R.color.color_primary);

        //对sharedpreference进行初始化
        PreferenceHelper.getInstance(this).initMarkerIconPreference();

        prjItem = (PrjItem) getIntent().getParcelableExtra(Constant.EXTRA_KEY_PRJ_ITEM);

        //初始化View
        initView();

        //检查版本更新
        if(Constant.firstOpen) {
            Update.checkversion(this);
            Constant.firstOpen = false;
        }
    }

    /**
     * 初始化数据
     */
    private void initdata(){
        railWayHolder = new BaiduRailWayHolder(this, prjItem.getDbLocation());
        kmlParser = new KMLParser(prjItem.getDbLocation());
    }

    /**
     * 初始化View
     */
    private void initView() {
        ViewHelper.initActionBar(this, getSupportActionBar(), prjItem.getPrjName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //progressbar控件
        pbBlock = findViewById(R.id.id_pb_block);
        tvPbComment = (TextView) findViewById(R.id.id_tv_pb_comment);

        zoomlayout = (LinearLayout) findViewById(R.id.view_zoom_control);
        floatingActionsMenu_hide_left = (FloatingActionsMenu) findViewById(R.id.multiple_actions_hide_left);
        floatingActionsMenu_hide_up = (FloatingActionsMenu) findViewById(R.id.multiple_actions_hide_up);
        floatingActionButton_expand = (FloatingActionButton) findViewById(R.id
                .multiple_actions_expand);
        floatingActionButton_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!expand) {
                    expand=true;
                    zoomlayout.setVisibility(View.GONE);
                    floatingActionsMenu_hide_left.expand();
                    floatingActionsMenu_hide_up.expand();
                    floatingActionButton_expand.setIcon(R.drawable.ic_close);
                }else {
                    expand=false;
                    zoomlayout.setVisibility(View.VISIBLE);
                    floatingActionsMenu_hide_left.collapse();
                    floatingActionsMenu_hide_up.collapse();
                    floatingActionButton_expand.setIcon(R.drawable.ic_menu_white_48dp);
                }
            }
        });

        //获取缩放控件
        findViewById(R.id.btn_zoom_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baiduMapFragment.getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomIn());
            }
        });
        findViewById(R.id.btn_zoom_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baiduMapFragment.getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomOut());
            }
        });
        //初始化map_mode控件
        initMapMode();

        //启动Map的片段
        Bundle bundle = new Bundle();
        bundle.putParcelable("prjItem", prjItem);
        baiduMapFragment = BaiduMapFragment.getInstance(bundle);
        getFragmentManager().beginTransaction().replace(R.id.id_fragment_container,
                baiduMapFragment).commit();

        //选址
        findViewById(R.id.fb_action_choose_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawing)return;
                //首先创建一个markerItem放到数据库中(在新开启Activity中--如果没有点击确定---就删除)
                MarkerItem markerItem = new MarkerItem();
                BaiduMarkerActivity.start(BaiduPrjEditActivity.this,
                        markerItem, prjItem.getDbLocation(),baiduMapFragment.getTarget(),baiduMapFragment.getBaiduMap().getMapStatus().zoom, REQUEST_CODE_MARKER_ACTIVITY);
                handler.sendEmptyMessageDelayed(0, 300);
            }
        });

        //获取缩放控件
        findViewById(R.id.btn_zoom_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baiduMapFragment.getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomIn());
            }
        });
        findViewById(R.id.btn_zoom_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baiduMapFragment.getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomOut());
            }
        });
        //显示地图数据按钮
        swMapDatabtn = (FloatingActionButton) findViewById(R.id.id_btn_sw_map_data);
        swMapDatabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawing) {
                    ToastHelper.show(BaiduPrjEditActivity.this,"地图数据正在绘制中，请稍后");
                    return;
                }
                if ((!isChecked && railWayHolder == null) || (railWayHolder.isempty())) {
                    ToastHelper.show(BaiduPrjEditActivity.this, "请先加载数据文件");
                } else if (!isChecked && railWayHolder != null) {
                    EventBus.getDefault().post(new ProgressbarEvent(true));
                    isChecked = true;
                    swMapDatabtn.setIcon(R.drawable.map_action_draw_open);
                    if(railWayHolder.getTextList() != null && railWayHolder.getTextList().size() !=0) {
                        MapHelper.animateToPoint(baiduMapFragment.getBaiduMap(),railWayHolder.getTextList().get(0).getLatLng());
                    }
                    Float zoom =baiduMapFragment.getBaiduMap().getMapStatus().zoom;
                    if (zoom > BaiduMapCons.zoomLevel) {
                        railWayHolder.draw(baiduMapFragment.getBaiduMap(),clear);
                        clear = false;
                    } else {
                        railWayHolder.drawLine(baiduMapFragment.getBaiduMap(),clear);
                        clear = false;
                    }
                } else if (isChecked) {
//                    EventBus.getDefault().post(new ProgressbarEvent(true));
                    Logger.d("更新了");
                    isChecked = false;
                    clear = true;
                    swMapDatabtn.setIcon(R.drawable.map_action_draw_close);
                    baiduMapFragment.getBaiduMap().clear();
                    baiduMapFragment.loadMarker();
                }
            }
        });

        //监测---地图的大小变化---画出/隐藏---文字
        listener = new BaiduMap.OnMapStatusChangeListener() {
            /**
             * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
             * @param status 地图状态改变开始时的地图状态
             */
            public void onMapStatusChangeStart(MapStatus status){
            }
            /**
             * 地图状态变化中
             * @param status 当前地图状态
             */
            public void onMapStatusChange(MapStatus status){
            }
            /**
             * 地图状态改变结束
             * @param status 地图状态改变结束后的地图状态
             */
            public void onMapStatusChangeFinish(MapStatus status){
                if (drawing)return;
                //如果 xml文件已经加载 且 switch为开
                if (railWayHolder != null && isChecked) {
                    Float zoom =status.zoom;
                    //如果放大到16以上
                    if (zoom > BaiduMapCons.zoomLevel && !isMapTextShowed) {
                        railWayHolder.drawText(baiduMapFragment.getBaiduMap(),clear);
                        isMapTextShowed = true;
                    } else if (zoom < BaiduMapCons.zoomLevel && isMapTextShowed) {
                        railWayHolder.hideText();
                        isMapTextShowed = false;
                    }
                }
            }
        };

        //定位
        findViewById(R.id.id_btn_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawing)return;
                baiduMapFragment.locate();
            }
        });

        //测量
        findViewById(R.id.fb_action_measure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawing)return;
                //开启测量Activity
                handler.sendEmptyMessageDelayed(0, 300);
                BaiduMeasureActivity.start(BaiduPrjEditActivity.this,
                        baiduMapFragment.getBaiduMap().getMapStatus().target,
                        baiduMapFragment.getBaiduMap().getMapStatus().zoom);
            }
        });
        //公里标
        llPosition = findViewById(R.id.id_ll_position);
        findViewById(R.id.fb_action_marker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawing)return;
                showFindMarkerDialog(BaiduPrjEditActivity.this);
                //baiduMapFragment.loadMarker(prjItem);
            }
        });
        double lon = getIntent().getDoubleExtra(Constant.EXTRA_KEY_LONGITUDE,Constant.LONGITUDE_DEFAULT);
        Logger.d("lon:%f",lon);
        handler.sendEmptyMessageDelayed(2,1000);
    }

    /**
     * 寻找公里标对话框
     * @param context
     */
    private void showFindMarkerDialog(final Context context) {
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(context).
                inflate(R.layout.dialog_prj_name, null);
        final EditText etPrjName = (EditText) llPrjName.findViewById(R.id.id_et);
        Button confirmButton = (Button) llPrjName.findViewById(R.id.tv_input_confirm);
        Button cancelButton = (Button) llPrjName.findViewById(R.id.tv_input_cancel);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog dialog = dialogBuilder
                .setTitle("公里标")
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
                KilometerMark kilometerMark= railWayHolder.getKilometerMarkHolder().findKM(etPrjName.getText().toString());
                if (kilometerMark==null){
                    ToastHelper.show(BaiduPrjEditActivity.this,"未找到该公里标");
                }else{
                    baiduMapFragment.animateToPoint(kilometerMark.getBaiDuLatlng());
                }
            }
        };
        confirmButton.setOnClickListener(confirmListener);
        cancelButton.setOnClickListener(cancelListener);
        dialog.show();
    }

    /**
     * 初始化地图Mode控件
     */
    private void initMapMode() {
        //地图模式切换
        mapModeBtn = (FloatingActionButton) findViewById(R.id.id_ib_open_map_mode);
        //map_mode切换
        mapModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (ModeValue){
                    case Constant.MODE_MAP_2D: {//2D地图切换至3D地图
                        ModeValue = Constant.MODE_MAP_SATELLITE;
                        mapModeBtn.setIcon(R.drawable.map_action_mode_satellite);
                        baiduMapFragment.getBaiduMap().setMapType(BaiduMap.MAP_TYPE_SATELLITE);
//                        MapStatus mapStatus = new MapStatus.Builder(baiduMapFragment.getBaiduMap().getMapStatus()).overlook(-45).build();
//                        MapStatusUpdate msu = MapStatusUpdateFactory.newMapStatus(mapStatus);
//                        baiduMapFragment.getBaiduMap().animateMapStatus(msu);
                        break;
                    }
//                    case Constant.MODE_MAP_3D: {//3D地图切换至卫星地图
//                        ModeValue = Constant.MODE_MAP_SATELLITE;
//                        mapModeBtn.setIcon(R.drawable.map_action_mode_satellite);
//                        baiduMapFragment.getBaiduMap().setMapType(BaiduMap.MAP_TYPE_SATELLITE);
//                        MapStatus mapStatus = new MapStatus.Builder(baiduMapFragment.getBaiduMap().getMapStatus()).overlook(0).build();
//                        MapStatusUpdate msu = MapStatusUpdateFactory.newMapStatus(mapStatus);
//                        baiduMapFragment.getBaiduMap().animateMapStatus(msu);
//                        break;
//                    }
                    case Constant.MODE_MAP_SATELLITE: {//卫星地图切换至2D地图
                        ModeValue = Constant.MODE_MAP_2D;
                        mapModeBtn.setIcon(R.drawable.map_action_mode_2d);
                        baiduMapFragment.getBaiduMap().setMapType(BaiduMap.MAP_TYPE_NORMAL);
                        break;
                    }
                }

            }
        });
    }

    /**
     * 和InfoWindow绑定的点击事件
     *
     * @param v
     */
    public void clickEdit(View v) {
        baiduMapFragment.hideInfoWindow();
        //生成MarkerItem--跳转到MarkerEditActivity
        BaiduMarkerActivity.start(this, baiduMapFragment.getMarkerHolder().getCurrentMarkerItem()
                ,prjItem.getDbLocation(),baiduMapFragment.getTarget(),
                baiduMapFragment.getBaiduMap().getMapStatus().zoom,
                BaiduPrjEditActivity.REQUEST_CODE_MARKER_EDIT_ACTIVITY);
        handler.sendEmptyMessageDelayed(0,300);
    }

    /**
     * 和InfoWindow绑定的点击事件
     *
     * @param v
     */
    public void clickPhoto(View v) {
        baiduMapFragment.hideInfoWindow();
        PicGridActivity.start(this, baiduMapFragment.getMarkerHolder().getCurrentMarkerItem(),
                prjItem.getDbLocation(),
                BaiduPrjEditActivity.REQUEST_CODE_PICTURE_ACTIVITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_prj_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //切换工程
//            case R.id.id_action_change_project:
//                finish();
//                PrjSelectActivity.start(this, true);
                //加载铁路地图
//            case R.id.id_action_load_digital_file:
//                //首先判断数据库是否绑定
//                baiduMapFragment.loadRail();
//                break;
            //数据导出
            case R.id.id_action_export_data:
                FileHelper.sendDbFile(this,prjItem.getDbLocation());
                break;
            case R.id.id_action_layer_choice:
                showChoiceLayerDialog();
                break;
            case R.id.id_action_offline_map:
                //开启离线地图管理Activity
                BaiduOfflineActivity.start(this);
                break;
            case R.id.id_action_navi_map:
                showNaviDialog();
                break;
            //设置
            case R.id.id_action_setting:
                SettingActivity.start(this);
                break;
            //返回
            case android.R.id.home:
                finish();
                PrjSelectActivity.start(this, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 导航功能
     */
    private void showNaviDialog() {
        LinearLayout llnavidialog = (LinearLayout) LayoutInflater.from(this).
                inflate(R.layout.dialog_navi_choice, null);
        etStart = (TextView) llnavidialog.findViewById(R.id.et_navi_start);
        etEnd = (TextView) llnavidialog.findViewById(R.id.et_navi_end);
        etStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Naviinputpoint.start(BaiduPrjEditActivity.this,etStart.getText().toString(),
                        NaviInputEvent.START, Constant.EXTRA_KEY_GAODE);
                handler.sendEmptyMessageDelayed(1,300);
            }
        });
        etEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Naviinputpoint.start(BaiduPrjEditActivity.this,etEnd.getText().toString(),
                        NaviInputEvent.END, Constant.EXTRA_KEY_GAODE);
                handler.sendEmptyMessageDelayed(1,300);
            }
        });
        //Button FootButton = (Button) llnavidialog.findViewById(R.id.btn_navi_foot);
        Button DriveButton = (Button) llnavidialog.findViewById(R.id.btn_navi_drive);
       // Button BusButton = (Button) llnavidialog.findViewById(R.id.btn_navi_bus);
//        FootButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (etStart.getText().toString().isEmpty() || etEnd.getText().toString().isEmpty()){
//                    ToastHelper.show(BaiduPrjEditActivity.this,"请输入起点或终点名称");
//                    return;
//                }
//                Navistart(NAVI_FOOT,etStart.getText().toString(),etEnd.getText().toString());
//            }
//        });
        DriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etStart.getText().toString().isEmpty() || etEnd.getText().toString().isEmpty()){
                    ToastHelper.show(BaiduPrjEditActivity.this,"请输入起点或终点名称");
                    return;
                }
                Navistart(NAVI_DRIVE,etStart.getText().toString(),etEnd.getText().toString());
            }
        });
//        BusButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (etStart.getText().toString().isEmpty() || etEnd.getText().toString().isEmpty()){
//                    ToastHelper.show(BaiduPrjEditActivity.this,"请输入起点或终点名称");
//                    return;
//                }
//                Navistart(NAVI_BUS,etStart.getText().toString(),etEnd.getText().toString());
//            }
//        });
        new AlertDialog.Builder(this)
                .setTitle("导航")
                .setView(llnavidialog)
                .setCancelable(true)
                .show();
    }

    private void Navistart(int method, String start, String end) {
        RouteParaOption para = new RouteParaOption();
        if (start.contains("选点")){
            NaviLatLng latLng = parseEditText(startsearch.getValue());
            para.startPoint(new LatLng(latLng.getLatitude(),latLng.getLongitude()));
        }else {
            para.startName(start);
        }
        if (start.contains("选点")){
            NaviLatLng latLng = parseEditText(endsearch.getValue());
            para.endPoint(new LatLng(latLng.getLatitude(),latLng.getLongitude()));
        }else {
            para.endName(end);
        }
        switch (method) {
            case NAVI_FOOT:
                try {
                    BaiduMapRoutePlan.openBaiduMapWalkingRoute(para, this);
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog();
                }
                break;
            case NAVI_DRIVE:
                try {
                    BaiduMapRoutePlan.openBaiduMapDrivingRoute(para, this);
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog();
                }
                break;
            case NAVI_BUS:
                try {
                    para.busStrategyType(RouteParaOption.EBusStrategyType.bus_recommend_way);
                    BaiduMapRoutePlan.openBaiduMapTransitRoute(para, this);
                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog();
                }
                break;
//        }
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
    /**
     * 提示未安装百度地图app或app版本过低
     */
    public void showDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OpenClientUtil.getLatestBaiduMapApp(BaiduPrjEditActivity.this);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * 图层选择对话框
     */
    private void showChoiceLayerDialog() {
        if (layername == null) {
            layername = new String[railWayHolder.getLayerList().size()];
            layerboolean = new boolean[railWayHolder.getLayerList().size()];
            for (int i = 0; i < railWayHolder.getLayerList().size(); i++) {
                layername[i] = railWayHolder.getLayerList().get(i);
            }
            for (int i = 0; i < layerboolean.length; i++) {
                layerboolean[i] = true;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("图层选择")
                .setMultiChoiceItems(layername, layerboolean, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        layerboolean[which] =isChecked;
                    }
                })
                .setCancelable(false)
                .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> showList = new ArrayList<String>();
                        for (int i = 0; i < layerboolean.length; i++) {
                            if (layerboolean[i]){
                                showList.add(layername[i]);
                            }
                        }
                        railWayHolder.initshow(showList);
//                        if (isChecked){
//                            if (baiduMapFragment.getBaiduMap().getMapStatus().zoom > BaiduMapCons.zoomLevel) {
//                                railWayHolder.draw(baiduMapFragment.getBaiduMap());
//                            } else {
//                                railWayHolder.drawLine(baiduMapFragment.getBaiduMap());
//                            }
//                        }
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_MARKER_ACTIVITY:
                if (resultCode == Constant.RESULT_CODE_OK) {
                    baiduMapFragment.loadMarker();
                }
                break;
            case REQUEST_CODE_MARKER_EDIT_ACTIVITY:
                if (resultCode == Constant.RESULT_CODE_OK) {
                    baiduMapFragment.loadMarker();
                }
                break;
            case Constant.REQUEST_CODE_DB_FILE:
                //如果是加载.db文件
                Uri uri = data.getData();
                L.log(TAG, uri.getEncodedPath());
                break;
            case REQUEST_CODE_ROUTE_ACTIVITY:
                break;
            case REQUEST_CODE_PICTURE_ACTIVITY:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 实现两次返回退出程序
     */
    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    //生命周期***********************************************************
    @Override
    protected void onPause() {
        super.onPause();
        baiduMapFragment.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        baiduMapFragment.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baiduMapFragment.destory();
    }

    //事件监听******************************************************************
    /**
     * setting界面的定位mode改变事件
     *
     * @param event
     */
    public void onEventMainThread(LocateModeChangeEvent event) {
        baiduMapFragment.initLocationClient();
    }

    public void onEventMainThread(BaiduFragmentInitFinishEvent event) {
        initdata();
        baiduMapFragment.getBaiduMap().setOnMapStatusChangeListener(listener);
    }
    /**
     * prjEditActivity的回调方法
     */
    public void onEventMainThread(MarkerEditEvent markerEditEvent) {
        switch (markerEditEvent.getBackState()) {
            case CHANGED:
                baiduMapFragment.loadMarker(prjItem);
                break;
            case UNCHANGED:
                break;
        }
    }

    /**
     * 更新Marker图标颜色
     *
     * @param event
     */
    public void onEventMainThread(MarkerIconChangeEvent event) {
        if (event.isChanged()) {
            baiduMapFragment.loadMarker(prjItem);
        }
    }

    /**
     * 提示xml文件加载情况
     *
     * @param excelXmlDataEvent
     */
    public void onEventMainThread(ExcelXmlDataEvent excelXmlDataEvent) {
        if (excelXmlDataEvent.isParseSuccess()) {
            ToastHelper.show(this, "xml中预设标记点数据添加成功");
        } else {
            ToastHelper.show(this, "xml中预设标记点数据添加失败, 请检查excel文件格式是否正确");
        }
    }

    /**
     * progressbar是否显示的回调控制方法
     *
     * @param progressbarEvent
     */
    public void onEventMainThread(ProgressbarEvent progressbarEvent) {
        if (progressbarEvent.isShow()) {
//            tvPbComment.setText("正在加载，请稍后。");
//            pbBlock.setVisibility(View.VISIBLE);
            drawing = true;
        } else {
            drawing = false;
//            pbBlock.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * kml数据加载完成事件
     * @param kmlLoadFinishedEvent
     */
    public void onEventMainThread(KMLLoadFinishedEvent kmlLoadFinishedEvent) {
        kmlParser.draw(baiduMapFragment.getBaiduMap());
    }

    /**
     * Navi起点终点输入返回数据
     * @param naviInputEvent
     */
    public void onEventMainThread(NaviInputEvent naviInputEvent) {
        if (etEnd == null || etStart == null)return;
        if (naviInputEvent.getStyle() == naviInputEvent.START){
            etStart.setText(naviInputEvent.getSearchItem().getText());
            startsearch = naviInputEvent.getSearchItem();
        }else {
            etEnd.setText(naviInputEvent.getSearchItem().getText());
            endsearch = naviInputEvent.getSearchItem();
        }
    }
}
