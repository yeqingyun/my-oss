package com.gionee.oss.integration.dao;

import com.gionee.oss.biz.model.OssDateLog;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OssDateLogDao {

    int insert(OssDateLog ossDateLog);

    int update(OssDateLog ossDateLog);

    int delete(Integer id);

    OssDateLog get(Date date);

    List<OssDateLog> getExcept(Date date);

    List<OssDateLog> getPage(Map<String, Object> param);

    int getPageCount(Map<String, Object> param);

    List<OssDateLog> getAll(Map<String, Object> param);

}
