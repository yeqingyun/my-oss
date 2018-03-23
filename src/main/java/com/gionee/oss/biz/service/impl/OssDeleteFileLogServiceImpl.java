package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.oss.biz.model.OssDeleteFileLog;
import com.gionee.oss.biz.service.OssDeleteFileLogService;
import com.gionee.oss.integration.dao.OssDeleteFileLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OssDeleteFileLogServiceImpl implements OssDeleteFileLogService {

    @Autowired
    private OssDeleteFileLogDao ossDeleteFileLogDao;

    @Override
    public OssDeleteFileLog getOssDeleteFileLog(Integer id) {
        return ossDeleteFileLogDao.get(id);
    }

    @Override
    public void save(OssDeleteFileLog ossDeleteFileLog) {
        if (ossDeleteFileLog.getId() == null) {
            ossDeleteFileLogDao.insert(ossDeleteFileLog);
        } else {
            ossDeleteFileLogDao.update(ossDeleteFileLog);
        }
    }

    @Override
    public void delete(Integer id) {
        ossDeleteFileLogDao.delete(id);
    }


    @Override
    public List<OssDeleteFileLog> query(QueryMap critera) {
        return ossDeleteFileLogDao.getAll(critera.getMap());
    }

    @Override
    public PageResult<OssDeleteFileLog> queryPage(QueryMap critera) {
        PageResult<OssDeleteFileLog> result = new PageResult<OssDeleteFileLog>();
        result.setRows(ossDeleteFileLogDao.getPage(critera.getMap()));
        result.setTotal(ossDeleteFileLogDao.getPageCount(critera.getMap()));
        return result;
    }

}
