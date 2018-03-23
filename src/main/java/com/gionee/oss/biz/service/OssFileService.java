package com.gionee.oss.biz.service;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.web.response.MachineVolume;

import java.util.List;

public interface OssFileService {

    void save(OssFile ossFile);

    OssFile getOssFile(Integer id);

    boolean delete(OssFile ossFile);

    void removeOssFile(Integer id);

    List<OssFile> query(QueryMap critera);

    PageResult<OssFile> queryPage(QueryMap critera);

    List<OssFile> queryAll();

    OssFile getByMd5AndSize(String md5, Integer size);

    OssFile realDelete(Integer id);

    void deleteTmp(Integer tmp);

    void changeTmp(QueryMap critera);

    List<OssFile> getByIds(QueryMap critera);

    void addRefer(QueryMap critera);

    int deleteByIds(List<Integer> ids);

    MachineVolume getAllFileInfo();

}
