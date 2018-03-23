package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.gnif.web.response.MessageResponse;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssTripartiteSystemService;
import com.gionee.oss.cache.Cache;
import com.gionee.oss.cache.SystemKeyCache;
import com.gionee.oss.constant.Info;
import com.gionee.oss.constant.PropertiesConfig;
import com.gionee.oss.constant.TmpFile;
import com.gionee.oss.integration.dao.OssFileDao;
import com.gionee.oss.integration.dao.OssTripartiteSystemDao;
import com.gionee.oss.util.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class OssTripartiteSystemServiceImpl implements OssTripartiteSystemService {
    private final Logger logger = Logger.getLogger(OssTripartiteSystemServiceImpl.class);
    @Value("${system.rootPath}")
    private String defaultRootPath;
    @Autowired
    private OssTripartiteSystemDao ossTripartiteSystemDao;
    @Autowired
    private OssFileDao ossFileDao;
    @Autowired
    private Cache<String, OssTripartiteSystem> systemKeyCache;

    @Override
    public MessageResponse add(OssTripartiteSystem ossTripartiteSystem) {
        MessageResponse response = addCheckSystemModel(ossTripartiteSystem);
        if (response != null) {
            return response;
        }
        ossTripartiteSystemDao.insert(ossTripartiteSystem);
        systemKeyCache.set(ossTripartiteSystem.getCode(), ossTripartiteSystem);
        //再一次建系统文件夹
        ((SystemKeyCache) systemKeyCache).createDir(ossTripartiteSystem, 12);

        return new MessageResponse();
    }

    /**
     * 修改三方系统 1.可修改code 需要将file的code也同步修改过来，不会影响下载与上传
     * 2.可修改key 不影响上传下载
     *
     * @param ossTripartiteSystem
     * @return
     */
    @Override
    @Transactional
    public MessageResponse update(OssTripartiteSystem ossTripartiteSystem) {
        MessageResponse response = updateCheckSystemModel(ossTripartiteSystem);
        if (response != null) {
            return response;
        }

        ossTripartiteSystemDao.update(ossTripartiteSystem);
        systemKeyCache.set(ossTripartiteSystem.getCode(), ossTripartiteSystem);
        //再一次建系统文件夹
        ((SystemKeyCache) systemKeyCache).createDir(ossTripartiteSystem, 12);
        return new MessageResponse();
    }


    @Override
    public List<OssTripartiteSystem> query(QueryMap critera) {
        return ossTripartiteSystemDao.getAll(critera.getMap());
    }

    @Override
    public PageResult<OssTripartiteSystem> queryPage(QueryMap critera) {
        PageResult<OssTripartiteSystem> result = new PageResult<>();
        result.setRows(ossTripartiteSystemDao.getPage(critera.getMap()));
        result.setTotal(ossTripartiteSystemDao.getPageCount(critera.getMap()));
        return result;
    }

    @Override
    public OssTripartiteSystem getByCode(String code) {
        return ossTripartiteSystemDao.getByCode(code);
    }

    @Override
    @Transactional
    public void delete(String code) {
        Map<String, Object> map = new HashMap<>();
        String[] codes = code.split(",");
        map.put("codes", codes);
        ossTripartiteSystemDao.delete(map);
        for (String c : codes) {
            systemKeyCache.delete(c);
        }

        //将要删除系统的文件全设为临时文件
        List<OssFile> files = ossFileDao.getByCode(code);
        List<Integer> ids = new LinkedList<>();
        for (OssFile file : files) {
            ids.add(file.getId());
        }
        Map<String, Object> parmas = new HashMap<>();
        parmas.put("tmp", TmpFile.TMP_FILE.getType());
        parmas.put("ids", ids);
        ossFileDao.changeTmp(parmas);
    }

    /**
     * 修改时判断系统参数是否正确
     *
     * @param ossTripartiteSystem
     * @return
     */
    private MessageResponse updateCheckSystemModel(OssTripartiteSystem ossTripartiteSystem) {
        OssTripartiteSystem oldTripartiteSystem = ossTripartiteSystemDao.getByCode(ossTripartiteSystem.getOldCode());

        if (StringUtil.isEmpty(ossTripartiteSystem.getOldCode())) {//判断旧编码是否为空
            return new MessageResponse(Info.SYSTEM_NOTEXIT.getInfo());
        }
        OssTripartiteSystem ots = ossTripartiteSystemDao.getByCode(ossTripartiteSystem.getOldCode());
        if (ots == null) {//判断旧编码系统是否存在
            return new MessageResponse(Info.SYSTEM_NOTEXIT.getInfo());
        }

        //判断系统编码，系统名称是否为空
        if (StringUtil.hasEmpty(ossTripartiteSystem.getCode(), ossTripartiteSystem.getName())) {
            return new MessageResponse(Info.SYSTEM_CODE_NAME_CANNOTNULL.getInfo());
        }

        //判断系统秘钥为空以及系统秘钥长度是否正确
        if (StringUtil.isEmpty(ossTripartiteSystem.getKey()) || ossTripartiteSystem.getKey().length() < 16 || ossTripartiteSystem.getKey().length() > 32) {
            return new MessageResponse(Info.SYSTEM_KEY_ERROR.getInfo());
        }

        //将系统根路径后多余的分隔符删掉
        while (StringUtil.isNotEmpty(ossTripartiteSystem.getSystemRootpath()) && ossTripartiteSystem.getSystemRootpath().endsWith(PropertiesConfig.fileSeparator)) {
            ossTripartiteSystem.setSystemRootpath(ossTripartiteSystem.getSystemRootpath().substring(0, ossTripartiteSystem.getSystemRootpath().length() - 1));
        }

        if (!oldTripartiteSystem.getCode().equals(ossTripartiteSystem.getCode())) {//如果新编码与旧编码不同
            if (ossTripartiteSystemDao.getByCode(ossTripartiteSystem.getOldCode()) != null) {
                return new MessageResponse(Info.SYSTEM_CODE_EXIT.getInfo());
            }
            ossFileDao.updateFileCodeByCode(oldTripartiteSystem.getCode(), ossTripartiteSystem.getCode());
        }

        if (!oldTripartiteSystem.getSystemRootpath().equals(ossTripartiteSystem.getSystemRootpath())) {//新路径与旧路径不同
            if (StringUtil.isEmpty(ossTripartiteSystem.getSystemRootpath())) {//新路径为空
                String path = new StringBuilder(defaultRootPath).append(PropertiesConfig.fileSeparator).append(ossTripartiteSystem.getCode()).toString();
                ossTripartiteSystem.setSystemRootpath(path);
                if (!oldTripartiteSystem.getSystemRootpath().equals(path)) {//如果旧路径与新路径不同
                    File file = new File(path);
                    if (file.exists()) {
                        if (!file.isDirectory()) {//如果不是目录
                            return new MessageResponse(Info.SYSTEM_PATH_NOTDIR.getInfo());
                        }
                        if (file.listFiles().length > 0) {//如果该目录下面已有文件
                            return new MessageResponse(Info.SYSTEM_PATH_EXITNOTNULL.getInfo());
                        }
                    } else {
                        if (!file.mkdirs()) {//如果创建失败
                            logger.error(path + "created failed. . .");
                            return new MessageResponse(Info.SYSTEM_CREATE_FAILED.getInfo());
                        }
                    }

                }
            } else {
                File file = new File(ossTripartiteSystem.getSystemRootpath());
                MessageResponse x = makeDirWhenNotExist(ossTripartiteSystem, file);
                if (x != null) return x;
            }
        }

        return null;
    }

    private MessageResponse makeDirWhenNotExist(OssTripartiteSystem ossTripartiteSystem, File file) {
        if (file.exists()) {//如果已存在
            if (!file.isDirectory()) {//如果不是目录
                return new MessageResponse(Info.SYSTEM_PATH_NOTDIR.getInfo());
            }
            if (file.listFiles().length > 0) {//如果该目录下面已有文件
                return new MessageResponse(Info.SYSTEM_PATH_EXITNOTNULL.getInfo());
            }
        } else {//如果不存在
            if (!file.mkdirs()) {//如果创建失败
                logger.error(ossTripartiteSystem.getSystemRootpath() + "created failed. . .");
                return new MessageResponse(Info.SYSTEM_CREATE_FAILED.getInfo());
            }
        }
        return null;
    }

    /**
     * 新增时判断系统参数是否正确
     *
     * @param ossTripartiteSystem
     * @return
     */
    private MessageResponse addCheckSystemModel(OssTripartiteSystem ossTripartiteSystem) {

        //判断系统编码，系统名称是否为空
        if (StringUtil.hasEmpty(ossTripartiteSystem.getCode(), ossTripartiteSystem.getName())) {
            return new MessageResponse(Info.SYSTEM_CODE_NAME_CANNOTNULL.getInfo());
        }

        OssTripartiteSystem ots = ossTripartiteSystemDao.getByCode(ossTripartiteSystem.getCode());
        if (ots != null) {//如果该编码已存在
            return new MessageResponse(Info.SYSTEM_CODE_EXIT.getInfo());
        }

        //判断系统秘钥为空以及系统秘钥长度是否正确
        if (StringUtil.isEmpty(ossTripartiteSystem.getKey()) || ossTripartiteSystem.getKey().length() < 16 || ossTripartiteSystem.getKey().length() > 32) {
            return new MessageResponse(Info.SYSTEM_KEY_ERROR.getInfo());
        }

        if (StringUtil.isNotEmpty(ossTripartiteSystem.getSystemRootpath())) {//如果不为空，需要检验目录正确性
            File file = new File(ossTripartiteSystem.getSystemRootpath());
            //1.判断目录是否存在
            MessageResponse x = makeDirWhenNotExist(ossTripartiteSystem, file);
            if (x != null) return x;
        } else {//如果三方系统根路径为空，则默认为systemRootPath+code
            String path = new StringBuilder(defaultRootPath).append(PropertiesConfig.fileSeparator).append(ossTripartiteSystem.getCode()).toString();
            ossTripartiteSystem.setSystemRootpath(path);
            if (!new File(path).mkdirs()) {//如果创建失败
                logger.error(path + "created failed. . .");
                return new MessageResponse(Info.SYSTEM_CREATE_FAILED.getInfo());
            }
        }


        return null;
    }

}
