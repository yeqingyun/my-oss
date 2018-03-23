package com.gionee.oss.task;

import com.gionee.oss.biz.model.OssDeleteFileLog;
import com.gionee.oss.biz.service.OssDeleteFileLogService;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by yeqy on 2017/8/28.
 */
public class OssFileDeleteLogTask implements Runnable {

    private final Logger logging = Logger.getLogger(OssFileDeleteLogTask.class);

    private OssDeleteFileLogService ossDeleteFileLogService;

    private List<OssDeleteFileLog> ossDeleteFileLogs;

    public OssFileDeleteLogTask(OssDeleteFileLogService ossDeleteFileLogService, List<OssDeleteFileLog> ossDeleteFileLogs) {
        this.ossDeleteFileLogService = ossDeleteFileLogService;
        this.ossDeleteFileLogs = ossDeleteFileLogs;
    }

    public void setOssDeleteFileLog(List<OssDeleteFileLog> ossDeleteFileLogs) {
        this.ossDeleteFileLogs = ossDeleteFileLogs;
    }

    public void setOssDeleteFileLogService(OssDeleteFileLogService ossDeleteFileLogService) {
        this.ossDeleteFileLogService = ossDeleteFileLogService;
    }

    @Override
    public void run() {
        for (OssDeleteFileLog ossDeleteFileLog : ossDeleteFileLogs) {
            try {
                ossDeleteFileLogService.save(ossDeleteFileLog);
            } catch (Exception e) {
                logging.error(e.getMessage());
                e.printStackTrace();
                continue;
            }
        }
    }
}
