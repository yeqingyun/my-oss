package com.gionee.oss.biz.service;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.oss.biz.model.OssDeleteFileLog;

import java.util.List;

public interface OssDeleteFileLogService {

    OssDeleteFileLog getOssDeleteFileLog(Integer id);

    void save(OssDeleteFileLog ossDeleteFileLog);

    void delete(Integer id);

    List<OssDeleteFileLog> query(QueryMap critera);

    PageResult<OssDeleteFileLog> queryPage(QueryMap critera);

}
