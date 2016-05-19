package com.xunce.gsmr.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.orhanobut.logger.Logger;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.model.PrjItemRealmObject;
import com.xunce.gsmr.util.DBConstant;
import com.xunce.gsmr.util.DBHelper;
import com.xunce.gsmr.util.FileHelper;
import com.xunce.gsmr.util.VibrateHelper;
import com.xunce.gsmr.util.preference.PreferenceHelper;
import com.xunce.gsmr.util.view.ToastHelper;
import com.xunce.gsmr.util.view.ViewHelper;
import com.xunce.gsmr.view.activity.baidu.BaiduPrjEditActivity;
import com.xunce.gsmr.view.activity.gaode.GaodePrjEditActivity;
import com.xunce.gsmr.view.adapter.PrjLvAdapter;
import com.xunce.gsmr.view.style.TransparentStyle;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import io.realm.Realm;

/**
 * 主界面选择工程的Activity Created by ssthouse on 2015/7/17.
 */
public class PrjSelectActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOAD_DB = 1000;
    private static String EXTRA_KEY_IS_CALLED = "is_called_by_prj_edit";
    private ListView lv;
    private boolean isInSelectMode = false;
    private PrjLvAdapter adapter;
    private FloatingActionButton btnAdd;
    private long mExitTime;
    private Realm realm;
    private CoordinatorLayout container;

    public static void start(Activity activity, boolean isCalledByPrjEditAty) {
        Intent intent = new Intent(activity, PrjSelectActivity.class);
        intent.putExtra(EXTRA_KEY_IS_CALLED, isCalledByPrjEditAty);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getInstance(this);
        setContentView(R.layout.activity_prj_select);
        TransparentStyle.setTransparentStyle(this, R.color.color_primary);
        //监测是否为PrjEditActivity调用
        boolean isCalled = getIntent().getBooleanExtra(EXTRA_KEY_IS_CALLED, false);
        //检查工程内存的数据库是否存在
        checkDbLocation();
        if (isCalled) {
            initView();
            return;
        }

        //判断---如果有上次打开的Project---就直接跳转
        //判断是否有上次编辑的project
        if (PreferenceHelper.getInstance(this).hasLastEditPrjItem(this)) {
            PrjItemRealmObject prjItemRealmObject = DBHelper.getPrjItemByName(realm, PreferenceHelper.getInstance(this)
                    .getLastEditPrjName(this));
            if (prjItemRealmObject != null) {
                //判断MapType
                //判断地图类型--启动Activity
                if (PreferenceHelper.getInstance(PrjSelectActivity.this).getMapType()
                        == PreferenceHelper.MapType.BAIDU_MAP) {
                    BaiduPrjEditActivity.start(PrjSelectActivity.this, DBHelper.toPrjItem(prjItemRealmObject));
                } else {
                    GaodePrjEditActivity.start(PrjSelectActivity.this, DBHelper.toPrjItem(prjItemRealmObject));
                }
                finish();
            }
        }

        //初始化View
        initView();
    }

    /**
     * 检查数据库是否存在 不存在的话就删除这个内容
     */
    private void checkDbLocation() {
        List<PrjItemRealmObject> prjItemRealmObjectList = DBHelper.getPrjItemRealmList(realm);
        if (prjItemRealmObjectList == null || prjItemRealmObjectList.size() == 0) return;
        for (int i = 0; i < prjItemRealmObjectList.size(); i++) {
            File file = new File(prjItemRealmObjectList.get(i).getDbLocation());
            if (!file.exists()) {
                if (PreferenceHelper.getInstance(this).getLastEditPrjName(this).equals(prjItemRealmObjectList.get(i)
                        .getPrjName())) {
                    PreferenceHelper.getInstance(this).deleteLastEditPrjName(this);
                }
                realm.beginTransaction();
                prjItemRealmObjectList.get(i).removeFromRealm();
                realm.commitTransaction();
            }
        }
//        for (PrjItemRealmObject item : prjItemRealmObjectList) {
//            File file = new File(item.getDbLocation());
//            if (!file.exists()) {
//                if (PreferenceHelper.getInstance(this).getLastEditPrjName(this).equals(item
//                        .getPrjName())) {
//                    PreferenceHelper.getInstance(this).deleteLastEditPrjName(this);
//                }
//                realm.beginTransaction();
//                item.removeFromRealm();
//                realm.commitTransaction();
//            }
//        }
    }

    /**
     * 初始化View
     */
    private void initView() {
        ViewHelper.initActionBar(this, getSupportActionBar(), "基址勘察");
        container = (CoordinatorLayout) findViewById(R.id.container);
        lv = (ListView) findViewById(R.id.id_lv);
        adapter = new PrjLvAdapter(this, DBHelper.getPrjItemList(realm));
        lv.setAdapter(adapter);
        lv.setTextFilterEnabled(true);
        //开启工程编辑Activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isInSelectMode) {
                    PrjItem prjItem = adapter.getPrjItemList().get(position);
                    //如果已经选中了的又被点击---剔除
                    if (adapter.isinselectList(prjItem)) {
                        adapter.removefromSelectList(prjItem);
                    } else {
                        adapter.getSelectList().add(prjItem);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    //点击某一个prjItem的时候跳转到---具体的编辑界面(一个地图---很多按钮)
                    //保存当前要编辑的PrjName到preference
                    PreferenceHelper.getInstance(PrjSelectActivity.this)
                            .setLastEditPrjName(PrjSelectActivity.this,
                                    adapter.getPrjItemList().get(position).getPrjName());
                    finish();
                    //判断地图类型--启动Activity
                    if (PreferenceHelper.getInstance(PrjSelectActivity.this).getMapType()
                            == PreferenceHelper.MapType.BAIDU_MAP) {
                        BaiduPrjEditActivity.start(PrjSelectActivity.this, adapter.getPrjItemList()
                                .get(position));
                    } else {
                        GaodePrjEditActivity.start(PrjSelectActivity.this, adapter.getPrjItemList()
                                .get(position));
                    }
                }
            }
        });
        //长按监听事件
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //震动
                VibrateHelper.shortVibrate(PrjSelectActivity.this);
                //显示长按的菜单Dialog
                showLvLongClickDialog(PrjSelectActivity.this,
                        adapter.getPrjItemList().get(position), adapter);
                return true;
            }
        });

        btnAdd = (FloatingActionButton) findViewById(R.id.id_btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInSelectMode) {
                    btnAdd.setVisibility(View.GONE);
                    btnAdd.setImageResource(R.drawable.fab_add);
                    adapter.CheckBox_Moveout();
                    isInSelectMode = false;
                    List<PrjItem> list = adapter.getSelectList();
                    if (list != null) {
                        for (PrjItem prjItem : list) {
                            FileHelper.sendDbFile(PrjSelectActivity.this, prjItem.getDbLocation());
                        }
                        adapter.cleanSelectList();
                    }
                } else {
                    //会自动回调----刷新界面
                    showPrjNameDialog(PrjSelectActivity.this, adapter);
                }
            }
        });
    }

    /**
     * 长按显示的Menu的Dialog
     *
     * @param context
     * @param prjItem
     * @param adapter
     */
    public void showLvLongClickDialog(final Context context, final PrjItem prjItem, final PrjLvAdapter adapter) {
        //build出dialog
        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);
        //inflate出View---配置点击事件
        LinearLayout ll = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_lv_item, null);
        ll.findViewById(R.id.id_menu_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
                showMakesureDeleteDialog(context,prjItem.toRealmObject(realm));
                //刷新视图
                adapter.notifyDataSetChanged();
            }
        });
        ll.findViewById(R.id.id_menu_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
                //开启编辑PrjItem的Activity
                FileHelper.sendDbFile(PrjSelectActivity.this, prjItem.getDbLocation());
            }
        });
        ll.findViewById(R.id.id_menu_rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
                //开启重命名的Dialog
                showChangeNameDialog(context, prjItem.toRealmObject(realm));
            }
        });
        dialogBuilder.withTitle(null)             //.withTitle(null)  no title
                .withTitleColor("#FFFFFF")
                .withDividerColor("#11000000")
                .withMessage(null)//.withMessage(null)  no Msg
                .withMessageColor("#FFFFFFFF")
                .withDialogColor(context.getResources().getColor(R.color.dialog_color))
                .withEffect(Effectstype.Slidetop)       //def Effectstype.Slidetop
                .setCustomView(ll, context)
                .isCancelableOnTouchOutside(false)       //不可以点击外面取消
                .show();
    }

    /**
     * 重命名的Dialog
     *
     * @param context
     * @param prjItemRealmObject
     */
    public void showChangeNameDialog(final Context context, final PrjItemRealmObject prjItemRealmObject) {
        //导出View
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(context).
                inflate(R.layout.dialog_prj_name, null);
        final EditText etPrjName = (EditText) llPrjName.findViewById(R.id.id_et);
        //导出Dialog
        Button confirmButton = (Button) llPrjName.findViewById(R.id.tv_input_confirm);
        Button cancelButton = (Button) llPrjName.findViewById(R.id.tv_input_cancel);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog dialog = dialogBuilder
                .setTitle("重命名")
                .setView(llPrjName)
                .create();
        //创建监听事件
        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        };
        View.OnClickListener confirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prjName = etPrjName.getText().toString();
                if (prjName.equals("")) {
                    ToastHelper.showSnack(context, container, "工程名不可为空");
                } else {
                    if (DBHelper.isPrjExist(realm, prjName)) {
                        ToastHelper.showSnack(context, container, "该工程已存在");
                    } else {
                        changeName(prjItemRealmObject, prjName);
                        //刷新视图
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                        ToastHelper.showSnack(context, container, "重命名成功!");
                    }
                }
            }
        };
        confirmButton.setOnClickListener(confirmListener);
        cancelButton.setOnClickListener(cancelListener);
        dialog.show();
    }

    public void showMakesureDeleteDialog(Context context, final PrjItemRealmObject prjItemRealmObject) {
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(context).
                inflate(R.layout.dialog_delete_makesure, null);
        Button confirmButton = (Button) llPrjName.findViewById(R.id.tv_input_confirm);
        Button cancelButton = (Button) llPrjName.findViewById(R.id.tv_input_cancel);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog dialog = dialogBuilder
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
                deletePrj(prjItemRealmObject);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        };
        confirmButton.setOnClickListener(confirmListener);
        cancelButton.setOnClickListener(cancelListener);
        dialog.show();
    }

    /**
     * 显示新工程名输入的Dialog
     *
     * @param context
     * @param adapter
     */
    public void showPrjNameDialog_Nifty(final Context context, final PrjLvAdapter adapter) {
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(context).
                inflate(R.layout.dialog_prj_name, null);
        final EditText etPrjName = (EditText) llPrjName.findViewById(R.id.id_et);
        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);
        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        };
        View.OnClickListener confirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prjName = etPrjName.getText().toString();
                if (prjName.equals("")) {
                    ToastHelper.showSnack(context, v, "工程名不可为空");
                } else {
                    if (DBHelper.isPrjExist(realm, prjName)) {
                        ToastHelper.showSnack(context, v, "该工程已存在");
                    } else {
                        //将新的prjItem保存进数据库
                        if (!DBHelper.createDbData(Constant.DbTempPath + prjName + ".db", prjName)) {
                            ToastHelper.showSnack(context, v, "该工程已存在，请重新命名或选择导入工程");
                        } else {
                            PrjItemRealmObject prjItemRealmObject = new PrjItemRealmObject(prjName, Constant.DbTempPath + prjName +
                                    ".db", DBHelper.getTimeNow());
                            realm.beginTransaction();
                            realm.copyToRealm(prjItemRealmObject);
                            realm.commitTransaction();
                            DBHelper.insertPrjInfo(prjItemRealmObject.getDbLocation(), prjItemRealmObject);
                            //重新加载工程视图
                            adapter.notifyDataSetChanged();
                            //消除Dialog
                            dialogBuilder.dismiss();
                            //Toast 提醒成功
                            ToastHelper.showSnack(context, v, "工程创建成功!");
                        }
                    }
                }
            }
        };
        dialogBuilder.withTitle("工程名")             //.withTitle(null)  no title
                .withTitleColor("#FFFFFF")
                .withDividerColor("#11000000")
                .withMessage(null)//.withMessage(null)  no Msg
                .withMessageColor("#FFFFFFFF")
                .withDialogColor(context.getResources().getColor(R.color.dialog_color))
                .withEffect(Effectstype.Slidetop)       //def Effectstype.Slidetop
                .setCustomView(llPrjName, context)
                .withButton1Text("确认")                 //def gone
                .withButton2Text("取消")                 //def gone
                .isCancelableOnTouchOutside(false)
                .setButton1Click(confirmListener)
                .setButton2Click(cancelListener)
                .show();
    }

    /**
     * 新建项目对话框
     *
     * @param context
     * @param adapter
     */
    public void showPrjNameDialog(final Context context, final PrjLvAdapter adapter) {
        LinearLayout llPrjName = (LinearLayout) LayoutInflater.from(context).
                inflate(R.layout.dialog_prj_name, null);
        final EditText etPrjName = (EditText) llPrjName.findViewById(R.id.id_et);
        Button confirmButton = (Button) llPrjName.findViewById(R.id.tv_input_confirm);
        Button cancelButton = (Button) llPrjName.findViewById(R.id.tv_input_cancel);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog dialog = dialogBuilder
                .setTitle("新建项目")
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
                String prjName = etPrjName.getText().toString();
                if (prjName.equals("")) {
                    ToastHelper.showSnack(context, container, "工程名不可为空");
                } else {
                    if (DBHelper.isPrjExist(realm, prjName)) {
                        ToastHelper.showSnack(context, container, "该工程已存在");
                    } else {
                        //将新的prjItem保存进数据库
                        if (!DBHelper.createDbData(Constant.DbTempPath + prjName + ".db", prjName)) {
                            ToastHelper.showSnack(context, container, "创建失败");
                        } else {
                            PrjItemRealmObject prjItemRealmObject = new PrjItemRealmObject(prjName, Constant.DbTempPath + prjName +
                                    ".db", DBHelper.getTimeNow());
                            realm.beginTransaction();
                            realm.copyToRealm(prjItemRealmObject);
                            realm.commitTransaction();
                            //重新加载工程视图
                            adapter.notifyDataSetChanged();
                            //消除Dialog
                            dialog.dismiss();
                            //Toast 提醒成功
                            ToastHelper.showSnack(context, container, "工程创建成功!");
                        }
                    }
                }
            }
        };
        confirmButton.setOnClickListener(confirmListener);
        cancelButton.setOnClickListener(cancelListener);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_prj_select, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.id_action_search).getActionView();
        searchView.setQueryHint("工程名");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Logger.d("输入的内容是：%s", newText);
                adapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_action_add_db:
                FileHelper.showFileChooser(this, REQUEST_CODE_LOAD_DB);
                break;
            case R.id.id_action_export_data:
                isInSelectMode = true;
                adapter.CheckBox_Movein();
                btnAdd.setVisibility(View.VISIBLE);
                btnAdd.setImageResource(R.drawable.ic_action_accept);
                break;
            case R.id.id_action_setting:
                ToastHelper.showSnack(PrjSelectActivity.this, lv, "设置");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_LOAD_DB:
                if (data == null) return;
                Uri uriDigitalFile = data.getData();
                String path = uriDigitalFile.getPath();
                //如果没有选择文件直接返回
                if (path == null || path.length() == 0) {
                    return;
                }
                //判断是不是数据库文件
                File file = new File(path);
                if (!file.getName().endsWith(".db")) {
                    ToastHelper.show(this, "您选取文件格式有误!");
                    return;
                }
                SQLiteDatabase db = DBHelper.openDatabase(path);
                try {
                    Cursor cursor = db.rawQuery("SELECT * FROM " + Constant.TABLE_PROJECT_INFO, null);
                    if (cursor == null || !cursor.moveToFirst()) {
                        return;
                    }
                    String prjName = cursor.getString(cursor.getColumnIndex(DBConstant
                            .prjInfo_coloum_prjName));
                    String creationTime = cursor.getString(cursor.getColumnIndex(DBConstant
                            .prjInfo_coloum_creationTime));
                    if (DBHelper.isPrjExist(realm, prjName)) {
                        ToastHelper.show(PrjSelectActivity.this, "该工程已存在");
                    } else {
                        PrjItemRealmObject prjItemRealmObject = new PrjItemRealmObject(prjName, path, creationTime);
                        realm.beginTransaction();
                        PrjItemRealmObject prjItemRealmObject1 = realm.copyToRealm(prjItemRealmObject);
                        realm.commitTransaction();
                        //重新加载工程视图
                        adapter.notifyDataSetChanged();
                    }
                } catch (SQLException e) {
                    ToastHelper.show(this, "选择的数据库有误");
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 删除一个PrjItem的所有数据
     */
    public void deletePrj(PrjItemRealmObject prjItemRealmObject) {
        //首先要判断Preference中保存的是不是当前工程
        //如果是要删除Preference
        if (PreferenceHelper.getInstance(this).getLastEditPrjName(this).equals(prjItemRealmObject.getPrjName
                ())) {
            PreferenceHelper.getInstance(this).deleteLastEditPrjName(this);
        }
        //删除DB数据库
        File file = new File(prjItemRealmObject.getDbLocation());
        file.delete();

        realm.beginTransaction();
        prjItemRealmObject.removeFromRealm();
        realm.commitTransaction();
    }

    /**
     * 改变工程名
     *
     * @param newName
     */
    public void changeName(PrjItemRealmObject prjItemRealmObject, String newName) {
        //首先要判断Preference中保存的是不是当前工程
        //如果是要修改Preference
        if (PreferenceHelper.getInstance(this).getLastEditPrjName(this).equals(prjItemRealmObject.getPrjName())) {
            PreferenceHelper.getInstance(this).setLastEditPrjName(this, newName);
        }
        SQLiteDatabase db = SQLiteDatabase.openDatabase(prjItemRealmObject.getDbLocation(), null, SQLiteDatabase
                .OPEN_READWRITE);
        db.execSQL("update Projectinfo set prjName = '" + newName + "' WHERE prjName = '" + prjItemRealmObject
                .getPrjName() + "'");
        realm.beginTransaction();
        prjItemRealmObject.setPrjName(newName);
        realm.commitTransaction();
    }

    /**
     * 实现两次返回退出程序
     */
    @Override
    public void onBackPressed() {
        if (isInSelectMode) {
            btnAdd.setImageResource(R.drawable.fab_add);
            adapter.CheckBox_Moveout();
            isInSelectMode = false;
            adapter.cleanSelectList();
        } else if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
            //杀掉当前app的进程---释放地图的内存
            System.exit(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDbLocation();
    }
}
