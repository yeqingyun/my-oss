package com.gionee.oss.biz.service;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.gnif.model.PageResult;
import com.gionee.oss.biz.model.OssDownloadUrl;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface OssDownloadUrlService {

    void save(OssDownloadUrl ossDownloadUrl);

    OssDownloadUrl getOssDownloadUrl(Integer id);

    void delete(Integer id);

    List<OssDownloadUrl> query(QueryMap critera);

    PageResult<OssDownloadUrl> queryPage(QueryMap critera);

    OssDownloadUrl getByCodeAndPolicy(String policy, String code);

    Message generateFileLink(Integer id, Long overTime, Long downloadCount, Integer downloadType, HttpServletRequest request) throws UnsupportedEncodingException;
}
