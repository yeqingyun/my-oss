package com.gionee.oss.web.response;

import com.gionee.oss.biz.model.OssFile;

import java.util.Date;


public class OssFileResponse {

    private Integer id;
    private String systemCode;
    private String name;
    private String md5;
    private Integer size;
    private String path;
    private Integer tmp;
    private Integer status;
    private String remark;
    private Integer refer;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;

    public OssFileResponse(OssFile ossFile) {
        setId(ossFile.getId());
        setSystemCode(ossFile.getSystemCode());
        setName(ossFile.getName());
        setMd5(ossFile.getMd5());
        setSize(ossFile.getSize());
        setPath(ossFile.getPath());
        setTmp(ossFile.getTmp());
        setStatus(ossFile.getStatus());
        setRemark(ossFile.getRemark());
        setCreateBy(ossFile.getCreateBy());
        setCreateTime(ossFile.getCreateTime());
        setUpdateBy(ossFile.getUpdateBy());
        setUpdateTime(ossFile.getUpdateTime());
        setRefer(ossFile.getRefer());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getRefer() {
        return refer;
    }

    public void setRefer(Integer refer) {
        this.refer = refer;
    }
}
