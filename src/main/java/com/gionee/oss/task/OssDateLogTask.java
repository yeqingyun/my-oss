package com.gionee.oss.task;

import com.gionee.oss.biz.model.OssDateLog;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.constant.HandleInfo;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Created by yeqy on 2017/6/21.
 */
public class OssDateLogTask implements Runnable {
    private final Logger logging = Logger.getLogger(OssDateLogTask.class);
    private OssDateLogService logService;
    private HandleInfo[] handleInfos;

    public OssDateLogTask(OssDateLogService logService, HandleInfo... handleInfos) {
        this.logService = logService;
        this.handleInfos = handleInfos;
    }

    @Override
    public void run() {
        try {

            if (handleInfos != null && handleInfos.length > 0) {
                Date now = new Date();
                OssDateLog ossDateLog = logService.getOssDateLog(now);
                if (ossDateLog == null) {
                    ossDateLog = new OssDateLog();
                    ossDateLog.setDate(now);
                }

                for (HandleInfo handleInfo : handleInfos) {//可同时传入多个类型
                    if (handleInfo == null) {
                        continue;
                    }

                    switch (handleInfo.getHandleType()) {//根据不同类型记录日志
                        case UPLOAD: {
                            ossDateLog.setUpload(ossDateLog.getUpload() + handleInfo.getValue());
                            break;
                        }
                        case UPLOAD_TMP: {
                            ossDateLog.setUploadTmp(ossDateLog.getUploadTmp() + handleInfo.getValue());
                            break;
                        }
                        case DOWNLOAD: {
                            ossDateLog.setDownload(ossDateLog.getDownload() + handleInfo.getValue());
                            break;
                        }
                        case DELETE: {
                            ossDateLog.setDelete(ossDateLog.getDelete() + handleInfo.getValue());
                            break;
                        }
                        case CLEAR: {
                            ossDateLog.setClear(ossDateLog.getClear() + handleInfo.getValue());
                        }
                    }

                }

                if (ossDateLog.getUploadTmp() < 0) {
                    ossDateLog.setUploadTmp(0);
                }
                logService.save(ossDateLog);
            }
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
