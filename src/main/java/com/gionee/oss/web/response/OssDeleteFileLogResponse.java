package com.gionee.oss.web.response;


import com.gionee.oss.biz.model.OssDeleteFileLog;

import java.util.Date;


public class OssDeleteFileLogResponse {

    private Integer id;
    private Integer fileId;
    private String fileName;
    private Integer fileSize;
    private String filePath;
    private String fileMd5;
    private Date deleteTime;
    private String systemCode;
    private Integer fileRefer;
    private Integer status;
    private String remark;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;

    public OssDeleteFileLogResponse(OssDeleteFileLog ossDeleteFileLog) {
        setId(ossDeleteFileLog.getId());
        setFileName(ossDeleteFileLog.getFileName());
        setFileSize(ossDeleteFileLog.getFileSize());
        setFilePath(ossDeleteFileLog.getFilePath());
        setFileMd5(ossDeleteFileLog.getFileMd5());
        setDeleteTime(ossDeleteFileLog.getDeleteTime());
        setStatus(ossDeleteFileLog.getStatus());
        setRemark(ossDeleteFileLog.getRemark());
        setCreateBy(ossDeleteFileLog.getCreateBy());
        setCreateTime(ossDeleteFileLog.getCreateTime());
        setUpdateBy(ossDeleteFileLog.getUpdateBy());
        setUpdateTime(ossDeleteFileLog.getUpdateTime());
        setFileRefer(ossDeleteFileLog.getFileRefer());
        setSystemCode(ossDeleteFileLog.getSystemCode());
        setFileId(ossDeleteFileLog.getFileId());
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public Integer getFileRefer() {
        return fileRefer;
    }

    public void setFileRefer(Integer fileRefer) {
        this.fileRefer = fileRefer;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
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
