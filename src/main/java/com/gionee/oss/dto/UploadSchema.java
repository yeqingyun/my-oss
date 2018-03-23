package com.gionee.oss.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by yeqy on 2017/5/27.
 */
public class UploadSchema extends Schema {
    private String fileInfo;
    private int step;
    private Long chunkSize;
    private int chunkOrder;
    private String chunkMd5;
    private String fileMd5;
    private String fileName;
    private Long fileSize;
    private String savePath;
    private String call;
    private String chunk;
    private String f;
    private int tmp = 0;


    public UploadSchema(String code, String signature, String fileInfo, int step) {
        this.signature = signature;
        this.code = code;
        this.step = step;
        this.fileInfo = fileInfo;
    }

    public UploadSchema(String code, String signature, String fileInfo, int step, Long chunkSize, int chunkOrder, String fileMd5) {
        this.code = code;
        this.signature = signature;
        this.fileInfo = fileInfo;
        this.step = step;
        this.chunkSize = chunkSize;
        this.chunkOrder = chunkOrder;
        this.fileMd5 = fileMd5;
    }


    public UploadSchema() {
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        try {
            this.signature = URLDecoder.decode(URLDecoder.decode(signature, "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(String fileInfo) {
        try {
            this.fileInfo = URLDecoder.decode(URLDecoder.decode(fileInfo, "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkOrder() {
        return chunkOrder;
    }

    public void setChunkOrder(int chunkOrder) {
        this.chunkOrder = chunkOrder;
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

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getChunkMd5() {
        return chunkMd5;
    }

    public void setChunkMd5(String chunkMd5) {
        this.chunkMd5 = chunkMd5;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String chunk) {
        this.chunk = chunk;
    }

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        try {
            this.call = URLDecoder.decode(call, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Integer getTmp() {
        return tmp;
    }

    public void setTmp(Integer tmp) {
        this.tmp = tmp;
    }

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }
}
