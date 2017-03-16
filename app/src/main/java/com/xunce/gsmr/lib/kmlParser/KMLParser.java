package com.xunce.gsmr.lib.kmlParser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.baidu.mapapi.map.BaiduMap;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.model.event.KMLLoadFinishedEvent;
import com.xunce.gsmr.model.gaodemap.graph.Point;
import com.xunce.gsmr.model.gaodemap.graph.Text;
import com.xunce.gsmr.util.DBHelper;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * KML文件加载器
 * Created by ssthouse on 2015/11/3.
 */
public class KMLParser extends DefaultHandler {
    private KmlData mydata = new KmlData();
    private List<KmlData> polyList = new ArrayList<>();
    private List<KmlData> textList = new ArrayList<>();
    private String qname = null;
    private String dbPath;

    /**
     * 构造方法
     *
     * @param path
     */
    public KMLParser(String path,String dbPath) {
        try {
            this.dbPath = dbPath;
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);//设定该解析器工厂支持名称空间
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new File(path), this);
        } catch (Exception e) {
            Timber.e(Log.getStackTraceString(e));
        }
    }

    public KMLParser(final String dbPath)
    {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                polyList = DBHelper.getKMLPolyInDB(dbPath);
                textList = DBHelper.getKMLTextInDB(dbPath);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                EventBus.getDefault().post(new KMLLoadFinishedEvent());
            }
        }.execute();

    }

    public void draw(AMap amap){
        if (textList !=null && textList.size() !=0)
        for(KmlData data : textList){
            data.draw(amap);
        }
        if (polyList!=null && polyList.size() !=0)
        for(KmlData data : polyList){
            data.draw(amap);
        }
    }

    public void draw(BaiduMap baidu){
        if (textList !=null && textList.size() !=0)
            for(KmlData data : textList){
                data.draw(baidu);
            }
        if (polyList!=null && polyList.size() !=0)
            for(KmlData data : polyList){
                data.draw(baidu);
            }
    }

    public KmlData findKmlData(String name){
        if (textList !=null && textList.size() !=0)
            for(KmlData data : textList){
                if (data.getName().contains(name))return data;
            }
        return null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) {
        qname = qName;
        if (qName.equals("Placemark")) {
            mydata = new KmlData();
        }
    }

    public void characters(char[] ch, int start, int length) {
        String text = new String(ch, start, length);
        if (qname.equals("name"))//如果标记间的数据为文本数据
        {
            String str = text.trim();
            if (str.length() > 0) {
                mydata.setName(str);
            }
        } else if (qname.equals("longitude")) {
            String str = text.trim();
            if (str.length() > 0) {
                mydata.setLongitude(str);
            }
        } else if (qname.equals("latitude")) {
            String str = text.trim();
            if (str.length() > 0) {
                mydata.setLatitude(str);
            }
        } else if (qname.equals("coordinates")) {
            String str = text.trim();
            if (str.length() > 0) {
                String strs[] = str.split(",");
                double longitude = Double.parseDouble(strs[0]);
                double latitude = Double.parseDouble(strs[1]);
                if(longitude>0.0 && latitude>0) {
                    mydata.getPointList().add(new GpsPoint(longitude, latitude));
                }
            }
        } else if (qname.equals("styleUrl")) {
            String str = text.trim();
            if (str.length() > 0) {
                mydata.setStyleUrl(str);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("Placemark")) {
            if (mydata.getName() != null && mydata.getLatitude() != null && mydata.getLongitude() != null) {
                if (mydata.getStyleUrl().equals("#polystyle")) {
                    polyList.add(mydata);
                } else {
                    textList.add(mydata);
                }
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
//        for (KmlData data : polyList) {
//            Timber.e("poly:\t" + data.toString());
//        }
//        for (KmlData data : textList) {
//            Timber.e("text:\t" + data.toString());
//        }
        //将数据存入数据库中
        saveintoDb();

        super.endDocument();
    }

    /**
     * 将数据存入DB数据库中
     */
    private void saveintoDb(){
        //将数据存入数据库中
        SQLiteDatabase db = DBHelper.openDatabase(dbPath);
        db.beginTransaction();
        db.execSQL("DELETE FROM " + Constant.TABLE_KML_TEXT + " WHERE 1=1");
        db.execSQL("DELETE FROM " + Constant.TABLE_KML_POLY + " WHERE 1=1");
        for (KmlData text : textList) {
            DBHelper.insertKMLText(db, Double.valueOf(text.getLongitude()), Double.valueOf(text.getLatitude()),
                    text.getName());
        }
        int id =0;int orderId = 0;
        for (KmlData kmldata: polyList) {
            for (GpsPoint point : kmldata.getPointList()) {
                DBHelper.insertKMLPoly(db, id, orderId, point.getLongitude(), point.getLatitude(), kmldata.getName());
                orderId++;
            }
            orderId = 0;
            id++;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}

