package com.gionee.oss.cache;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssTripartiteSystemService;
import com.gionee.oss.constant.PropertiesConfig;
import com.gionee.oss.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yeqy on 2017/6/6.
 */
public class SystemKeyCache implements Cache<String, OssTripartiteSystem> {
    private final Logger logger = Logger.getLogger(SystemKeyCache.class);
    private volatile Map<String, OssTripartiteSystem> map = new ConcurrentHashMap<>();
    @Autowired
    private OssTripartiteSystemService ossTripartiteSystemService;


    public void set(String code, OssTripartiteSystem os) {
        map.put(code, os);
    }

    public void delete(String param) {
        map.remove(param);
    }

    public OssTripartiteSystem get(String code) {
        return map.get(code);
    }

    public boolean exist(String code) {
        return map.containsKey(code);
    }


    public void init() {
        logger.info("SystemKeyCache init. . .");
        List<OssTripartiteSystem> os = ossTripartiteSystemService.query(new QueryMap());
        for (OssTripartiteSystem system : os) {
            set(system.getCode(), system);
            createDir(system, 12);
        }
    }

    public void createDir(OssTripartiteSystem system, Integer count) {
        StringBuilder dirPath = new StringBuilder(system.getSystemRootpath());
        File file = new File(dirPath.toString());
        if (!file.exists()) {
            synchronized (this) {
                if (!file.exists()) {
                    if (file.mkdirs()) {
                        logger.info(system.getSystemRootpath() + " created. . .");
                    } else {
                        logger.error(system.getSystemRootpath() + " create failed. . .");
                    }
                }
            }
        } else {
            logger.info(system.getSystemRootpath() + " exited. . .");
        }

        if (count <= 0) {
            return;
        }
        Date date = new Date();
        for (int i = 0; i <= count; i++) {
            //年月
            String dm = DateUtil.getDateYearMonth(date, i);
            String mothpath = new StringBuilder(dirPath).append(PropertiesConfig.fileSeparator).append(dm).toString();
            File monthFile = new File(mothpath);
            if (!monthFile.exists()) {
                synchronized (this) {
                    if (monthFile.mkdir()) {
                        logger.info(mothpath + " created. . .");
                    } else {
                        logger.error(mothpath + " create failed. . .");
                    }
                }
            } else {
                logger.info(mothpath + " exited. . .");
            }
        }
    }

    public void clear() {
        map.clear();
    }

    public OssTripartiteSystem getAndSysnc(String code) {
        OssTripartiteSystem ossTripartiteSystem = this.get(code);
        if (ossTripartiteSystem == null) {
            ossTripartiteSystem = ossTripartiteSystemService.getByCode(code);
            if (ossTripartiteSystem != null) {
                this.set(code, ossTripartiteSystem);
            } else {//系统编码不存在
                return null;
            }
        }
        return ossTripartiteSystem;
    }

}
