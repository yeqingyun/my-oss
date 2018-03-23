package com.gionee.oss.constant;

/**
 * Created by yeqy on 2017/6/7.
 */
public enum DownLoadType {
    ATTACHMENT(0),
    OTHER(1);

    private int type;

    DownLoadType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
