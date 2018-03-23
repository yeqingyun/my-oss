package com.gionee.oss.integration.dao;


import com.gionee.oss.biz.model.OssDeleteFileLog;

import java.util.List;
import java.util.Map;

public interface OssDeleteFileLogDao {

    int insert(OssDeleteFileLog ossDeleteFileLog);

    int update(OssDeleteFileLog ossDeleteFileLog);

    int delete(Integer id);

    OssDeleteFileLog getById(Integer id);

    List<OssDeleteFileLog> getAll(Map<String, Object> map);

    List<OssDeleteFileLog> getPage(Map<String, Object> map);

    Integer getPageCount(Map<String, Object> map);

    OssDeleteFileLog get(Integer id);
}
