package com.gionee.oss.biz.model;

import com.gionee.gnif.model.base.BusinessObject;

public class OssTripartiteSystem extends BusinessObject {
    private String name;
    private String url;
    private String code;
    private String key;
    private String systemRootpath;
    private String oldCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSystemRootpath() {
        return systemRootpath;
    }

    public void setSystemRootpath(String systemRootpath) {
        this.systemRootpath = systemRootpath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOldCode() {
        return oldCode;
    }

    public void setOldCode(String oldCode) {
        this.oldCode = oldCode;
    }
}
