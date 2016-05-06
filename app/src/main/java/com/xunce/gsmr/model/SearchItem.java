package com.xunce.gsmr.model;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Xingw on 2016/5/5.
 */
public class SearchItem extends RealmObject {
    private String text;
    private String value;

    public SearchItem() {
        super();
    }

    public SearchItem(String text, String value) {
        super();
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
