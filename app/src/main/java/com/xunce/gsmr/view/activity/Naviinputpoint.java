package com.xunce.gsmr.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.xunce.gsmr.R;
import com.xunce.gsmr.app.Constant;
import com.xunce.gsmr.model.SearchItem;
import com.xunce.gsmr.util.DBHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by Xingw on 2016/5/5.
 */
public class Naviinputpoint extends AppCompatActivity implements View.OnClickListener{
    private Realm realm;
    private EditText input;

    public static void start(Activity activity, String input, int requestCode) {
        Intent intent = new Intent(activity, Naviinputpoint.class);
        intent.putExtra(Constant.EXTRA_KEY_INPUT, input);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi_find);
        realm = Realm.getInstance(this);
        initview();
    }

    private void initview() {
        final List<String> historys =getSearchItemList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                historys);
        ListView listView = (ListView) findViewById(R.id.lv_navi_history);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(Constant.RESULT_CODE_NAVI_VALUE,historys.get(position));
                setResult(Constant.RESULT_CODE_NAVI,intent);
                finish();
            }
        });
        input = (EditText)findViewById(R.id.et_navi_input);
        input.setText(getIntent().getStringExtra(Constant.EXTRA_KEY_INPUT));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_navi_sure:
                Intent intent = new Intent();
                intent.putExtra(Constant.RESULT_CODE_NAVI_VALUE,input.getText().toString());
                setResult(Constant.RESULT_CODE_NAVI,intent);
                finish();
                break;
            case R.id.btn_navi_back:
                finish();
                break;
            case R.id.btn_navi_find_from_map:

                break;
            case R.id.btn_navi_find_from_marker:

                break;
        }
    }


    public List<String> getSearchItemList() {
        List<SearchItem> searchItems = realm.where(SearchItem.class).findAll();
        List<String> list = new ArrayList<>();
        list.add("我的位置");
        if (searchItems == null || searchItems.size()==0)return list;
        for (SearchItem searchItem : searchItems) {
            list.add(searchItem.toString());
        }
        return list;
    }
}
