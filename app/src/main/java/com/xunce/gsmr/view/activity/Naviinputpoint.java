package com.xunce.gsmr.view.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.amap.api.maps.model.LatLng;
import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.kilometerMark.KilometerMark;
import com.xunce.gsmr.kilometerMark.KilometerMarkHolder;
import com.xunce.gsmr.model.SearchItem;
import com.xunce.gsmr.model.event.KilomarkerHolderPostEvent;
import com.xunce.gsmr.model.event.NaviInputEvent;
import com.xunce.gsmr.util.DBHelper;
import com.xunce.gsmr.util.view.ToastHelper;
import com.xunce.gsmr.view.activity.gaode.GaodeMarkerActivity;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.Realm;

/**
 * Created by Xingw on 2016/5/5.
 */
public class Naviinputpoint extends AppCompatActivity implements View.OnClickListener {
    private Realm realm;
    private EditText input;
    private KilometerMarkHolder kilometerMarkHolder;
    private SearchItem currentSearchItem;
    private List<SearchItem> historysearchItems;
    private ArrayAdapter<String> adapter;

    public static void start(Activity activity, String input, boolean inputstyle, boolean mapstyle) {
        Intent intent = new Intent(activity, Naviinputpoint.class);
        intent.putExtra(Constant.EXTRA_KEY_INPUT, input);
        intent.putExtra(Constant.EXTRA_KEY_NAVI_STYLE, inputstyle);
        intent.putExtra(Constant.EXTRA_KEY_MAP_STYLE, mapstyle);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_navi_find);
        realm = Realm.getInstance(this);
        initview();
    }

    private void initview() {
        final List<String> historys = getSearchItemList();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                historys);
        ListView listView = (ListView) findViewById(R.id.lv_navi_history);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentSearchItem = new SearchItem("我的位置", "我的位置");
                } else {
                    currentSearchItem = historysearchItems.get(position - 1);
                }
                //判断是起点还是重点的输入
                if (getIntent().getBooleanExtra(Constant.EXTRA_KEY_NAVI_STYLE, NaviInputEvent
                        .START)) {
                    EventBus.getDefault().post(new NaviInputEvent(currentSearchItem, NaviInputEvent
                            .START));
                } else {
                    EventBus.getDefault().post(new NaviInputEvent(currentSearchItem, NaviInputEvent
                            .END));
                }
                finish();
            }
        });
        input = (EditText) findViewById(R.id.et_navi_input);
        input.setText(getIntent().getStringExtra(Constant.EXTRA_KEY_INPUT));
        findViewById(R.id.btn_navi_back).setOnClickListener(this);
        findViewById(R.id.btn_navi_sure).setOnClickListener(this);
        findViewById(R.id.btn_navi_find_from_map).setOnClickListener(this);
        findViewById(R.id.btn_navi_find_from_marker).setOnClickListener(this);
        findViewById(R.id.btn_navi_delete).setOnClickListener(this);
    }

    /**
     * 保存本次搜索记录
     */
    private void saveSearchHistory() {
        if (currentSearchItem.getText() == "我的位置" )return;
        realm.beginTransaction();
        realm.copyToRealm(currentSearchItem);
        realm.commitTransaction();
    }

    /**
     * 删除所有历史记录
     */
    private void deleteHistory() {
        List<SearchItem> removelist = realm.where(SearchItem.class).findAll();
        realm.beginTransaction();
        for (int i = 0; i < removelist.size(); i++) {
            removelist.get(i).removeFromRealm();
        }
        realm.commitTransaction();
        adapter.clear();
        adapter.add("我的位置");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_navi_sure:
                //如果不相同，说明INPUT的内容被改变了，按照改变后的值传值
                if (currentSearchItem == null || !input.getText().toString().equals(currentSearchItem.getText())) {
                    currentSearchItem = new SearchItem(input.getText().toString(), input.getText()
                            .toString());
                }
                sendcurrentSearchItem();
                break;
            case R.id.btn_navi_back:
                finish();
                break;
            case R.id.btn_navi_find_from_map:
                if (getIntent().getBooleanExtra(Constant.EXTRA_KEY_MAP_STYLE,true)){
                    GaodeMarkerActivity.start(this,null,null,new LatLng(0,0),12,Constant
                            .REQUEST_CODE_NAVI_MAP);
                }
                break;
            case R.id.btn_navi_delete:
                deleteHistory();
                break;
            case R.id.btn_navi_find_from_marker:
                showChoiceMarkerDialog();
                break;
        }
    }

    /**
     * 公里标选点对话框
     */
    private void showChoiceMarkerDialog() {
        if (kilometerMarkHolder == null) {
            ToastHelper.show(this,"没有可以选择的公里标");
            return;
        }
        String[] list = new String[kilometerMarkHolder.getKilometerMarkList().size()];
        for (int i = 0; i < kilometerMarkHolder.getKilometerMarkList().size(); i++) {
            list[i] = kilometerMarkHolder.getKilometerMarkList().get(i).getText();
        }
        new AlertDialog.Builder(this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        KilometerMark kilometerMark = kilometerMarkHolder.getKilometerMarkList().get(which);
                        SearchItem searchItem = new SearchItem("公里标选点" + " -- "+ kilometerMark.getText(),
                                kilometerMark.getLatitude() + "," + kilometerMark.getLongitude());
                        setInputText(searchItem);
                        sendcurrentSearchItem();
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
     * 设置历史记录
     *
     * @return
     */
    public List<String> getSearchItemList() {
        historysearchItems = realm.where(SearchItem.class).findAll();
        List<String> list = new ArrayList<>();
        list.add("我的位置");
        if (historysearchItems == null || historysearchItems.size() == 0) return list;
        for (SearchItem searchItem : historysearchItems) {
                list.add(searchItem.getText());
        }
        return list;
    }

    /**
     * 将currentSearchItem的值返回
     */
    private void sendcurrentSearchItem(){
        //判断是起点还是重点的输入
        if (getIntent().getBooleanExtra(Constant.EXTRA_KEY_NAVI_STYLE, NaviInputEvent
                .START)) {
            EventBus.getDefault().post(new NaviInputEvent(currentSearchItem, NaviInputEvent
                    .START));
        } else {
            EventBus.getDefault().post(new NaviInputEvent(currentSearchItem, NaviInputEvent
                    .END));
        }
        saveSearchHistory();
        finish();
    }
    /**
     * 获取kilomarkerHolder
     *
     * @param kilomarkerHolderPostEvent
     */
    public void onEventMainThread(KilomarkerHolderPostEvent kilomarkerHolderPostEvent) {
        //加载数据
        kilometerMarkHolder = kilomarkerHolderPostEvent.getKilometerMarkHolder();
    }

    /**
     * 设置输入框的内容
     *
     * @param searchItem
     */
    private void setInputText(SearchItem searchItem) {
        input.setText(searchItem.getText());
        currentSearchItem = searchItem;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case Constant.RESULT_CODE_NAVI:
                Bundle b=data.getExtras(); //data为B中回传的Intent
                double latitude=b.getDouble(Constant.EXTRA_KEY_LATITUDE);//str即为回传的值
                double longitude = b.getDouble(Constant.EXTRA_KEY_LONGITUDE);
                currentSearchItem = new SearchItem("地图选点 -- "+latitude + "," + longitude,latitude + "," + longitude);
                saveSearchHistory();
                sendcurrentSearchItem();
                break;
            default:
                break;
        }
    }
}
