package com.xunce.gsmr.util.gps;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;

import com.orhanobut.logger.Logger;
import com.xunce.gsmr.util.view.ToastHelper;

import java.util.List;

/**
 * Created by Xingw on 2016/5/12.
 */
public class GPSUtil {

    public static LocationManager getCORSLocationManager(Context context){
        LocationManager locationManager = (LocationManager)context.getSystemService(Context
                .LOCATION_SERVICE);
        if (!locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
        {
            ToastHelper.show(context,"对不起，您的设备无法使用GPS服务，请切换至wifi定位");
            return null;
        }
       return locationManager;
    }
}
