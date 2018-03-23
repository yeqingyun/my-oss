package com.gionee.oss.dto;

/**
 * Created by yeqy on 2017/6/30.
 */
public abstract class Schema {

    protected String code;
    protected String signature;
    protected String callback;

    public String getCode() {
        return code;
    }

    public abstract void setCode(String code);

    public String getSignature() {
        return signature;
    }

    public abstract void setSignature(String signature);

    public String getCallback() {
        return callback;
    }

    public abstract void setCallback(String callback);
}
