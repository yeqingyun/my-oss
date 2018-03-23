package com.gionee.oss.biz.model;

import com.gionee.gnif.model.base.BusinessObject;

import java.util.Date;


public class OssDeleteFileLog extends BusinessObject {
    private Integer fileId;
    private String fileName;
    private String systemCode;
    private Integer fileSize;
    private Integer fileRefer;
    private String filePath;
    private String fileMd5;
    private Date deleteTime;
    private Integer fileTmp;

    public OssDeleteFileLog() {
    }

    public OssDeleteFileLog(OssFile ossFile) {
        this.fileName = ossFile.getName();
        this.fileSize = ossFile.getSize();
        this.filePath = ossFile.getPath();
        this.fileMd5 = ossFile.getMd5();
        this.systemCode = ossFile.getSystemCode();
        this.fileRefer = ossFile.getRefer();
        this.fileId = ossFile.getId();
        this.fileTmp = ossFile.getTmp();
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

    public Integer getFileTmp() {
        return fileTmp;
    }

    public void setFileTmp(Integer fileTmp) {
        this.fileTmp = fileTmp;
    }
}
