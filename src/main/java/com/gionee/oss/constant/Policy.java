package com.gionee.oss.constant;

/**
 * Created by yeqy on 2017/5/27.
 */
public class Policy {

    private Long count;
    private Long fileNo;
    private Long expire;
    private String identify;


    public Policy(Long count, Long fileNo, Long expire) {
        this.count = count;
        this.fileNo = fileNo;
        this.expire = expire;
    }

    public Policy(Long count, Long fileNo, Long expire, String identify) {
        this.count = count;
        this.fileNo = fileNo;
        this.expire = expire;
        this.identify = identify;
    }

    public Policy() {
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getFileNo() {
        return fileNo;
    }

    public void setFileNo(Long fileNo) {
        this.fileNo = fileNo;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public String getIdentify() {
        return identify;
    }

    public void setIdentify(String identify) {
        this.identify = identify;
    }
}
