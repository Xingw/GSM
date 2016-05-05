package com.xunce.gsmr.model;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Xingw on 2016/5/5.
 */
public class SearchItem extends RealmObject {
    String value;

    public SearchItem(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
