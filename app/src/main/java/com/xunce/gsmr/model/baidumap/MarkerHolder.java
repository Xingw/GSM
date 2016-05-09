package com.xunce.gsmr.model.baidumap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.xunce.gsmr.R;
import com.xunce.gsmr.model.MarkerIconCons;
import com.xunce.gsmr.model.MarkerItem;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.util.DBHelper;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 持有一个baiduMap的引用 承载PrjEditActivity中的所有Marker Created by ssthouse on 2015/8/21.
 */
public class MarkerHolder {
    //标记点相关的
    public static BitmapDescriptor descriptorBlue = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marker_blue);
    public static BitmapDescriptor descriptorRed = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marker_red);

    /**
     * Marker相关
     */
    //当前选中的marker和markerItem
    private Marker currentMarker;
    private MarkerItem currentMarkerItem;
    //marker和markerItem的list
    private List<Marker> markerOnMapList;
    private List<MarkerItem> markerOnDbList;

    /**
     * 地图和数据源头
     */
    private BaiduMap baiduMap;
    private Context context;
    private PrjItem prjItem;

    /**
     * 构造方法
     *
     * @param prjItem
     * @param baiduMap
     */
    public MarkerHolder(Context context, PrjItem prjItem, BaiduMap baiduMap) {
        this.context = context;
        this.prjItem = prjItem;
        this.baiduMap = baiduMap;
        markerOnMapList = new ArrayList<>();
        markerOnDbList = new ArrayList<>();

        initMarkerList();
    }

    /**
     * 初始化并且画出Marker
     */
    public void initMarkerList() {
        baiduMap.clear();
        //清空markerList
        markerOnMapList.clear();
        markerOnDbList.clear();
        currentMarker = null;
        currentMarkerItem = null;
        //初始化markerList
        SQLiteDatabase db = SQLiteDatabase.openDatabase(prjItem.getDbLocation(), null,
                SQLiteDatabase.OPEN_READWRITE);
        markerOnDbList = DBHelper.getMarkerList(db);
        db.close();
        ToastHelper.show(context, "数据库读取失败，请尝试重新打开程序");
        for (int i = 0; i < markerOnDbList.size(); i++) {
            LatLng latLng = markerOnDbList.get(i).getBaiduLatLng();
            BitmapDescriptor bitmapDescriptor;
            String deviceType = markerOnDbList.get(i).getDeviceType();
            String colorStr = PreferenceHelper.getInstance(context).getMarkerColorName(deviceType);
            switch (colorStr) {
                case MarkerIconCons.ColorName.BLUE:
                    bitmapDescriptor = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marker_blue);
                    break;
                case MarkerIconCons.ColorName.GREEN:
                    bitmapDescriptor = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marker_green);
                    break;
                case MarkerIconCons.ColorName.ORANGE:
                    bitmapDescriptor = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marker_orange);
                    break;
                case MarkerIconCons.ColorName.PURPLE:
                    bitmapDescriptor = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marker_purple);
                    break;
                case MarkerIconCons.ColorName.RED:
                    bitmapDescriptor = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marker_red);
                    break;
                default:
                    bitmapDescriptor = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marker_blue);
                    break;
            }

            OverlayOptions MarkerOverlay = new MarkerOptions()
                    .position(latLng)
                    .icon(bitmapDescriptor)
                    .zIndex(16)
                    .draggable(false);
            markerOnMapList.add((Marker) baiduMap.addOverlay(MarkerOverlay));
            Timber.e("我添加了一个点:    " + latLng.latitude + ":" + latLng.longitude);
        }
//        if(markerOnDbList.size() >0) {
//            //动画移动过去
//            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(
//                    new LatLng(markerOnDbList.get(0).getLatitude(), markerOnDbList.get(0).getLongitude()));
//            baiduMap.animateMapStatus(u);
//        }
    }

    /**
     * 取消当前选中的Marker
     */
    public void clearSelection() {
        currentMarker.setIcon(descriptorBlue);
        //没有当前选中的Marker
        currentMarker = null;
    }

    public void setAll2Blue() {
        for (Marker marker : markerOnMapList) {
            marker.setIcon(descriptorBlue);
        }
    }

    public void setAll2Red() {
        for (Marker marker : markerOnMapList) {
            marker.setIcon(descriptorRed);
        }
    }

    //getter------------and------------setter-----------------
    public Marker getCurrentMarker() {
        return currentMarker;
    }

    public void setCurrentMarker(Marker currentMarker) {
        this.currentMarker = currentMarker;
        this.currentMarkerItem = markerOnDbList.get(markerOnMapList.indexOf(currentMarker));
    }

    public MarkerItem getCurrentMarkerItem() {
        return currentMarkerItem;
    }

    public List<MarkerItem> getMarkerOnDbList() {
        return markerOnDbList;
    }

    public List<Marker> getMarkerOnMapList() {
        return markerOnMapList;
    }

    public void setMarkerOnMapList(List<Marker> markerOnMapList) {
        this.markerOnMapList = markerOnMapList;
    }
}
