package com.xunce.gsmr.Net;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.xunce.gsmr.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Xingw on 2016/3/21.
 */
public class DownloadService extends Service {
    public static final String Install_Apk = "Install_Apk";
    public static final String Key_App_Name = "Key_App_Name";
    public static final String Key_Down_Url = "Key_Down_Url";

    /********
     * download progress step
     *********/
    private static final int down_step_custom = 3;

    private static final int TIMEOUT = 10 * 1000;// 超时
    private static String down_url;
    private static final int DOWN_OK = 1;
    private static final int DOWN_ERROR = 0;

    private String app_name = "GSM";

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews contentView;
    private File updateFile;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 方法描述：onStartCommand方法
     *
     * @return int
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        app_name = intent.getStringExtra("Key_App_Name");
        down_url = intent.getStringExtra("Key_Down_Url");
        updateFile = new File(Environment.getDataDirectory()+"/"+app_name+".apk");
        if(updateFile.exists()){
            updateFile.delete();
        }
        try {
            updateFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        createNotification();
        createThread();
        return super.onStartCommand(intent, flags, startId);
    }


    /*********
     * update UI
     ******/
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_OK:
                    /*****安装APK******/
                    installApk();
                    /***stop service*****/
                    stopSelf();
                    break;

                case DOWN_ERROR:
                    notification.flags = Notification.FLAG_AUTO_CANCEL;
                    //notification.setLatestEventInfo(UpdateService.this,app_name, getString(R.string.down_fail), pendingIntent);
                    notification.setLatestEventInfo(DownloadService.this, app_name,"下载失败", null);
                    /***stop service*****/
                    //onDestroy();
                    stopSelf();
                    break;

                default:
                    //stopService(updateIntent);
                    /******Stop service******/
                    //stopService(intentname)
                    //stopSelf();
                    break;
            }
        }
    };

    private void installApk() {
        // TODO Auto-generated method stub
        /*********下载完成，点击安装***********/
        Uri uri = Uri.fromFile(updateFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        /**********加这个属性是因为使用Context的startActivity方法的话，就需要开启一个新的task**********/
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        DownloadService.this.startActivity(intent);
    }

    /**
     * 方法描述：createThread方法, 开线程下载
     *
     * @param
     * @return
     */
    public void createThread() {
        new DownLoadThread().start();
    }


    private class DownLoadThread extends Thread {
        @Override
        public void run() {
            Message message = new Message();
            try {
                long downloadSize = downloadUpdateFile(down_url, updateFile.toString());
                if (downloadSize > 0) {
                    // down success
                    message.what = DOWN_OK;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                message.what = DOWN_ERROR;
                handler.sendMessage(message);
            }
        }
    }


    /**
     * 方法描述：createNotification方法
     *
     * @param
     * @return
     */
    public void createNotification() {

        //notification = new Notification(R.drawable.dot_enable,app_name + getString(R.string.is_downing) ,System.currentTimeMillis());
        notification = new Notification(
                //R.drawable.video_player,//应用的图标
                R.mipmap.ic_launcher,//应用的图标
                app_name + "正在下载",
                System.currentTimeMillis());
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        //notification.flags = Notification.FLAG_AUTO_CANCEL;

        /*** 自定义  Notification 的显示****/
        contentView = new RemoteViews(getPackageName(), R.layout.notification_update_layout);
        contentView.setTextViewText(R.id.tv_name_update, app_name + "正在下载");
        contentView.setTextViewText(R.id.tv_name_update, "0%");
        contentView.setProgressBar(R.id.progressbar_update, 100, 0, false);
        notification.contentView = contentView;

//      updateIntent = new Intent(this, AboutActivity.class);
//      updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//      //updateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//      pendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);
//      notification.contentIntent = pendingIntent;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.layout.notification_update_layout, notification);
    }

    /***
     * down file
     *
     * @return
     */
    public long downloadUpdateFile(String down_url, String file) throws Exception {

        int down_step = down_step_custom;// 提示step
        int totalSize;// 文件总大小
        int downloadCount = 0;// 已经下载好的大小
        int updateCount = 0;// 已经上传的文件大小

        InputStream inputStream;
        OutputStream outputStream;

        URL url = new URL(down_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(TIMEOUT);
        httpURLConnection.setReadTimeout(TIMEOUT);
        // 获取下载文件的size
        totalSize = httpURLConnection.getContentLength();

        if (httpURLConnection.getResponseCode() == 404) {
            throw new Exception("fail!");
            //这个地方应该加一个下载失败的处理，但是，因为我们在外面加了一个try---catch，已经处理了Exception,
            //所以不用处理
        }

        inputStream = httpURLConnection.getInputStream();
        outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉

        byte buffer[] = new byte[1024];
        int readsize = 0;

        while ((readsize = inputStream.read(buffer)) != -1) {

//          /*********如果下载过程中出现错误，就弹出错误提示，并且把notificationManager取消*********/
//          if (httpURLConnection.getResponseCode() == 404) {
//              notificationManager.cancel(R.layout.notification_item);
//              throw new Exception("fail!");
//              //这个地方应该加一个下载失败的处理，但是，因为我们在外面加了一个try---catch，已经处理了Exception,
//              //所以不用处理
//          }

            outputStream.write(buffer, 0, readsize);
            downloadCount += readsize;// 时时获取下载到的大小
            /*** 每次增张3%**/
            if (updateCount == 0 || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
                updateCount += down_step;
                // 改变通知栏
                contentView.setTextViewText(R.id.tv_name_update, updateCount + "%");
                contentView.setProgressBar(R.id.progressbar_update, 100, updateCount, false);
                notification.contentView = contentView;
                notificationManager.notify(R.layout.notification_update_layout, notification);
            }
        }
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
        inputStream.close();
        outputStream.close();

        return downloadCount;
    }
}
