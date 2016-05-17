package com.xunce.gsmr.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.xunce.gsmr.R;

import java.util.List;

/**
 * Created by Xingw on 2016/5/17.
 */
public class LayerSelectAdapter extends BaseAdapter {
    private List<String> layerList;
    private boolean[] layerboolean;
    private Context context;

    public LayerSelectAdapter(List<String> layerList, boolean[] layerboolean,Context context) {

        this.layerList = layerList;
        this.layerboolean = layerboolean;
        this.context = context;
    }

    @Override
    public int getCount() {
        return layerList.size();
    }

    @Override
    public String getItem(int position) {
        return layerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            //初始话ConvertView
            convertView = LayoutInflater.from(context).inflate(R.layout.view_lv_item_layer_select, null);
            //初始化ViewHoler
            viewHolder = new ViewHolder();
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_dialog_layer_select);
            viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb_dialog_layer_select);
            //set to tag
            convertView.setTag(viewHolder);
            //set  data
            viewHolder.tv.setText(layerList.get(position));
            viewHolder.cb.setChecked(layerboolean[position]);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.tv.setText(layerList.get(position));
            viewHolder.cb.setChecked(layerboolean[position]);
        }
        return convertView;
    }

    public List<String> getLayerList() {
        return layerList;
    }

    public boolean[] getLayerboolean() {
        return layerboolean;
    }

    private class ViewHolder {
        TextView tv;
        CheckBox cb;
    }
}
