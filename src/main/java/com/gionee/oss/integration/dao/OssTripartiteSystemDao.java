package com.gionee.oss.integration.dao;

import com.gionee.oss.biz.model.OssTripartiteSystem;

import java.util.List;
import java.util.Map;

public interface OssTripartiteSystemDao {

    int update(OssTripartiteSystem ossTripartiteSystem);

    int insert(OssTripartiteSystem ossTripartiteSystem);

    List<OssTripartiteSystem> getAll(Map<String, Object> param);

    List<OssTripartiteSystem> getPage(Map<String, Object> param);

    int getPageCount(Map<String, Object> param);

    OssTripartiteSystem getByCode(String code);

    int delete(Map<String, Object> map);
}
