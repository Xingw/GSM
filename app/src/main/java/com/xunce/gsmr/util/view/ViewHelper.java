package com.xunce.gsmr.util.view;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.xunce.gsmr.R;

/**
 * View工具类
 * Created by ssthouse on 2015/8/20.
 */
public class ViewHelper {

    public static void initActionBar(AppCompatActivity activity, ActionBar actionBar, String title){
        actionBar.setBackgroundDrawable(activity.getResources().getDrawable(R.color.color_primary));
        actionBar.setTitle(title);
    }
}
