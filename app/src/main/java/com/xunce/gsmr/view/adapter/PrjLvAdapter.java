package com.xunce.gsmr.view.adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.xunce.gsmr.R;
import com.xunce.gsmr.model.PrjItem;
import com.xunce.gsmr.util.DBHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * 工程的adapter
 * Created by ssthouse on 2015/7/18.
 */
public class PrjLvAdapter extends BaseAdapter implements Filterable {
    private PrjNameFilter prjNameFilter;
    private Context context;
    //下面两份表单记录同样的数据，prjItemList用于查询时展示，prjItemListdata用于记录原始数据;
    private List<PrjItem> prjItemList;
    private List<PrjItem> prjItemListdata;
    private List<PrjItem> selectList;
    //颜色Id表单
    private int[] colorIdList={
            R.color.blue_500,R.color.orange_500,R.color.red_500,R.color.cyan_500,R.color.brown_500,
            R.color.purple_500,R.color.lime_500,R.color.pink_500,R.color.green_500
    };
    private LayoutInflater inflater;
    private int anim = 0;
    private Animation MoveIn;
    private Animation MoveOut;
    private Animation MoveOutTv;

    public PrjLvAdapter(Context context, List<PrjItem> prjItemList) {
        this.context = context;
        this.prjItemList = prjItemList;
        this.prjItemListdata = prjItemList;

        inflater = LayoutInflater.from(context);
        MoveIn = AnimationUtils.loadAnimation(context, R.anim.translate_right);
        MoveOut = AnimationUtils.loadAnimation(context, R.anim.translate_left);
        MoveOutTv = AnimationUtils.loadAnimation(context, R.anim.translate_textview_left);
        selectList = new ArrayList<>();
        MoveIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                anim = 0;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        MoveOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                anim = 0;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public int getCount() {
        return prjItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return prjItemList.get(position);
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
            convertView = inflater.inflate(R.layout.view_lv_item_prj_select, null);
            //初始化ViewHoler
            viewHolder = new ViewHolder();
            viewHolder.iv = (ImageView) convertView.findViewById(R.id.id_img_prj_icon);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.id_tv_prj_name);
            viewHolder.cb = (CheckBox) convertView.findViewById(R.id.id_cb_prj_name);
            //set to tag
            convertView.setTag(viewHolder);
            //set  data
            setviewcolor(viewHolder.iv,colorIdList[position%9]);
            viewHolder.tv.setText(prjItemList.get(position).getPrjName());
            viewHolder.cb.setChecked(isinselectList(prjItemList.get(position)));
            viewHolder.date.setText(prjItemList.get(position).getCreationTime());
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            setviewcolor(viewHolder.iv,colorIdList[position%9]);
            viewHolder.tv.setText(prjItemList.get(position).getPrjName());
            viewHolder.cb.setChecked(isinselectList(prjItemList.get(position)));
            viewHolder.date.setText(prjItemList.get(position).getCreationTime());
            if (anim == 1) {
                viewHolder.cb.setVisibility(View.VISIBLE);
                viewHolder.iv.startAnimation(MoveIn);
                viewHolder.cb.startAnimation(MoveIn);
                viewHolder.tv.startAnimation(MoveIn);
            } else if (anim == 2) {
                viewHolder.cb.startAnimation(MoveOut);
                viewHolder.iv.startAnimation(MoveOutTv);
                viewHolder.cb.setVisibility(View.GONE);
                viewHolder.tv.startAnimation(MoveOutTv);
            }
        }
        return convertView;
    }

    private  void setviewcolor(ImageView v,int colorId){
        GradientDrawable bgshape = (GradientDrawable) v.getBackground();
        bgshape.setColor(context.getResources().getColor(colorId));
    }
    public void CheckBox_Movein() {
        anim = 1;
        notifyDataSetChanged();
    }

    public void CheckBox_Moveout() {
        anim = 2;
        notifyDataSetChanged();
    }

    private boolean isinselectList(PrjItem prjItem) {
        if (selectList == null || selectList.size() == 0) {
            return false;
        }
        if (selectList.contains(prjItem)) {
            return true;
        }
        return false;
    }

    class ViewHolder {
        CheckBox cb;
        ImageView iv;
        TextView tv;
        TextView date;
    }

    @Override
    public void notifyDataSetChanged() {
        //刷新数据
        prjItemList = DBHelper.getPrjItemList(Realm.getInstance(context));
        prjItemListdata = prjItemList;
        //刷新数据库
        super.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        if (prjNameFilter == null)
            prjNameFilter = new PrjNameFilter();
        return prjNameFilter;
    }


    private class PrjNameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<PrjItem> mFilteredArrayList = new ArrayList<>();
            if (TextUtils.isEmpty(constraint)) {
                filterResults.values = prjItemListdata;
                return filterResults;
            }
            for (PrjItem prjItem : prjItemListdata) {
                if (prjItem.getPrjName().contains(constraint))
                    mFilteredArrayList.add(prjItem);
            }
            filterResults.values = mFilteredArrayList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            prjItemList = (List<PrjItem>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }


    }
    //getter----------and-----------setter

    public List<PrjItem> getPrjItemList() {
        return prjItemList;
    }

    public void setPrjItemList(List<PrjItem> prjItemList) {
        this.prjItemList = prjItemList;
    }

    public List<PrjItem> getSelectList() {
        return selectList;
    }

    public void cleanSelectList() {
        selectList.clear();
    }

    public void setSelectList(List<PrjItem> selectList) {
        this.selectList = selectList;
    }


}
