package com.gionee.oss.integration.dao;

import com.gionee.oss.biz.model.OssDownloadUrl;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OssDownloadUrlDao {

    int insert(OssDownloadUrl ossDownloadUrl);

    int update(OssDownloadUrl ossDownloadUrl);

    int delete(Integer id);

    List<OssDownloadUrl> getAllById(Integer id);

    OssDownloadUrl getById(Integer id);

    List<OssDownloadUrl> getPageById(Integer id);

    int getPageCountById(Integer id);

    List<OssDownloadUrl> getPage(Map<String, Object> param);

    int getPageCount(Map<String, Object> param);

    OssDownloadUrl get(Integer id);

    List<OssDownloadUrl> getAll(Map<String, Object> param);

    OssDownloadUrl getByCodeAndPolicy(@Param("code") String code, @Param("policy") String policy);

}
