package com.xunce.gsmr.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.orhanobut.logger.Logger;
import com.xunce.gsmr.R;
import com.xunce.gsmr.model.event.LocateModeChangeEvent;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;
import com.xunce.gsmr.util.view.ViewHelper;
import com.xunce.gsmr.view.activity.gaode.GaodePrjEditActivity;
import com.xunce.gsmr.view.style.TransparentStyle;
import com.zhd.zhdcorsnet.CorsGprsService;
import com.zhd.zhdcorsnet.MainActivity;
import com.zhd.zhdcorsnet.NetHelper;
import com.zhd.zhdcorsnet.SourceNode;

import java.util.List;

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
        LocationManager locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
        {
            ToastHelper.show(this,"对不起，您的设备不支持CORS服务");
            return;
        }
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(this).
                inflate(R.layout.dialog_cors_setting, null);
        final EditText ipET_1 = (EditText) llPrjName.findViewById(R.id.et_cors_ip_1);
        final EditText ipET_2 = (EditText) llPrjName.findViewById(R.id.et_cors_ip_2);
        final EditText ipET_3 = (EditText) llPrjName.findViewById(R.id.et_cors_ip_3);
        final EditText ipET_4 = (EditText) llPrjName.findViewById(R.id.et_cors_ip_4);
        final EditText portET = (EditText) llPrjName.findViewById(R.id.et_cors_port);
        final EditText usernameET = (EditText) llPrjName.findViewById(R.id.et_cors_username);
        final EditText passwordET = (EditText) llPrjName.findViewById(R.id.et_cors_password);
        String ip_saved = PreferenceHelper.getInstance(this).getCORSip(this);
        String[] ip_split=ip_saved.split("\\.");
        ipET_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()==3)ipET_2.requestFocus();
            }
        });
        ipET_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()==3)ipET_3.requestFocus();
                if (s.length()==0) {
                    ipET_1.requestFocus();
                    ipET_1.setSelection(ipET_1.getText().length());
                }
            }
        });
        ipET_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()==3)ipET_4.requestFocus();
                if (s.length()==0) {
                    ipET_2.requestFocus();
                    ipET_2.setSelection(ipET_2.getText().length());
                }
            }
        });
        ipET_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()==0) {
                    ipET_3.requestFocus();
                    ipET_3.setSelection(ipET_3.getText().length());
                }
            }
        });
        if(ip_split !=null && ip_split.length>3) {
            ipET_1.setText(ip_split[0]);
            ipET_2.setText(ip_split[1]);
            ipET_3.setText(ip_split[2]);
            ipET_4.setText(ip_split[3]);
        }
        portET.setText(PreferenceHelper.getInstance(this).getCORSport(this));
        usernameET.setText(PreferenceHelper.getInstance(this).getCORSusername(this));
        passwordET.setText(PreferenceHelper.getInstance(this).getCORSpassword(this));

        String node = PreferenceHelper.getInstance(this).getCORSnode(this);
        final Button getNodeBtn = (Button) llPrjName.findViewById(R.id.id_btn_get_cors_node);
        if (!node.isEmpty()){
            getNodeBtn.setText(node);
            currentNode = node;
        }
        getNodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip_1 = ipET_1.getText().toString();
                String ip_2 = ipET_2.getText().toString();
                String ip_3 = ipET_3.getText().toString();
                String ip_4 = ipET_4.getText().toString();
                if (ip_1.isEmpty() || ip_2.isEmpty() || ip_3.isEmpty() || ip_4.isEmpty()){
                    ToastHelper.show(getBaseContext(),"请输入IP");
                    return;
                }
                String ip = (ip_1+"."+ip_2+"."+ip_3+"."+ip_4);
                String port = portET.getText().toString();
                if (port.isEmpty()){
                    ToastHelper.show(SettingActivity.this,"请输入PORT");
                    return;
                }
                sourceNodeList = NetHelper.GetSourceNode(ip,port);
                if (sourceNodeList == null || sourceNodeList.size()==0){
                    ToastHelper.show(SettingActivity.this,"查询失败");
                    showNodeInputDialog(getNodeBtn);
                    return;
                }
                showNodeSelectDialog(sourceNodeList,getNodeBtn);
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
                        String ip_1 = ipET_1.getText().toString();
                        String ip_2 = ipET_2.getText().toString();
                        String ip_3 = ipET_3.getText().toString();
                        String ip_4 = ipET_4.getText().toString();
                        if (ip_1.isEmpty() || ip_2.isEmpty() || ip_3.isEmpty() || ip_4.isEmpty()){
                            ToastHelper.show(getBaseContext(),"请输入IP");
                            return;
                        }
                        String ip = (ip_1+"."+ip_2+"."+ip_3+"."+ip_4);
                        Logger.d("IP地址是：%s",ip);
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
                        if (currentNode == null || currentNode.isEmpty()){
                            ToastHelper.show(getBaseContext(),"请选择Node节点");
                            return;
                        }
                        currentNode = currentNode.toUpperCase();
                        Intent intent = new Intent(getApplicationContext(), CorsGprsService.class);
                        intent.putExtra("ip",ip);
                        intent.putExtra("port",port);
                        intent.putExtra("username",username);
                        intent.putExtra("password",password);
                        intent.putExtra("sourcenode",currentNode);
                        intent.putExtra("reconnect",reconnect);
                        getApplicationContext().startService(intent);
                        Logger.d("ip:%s\nport:%s\nusername:%s\npassword:%s\nsourcenode:%s\n",ip,
                                port,username,password,currentNode);
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

    private void showNodeInputDialog(final Button getNodeBtn) {
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(this).
                inflate(R.layout.dialog_prj_name, null);
        final EditText etPrjName = (EditText) llPrjName.findViewById(R.id.id_et);
        Button confirmButton = (Button) llPrjName.findViewById(R.id.tv_input_confirm);
        Button cancelButton = (Button) llPrjName.findViewById(R.id.tv_input_cancel);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog dialog = dialogBuilder
                .setTitle("输入源节点")
                .setMessage("未找到可以使用的源节点，请尝试手动输入")
                .setView(llPrjName)
                .create();
        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        };
        View.OnClickListener confirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNodeBtn.setText(etPrjName.getText().toString());
               currentNode = etPrjName.getText().toString();
                dialog.dismiss();
            }
        };
        confirmButton.setOnClickListener(confirmListener);
        cancelButton.setOnClickListener(cancelListener);
        dialog.show();
    }

    private void showNodeSelectDialog(List<SourceNode> sourceNodeList, final Button getNodeBtn) {
        final String[] Node = new String[sourceNodeList.size()];
        for (int i = 0; i < sourceNodeList.size(); i++) {
            Node[i] = sourceNodeList.get(i).Authentication;
        }
        new AlertDialog.Builder(this)
                .setItems(Node, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getNodeBtn.setText(Node[which]);
                        currentNode = Node[which];
                    }
                })
                .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
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
