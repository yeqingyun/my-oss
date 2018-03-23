package com.gionee.oss.biz.model;

import com.gionee.gnif.model.base.BusinessObject;

import java.util.Date;


public class OssDateLog extends BusinessObject {

    private int download;
    private int upload;
    private int uploadTmp;
    private int delete;
    private int clear;
    private Date date;

    public OssDateLog(int download, int upload, int uploadTmp, int delete, int clear, Date date) {
        this.download = download;
        this.upload = upload;
        this.uploadTmp = uploadTmp;
        this.delete = delete;
        this.clear = clear;
        this.date = date;
    }

    public OssDateLog() {
    }

    public int getDownload() {
        return download;
    }

    public void setDownload(int download) {
        this.download = download;
    }

    public int getUpload() {
        return upload;
    }

    public void setUpload(int upload) {
        this.upload = upload;
    }

    public int getUploadTmp() {
        return uploadTmp;
    }

    public void setUploadTmp(int uploadTmp) {
        this.uploadTmp = uploadTmp;
    }

    public int getDelete() {
        return delete;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }

    public int getClear() {
        return clear;
    }

    public void setClear(int clear) {
        this.clear = clear;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
