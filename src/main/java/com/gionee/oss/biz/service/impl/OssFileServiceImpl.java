package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.constant.HandleInfo;
import com.gionee.oss.constant.HandleType;
import com.gionee.oss.constant.TmpFile;
import com.gionee.oss.integration.dao.OssFileDao;
import com.gionee.oss.task.OssDateLogTask;
import com.gionee.oss.util.StringUtil;
import com.gionee.oss.web.response.MachineVolume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OssFileServiceImpl implements OssFileService {

    @Autowired
    private OssFileDao ossFileDao;
    @Autowired
    @Qualifier("threadPool")
    private TaskExecutor taskExecutor;
    @Autowired
    private OssDateLogService ossDateLogService;

    @Override
    public void save(OssFile ossFile) {
        if (ossFile.getId() == null) {
            ossFileDao.insert(ossFile);
        } else {
            if (StringUtil.isNotEmpty(ossFile.getPath())) {
                OssFile oldFile = ossFileDao.get(ossFile.getId());
                if (oldFile == null)
                    return;
                File file = new File(oldFile.getPath());
                if (file.exists()) {
                    file.renameTo(new File(ossFile.getPath()));
                }
            }
            ossFileDao.update(ossFile);
        }
    }

    @Override
    public OssFile getOssFile(Integer id) {
        return ossFileDao.get(id);
    }


    @Override
    public boolean delete(OssFile ossfile) {
        if (ossfile.getRefer() <= 1) {
            ossFileDao.delete(ossfile.getId());
            List<OssFile> files = ossFileDao.getByMd5AndSize(ossfile.getMd5(), ossfile.getSize());
            if (files.size() == 0) {
                File file = new File(ossfile.getPath());
                if (file.delete()) {
                    return true;
                }
            }
        } else {
            ossfile.setRefer(ossfile.getRefer() - 1);
            ossFileDao.update(ossfile);
        }

        return false;
    }

    @Override
    public void removeOssFile(Integer id) {
        ossFileDao.delete(id);
    }


    @Override
    public List<OssFile> query(QueryMap critera) {
        return ossFileDao.getAll(critera.getMap());
    }

    @Override
    public PageResult<OssFile> queryPage(QueryMap critera) {
        PageResult<OssFile> result = new PageResult<OssFile>();
        result.setRows(ossFileDao.getPage(critera.getMap()));
        result.setTotal(ossFileDao.getPageCount(critera.getMap()));
        return result;
    }

    @Override
    public List<OssFile> queryAll() {
        return ossFileDao.getAll(null);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public OssFile getByMd5AndSize(String md5, Integer size) {
        List<OssFile> files = ossFileDao.getByMd5AndSize(md5, size);
        if (files == null || files.size() == 0) {
            return null;
        }
        return files.get(0);
    }

    @Override
    public OssFile realDelete(Integer id) {
        OssFile ossFile = ossFileDao.get(id);
        ossFileDao.delete(id);
        List<OssFile> files = ossFileDao.getByMd5AndSize(ossFile.getMd5(), ossFile.getSize());
        if (files.size() == 0) {
            new File(ossFile.getPath()).delete();
        }
        return ossFile;
    }

    @Override
    public void deleteTmp(Integer tmp) {
        ossFileDao.deleteTmp(tmp);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void changeTmp(QueryMap critera) {
        Map map = critera.getMap();
        Integer tmp = (Integer) map.get("tmp");
        List<Integer> other = new ArrayList<>();
        HandleInfo handleInfoUnTmp = null;
        HandleInfo handleInfoChangeTmp = null;
        HandleInfo handleInfoTmp = new HandleInfo(HandleType.UPLOAD_TMP);

        if (tmp != null && tmp == TmpFile.UNTMP_FILE.getType()) {//如果需要修改为永久文件
            //找到已存在的文件
            Map tmpMap = new HashMap();
            tmpMap.put("ids", map.get("ids"));
            List<OssFile> files = ossFileDao.getByIds(tmpMap);


            //遍历文件
            if (files.size() > 0) {
                List<Integer> untmpList = new ArrayList<>();
                List<Integer> tmpList = new ArrayList<>();
                for (OssFile file : files) {
                    if (file.getTmp() == TmpFile.UNTMP_FILE.getType())
                        untmpList.add(file.getId());//已存在的永久文件
                    else if (file.getRefer() > 1)
                        tmpList.add(file.getId());//已存在且拥有多个引用的临时文件
                    else
                        other.add(file.getId());//引用为1的临时文件
                }
                if (untmpList.size() > 0) {//为已存在的永久文件添加引用
                    Map ids = new HashMap();
                    ids.put("ids", untmpList);
                    ossFileDao.addRefer(ids);

                    handleInfoUnTmp = new HandleInfo(HandleType.UPLOAD, untmpList.size());
                    handleInfoTmp.setValue(0 - untmpList.size());
                }
                if (tmpList.size() > 0) {//将已存在且拥有多个引用的临时文件变为有一个引用的永久文件
                    Map param = new HashMap();
                    param.put("ids", tmpList);
                    param.put("tmp", TmpFile.UNTMP_FILE.getType());
                    ossFileDao.changeTmpToUnTmp(param);
                    handleInfoTmp.setValue(handleInfoTmp.getValue() - tmpList.size());
                    if (handleInfoUnTmp != null) {
                        handleInfoUnTmp.setValue(handleInfoUnTmp.getValue() + 1);
                    } else {
                        handleInfoUnTmp = new HandleInfo(HandleType.UPLOAD, 1);
                    }
                }


            }
        }
        if (other.size() > 0) {
            critera.put("ids", other);
            ossFileDao.changeTmp(critera.getMap());

            if (tmp == TmpFile.UNTMP_FILE.getType()) {//如果是永久文件
                handleInfoChangeTmp = new HandleInfo(HandleType.UPLOAD, other.size());
                handleInfoTmp.setValue(handleInfoTmp.getValue() - other.size() - 1);
            } else {//如果是临时文件
                handleInfoChangeTmp = new HandleInfo(HandleType.UPLOAD_TMP, other.size());
            }
        }

        taskExecutor.execute(new OssDateLogTask(ossDateLogService, handleInfoUnTmp, handleInfoChangeTmp, handleInfoTmp));
    }

    @Override
    public List<OssFile> getByIds(QueryMap critera) {
        return ossFileDao.getByIds(critera.getMap());
    }

    @Override
    public void addRefer(QueryMap critera) {
        ossFileDao.addRefer(critera.getMap());
    }

    @Override
    public int deleteByIds(List<Integer> ids) {
        return ossFileDao.deleteByIds(ids);
    }

    @Override
    public MachineVolume getAllFileInfo() {
        return ossFileDao.getAllFileInfo();
    }

}
