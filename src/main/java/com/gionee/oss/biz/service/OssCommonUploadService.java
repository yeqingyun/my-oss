package com.gionee.oss.biz.service;

import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.dto.UploadSchema;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Created by yeqy on 2017/6/7.
 */
public interface OssCommonUploadService {
    Message upload(UploadSchema schema, Message message) throws UnsupportedEncodingException, ParseException;
}
