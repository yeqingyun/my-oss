package com.gionee.oss.biz.service;

import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.dto.DownloadSchema;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yeqy on 2017/6/6.
 */
public interface OssDownloadService {
    Message download(DownloadSchema schema, HttpServletResponse response, HttpServletRequest request) throws Exception;
}
