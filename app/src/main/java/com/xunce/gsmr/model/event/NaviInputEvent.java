package com.xunce.gsmr.model.event;

import com.xunce.gsmr.model.SearchItem;

/**
 * Created by Xingw on 2016/5/6.
 */
public class NaviInputEvent{
    public static boolean START = true;
    public static boolean END = false;

    private boolean style;
    private SearchItem searchItem;

    public NaviInputEvent(SearchItem searchItem, boolean style) {
        this.searchItem = searchItem;
        this.style = style;
    }

    public SearchItem getSearchItem() {
        return searchItem;
    }

    public void setSearchItem(SearchItem searchItem) {
        this.searchItem = searchItem;
    }

    public boolean getStyle() {
        return style;
    }

    public void setStyle(boolean style) {
        this.style = style;
    }
}
