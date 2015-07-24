package com.xunce.gsmr.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.xunce.gsmr.Constant;
import com.xunce.gsmr.model.BitmapItem;
import com.xunce.gsmr.model.MarkerItem;
import com.xunce.gsmr.model.PrjItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * app的文件管理类
 * Created by ssthouse on 2015/7/16.
 */
public class FileHelper {
    private static final String TAG = "FileHelper";

    /**
     * 删除一个PrjItem的数据
     *
     * @param prjItem
     */
    public static void deletePrjItem(PrjItem prjItem) {
        if (prjItem == null) {
            return;
        }
        //删除照片文件
        String path = Constant.PICTURE_PATH + prjItem.getPrjName();
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        //删除数据库文件
        List<MarkerItem> markerItemList = prjItem.getMarkerItemList();
        if (markerItemList != null) {
            for (MarkerItem item : markerItemList) {
                item.delete();
            }
        }
        prjItem.delete();
    }

    /**
     * 为PrjImte重命名
     *
     * @param prjItem
     * @param newName
     */
    public static void changePrjItemName(PrjItem prjItem, String newName) {
        if (prjItem == null || newName == null) {
            return;
        }
        //修改照片文件名称
        String path = Constant.PICTURE_PATH + prjItem.getPrjName();
        File file = new File(path);
        if (file.exists()) {
            file.renameTo(new File(Constant.PICTURE_PATH + newName));
        }
        //修改数据库文件
        List<MarkerItem> markerItemList = prjItem.getMarkerItemList();
        if (markerItemList != null) {
            for (MarkerItem item : markerItemList) {
                LogHelper.Log(TAG, "我修改了MarkerItem的prjName");
                item.setPrjName(newName);
                item.save();
            }
        }
        prjItem.setPrjName(newName);
        prjItem.save();
    }

    /**
     * 获取File根目录的文件路径
     *
     * @return
     */
    public static String getFileParentPath(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    public static String getFilePath(Context context, String fileName) {
        return getFileParentPath(context) + fileName;
    }

    /**
     * 获取指定数据库的绝对路径
     *
     * @param context
     * @param dataBaseName
     * @return
     */
    public static String getDataBasePath(Context context, String dataBaseName) {
        return context.getDatabasePath(dataBaseName).getAbsolutePath();
    }

    /**
     * 获取SD卡路径
     *
     * @return
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            return sdDir.toString();
        } else {
            return null;
        }
    }

    public static String getSDPath(String fileName) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            return sdDir.toString() + "/" + fileName;
        } else {
            return null;
        }
    }

    public static void sendPicture(Context context, List<BitmapItem> bitmapItemList) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> uriList = new ArrayList<>();
        //获取对应TourItem的文件的URL
        for (BitmapItem item : bitmapItemList) {
            File file = new File(item.getPath());
            uriList.add(Uri.fromFile(file));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        intent.setType("image/jpg");
        //调用系统的----发送
        context.startActivity(Intent.createChooser(intent, "Share　Image"));
    }

    public static void sendDbFile(Activity context) {
        File tempDbFile = new File(Constant.TEMP_FILE_PATH, System.currentTimeMillis() + ".db");
        try {
            tempDbFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "creat new file is worng");
            e.printStackTrace();
        }
        try {
            copyFile(new FileInputStream(new File("/data/data/com.xunce.gsmr/databases/Location.db")), tempDbFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "somehing is worng");
        }
        if (tempDbFile.exists()) {
            LogHelper.Log(TAG, "我是存在的!!!!!");
        }
        LogHelper.Log(TAG, tempDbFile.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempDbFile));
        intent.setType("*/*");
        context.startActivity(intent);
    }

    /**
     * 复制单个文件
     *
     * @return boolean
     */
    public static void copyFile(FileInputStream fis, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((byteread = fis.read(buffer)) != -1) {
                bytesum += byteread; //字节数 文件大小
                fs.write(buffer, 0, byteread);
            }
            fis.close();
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }
    }
}
