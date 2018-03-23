package com.gionee.oss.web.response;

import com.gionee.oss.biz.model.OssTripartiteSystem;

import java.util.Date;


public class OssTripartiteSystemResponse {

    private String code;
    private String key;
    private String systemRootpath;
    private String name;
    private String url;
    private Integer status;
    private String remark;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;

    public OssTripartiteSystemResponse(OssTripartiteSystem ossTripartiteSystem) {
        setCode(ossTripartiteSystem.getCode());
        setKey(ossTripartiteSystem.getKey());
        setSystemRootpath(ossTripartiteSystem.getSystemRootpath());
        setStatus(ossTripartiteSystem.getStatus());
        setRemark(ossTripartiteSystem.getRemark());
        setCreateBy(ossTripartiteSystem.getCreateBy());
        setCreateTime(ossTripartiteSystem.getCreateTime());
        setUpdateBy(ossTripartiteSystem.getUpdateBy());
        setUpdateTime(ossTripartiteSystem.getUpdateTime());
        setName(ossTripartiteSystem.getName());
        setUrl(ossTripartiteSystem.getUrl());
    }

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
}
