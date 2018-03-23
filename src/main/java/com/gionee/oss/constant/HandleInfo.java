package com.gionee.oss.constant;

/**
 * Created by yeqy on 2017/7/24.
 */
public class HandleInfo {
    private HandleType handleType;
    private int value;

    public HandleInfo(HandleType handleType, int value) {
        this.handleType = handleType;
        this.value = value;
    }

    public HandleInfo(HandleType handleType) {
        this.handleType = handleType;
        this.value = 1;
    }

    public HandleType getHandleType() {
        return handleType;
    }

    public void setHandleType(HandleType handleType) {
        this.handleType = handleType;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
