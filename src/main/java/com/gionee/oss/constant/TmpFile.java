package com.gionee.oss.constant;

/**
 * Created by yeqy on 2017/6/29.
 */
public enum TmpFile {
    TMP_FILE(1),
    UNTMP_FILE(0);

    private int type;

    TmpFile(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
