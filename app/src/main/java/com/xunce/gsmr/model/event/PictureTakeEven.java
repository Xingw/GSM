package com.xunce.gsmr.model.event;

import android.net.Uri;

/**
 * Created by Xingw on 2016/3/10.
 */
public class PictureTakeEven {

    private Uri uri;

    public PictureTakeEven(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
