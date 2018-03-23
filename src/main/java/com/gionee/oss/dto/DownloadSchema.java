package com.gionee.oss.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by yeqy on 2017/5/27.
 */
public class DownloadSchema extends Schema {
    private String policy;
    private int download;


    public DownloadSchema() {
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

    @Override
    public void setCallback(String callback) {
        this.callback = callback;
    }

    public int getDownload() {
        return download;
    }

    public void setDownload(int download) {
        this.download = download;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
}
