package com.gionee.oss.constant;

/**
 * Created by yeqy on 2017/6/7.
 */
public enum DownLoadCountLess {
    LIMIT, COUNT_LESS;

    public int getValue() {
        return this == COUNT_LESS ? 1 : 0;
    }
}
