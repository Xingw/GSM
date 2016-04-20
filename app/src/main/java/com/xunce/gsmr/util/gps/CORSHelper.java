package com.xunce.gsmr.util.gps;

import android.content.Context;
import android.content.Intent;

import com.zhd.zhdcorsnet.CorsGprsService;
import com.zhd.zhdcorsnet.NetHelper;
import com.zhd.zhdcorsnet.SourceNode;

import java.util.List;

/**
 * Created by Xingw on 2016/4/20.
 */
public class CORSHelper {

    private static String CORS_IP;
    private static String CORS_PORT;
    private static String CORS_USERNAME;
    private static String CORS_PASSWORD;
    private static String CORS_SOURCENODE;
    private static String CORS_ISRECONNECT;
    //获取服务节点
    public static List<SourceNode> GetNode(){
        return NetHelper.GetSourceNode(CORS_IP, CORS_PORT);
    }


    public static void start(Context context){
        Intent intent = new Intent(
                context.getApplicationContext(),
                CorsGprsService.class);
        intent.putExtra("ip", CORS_IP);
        intent.putExtra("port", CORS_PORT);
        intent.putExtra("username", CORS_USERNAME);
        intent.putExtra("password", CORS_PASSWORD);
        intent.putExtra("sourcenode", CORS_SOURCENODE);
        intent.putExtra("reconnect", CORS_ISRECONNECT);
        context.getApplicationContext().startService(intent);
    }

    public static void cancel(Context context){
        Intent intent = new Intent(context.getApplicationContext(), CorsGprsService.class);
        context.getApplicationContext().stopService(intent);
    }
}
