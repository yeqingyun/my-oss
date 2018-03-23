package com.gionee.oss.integration.dao;

import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.web.response.MachineVolume;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OssFileDao {

    int insert(OssFile ossFile);

    int insertWithId(OssFile ossFile);

    int update(OssFile ossFile);

    int delete(Integer id);

    List<OssFile> getAll(Map<String, Object> param);

    List<OssFile> getPage(Map<String, Object> param);

    int getPageCount(Map<String, Object> param);

    OssFile get(Integer id);

    List<OssFile> getByMd5AndSize(@Param("md5") String md5, @Param("size") Integer size);

    int deleteTmp(Integer tmp);

    int changeTmp(Map<String, Object> param);

    int deleteByIds(List<Integer> ids);

    List<OssFile> getByIds(Map<String, Object> param);

    int addRefer(Map<String, Object> param);

    int changeTmpToUnTmp(Map<String, Object> param);

    int updateFileCodeByCode(@Param("oldCode") String oldCode, @Param("newCode") String newCode);

    List<OssFile> getByCode(String code);

    MachineVolume getAllFileInfo();

}
