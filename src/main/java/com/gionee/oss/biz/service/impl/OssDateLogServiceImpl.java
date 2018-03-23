package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.oss.biz.model.OssDateLog;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.integration.dao.OssDateLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OssDateLogServiceImpl implements OssDateLogService {

    @Autowired
    private OssDateLogDao ossDateLogDao;

    @Override
    public void save(OssDateLog ossDateLog) {
        if (ossDateLog.getCreateTime() == null) {
            ossDateLogDao.insert(ossDateLog);
        } else {
            ossDateLogDao.update(ossDateLog);
        }
    }

    @Override
    public OssDateLog getOssDateLog(Date date) {
        return ossDateLogDao.get(date);
    }

    @Override
    public void delete(Integer id) {
        OssDateLog ossDateLog = new OssDateLog();
        ossDateLog.setId(id);
        ossDateLog.setStatus(OssDateLog.STATUS_DELETED);
        ossDateLogDao.update(ossDateLog);
    }

    @Override
    public List<OssDateLog> query(QueryMap critera) {
        return ossDateLogDao.getAll(critera.getMap());
    }

    @Override
    public PageResult<OssDateLog> queryPage(QueryMap critera) {
        PageResult<OssDateLog> result = new PageResult<OssDateLog>();
        result.setRows(ossDateLogDao.getPage(critera.getMap()));
        result.setTotal(ossDateLogDao.getPageCount(critera.getMap()));
        return result;
    }

    @Override
    public List<OssDateLog> getOssDateLogExcept(Date date) {
        return ossDateLogDao.getExcept(date);
    }

}
