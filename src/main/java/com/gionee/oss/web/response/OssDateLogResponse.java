package com.gionee.oss.web.response;

import com.gionee.oss.biz.model.OssDateLog;

import java.util.Date;

/**
 * Created by yeqy on 2017/7/25.
 */
public class OssDateLogResponse {
    private int download;
    private int upload;
    private int uploadTmp;
    private int delete;
    private int clear;
    private Date date;
    private Integer status;
    private String remark;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;

    public OssDateLogResponse(OssDateLog ossDateLog) {
        this.download = ossDateLog.getDownload();
        this.upload = ossDateLog.getUpload();
        this.uploadTmp = ossDateLog.getUploadTmp();
        this.delete = ossDateLog.getDelete();
        this.clear = ossDateLog.getClear();
        this.date = ossDateLog.getDate();
        this.status = ossDateLog.getStatus();
        this.remark = ossDateLog.getRemark();
        this.createBy = ossDateLog.getCreateBy();
        this.createTime = ossDateLog.getCreateTime();
        this.updateBy = ossDateLog.getUpdateBy();
        this.updateTime = ossDateLog.getUpdateTime();
    }

    public OssDateLogResponse(int download, int upload, int uploadTmp, int delete, int clear, Date date, Integer status, String remark, String createBy, Date createTime, String updateBy, Date updateTime) {
        this.download = download;
        this.upload = upload;
        this.uploadTmp = uploadTmp;
        this.delete = delete;
        this.clear = clear;
        this.date = date;
        this.status = status;
        this.remark = remark;
        this.createBy = createBy;
        this.createTime = createTime;
        this.updateBy = updateBy;
        this.updateTime = updateTime;
    }

    public OssDateLogResponse() {
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
