package com.gionee.oss.biz.model;

import com.gionee.gnif.model.base.BusinessObject;

public class OssFile extends BusinessObject {

    private String systemCode;
    private String name;
    private String md5;
    private Integer size;
    private String path;
    private Integer tmp;
    private Integer refer;

    public OssFile(String systemCode, String name, String md5, Integer size, String path, Integer tmp, Integer refer) {
        this.systemCode = systemCode;
        this.name = name;
        this.md5 = md5;
        this.size = size;
        this.path = path;
        this.tmp = tmp;
        this.refer = refer;
    }

    public OssFile(Integer id, String systemCode, String name, String md5, Integer size, String path, Integer tmp, Integer refer) {
        this.setId(id);
        this.systemCode = systemCode;
        this.name = name;
        this.md5 = md5;
        this.size = size;
        this.path = path;
        this.tmp = tmp;
        this.refer = refer;
    }

    public OssFile() {
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getTmp() {
        return tmp;
    }

    public void setTmp(Integer tmp) {
        this.tmp = tmp;
    }

    public Integer getRefer() {
        return refer;
    }

    public void setRefer(Integer refer) {
        this.refer = refer;
    }
}
