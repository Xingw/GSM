package com.xunce.gsmr.app;

/**
 * 常量
 * Created by ssthouse on 2015/7/17.
 */
public class Constant {

    //app的文件夹名
    public static  final String APP_FOLDER_NAME = "GSM";

    //数据库文件的路径
    public static final String DbPath = "/data/data/com.xunce.gsmr/databases/Location.db";
    public static final String DbTempPath = "/storage/sdcard0/GSM/Temp/";
    //外部SD卡数据存储路径
    public static final String PICTURE_PATH = "/storage/sdcard0/GSM/Picture/";
    public static final String TEMP_FILE_PATH = "/storage/sdcard0/GSM/Temp/";
    public static final String TEMP_SHARE_PATH = "/storage/sdcard0/GSM/Share/";
    public static final String DATA_BASE_FILE_PATH = "/storage/sdcard0/GSM/DataBase/";

    //table名
    public static final String TABLE_PRJ_ITEM = "Projects";
    public static final String TABLE_MARKER_ITEM = "BaseStation";
    public static final String TABLE_PICTURE_ITEM = "Photo";
    public static final String TABLE_PROJECT_INFO = "ProjectInfo";
    public static final String TABLE_POLY = "Poly";
    public static final String TABLE_P2DPOLY = "P2DPoly";
    public static final String TABLE_TEXT = "Text";
    public static final String TABLE_TEXT_DIGITAL = "TextPoint";
    public static final String TABLE_LINE = "Line";
    public static final String TABLE_KML_POLY = "KMLPoly";
    public static final String TABLE_KML_TEXT = "KMLText";

    //Extra的key
    public static final String EXTRA_KEY_PRJ_ITEM = "prjItem";
//    public static final String EXTRA_KEY_PRJ_ITEM_DBPATH = "prjItem_DBPath";
    public static final String EXTRA_KEY_MARKER_ITEM = "markerItem";
    public static final String EXTRA_KEY_REQUEST_CODE = "requestCode";
    public static final String EXTRA_KEY_LATITUDE = "latitude";
    public static final String EXTRA_KEY_LONGITUDE = "longitude";
    public static final String EXTRA_KEY_DBPATH = "dbPath";
    public static final boolean EXTRA_KEY_GAODE = true;
    public static final boolean EXTRA_KEY_BAIDU = false;

    //result_code
    public static final int RESULT_CODE_OK = 2000;
    public static final int RESULT_CODE_NOT_OK = 2001;
    public static final int REQUEST_CODE_ALBUM = 2002;
    public static final int REQUEST_CODE_CAMERA = 2003;

    //.db文件的requestCode
    public static final int REQUEST_CODE_DB_FILE = 2004;
    public static final String EXTRA_KEY_ZOOM = "zoom";

    //北京天安门坐标
    public static final double LATITUDE_DEFAULT = 39.907591;
    public static final double LONGITUDE_DEFAULT = 116.415124;
    public static final String RESULT_CODE_NAVI_VALUE = "value";
    public static final String EXTRA_KEY_KILOMARKER_HOLDER = "kilomarker_holder";
    public static final String EXTRA_KEY_NAVI_STYLE = "navi_style";



    //FIR更新用的token
    public static String firToken = "aa38ac9ade93397254698e8783adce7f";

    //地图模式
    public static final int MODE_MAP_2D = 2005;
    public static final int MODE_MAP_3D = 2006;
    public static final int MODE_MAP_SATELLITE = 2007;

    public static final int RESULT_CODE_NAVI = 2008;
    public static final String EXTRA_KEY_MAP_STYLE ="map_style";
    public static boolean firstOpen = true;
    public static String EXTRA_KEY_INPUT = "input";
}
