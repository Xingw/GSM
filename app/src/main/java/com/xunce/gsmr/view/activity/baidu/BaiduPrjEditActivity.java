package com.xunce.gsmr.view.activity.baidu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.model.MarkerItem;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.model.baidumap.BaiduMapCons;
import com.xunce.gsmr.model.event.DrawMapDataEvent;
import com.xunce.gsmr.util.FileHelper;
import com.xunce.gsmr.util.L;
import com.xunce.gsmr.util.gps.MapHelper;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;
import com.xunce.gsmr.util.view.ViewHelper;
import com.xunce.gsmr.view.activity.PicGridActivity;
import com.xunce.gsmr.view.activity.PrjSelectActivity;
import com.xunce.gsmr.view.activity.SettingActivity;
import com.xunce.gsmr.view.activity.gaode.GaodeMarkerActivity;
import com.xunce.gsmr.view.activity.gaode.GaodePrjEditActivity;
import com.xunce.gsmr.view.style.TransparentStyle;

import de.greenrobot.event.EventBus;
import io.realm.Realm;

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
    public boolean isMapTextShowed = false;
    //公里标显示标志位
    private View llPosition;
    /**
     * 用于延时发送数据
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                //EventBus.getDefault().post(new DrawMapDataEvent(railWayHolder));
            }
        }
    };



    /**
     * 用于更加方便的开启Activity
     * 后面几个参数可以用来传递-----放入intent 的数据
     *
     * @param activity
     */
    public static void start(Activity activity, PrjItem prjItem) {
        Intent intent = new Intent(activity, BaiduPrjEditActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_PRJ_ITEM, prjItem);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_prj_edit);
        TransparentStyle.setTransparentStyle(this, R.color.color_primary);

        //对sharedpreference进行初始化
        PreferenceHelper.getInstance(this).initMarkerIconPreference();

        prjItem = (PrjItem) getIntent().getParcelableExtra(Constant.EXTRA_KEY_PRJ_ITEM);

        //初始化View
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        ViewHelper.initActionBar(this, getSupportActionBar(), prjItem.getPrjName());

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
                //首先创建一个markerItem放到数据库中(在新开启Activity中--如果没有点击确定---就删除)
                MarkerItem markerItem = new MarkerItem();
                BaiduMarkerActivity.start(BaiduPrjEditActivity.this,
                        markerItem, prjItem.getDbLocation(), REQUEST_CODE_MARKER_ACTIVITY);
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
                if ((!isChecked && railWayHolder == null) || (railWayHolder.isempty())) {
                    ToastHelper.show(BaiduPrjEditActivity.this, "请先加载数据文件");
                } else if (!isChecked && railWayHolder != null) {
                    isChecked = true;
                    swMapDatabtn.setIcon(R.drawable.map_action_draw_open);
                    if(railWayHolder.getTextList() != null && railWayHolder.getTextList().size() !=0) {
                        MapHelper.animateToPoint(baiduMapFragment.getBaiduMap(),railWayHolder.getTextList().get(0).getLatLng());
                    }
                    Float zoom =baiduMapFragment.getBaiduMap().getMapStatus().zoom;
                    if (zoom > BaiduMapCons.zoomLevel) {
                        railWayHolder.draw(baiduMapFragment.getBaiduMap());
                    } else {
                        railWayHolder.drawLine(baiduMapFragment.getBaiduMap());
                    }
                } else if (isChecked) {
                    isChecked = false;
                    swMapDatabtn.setIcon(R.drawable.map_action_draw_close);
                    railWayHolder.hide();
                }
            }
        });

        //监测---地图的大小变化---画出/隐藏---文字
        BaiduMap.OnMapStatusChangeListener listener = new BaiduMap.OnMapStatusChangeListener() {

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
                //如果 xml文件已经加载 且 switch为开
                if (railWayHolder != null && isChecked) {
                    Float zoom =baiduMapFragment.getBaiduMap().getMapStatus().zoom;
                    //如果放大到16以上
                    if (zoom > BaiduMapCons.zoomLevel && !isMapTextShowed) {
                        railWayHolder.drawText(baiduMapFragment.getBaiduMap());
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
                baiduMapFragment.locate();
            }
        });

        //测量
        findViewById(R.id.fb_action_measure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启测量Activity
                BaiduMeasureActivity.start(BaiduPrjEditActivity.this, baiduMapFragment.getBaiduMap().getMapStatus().target);
            }
        });
        //公里标
        llPosition = findViewById(R.id.id_ll_position);
        findViewById(R.id.fb_action_marker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFindMarkerDialog(BaiduPrjEditActivity.this);
                loadMarker(prjItem);
            }
        });
    }

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
                ToastHelper.show(context,"功能正在开发中……");
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
                        ModeValue = Constant.MODE_MAP_3D;
                        mapModeBtn.setIcon(R.drawable.map_action_mode_3d);
                        baiduMapFragment.getBaiduMap().setMapType(BaiduMap.MAP_TYPE_NORMAL);
                        MapStatus mapStatus = new MapStatus.Builder(baiduMapFragment.getBaiduMap().getMapStatus()).rotate(45).build();
                        MapStatusUpdate msu = MapStatusUpdateFactory.newMapStatus(mapStatus);
                        baiduMapFragment.getBaiduMap().animateMapStatus(msu);
                        break;
                    }
                    case Constant.MODE_MAP_3D: {//3D地图切换至卫星地图
                        ModeValue = Constant.MODE_MAP_SATELLITE;
                        mapModeBtn.setIcon(R.drawable.map_action_mode_satellite);
                        baiduMapFragment.getBaiduMap().setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                        MapStatus mapStatus = new MapStatus.Builder(baiduMapFragment.getBaiduMap().getMapStatus()).rotate(0).build();
                        MapStatusUpdate msu = MapStatusUpdateFactory.newMapStatus(mapStatus);
                        baiduMapFragment.getBaiduMap().animateMapStatus(msu);
                        break;
                    }
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
                ,prjItem.getDbLocation(), BaiduPrjEditActivity.REQUEST_CODE_MARKER_EDIT_ACTIVITY);
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
            case R.id.id_action_offline_map:
                //开启离线地图管理Activity
                BaiduOfflineActivity.start(this);
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
}
