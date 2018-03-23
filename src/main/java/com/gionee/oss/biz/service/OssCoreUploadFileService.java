package com.gionee.oss.biz.service;

import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.dto.AdminUploadSchema;
import com.gionee.oss.dto.UploadSchema;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Created by yeqy on 2017/6/5.
 */
public interface OssCoreUploadFileService {
    Message fileExist(UploadSchema schema) throws UnsupportedEncodingException;

    Message chunkFileExist(UploadSchema schema);

    Message chunkFileUpload(UploadSchema schema, HttpServletRequest request) throws Exception;

    Message mergeChunkFile(UploadSchema schema, Message message) throws UnsupportedEncodingException, ParseException;

    //管理员上传接口
    Message fileExistWithOutAuth(AdminUploadSchema schema);

    Message adminUploadChunkFileExist(AdminUploadSchema schema);

    Message adminUploadChunkFileUpload(AdminUploadSchema schema, HttpServletRequest request);

    Message adminUploadFile(AdminUploadSchema schema, Message message);
}
