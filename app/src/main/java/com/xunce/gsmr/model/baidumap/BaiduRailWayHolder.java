package com.xunce.gsmr.model.baidumap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.amap.api.maps.AMap;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.kilometerMark.KilometerMark;
import com.xunce.gsmr.kilometerMark.KilometerMarkHolder;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.model.baidumap.graph.Circle;
import com.xunce.gsmr.model.baidumap.graph.Line;
import com.xunce.gsmr.model.baidumap.graph.Point;
import com.xunce.gsmr.model.baidumap.graph.Text;
import com.xunce.gsmr.model.baidumap.graph.Vector;
import com.xunce.gsmr.model.event.ProgressbarEvent;
import com.xunce.gsmr.util.DBConstant;
import com.xunce.gsmr.util.DBHelper;
import com.xunce.gsmr.util.L;
import com.xunce.gsmr.util.gps.PositionUtil;
import com.xunce.gsmr.util.view.ToastHelper;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 铁路的管理类
 * 1.一条铁路应该是对应的一个数据库中的数据
 * Created by ssthouse on 2015/8/21.
 */
public class BaiduRailWayHolder {
    private static final String TAG = "BaiduRailWayHolder";

    /**
     * 用于接收数据的临时变量
     */
    private Line line;
    private Text text;
    private Vector vector;

    /**
     * 所有绘图数据
     */
    private List<Circle> circles;
    private List<Line> lineList = new ArrayList<>();
    private List<Text> textList = new ArrayList<>();
    private List<Vector> vectorList = new ArrayList<>();
    private List<String> layerList = new ArrayList<>();
    /**
     * 公里标管理器
     */
    private KilometerMarkHolder kilometerMarkHolder = new KilometerMarkHolder();

    public BaiduRailWayHolder(final Context context, final String dbPath) {
        lineList = new ArrayList<>();
        textList = new ArrayList<>();
        vectorList = new ArrayList<>();
        kilometerMarkHolder = new KilometerMarkHolder();
        //启动线程前___显示progressbar
        EventBus.getDefault().post(new ProgressbarEvent(true));
        //启动异步线程解析数据
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SQLiteDatabase db = DBHelper.openDatabase(dbPath);
                db.beginTransaction();
                //开启线程执行前显示进度条
                getLineList(db);
                getTextList(db);
                getPolyList(db);
                getP2DPolyList(db);
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
                initLayerList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //将公里标进行排序
                kilometerMarkHolder.sort();
                //运行完将progressbar隐藏
                EventBus.getDefault().post(new ProgressbarEvent(false));
                ToastHelper.show(context, "地图数据加载成功!");
            }
        }.execute();
    }

    private void getP2DPolyList(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + Constant.TABLE_P2DPOLY + " ORDER BY " +
                DBConstant.id + " , " + DBConstant.orderId, null);
        if (cursor == null || !cursor.moveToFirst()) {
            return;
        }
        do {
            int order = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBConstant
                    .orderId)));
            if (order == 0) {
                if (vector != null && vector.getPointList().size() > 1) {
                    vectorList.add(vector);
                }
                vector = new Vector("");
            }
            double longitude = cursor.getDouble(cursor.getColumnIndex(DBConstant.longitude));
            double latitude = cursor.getDouble(cursor.getColumnIndex(DBConstant.latitude));
            vector.getPointList().add(new Point(longitude, latitude));
        } while (cursor.moveToNext());
        vectorList.add(vector);
        return;
    }

    /**
     * 构造方法 传入数据
     *
     * @param lineList
     * @param textList
     * @param vectorList
     */
    public BaiduRailWayHolder(List<Line> lineList, List<Text> textList, List<Vector> vectorList) {
        this.lineList = lineList;
        this.textList = textList;
        this.vectorList = vectorList;

        for (Text text : textList) {
            text.setLatLng(PositionUtil.Gps84_To_bd09(text.getLatLng()));
        }
        for (Line line : lineList) {
            line.setLatLngBegin(PositionUtil.Gps84_To_bd09(line.getLatLngBegin()));
            line.setLatLngEnd(PositionUtil.Gps84_To_bd09(line.getLatLngEnd()));
        }
        kilometerMarkHolder = new KilometerMarkHolder();
        for (Text text1 : textList) {
            //文字需要判断是不是公里标(是的话需要加入KilometerMarkHolder中)
            KilometerMark kilometerMark = KilometerMark.getKilometerMark(text1.getLatLng().longitude,
                    text1.getLatLng().latitude, text1.getContent());
            kilometerMarkHolder.addKilometerMark(kilometerMark);
        }

    }

    /**
     * 判断是否为空
     */
    public boolean isempty() {
        if (textList != null && lineList == null && vectorList == null)
            return true;
        if (textList.size() == 0 && lineList.size() == 0 && vectorList.size() == 0)
            return true;
        return false;
    }

    /**
     * 画出自己
     */
    public void draw(BaiduMap baiduMap) {
//        for(Circle circle : circles){
//            circle.draw(baiduMap);
//        }
        for (Line line : lineList) {
            line.draw(baiduMap);
        }
        for (Text text : textList) {
            text.draw(baiduMap);
        }
        for (Vector vector : vectorList) {
            vector.draw(baiduMap);
        }
    }

    /**
     * 仅仅画出线段
     *
     * @param openGLLatLngs
     */
    public void drawLine(List<openGLLatLng> openGLLatLngs) {
        for (Line line : lineList) {
            line.draw(openGLLatLngs);
        }
        for (Vector vector : vectorList) {
            vector.draw(openGLLatLngs);
            //Timber.e("我画了一条vector");
        }
    }

    public void drawLine(BaiduMap baiduMap) {
        for (Line line : lineList) {
            line.draw(baiduMap);
        }
        for (Vector vector : vectorList) {
            vector.draw(baiduMap);
            //Timber.e("我画了一条vector");
        }
    }

    /**
     * 仅仅画出文字
     *
     * @param baiduMap
     */
    public void drawText(BaiduMap baiduMap) {
        for (Text text : textList) {
            text.draw(baiduMap);
        }
    }

    private void getLineList(SQLiteDatabase database) {
        Cursor cursor = database.query(Constant.TABLE_LINE, null, null, null, null, null, null);
        if (cursor == null) {
            return;
        }
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            LatLng latLngStart = PositionUtil.Gps84_To_bd09(cursor.getDouble(cursor.getColumnIndex(DBConstant.latitude_start)),
                    cursor.getDouble(cursor.getColumnIndex(DBConstant.longitude_start)));
            LatLng latLngEnd = PositionUtil.Gps84_To_bd09(
                    cursor.getDouble(cursor.getColumnIndex(DBConstant.latitude_end)),
                    cursor.getDouble(cursor.getColumnIndex(DBConstant.longitude_end)));
            line = new Line(latLngStart, latLngEnd, cursor.getString(cursor.getColumnIndex(DBConstant.layer)));
            lineList.add(line);
            cursor.moveToNext();
        }
        return;
    }

    private void getCircleList(SQLiteDatabase database) {
        List<Circle> circleList = new ArrayList<>();
        Cursor cursor = database.query("Circle", null, null, null, null, null, null);
        if (cursor == null) {
            return;
        }
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            LatLng latLng = new LatLng(cursor.getFloat(1), cursor.getFloat(2));
            circleList.add(new Circle(latLng, (int) cursor.getFloat(3)));
            L.log(TAG, cursor.getFloat(1) + ":" + cursor.getFloat(2) + ":" +
                    cursor.getFloat(3));
            cursor.moveToNext();
        }
        return;
    }

    private void getTextList(SQLiteDatabase database) {
        Cursor cursor = database.query(Constant.TABLE_TEXT, null, null, null, null, null, null);
        if (cursor == null) {
            return;
        }
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            double latitude = cursor.getDouble(cursor.getColumnIndex(DBConstant.latitude));
            double longitude = cursor.getDouble(cursor.getColumnIndex(DBConstant.longitude));
            LatLng latLng = PositionUtil.Gps84_To_bd09(latitude, longitude);
            String content = cursor.getString(cursor.getColumnIndex(DBConstant.content));
            text = new Text(latLng, content, cursor.getString(cursor.getColumnIndex(DBConstant.layer)));
            textList.add(text);
            //文字需要判断是不是公里标(是的话需要加入KilometerMarkHolder中)
            KilometerMark kilometerMark = KilometerMark.getKilometerMark(longitude, latitude, content);
            kilometerMarkHolder.addKilometerMark(kilometerMark);
            cursor.moveToNext();
        }
        return;
    }


    /**
     * 获取Poly矢量List
     *
     * @param database
     * @return
     */
    private void getPolyList(SQLiteDatabase database) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + Constant.TABLE_POLY + " ORDER BY " +
                DBConstant.id + " , " + DBConstant.orderId, null);
        if (cursor == null || !cursor.moveToFirst()) {
            return;
        }
        do {
            int order = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBConstant
                    .orderId)));
            if (order == 0) {
                if (vector != null && vector.getPointList().size() > 1) {
                    vectorList.add(vector);
                }
                //存入Vector的图层名
                vector = new Vector(cursor.getString(cursor.getColumnIndex(DBConstant.layer)));
            }
            double longitude = cursor.getDouble(cursor.getColumnIndex(DBConstant.longitude));
            double latitude = cursor.getDouble(cursor.getColumnIndex(DBConstant.latitude));
            vector.getPointList().add(new Point(longitude, latitude));
        } while (cursor.moveToNext());
        vectorList.add(vector);
        return;
    }

    /**
     * 将画好的图像隐藏
     */
    public void hide() {
        for (Line line : lineList) {
            line.hide();
        }
        for (Text text : textList) {
            text.hide();
        }
        for (Vector vector : vectorList) {
            vector.hide();
        }
    }

    /**
     * 隐藏文字
     */
    public void hideText() {
        for (Text text : textList) {
            text.hide();
        }
    }

    private void initLayerList() {
        for (Text text1 : textList) {
            if (!layerList.contains(text1.getLayerName())) {
                layerList.add(text1.getLayerName());
            }
        }
        for (Line line1 : lineList) {
            if (!layerList.contains(line1.getLayerName())) {
                layerList.add(line1.getLayerName());
            }
        }
        for (Vector vector1 : vectorList) {
            if (!layerList.contains(vector1.getLayerName())) {
                layerList.add(vector1.getLayerName());
            }
        }
    }

    public void initshow(List<String> showList) {
        for (Text text1 : textList) {
            if (showList.contains(text1.getLayerName())) {
                text1.setShow(true);
            } else {
                text1.setShow(false);
            }
        }
        for (Vector vector1 : vectorList) {
            if (showList.contains(vector1.getLayerName())) {
                vector1.setShow(true);
            } else {
                vector1.setShow(false);
            }
        }
        for (Line line1 : lineList) {
            if (showList.contains(line1.getLayerName())) {
                line1.setShow(true);
            } else {
                line1.setShow(false);
            }
        }
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

    public List<Text> getTextList() {
        return textList;
    }

    public void setTextList(List<Text> textList) {
        this.textList = textList;
    }

    public List<Vector> getVectorList() {
        return vectorList;
    }

    public void setVectorList(List<Vector> vectorList) {
        this.vectorList = vectorList;
    }

    public KilometerMarkHolder getKilometerMarkHolder() {
        return kilometerMarkHolder;
    }

    public void setKilometerMarkHolder(KilometerMarkHolder kilometerMarkHolder) {
        this.kilometerMarkHolder = kilometerMarkHolder;
    }

    public List<String> getLayerList() {
        return layerList;
    }

    public void setLayerList(List<String> layerList) {
        this.layerList = layerList;
    }
}
