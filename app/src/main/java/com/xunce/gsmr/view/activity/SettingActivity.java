package com.xunce.gsmr.view.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.xunce.gsmr.R;
import com.xunce.gsmr.model.event.LocateModeChangeEvent;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;
import com.xunce.gsmr.util.view.ViewHelper;
import com.xunce.gsmr.view.style.TransparentStyle;
import com.zhd.zhdcorsnet.CorsGprsService;
import com.zhd.zhdcorsnet.NetHelper;
import com.zhd.zhdcorsnet.SourceNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * 设置Activity
 * Created by ssthouse on 2015/9/8.
 */
public class SettingActivity extends AppCompatActivity {
    /**
     * 是否使用wifi的switch
     */
    private Switch locateModeSwitch;
    static boolean reconnect = true;
    String currentNode = null;
    List<SourceNode> sourceNodeList;
    ArrayAdapter NodeAdapter;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingActivity.class));
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        TransparentStyle.setTransparentStyle(this, R.color.color_primary);

        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ViewHelper.initActionBar(this, getSupportActionBar(), "设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //地图定位模式切换
        locateModeSwitch = (Switch) findViewById(R.id.id_sw_locate_mode);
        //首先设置为preference中的状态
        locateModeSwitch.setChecked(PreferenceHelper.getInstance(SettingActivity.this).getIsWifiLocateMode(this));
        locateModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceHelper.getInstance(SettingActivity.this).setLocateMode(SettingActivity.this, isChecked);
                //发送定位模式改变event
                EventBus.getDefault().post(new LocateModeChangeEvent());
                if (isChecked) {
                    ToastHelper.show(SettingActivity.this, "切换为使用Wifi定位");
                } else {
                    ToastHelper.show(SettingActivity.this, "切换为GPS定位");
                }
            }
        });

        //spinner选择地图类型
        Spinner sp = (Spinner) findViewById(R.id.id_sp_map_type);
        if (PreferenceHelper.getInstance(SettingActivity.this).getMapType() ==
                PreferenceHelper.MapType.BAIDU_MAP) {
            sp.setSelection(0);
        } else {
            sp.setSelection(1);
        }
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    PreferenceHelper.getInstance(SettingActivity.this)
                            .setMapType(PreferenceHelper.MapType.BAIDU_MAP);
                    Timber.e("我设置了---百度地图");
                } else if (position == 1) {
                    PreferenceHelper.getInstance(SettingActivity.this)
                            .setMapType(PreferenceHelper.MapType.GAODE_MAP);
                    Timber.e("我设置了--高德地图");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //选中点的Icon的设置界面
        findViewById(R.id.id_ll_marker_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkerIconSetActivity.start(SettingActivity.this);
            }
        });

        //app版本按钮
        findViewById(R.id.id_tv_app_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAppVersionDialog();
            }
        });

        findViewById(R.id.id_tv_cors_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCORSSetting();
            }
        });
    }

    /**
     * 显示CORS设置对话框
     */
    private void showCORSSetting() {
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(this).
                inflate(R.layout.dialog_cors_setting, null);
        final EditText ipET = (EditText) llPrjName.findViewById(R.id.et_cors_ip);
        final EditText portET = (EditText) llPrjName.findViewById(R.id.et_cors_port);
        final EditText usernameET = (EditText) llPrjName.findViewById(R.id.et_cors_username);
        final EditText passwordET = (EditText) llPrjName.findViewById(R.id.et_cors_password);
        ipET.setText(PreferenceHelper.getInstance(this).getCORSip(this));
        portET.setText(PreferenceHelper.getInstance(this).getCORSport(this));
        usernameET.setText(PreferenceHelper.getInstance(this).getCORSusername(this));
        passwordET.setText(PreferenceHelper.getInstance(this).getCORSpassword(this));
        //Spinner初始化
        final Spinner sourceNodeSpinner = (Spinner) llPrjName.findViewById(R.id.sp_cors_sourcecode);
        final List<String> sourceNodeArrayList = new ArrayList<>();
        sourceNodeArrayList.add(PreferenceHelper.getInstance(this).getCORSnode(this));
        NodeAdapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,sourceNodeArrayList);
        sourceNodeSpinner.setAdapter(NodeAdapter);
        sourceNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentNode = sourceNodeArrayList.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button getNodeBtn = (Button) llPrjName.findViewById(R.id.id_btn_get_cors_node);
        getNodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipET.getText().toString();
                String port = portET.getText().toString();
                if (ip.isEmpty() || port.isEmpty()){
                    ToastHelper.show(SettingActivity.this,"请输入IP或PORT");
                    return;
                }
                sourceNodeList = NetHelper.GetSourceNode(ip,port);
                if (sourceNodeList == null || sourceNodeList.size()==0){
                    ToastHelper.show(SettingActivity.this,"查询失败");
                    return;
                }
                sourceNodeArrayList.clear();
                for (SourceNode sourceNode : sourceNodeList) {
                    sourceNodeArrayList.add(sourceNode.toString());
                    NodeAdapter = new ArrayAdapter(getBaseContext(),R.layout.support_simple_spinner_dropdown_item,sourceNodeArrayList);
                    sourceNodeSpinner.setAdapter(NodeAdapter);
                }
            }
        });
        RadioGroup reConnectRG = (RadioGroup) llPrjName.findViewById(R.id.rg_cors_choice);
        reconnect = PreferenceHelper.getInstance(this).getCORSreconnect(this);
        if (reconnect) {
            reConnectRG.check(R.id.rb_cors_yes);
        } else {
            reConnectRG.check(R.id.rb_cors_no);
        }
        reConnectRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_cors_yes:
                        reconnect = true;
                        break;
                    case R.id.rb_cors_no:
                        reconnect = false;
                        break;
                }
            }
        });
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog dialog = dialogBuilder
                .setTitle("CORS设置")
                .setView(llPrjName)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip = ipET.getText().toString();
                        String port = portET.getText().toString();
                        String username = usernameET.getText().toString();
                        String password = passwordET.getText().toString();
                        if (ip.isEmpty()){
                            ToastHelper.show(getBaseContext(),"请输入IP");
                            return;
                        }
                        if (port.isEmpty()){
                            ToastHelper.show(getBaseContext(),"请输入PORT");
                            return;
                        }
                        if (username.isEmpty()){
                            ToastHelper.show(getBaseContext(),"请输入USERNAME");
                            return;
                        }
                        if (password.isEmpty()){
                            ToastHelper.show(getBaseContext(),"请输入PASSWORD");
                            return;
                        }
                        if (currentNode == null){
                            ToastHelper.show(getBaseContext(),"请选择Node节点");
                            return;
                        }
                        Intent intent = new Intent(getApplicationContext(), CorsGprsService.class);
                        intent.putExtra("ip",ip);
                        intent.putExtra("port",port);
                        intent.putExtra("username",username);
                        intent.putExtra("password",password);
                        intent.putExtra("sourcenode",currentNode);
                        intent.putExtra("reconnect",reconnect);
                        getApplicationContext().startService(intent);
                        //保存信息
                        PreferenceHelper.getInstance(getBaseContext()).setCORSip(getBaseContext(),ip);
                        PreferenceHelper.getInstance(getBaseContext()).setCORSport(getBaseContext(),port);
                        PreferenceHelper.getInstance(getBaseContext()).setCORSusername(getBaseContext(),username);
                        PreferenceHelper.getInstance(getBaseContext()).setCORSpassword(getBaseContext(),password);
                        PreferenceHelper.getInstance(getBaseContext()).setCORSnode(getBaseContext(),currentNode);
                        PreferenceHelper.getInstance(getBaseContext()).setCORSreconnect(getBaseContext(),reconnect);
                        ToastHelper.show(SettingActivity.this,"正在启动服务");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        dialog.show();
    }

    /**
     * 显示app的版本Dialog
     */
    private void showAppVersionDialog() {
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);
        dialogBuilder.setCustomView(R.layout.dialog_app_version, this)
                .withTitle("版本信息")
                //.withTitleColor(0xffffff)
                .withDialogColor(getResources().getColor(R.color.color_primary_light))
                .isCancelableOnTouchOutside(true)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
