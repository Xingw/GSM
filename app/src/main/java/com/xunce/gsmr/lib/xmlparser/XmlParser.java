package com.xunce.gsmr.lib.xmlparser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.kilometerMark.KilometerMark;
import com.xunce.gsmr.kilometerMark.KilometerMarkHolder;
import com.xunce.gsmr.model.event.ProgressbarEvent;
import com.xunce.gsmr.model.gaodemap.graph.Line;
import com.xunce.gsmr.model.gaodemap.graph.Point;
import com.xunce.gsmr.model.gaodemap.graph.Text;
import com.xunce.gsmr.model.gaodemap.graph.Vector;
import com.xunce.gsmr.util.DBHelper;
import com.xunce.gsmr.util.gps.PositionUtil;
import com.xunce.gsmr.util.view.ToastHelper;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * CAD导出的xml文件的解析
 * 解析出来的数据为:
 * Created by ssthouse on 2015/10/30.
 */
public class XmlParser extends DefaultHandler {
    private Context context;

    /**
     * 用于接收数据的临时变量
     */
    private Line line;
    private Text text;
    private com.xunce.gsmr.model.gaodemap.graph.Vector vector;
    private String dbPath;
    /**
     * 从xml中解析出来的数据
     */
    private List<Line> lineList = new ArrayList<>();
    private List<Text> textList = new ArrayList<>();
    private List<com.xunce.gsmr.model.gaodemap.graph.Vector> polyList = new ArrayList<>();
    private List<com.xunce.gsmr.model.gaodemap.graph.Vector> p2dpolyList = new ArrayList<>();
    /**
     * 当前解析的xml文件的路径
     */
    private String xmlFilePath;

    /**
     * 公里标控制器
     */
    private KilometerMarkHolder kilometerMarkHolder = new KilometerMarkHolder();

    public XmlParser(){

    }

    /**
     * 构造方法
     *
     * @param context
     */
    public XmlParser(final Context context, final String xmlFilePath,final String dbPath) {
        this.context = context;
        this.xmlFilePath = xmlFilePath;
        this.dbPath = dbPath;
        //开始解析xml文件
        parse();
    }

    /**
     * 开始解析xml文件
     */
    private void parse() {
        lineList.clear();
        textList.clear();
        polyList.clear();
        p2dpolyList.clear();
        //开启线程执行前显示进度条
        EventBus.getDefault().post(new ProgressbarEvent(true));
        //开一个新线程完成解析任务
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //获取解析器
                    SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                    XMLReader xmlReader = saxParser.getXMLReader();
                    //设置监听器
                    xmlReader.setContentHandler(XmlParser.this);
                    xmlReader.setErrorHandler(XmlParser.this);
                    //开始解析
                    xmlReader.parse(new InputSource(new FileInputStream(xmlFilePath)));
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                    Timber.e(Log.getStackTraceString(e));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                EventBus.getDefault().post(new ProgressbarEvent(false));
                ToastHelper.show(context, "Xml文件加载完成");
            }
        }.execute();
    }

//    /**
//     * 将解析出来的数据画出来
//     */
//    public void draw(AMap aMap) {
//        for (Line line : lineList) {
//            line.draw(aMap);
//        }
//        for (Text text : textList) {
//            text.draw(aMap);
//        }
//        for (com.xunce.gsmr.model.gaodemap.graph.Vector vector : vectorList) {
//            vector.draw(aMap);
//        }
//    }
//
//    /**
//     * 仅仅画出文字
//     *
//     * @param aMap
//     */
//    public void drawText(AMap aMap) {
//        for (Text text : textList) {
//            text.draw(aMap);
//        }
//    }
//
//    /**
//     * 仅仅画出线段
//     *
//     * @param aMap
//     */
//    public void drawLine(AMap aMap) {
//        for (Line line : lineList) {
//            line.draw(aMap);
//        }
//        for (com.xunce.gsmr.model.gaodemap.graph.Vector vector : vectorList) {
//            vector.draw(aMap);
//            //Timber.e("我画了一条vector");
//        }
//    }
//
//    /**
//     * 将画好的图像隐藏
//     */
//    public void hide() {
//        for (Line line : lineList) {
//            line.hide();
//        }
//        for (Text text : textList) {
//            text.hide();
//        }
//        for (com.xunce.gsmr.model.gaodemap.graph.Vector vector : vectorList) {
//            vector.hide();
//        }
//    }
//
//    /**
//     * 隐藏文字
//     */
//    public void hideText() {
//        for (Text text : textList) {
//            text.hide();
//        }
//    }
//
//    /**
//     * 清除数据
//     */
//    public void clearData(){
//        for (Line line : lineList) {
//            line.setPolyline(null);
//        }
//        for (Text text : textList) {
//            text.setText(null);
//        }
//        for (com.xunce.gsmr.model.gaodemap.graph.Vector vector : vectorList) {
//            vector.setPolyline(null);
//        }
//    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (Element.LINE.equals(localName)) {
            //获取的是高德地图的latlng
            LatLng latLngStart = new LatLng(Double.parseDouble(attributes.getValue(LineElement.latStart)),
                    Double.parseDouble(attributes.getValue(LineElement.longStart)));
            LatLng latLngEnd = new LatLng(Double.parseDouble(attributes.getValue(LineElement.latEnd)),
                    Double.parseDouble(attributes.getValue(LineElement.longEnd)));
            line = new Line(latLngStart, latLngEnd);
            lineList.add(line);
        } else if (Element.TEXT.equals(localName)) {
            double latitude = Double.parseDouble(attributes.getValue(TextElement.latitude));
            double longitude = Double.parseDouble(attributes.getValue(TextElement.longitude));
            LatLng latLng = new LatLng(latitude, longitude);
            String content = attributes.getValue(TextElement.value);
            text = new Text(latLng, content);
            textList.add(text);
            //文字需要判断是不是公里标(是的话需要加入KilometerMarkHolder中)
            KilometerMark kilometerMark = KilometerMark.getKilometerMark(longitude, latitude, content);
            kilometerMarkHolder.addKilometerMark(kilometerMark);
        } else if (Element.P2DPOLY.equals(localName)) {
            //判断order是0的话---要把前面的数据放进去
            int order = Integer.parseInt(attributes.getValue(Vector.order));
            if (order == 0) {
                if (vector != null && vector.getPointList().size() != 0) {
                    p2dpolyList.add(vector);
                }
                vector = new com.xunce.gsmr.model.gaodemap.graph.Vector(attributes.getValue(Vector.name));
            }
                vector.getPointList().add(new Point(Double.parseDouble(attributes.getValue(Vector.longitude)),
                        Double.parseDouble(attributes.getValue(Vector.latitude))));
        } else if (Element.POLY.equals(localName)) {
            //判断order是0的话---要把前面的数据放进去
            int order = Integer.parseInt(attributes.getValue(Vector.order));
            if (order == 0) {
                if (vector != null && vector.getPointList().size() != 0) {
                    polyList.add(vector);
                }
                vector = new com.xunce.gsmr.model.gaodemap.graph.Vector(attributes.getValue(Vector.name));
            }
                vector.getPointList().add(new Point(Double.parseDouble(attributes.getValue(Vector.longitude)),
                        Double.parseDouble(attributes.getValue(Vector.latitude))));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (Element.DATA.equals(localName)) {
            Timber.e("我添加了最后一个Vector");
            //Cad的数据中最后一条是P2dpoly
            p2dpolyList.add(vector);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        Timber.e("我开始解析了...");
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        //解析完毕后---将获得的公里标List进行排序
        kilometerMarkHolder.sort();
        //读取完成后把所有读到的数据存到指定的数据库中
        SQLiteDatabase db = DBHelper.openDatabase(dbPath);
        db.beginTransaction();
        db.execSQL("DELETE FROM " + Constant.TABLE_TEXT + " WHERE 1=1");
        db.execSQL("DELETE FROM " + Constant.TABLE_LINE + " WHERE 1=1");
        db.execSQL("DELETE FROM " + Constant.TABLE_POLY + " WHERE 1=1");
        db.execSQL("DELETE FROM "+ Constant.TABLE_P2DPOLY + " WHERE 1=1");
        for (Text text1 : textList) {
            DBHelper.insertText(db,text1.getLatLng().longitude,text1.getLatLng().latitude,
                    text1.getContent());
        }
        for (Line line1 : lineList) {
            DBHelper.insertLine(db,line1.getLatLngBegin().longitude, line1.getLatLngBegin()
                    .latitude,line1.getLatLngEnd().longitude,line1.getLatLngEnd().latitude);
        }
        int id =0;int orderId = 0;
        for (com.xunce.gsmr.model.gaodemap.graph.Vector vector1 : polyList) {
            for (Point point : vector1.getPointList()) {
                DBHelper.insertPoly(db,id,orderId,point.getLongitude(),point.getLatitude(),vector1.getName());
                orderId++;
            }
            orderId = 0;
            id++;
        }
        orderId = 0;id = 0;
        for (com.xunce.gsmr.model.gaodemap.graph.Vector vector1 : p2dpolyList) {
            for (Point point : vector1.getPointList()) {
                DBHelper.insertP2DPoly(db,id,orderId,point.getLongitude(),point.getLatitude(),vector1.getName());
                orderId++;
            }
            orderId = 0;
            id++;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        //打印查看公里标数据
        Timber.e("我解析完毕了...");
        Timber.e(kilometerMarkHolder.toString());
    }

    class Element {
        static final String DATA = "Data";
        static final String LINE = "LINE";
        static final String POLY = "POLY";
        static final String TEXT = "TEXT";
        static final String P2DPOLY = "P2DPOLY";
    }

    class LineElement {
        static final String longStart = "longitude_start";
        static final String latStart = "latitude_start";
        static final String longEnd = "longitude_end";
        static final String latEnd = "latitude_end";
    }

    class PolyElement {
        static final String longStart = "longitude_start";
        static final String latStart = "latitude_start";
        static final String longEnd = "longitude_end";
        static final String latEnd = "latitude_end";
    }

    class TextElement {
        static final String longitude = "longitude";
        static final String latitude = "latitude";
        static final String value = "value";
    }

    class Vector {
        static final String longitude = "longitude";
        static final String latitude = "latitude";
        static final String order = "order";
        static final String name = "layer";
    }

    //getter----and-----setter----------------------------------

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public KilometerMarkHolder getKilometerMarkHolder() {
        return kilometerMarkHolder;
    }

    public void setKilometerMarkHolder(KilometerMarkHolder kilometerMarkHolder) {
        this.kilometerMarkHolder = kilometerMarkHolder;
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

    public List<com.xunce.gsmr.model.gaodemap.graph.Vector> getPolyList() {
        return polyList;
    }

    public void setPolyList(List<com.xunce.gsmr.model.gaodemap.graph.Vector> polyList) {
        this.polyList = polyList;
    }

    public List<com.xunce.gsmr.model.gaodemap.graph.Vector> getP2dpolyList() {
        return p2dpolyList;
    }

    public void setP2dpolyList(List<com.xunce.gsmr.model.gaodemap.graph.Vector> p2dpolyList) {
        this.p2dpolyList = p2dpolyList;
    }
}
