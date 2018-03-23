package com.gionee.oss.constant;

/**
 * Created by yeqy on 2017/6/5.
 */
public enum Info {
    EINVAL("传递参数有误"),
    PERSMISSION_DENIED("系统编码错误"),
    OVERTIME("链接超时"),
    FILE_REPEAT("文件重复"),
    FILE_EMPTY("文件为空"),
    TAMPER_INFO("篡改信息"),
    FILE_ERROR("上传文件错误"),
    MERGE_FILE_ERROR("合并文件时发生错误"),
    SAVEPATH_NOTDIR("存放位置不是目录"),
    FILE_NOTEXIST("文件不存在或已被删除"),
    FILE_MORETHANFIFTY("文件超过50M，请使用大文件上传接口"),
    DOWNLOAD_COUNTERROR("下载次数错误"),
    DOWNLOAD_COUNTOVER("下载次数用尽"),
    DOWNLOAD_OVERTIME("下载链接已过期"),
    SYSTEM_ERROR("系统错误"),
    CALLBACK_ERROR("回调异常"),
    DELETE_SUCCESS("删除成功"),
    DELETE_FAIL("删除失败"),
    SAVE_FAIL("保存失败"),
    UPLOAD_SUCCESS("上传成功"),
    SYSTEM_CODE_EXIT("系统编码已存在"),
    SYSTEM_NOTEXIT("系统不存在"),
    SYSTEM_CODE_NAME_CANNOTNULL("系统编码，名称不能为空"),
    SYSTEM_PATH_NOTDIR("存放路径不是目录,创建失败"),
    SYSTEM_CREATE_FAILED("目录创建失败"),
    SYSTEM_PATH_EXITNOTNULL("存放路径已存在且不为空"),
    SYSTEM_KEY_ERROR("秘钥不能为空且长度必须为16~32位");

    private String info;

    Info(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }
}
