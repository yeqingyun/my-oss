package com.gionee.oss.constant;

import com.gionee.oss.util.StringUtil;

import java.util.Date;

/**
 * Created by yeqy on 2017/5/27.
 */
public class FileInfo {
    private Date date;
    private String fileMd5;
    private String fileName;
    private Integer fileSize;
    private String chunkMd5;
    private Integer chunkSize;


    public FileInfo(Date date, String fileMd5, String fileName, Integer fileSize, String chunkMd5, Integer chunkSize) {
        this.date = date;
        this.fileMd5 = fileMd5;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.chunkMd5 = chunkMd5;
        this.chunkSize = chunkSize;
    }

    public FileInfo(Date date, String fileMd5, String fileName, Integer fileSize) {
        this.date = date;
        this.fileMd5 = fileMd5;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
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

    public String getChunkMd5() {
        return chunkMd5;
    }

    public void setChunkMd5(String chunkMd5) {
        this.chunkMd5 = chunkMd5;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSafe() {
        return !StringUtil.hasEmpty(this.fileMd5, this.fileName) && this.fileSize > 0;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }
}
