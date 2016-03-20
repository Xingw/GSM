package com.xunce.gsmr.app;

import android.app.Application;

import im.fir.sdk.FIR;
import timber.log.Timber;

/**
 * 程序入口
 * Created by ssthouse on 2015/7/16.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        //fir统计
        FIR.init(this);
        //调试阶段---不提交数据
        //FIR.setDebug(true);
        super.onCreate();
        //初始化Timber日志
        Timber.plant(new Timber.DebugTree());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
