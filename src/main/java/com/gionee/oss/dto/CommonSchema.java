package com.gionee.oss.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by yeqy on 2017/6/15.
 */
public class CommonSchema extends Schema {

    private String policy;

    public String getCode() {
        return super.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        try {
            this.signature = URLDecoder.decode(signature, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        try {
            this.policy = URLDecoder.decode(policy, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}
