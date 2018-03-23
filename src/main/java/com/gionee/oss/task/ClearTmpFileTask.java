package com.gionee.oss.task;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.biz.service.OssDeleteFileLogService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.biz.service.OssTripartiteSystemService;
import com.gionee.oss.constant.PropertiesConfig;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import static com.gionee.oss.constant.TmpFile.TMP_FILE;

/**
 * Created by yeqy on 2017/6/21.
 */
@Component
public class ClearTmpFileTask {
    private final Logger logging = Logger.getLogger(ClearTmpFileTask.class);

    @Autowired
    private PropertiesConfig propertyConfig;
    @Autowired
    private OssFileService fileService;
    @Autowired
    private OssDateLogService ossDateLogService;
    @Autowired
    @Qualifier("threadPool")
    private TaskExecutor taskExecutor;
    @Autowired
    private OssTripartiteSystemService ossTripartiteSystemService;
    @Autowired
    private OssDeleteFileLogService ossDeleteFileLogService;

    /**
     * 清空临时文件 以及 创建每月目录
     */
    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 0/5 * * * ?")
    //@Scheduled(cron = "0 0 5 0/3 * ?")
    public void clearTmpFile() {
        try {
            logging.info("clearTmpFile started...");
            //清空临时文件和目录
            List<OssFile> tmpFiles = this.clearTmp();

            //创建系统根目录下年月目录
            GeneralTask.mkSystemMonthDir(ossTripartiteSystemService);

            //删除数据库临时文件数据
            GeneralTask.removeOssfileNotExist(tmpFiles, fileService, ossDateLogService, taskExecutor, ossDeleteFileLogService);

            System.gc();

            logging.info("clearTmpFile completed...");

        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
        }
        //删除数据库中文件实体不存在的数据
    }


    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (!file.delete()) {
                    deleteDir(file);
                    file.delete();
                }
            }
        } else {
            dir.delete();
        }
    }


    private List<OssFile> clearTmp() {
        String tmpRootPath = propertyConfig.getRootPath();
        deleteDir(new File(tmpRootPath));

        QueryMap queryMap = new QueryMap();
        queryMap.put("tmp", TMP_FILE.getType());
        List<OssFile> ossFiles = fileService.query(queryMap);
        fileService.deleteTmp(TMP_FILE.getType());
        return ossFiles;
    }
}
