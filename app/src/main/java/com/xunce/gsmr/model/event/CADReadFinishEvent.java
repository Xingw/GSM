package com.xunce.gsmr.model.event;

import com.xunce.gsmr.model.gaodemap.graph.Line;
import com.xunce.gsmr.model.gaodemap.graph.Text;
import com.xunce.gsmr.model.gaodemap.graph.Vector;

import java.util.List;

/**
 * Created by Xingw on 2016/3/16.
 */
public class CADReadFinishEvent {
    private final List<Text> textList;
    private final List<Line> lineList;
    private final List<Vector> vectorList;

    public CADReadFinishEvent(List<Text> textList, List<Line> lineList, List<Vector> vectorList) {
        this.textList = textList;
        this.lineList = lineList;
        this.vectorList = vectorList;
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public List<Text> getTextList() {
        return textList;
    }

    public List<Vector> getVectorList() {
        return vectorList;
    }
}
