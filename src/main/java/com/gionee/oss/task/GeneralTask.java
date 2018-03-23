package com.gionee.oss.task;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.oss.biz.model.OssDeleteFileLog;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.biz.service.OssDeleteFileLogService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.biz.service.OssTripartiteSystemService;
import com.gionee.oss.constant.HandleInfo;
import com.gionee.oss.constant.HandleType;
import com.gionee.oss.constant.PropertiesConfig;
import com.gionee.oss.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.core.task.TaskExecutor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yeqy on 2017/8/4.
 */
public class GeneralTask {

    private static final Logger logging = Logger.getLogger(GeneralTask.class);

    public static void mkSystemMonthDir(OssTripartiteSystemService ossTripartiteSystemService) {
        //创建 年月目录
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        String yearMonth = DateUtil.getDateYearMonth(calendar.getTime());

        List<OssTripartiteSystem> ossTripartiteSystems = ossTripartiteSystemService.query(new QueryMap());

        for (OssTripartiteSystem st : ossTripartiteSystems) {
            StringBuilder dirPath = new StringBuilder(st.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(yearMonth);
            File monthDir = new File(dirPath.toString());
            if (!monthDir.exists()) {
                if (monthDir.mkdirs()) {
                    logging.info(monthDir.getPath() + "created. . .");
                } else {
                    logging.error(monthDir.getPath() + "create failed. . .");
                }
            }
        }
    }


    public static void removeOssfileNotExist(List<OssFile> tmpFiles, OssFileService fileService, OssDateLogService ossDateLogService, TaskExecutor taskExecutor, OssDeleteFileLogService ossDeleteFileLogService) {

        List<OssDeleteFileLog> ossDeleteFileLogs = new LinkedList<>();

        //删除文件实体
        for (OssFile file : tmpFiles) {
            try {
                Files.delete(Paths.get(file.getPath()));
                ossDeleteFileLogs.add(new OssDeleteFileLog(file));
            } catch (Exception e) {
                continue;
            }
        }

        //删除文件不存在的数据库文件数据
        int count = removeNotExist(fileService, ossDeleteFileLogs);
        //记录文件服务日志
        taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.CLEAR, tmpFiles.size() + count)));

        //记录文件删除日志
        taskExecutor.execute(new OssFileDeleteLogTask(ossDeleteFileLogService, ossDeleteFileLogs));
    }

    //删除文件不存在的数据库文件数据
    private static int removeNotExist(OssFileService fileService, List<OssDeleteFileLog> ossDeleteFileLogs) {
        List<OssFile> allFiles = fileService.queryAll();
        List<Integer> delIds = new LinkedList<>();
        for (OssFile file : allFiles) {
            if (!Files.exists(Paths.get(file.getPath()))) {
                delIds.add(file.getId());
                ossDeleteFileLogs.add(new OssDeleteFileLog(file));
            }
        }
        int count = 0;
        if (delIds.size() > 0)
            count = fileService.deleteByIds(delIds);
        return count;
    }
}
