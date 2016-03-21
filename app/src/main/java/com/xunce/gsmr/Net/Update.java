package com.xunce.gsmr.Net;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.orhanobut.logger.Logger;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.util.DBHelper;
import com.xunce.gsmr.util.view.ToastHelper;

import org.json.JSONException;
import org.json.JSONObject;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

/**
 * Created by Xingw on 2016/3/20.
 */
public class Update {
    public static void checkversion(final Context context) {
        FIR.checkForUpdateInFIR(Constant.firToken, new VersionCheckCallback() {
            @Override
            public void onSuccess(String versionJson) {
                Logger.i("check from fir.im success! versionJson:%s", versionJson);
                try {
                    JSONObject jsonObject = new JSONObject(versionJson);
                    int version = jsonObject.getInt("version");
                    String versionShort = jsonObject.getString("versionShort");
                    String url = jsonObject.getString("direct_install_url");
                    PackageManager manager = context.getPackageManager();
                    PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                    String appVersion = info.versionName; // 版本名
                    int currentVersionCode = info.versionCode; // 版本号
                    if (version == currentVersionCode) {
                        if (appVersion.equals(versionShort)) {
                            showUpateDialog(context);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFail(Exception exception) {
                Logger.i("check fir.im fail! %s", exception.getMessage().toString());
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {
            }
        });
    }

    private static void showUpateDialog(Context context) {
        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);
        //创建监听事件
        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        };
        View.OnClickListener confirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        };
        dialogBuilder.withTitle("版本更新")
                .withTitleColor("#FFFFFF")
                .withDividerColor("#11000000")
                .withMessage("有新的版本，是否要更新")//.withMessage(null)  no Msg
                .withMessageColor("#FFFFFFFF")
                .withDialogColor(context.getResources().getColor(R.color.dialog_color))
                .withEffect(Effectstype.Slidetop)       //def Effectstype.Slidetop
                .withButton1Text("更新")                 //def gone
                .withButton2Text("取消")                 //def gone
                .isCancelableOnTouchOutside(false)
                .setButton1Click(confirmListener)
                .setButton2Click(cancelListener)
                .show();
    }
}
