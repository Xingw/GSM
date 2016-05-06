package com.xunce.gsmr.model.event;

import com.xunce.gsmr.kilometerMark.KilometerMarkHolder;

/**
 * Created by Xingw on 2016/5/6.
 */
public class KilomarkerHolderPostEvent {
    KilometerMarkHolder kilometerMarkHolder;

    public KilomarkerHolderPostEvent(KilometerMarkHolder kilometerMarkHolder) {
        this.kilometerMarkHolder = kilometerMarkHolder;
    }

    public KilometerMarkHolder getKilometerMarkHolder() {
        return kilometerMarkHolder;
    }

    public void setKilometerMarkHolder(KilometerMarkHolder kilometerMarkHolder) {
        this.kilometerMarkHolder = kilometerMarkHolder;
    }
}
