package com.gionee.oss.biz.service;

import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.dto.CommonSchema;

import java.io.UnsupportedEncodingException;

/**
 * Created by yeqy on 2017/6/15.
 */
public interface OssCommonService {
    Message fileDelete(CommonSchema schema) throws UnsupportedEncodingException;

    Message changeTmpFile(CommonSchema schema) throws UnsupportedEncodingException;

    Message adminFileDelete(Integer id);
}
