package com.gionee.oss.biz.model;

import com.gionee.gnif.model.base.BusinessObject;

public class OssDownloadUrl extends BusinessObject {

    private String policy;
    private String code;
    private Integer count;
    private Integer expire;
    private Integer countLess;

    public OssDownloadUrl(String policy, String code, Integer count, Integer expire, Integer countLess) {
        this.policy = policy;
        this.code = code;
        this.count = count;
        this.expire = expire;
        this.countLess = countLess;
    }

    public OssDownloadUrl() {
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }

    public Integer getCountLess() {
        return countLess;
    }

    public void setCountLess(Integer countLess) {
        this.countLess = countLess;
    }


}
