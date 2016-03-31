package com.xunce.gsmr.view.activity.gaode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.xunce.gsmr.Net.Update;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.lib.xmlparser.XmlParser;
import com.xunce.gsmr.lib.digitalmap.DigitalMapHolder;
import com.xunce.gsmr.lib.kmlParser.KMLParser;
import com.xunce.gsmr.lib.markerParser.XmlMarkerParser;
import com.xunce.gsmr.model.MarkerItem;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.model.event.CADReadFinishEvent;
import com.xunce.gsmr.model.event.CompressFileEvent;
import com.xunce.gsmr.model.event.DrawMapDataEvent;
import com.xunce.gsmr.model.event.ExcelXmlDataEvent;
import com.xunce.gsmr.model.event.LocateModeChangeEvent;
import com.xunce.gsmr.model.event.MarkerEditEvent;
import com.xunce.gsmr.model.event.MarkerIconChangeEvent;
import com.xunce.gsmr.model.event.ProgressbarEvent;
import com.xunce.gsmr.model.gaodemap.GaodeMapCons;
import com.xunce.gsmr.model.gaodemap.GaodeRailWayHolder;
import com.xunce.gsmr.util.FileHelper;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;
import com.xunce.gsmr.util.view.ViewHelper;
import com.xunce.gsmr.view.activity.PicGridActivity;
import com.xunce.gsmr.view.activity.PrjSelectActivity;
import com.xunce.gsmr.view.activity.SettingActivity;
import com.xunce.gsmr.view.style.TransparentStyle;

import java.io.File;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * 高德地图编辑Activity Created by ssthouse on 2015/9/14.
 */
public class GaodePrjEditActivity extends GaodeBaseActivity {
    //Activity请求码
    public static final int REQUEST_CODE_ROUTE_ACTIVITY = 1000;
    //创建Marker的Activity
    public static final int REQUEST_CODE_MARKER_ACTIVITY = 1001;
    //编辑Marker的Activity
    public static final int REQUEST_CODE_MARKER_EDIT_ACTIVITY = 1003;
    //打开当前Marker的图片展示的Activity
    public static final int REQUEST_CODE_PICTURE_ACTIVITY = 1002;
    //选取---初始选址文件(.xml)---数字地图文 件--xml文件---kml文件
    public static final int REQUEST_CODE_LOAD_XML_MARKER_FILE = 1007;
    public static final int REQUEST_CODE_LOAD_DIGITAL_FILE = 1004;
    public static final int REQUEST_CODE_LOAD_XML_FILE = 1005;
    public static final int REQUEST_CODE_LOAD_KML_FILE = 1006;

    /**
     * 用于点击两次退出
     */
    private long mExitTime;
    /**
     * 编辑的PrjItem
     */
    private PrjItem prjItem;
    private static boolean firstOpen = true;
    /**
     * 控件
     */
    //进度条
    private View pbBlock;
    //进度条说明文字
    private TextView tvPbComment;

    //公里标显示标志位
    private View llPosition;
    private boolean isLlPositionShowed;
    //cad的xml文件开关
    private FloatingActionButton swMapDatabtn;
    private boolean isMapTextShowed = false;
    //地图设置——指南针功能
    private UiSettings mUiSettings;
    /**
     * 数据解析器
     */
    //初始选址文件解析器
    private XmlMarkerParser xmlMarkerParser;
    //xml数据文件的解析工具
    private XmlParser xmlParser;
    private GaodeRailWayHolder railWayHolder;
    //数字地图文件解析器
    private DigitalMapHolder digitalMapHolder;
    private static boolean isChecked = false;
    //地图模式选择
    private FloatingActionButton mapModeBtn;
    private static int ModeValue=Constant.MODE_MAP_2D;

    //菜单按钮展开
    private FloatingActionsMenu floatingActionsMenu_hide_left;
    private FloatingActionsMenu floatingActionsMenu_hide_up;
    private FloatingActionButton floatingActionButton_expand;
    private boolean expand = false;

    private LinearLayout zoomlayout;
    /**
     * 用于延时发送数据
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                EventBus.getDefault().post(new DrawMapDataEvent(railWayHolder));
            }
        }
    };

    /**
     * 启动Activity
     *
     * @param activity 开启的上下文Activity
     * @param prjItem  当前处理的PrjItem
     */
    public static void start(Activity activity, PrjItem prjItem) {
        Intent intent = new Intent(activity, GaodePrjEditActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_PRJ_ITEM, prjItem);
//        intent.putExtra(Constant.EXTRA_KEY_PRJ_ITEM_DBPATH, prjItem.getDbLocation());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaode_prj_edit);
        //注册eventbus
        EventBus.getDefault().register(this);
        TransparentStyle.setTransparentStyle(this, R.color.color_primary);
        super.init(savedInstanceState);

        //对sharedpreference进行初始化
        PreferenceHelper.getInstance(this).initMarkerIconPreference();

        //接收数据
        prjItem =(PrjItem)getIntent().getParcelableExtra(Constant.EXTRA_KEY_PRJ_ITEM);

        //启动定位
        super.initLocate();

        //初始化View
        initView();

        //初始化数据
        initdata();

        //检查版本更新
        if(firstOpen) {
            Update.checkversion(this);
            firstOpen = false;
        }
    }

    /**
     * 初始化View
     */
    private void initView() {
        ViewHelper.initActionBar(this, getSupportActionBar(), prjItem.getPrjName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        zoomlayout = (LinearLayout) findViewById(R.id.view_zoom_control);
        //progressbar控件
        pbBlock = findViewById(R.id.id_pb_block);
        tvPbComment = (TextView) findViewById(R.id.id_tv_pb_comment);

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

        //放大缩小按钮
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
        //初始化地图Mode控件
        initMapMode();
        //填充Marker
        loadMarker(prjItem);
        //初始化Marker的点击事件--以及InfoWindow的填充
        initMarkerClick();
        //初始化--xml文件的Switch

        swMapDatabtn = (FloatingActionButton) findViewById(R.id.id_btn_sw_map_data);
        swMapDatabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((!isChecked && railWayHolder == null) || (railWayHolder.isempty())) {
                    ToastHelper.show(GaodePrjEditActivity.this, "请先加载数据文件");
                } else if (!isChecked && railWayHolder != null) {
                    isChecked = true;
                    swMapDatabtn.setIcon(R.drawable.map_action_draw_open);
                    if(railWayHolder.getTextList() != null && railWayHolder.getTextList().size() !=0) {
                        getaMap().moveCamera(CameraUpdateFactory.changeLatLng(railWayHolder.getTextList().get(0).getLatLng()));
                    }
                    CameraPosition cameraPosition = getaMap().getCameraPosition();

                    if (cameraPosition.zoom > GaodeMapCons.zoomLevel) {
                        railWayHolder.draw(getaMap());
                    } else {
                        railWayHolder.drawLine(getaMap());
                    }
                } else if (isChecked) {
                    isChecked = false;
                    swMapDatabtn.setIcon(R.drawable.map_action_draw_close);
                    railWayHolder.hide();
                }
            }
        });
        //监测---地图的大小变化---画出/隐藏---文字
        getaMap().setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                //如果 xml文件已经加载 且 switch为开
                if (railWayHolder != null && isChecked) {
                    //如果放大到16以上
                    if (cameraPosition.zoom > GaodeMapCons.zoomLevel && !isMapTextShowed) {
                        railWayHolder.drawText(getaMap());
                        isMapTextShowed = true;
                        Timber.e(">>>> 16了");
                    } else if (cameraPosition.zoom < GaodeMapCons.zoomLevel && isMapTextShowed) {
                        Timber.e("缩小到16以下了");
                        railWayHolder.hideText();
                        isMapTextShowed = false;
                    }
                }
            }
        });
        //选址
        findViewById(R.id.fb_action_choose_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //首先创建一个markerItem放到数据库中(在新开启Activity中--如果没有点击确定---就删除)
                MarkerItem markerItem = new MarkerItem();
                GaodeMarkerActivity.start(GaodePrjEditActivity.this,
                        markerItem, prjItem.getDbLocation(), getaMap().getCameraPosition().target,
                        REQUEST_CODE_MARKER_ACTIVITY);
                handler.sendEmptyMessageDelayed(0, 300);
            }
        });
        //定位
        findViewById(R.id.id_btn_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GaodePrjEditActivity.super.animateToMyLocation();
            }
        });
        //测量
        findViewById(R.id.fb_action_measure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GaodeMeasureActivity.start(GaodePrjEditActivity.this, getaMap().getCameraPosition().target);
            }
        });
        //公里标
        llPosition = findViewById(R.id.id_ll_position);
        findViewById(R.id.fb_action_marker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFindMarkerDialog(GaodePrjEditActivity.this);
                loadMarker(prjItem);
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initdata(){
        railWayHolder = new GaodeRailWayHolder(this, prjItem.getDbLocation());
        KMLParser kmlParser = new KMLParser(prjItem.getDbLocation());
        kmlParser.draw(getaMap());
    }
    /**
     * 初始化Marker的点击事件--以及InfoWindow的填充
     */
    private void initMarkerClick() {
        //填充InfoWindow
        getaMap().setInfoWindowAdapter(new AMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return View.inflate(GaodePrjEditActivity.this, R.layout.view_info_window, null);
            }

            @Override
            public View getInfoContents(Marker marker) {
                return View.inflate(GaodePrjEditActivity.this, R.layout.view_info_window, null);
            }
        });
        //设置Marker点击事件
        getaMap().setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                getMarkerHolder().setCurrentMarker(marker);
                Timber.e("这个点的经纬度是:   " + marker.getPosition().latitude + ":"
                        + marker.getPosition().longitude);
                return true;
            }
        });
    }

    /**
     * 初始化地图Mode控件
     */
    private void initMapMode() {
        //指南针功能
        mUiSettings = getaMap().getUiSettings();
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
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
                        CameraUpdate cameraUpdate = CameraUpdateFactory.changeTilt(45);
                        getaMap().moveCamera(cameraUpdate);
                        getaMap().setMapType(AMap.MAP_TYPE_NORMAL);
                        break;
                    }
                    case Constant.MODE_MAP_3D: {//3D地图切换至卫星地图
                        ModeValue = Constant.MODE_MAP_SATELLITE;
                        mapModeBtn.setIcon(R.drawable.map_action_mode_satellite);
                        getaMap().setMapType(AMap.MAP_TYPE_SATELLITE);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.changeTilt(0);
                        getaMap().moveCamera(cameraUpdate);
                        break;
                    }
                    case Constant.MODE_MAP_SATELLITE: {//卫星地图切换至2D地图
                        ModeValue = Constant.MODE_MAP_2D;
                        mapModeBtn.setIcon(R.drawable.map_action_mode_2d);
                        getaMap().setMapType(AMap.MAP_TYPE_NORMAL);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.changeTilt(0);
                        getaMap().moveCamera(cameraUpdate);
                        break;
                    }
                }

            }
        });
    }

    /**
     * 和InfoWindow绑定的点击事件
     *
     * @param v view就是InfoWindow
     */
    public void clickEdit(View v) {
        //生成MarkerItem--跳转到MarkerEditActivity
        GaodeMarkerActivity.start(this, getMarkerHolder().getCurrentMarkerItem(), prjItem
                .getDbLocation(), getaMap().getCameraPosition().target,
                REQUEST_CODE_MARKER_EDIT_ACTIVITY);
    }

    /**
     * 和InfoWindow绑定的点击事件
     *
     * @param v view就是InfoWindow
     */
    public void clickPhoto(View v) {
        //这里传入的MarkerItem
        PicGridActivity.start(this, getMarkerHolder().getCurrentMarkerItem(), prjItem
                .getDbLocation(), REQUEST_CODE_PICTURE_ACTIVITY);
    }

    /**
     * 切换公里标显示状态
     */
    private void toggleLlPosition() {
        if (isLlPositionShowed) {
            isLlPositionShowed = false;
            llPosition.startAnimation(AnimationUtils.loadAnimation(this, R.anim.drop_down));
            llPosition.setVisibility(View.GONE);
        } else {
            isLlPositionShowed = true;
            llPosition.setVisibility(View.VISIBLE);
            llPosition.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pop_up));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_prj_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            //项目管理
//            case R.id.id_action_change_project:
//                finish();
//                PrjSelectActivity.start(this, true);
//                break;
//            //加载初始xml中的Marker数据
//            case R.id.id_action_load_xml_marker:
//                //只有加载了xml文件才加载初始选址文件
//                if (railWayHolder == null) {
//                    ToastHelper.show(this, "请先加载地图文件");
//                } else {
//                    FileHelper.showFileChooser(this, REQUEST_CODE_LOAD_XML_MARKER_FILE);
//                }
//                break;
//            // 加载数字地图
//            case R.id.id_action_load_digital_file:
//                FileHelper.showFileChooser(this, REQUEST_CODE_LOAD_DIGITAL_FILE);
//                break;
//            //加载xml文件
//            case R.id.id_action_load_xml_file:
//                FileHelper.showFileChooser(this, REQUEST_CODE_LOAD_XML_FILE);
//                break;
//            //加载kml文件
//            case R.id.id_action_load_kml_file:
//                FileHelper.showFileChooser(this, REQUEST_CODE_LOAD_KML_FILE);
//                break;
            //数据导出
            case R.id.id_action_export_data:
                //FileHelper.sendDbFile(this);
                FileHelper.sendDbFile(this, prjItem.getDbLocation());
                break;
            //开启离线地图
            case R.id.id_action_offline_map:
                GaodeOfflineActivity.start(this);
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
     * setting界面的定位mode改变事件
     *
     * @param event
     */
    public void onEventMainThread(LocateModeChangeEvent event) {
        changeLocateMode();
    }

    /**
     * prjEditActivity的回调方法
     */
    public void onEventMainThread(MarkerEditEvent markerEditEvent) {
        switch (markerEditEvent.getBackState()) {
            case CHANGED:
                loadMarker(prjItem);
                break;
            case UNCHANGED:
                break;
        }
    }

    /**
     * Marker对话框
     * @param context
     */
    public void showFindMarkerDialog(final Context context) {
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
     * progressbar是否显示的回调控制方法
     *
     * @param progressbarEvent
     */
    public void onEventMainThread(ProgressbarEvent progressbarEvent) {
        if (progressbarEvent.isShow()) {
            tvPbComment.setText("正在加载，请稍后。");
            pbBlock.setVisibility(View.VISIBLE);
        } else {
            pbBlock.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 正在压缩文件提示
     */
    public void onEventMainThread(CompressFileEvent event) {
        switch (event.getState()) {
            case BEGIN:
                tvPbComment.setText("正在压缩输出文件，请稍后。");
                pbBlock.setVisibility(View.VISIBLE);
                break;
            case END:
                pbBlock.setVisibility(View.INVISIBLE);
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
            loadMarker(prjItem);
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
     * 提示xml文件加载情况
     *
     * @param cadReadFinishEvent
     */
    public void onEventMainThread(CADReadFinishEvent cadReadFinishEvent) {
        //加载数据
        railWayHolder.hide();
        railWayHolder.clearData();
        railWayHolder = new GaodeRailWayHolder(this, prjItem.getDbLocation());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //加载xml中的初始Marker数据
            case REQUEST_CODE_LOAD_XML_MARKER_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uriXmlFileUri = data.getData();
                    String filePath = uriXmlFileUri.getPath();
                    if (!filePath.endsWith(".xml")) {
                        ToastHelper.show(this, "请选取.xml文件");
                        return;
                    }
                    //解析出MarkerItem数据
                    xmlMarkerParser = new XmlMarkerParser(this, filePath);
                    xmlMarkerParser.parse();
                    //将数据增加到当前工程中去(给每一个Marker添加prjName---然后save)
                    xmlMarkerParser.saveMarkerItem(prjItem.getPrjName(), railWayHolder
                            .getKilometerMarkHolder(), prjItem.getDbLocation());
                    //重画界面的Marker
                    loadMarker(prjItem);
                }
                break;
            //加载数字地图文件
            case REQUEST_CODE_LOAD_DIGITAL_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uriDigitalFile = data.getData();
                    String path = uriDigitalFile.getPath();
                    //如果没有选择文件直接返回
                    if (path == null || path.length() == 0) {
                        return;
                    }
                    //判断是不是数据库文件
                    File file = new File(path);
                    if (!file.getName().endsWith(".db")) {
                        ToastHelper.show(this, "您选取的数字地图文件格式有误!");
                        return;
                    }
                    //如果获取路径成功就----加载digitalMapHolder
                    digitalMapHolder = new  DigitalMapHolder(this, path, prjItem.getDbLocation());
                    //保存数字地图数据
                    railWayHolder.hide();
                    railWayHolder.clearData();
                    railWayHolder = new GaodeRailWayHolder(digitalMapHolder
                            .getTextList(),digitalMapHolder.getVectorList());
                }
                break;
            //加载xml文件
            case REQUEST_CODE_LOAD_XML_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uriDigitalFile = data.getData();
                    String path = uriDigitalFile.getPath();
                    //判断是不是数据库文件
                    File file = new File(path);
                    if (!file.getName().endsWith(".xml")) {
                        ToastHelper.show(this, "您选取的XML文件格式有误!");
                        return;
                    }
                    xmlParser = new XmlParser(this, path, prjItem.getDbLocation());
                }
                break;
            //加载kml文件
            case REQUEST_CODE_LOAD_KML_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uriDigitalFile = data.getData();
                    String path = uriDigitalFile.getPath();
                    //判断是不是数据库文件
                    File file = new File(path);
                    if (!file.getName().endsWith(".kml")) {
                        ToastHelper.show(this, "您选取的KML文件格式有误!");
                        return;
                    }
                    KMLParser kmlParser = new KMLParser(path);
                    kmlParser.draw(getaMap());
                }
                break;
            case REQUEST_CODE_ROUTE_ACTIVITY:
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
            super.onBackPressed();
            //杀掉当前app的进程---释放地图的内存
            System.exit(0);
        }
    }

    public GaodeRailWayHolder getRailWayHolder() {
        return railWayHolder;
    }

    public void setRailWayHolder(GaodeRailWayHolder railWayHolder) {
        this.railWayHolder = railWayHolder;
    }
}
