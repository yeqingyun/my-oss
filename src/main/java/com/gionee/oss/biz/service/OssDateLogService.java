package com.gionee.oss.biz.service;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.oss.biz.model.OssDateLog;

import java.util.Date;
import java.util.List;

public interface OssDateLogService {

    void save(OssDateLog ossDateLog);

    OssDateLog getOssDateLog(Date date);

    void delete(Integer id);

    List<OssDateLog> query(QueryMap critera);

    PageResult<OssDateLog> queryPage(QueryMap critera);

    List<OssDateLog> getOssDateLogExcept(Date date);

}
