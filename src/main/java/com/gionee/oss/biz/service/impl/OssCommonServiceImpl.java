package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.file.util.CalcUtil;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.model.OssDeleteFileLog;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssCommonService;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.biz.service.OssDeleteFileLogService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.cache.Cache;
import com.gionee.oss.constant.HandleInfo;
import com.gionee.oss.constant.HandleType;
import com.gionee.oss.constant.Info;
import com.gionee.oss.dto.CommonSchema;
import com.gionee.oss.task.OssDateLogTask;
import com.gionee.oss.task.OssFileDeleteLogTask;
import com.gionee.oss.util.NumberUtil;
import com.gionee.oss.util.StringUtil;
import com.gionee.oss.util.VerifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yeqy on 2017/6/15.
 */
@Service
public class OssCommonServiceImpl implements OssCommonService {
    @Autowired
    private Cache<String, OssTripartiteSystem> systemKeyCache;
    @Autowired
    private OssFileService ossFileService;
    @Autowired
    private VerifyUtil verifyUtil;
    @Autowired
    @Qualifier("threadPool")
    private TaskExecutor taskExecutor;
    @Autowired
    private OssDateLogService ossDateLogService;
    @Autowired
    private OssDeleteFileLogService ossDeleteFileLogService;

    @Override
    public Message fileDelete(CommonSchema schema) throws UnsupportedEncodingException {
        if (StringUtil.hasEmpty(schema.getCode(), schema.getPolicy(), schema.getSignature())) {
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        OssTripartiteSystem ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());
        if (ossTripartiteSystem == null) {
            return Message.error(Info.PERSMISSION_DENIED.getInfo(), schema.getCallback());
        }
        String policyStr = CalcUtil.getFromBase64(schema.getPolicy());//base64解码

        if (!CalcUtil.hamcsha1(policyStr, ossTripartiteSystem.getKey()).toLowerCase().equals(CalcUtil.getFromBase64(schema.getSignature()).toLowerCase())) {
            return Message.error(Info.TAMPER_INFO.getInfo(), schema.getCallback());
        }
        if (policyStr.indexOf("\n") < 0) {
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        String[] info = policyStr.split("\n");
        String time;
        Integer fileNo;
        Integer tmp;

        try {

            if (info.length == 2) {
                time = info[0];
                String tmpStr = info[1];
                int idx;
                if ((idx = info[1].indexOf("\\n")) > 0) {
                    fileNo = Integer.valueOf(tmpStr.substring(0, idx));
                    tmp = Integer.valueOf(tmpStr.substring(idx + 2));
                } else {
                    return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
                }
            } else if (info.length == 3) {
                time = info[0];
                fileNo = Integer.valueOf(info[1]);
                tmp = Integer.valueOf(info[2]);
            } else {
                return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        //验证是否超时
        Message message = verifyUtil.verifyTime(time, schema);
        if (message != null)
            return message;

        OssFile ossFile;
        try {
            ossFile = ossFileService.getOssFile(fileNo);
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }

        if (ossFile == null) {
            return Message.pass(schema.getCallback());
        }

        if (ossFile.getTmp() != tmp) {//如果临时状态不相同
            return Message.pass(schema.getCallback());
        }

        File file = new File(ossFile.getPath());

        if (!file.exists()) {
            return Message.pass(schema.getCallback());
        }

        boolean deleteRealFile = ossFileService.delete(ossFile);

        taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.DELETE)));

        if (deleteRealFile) {
            List<OssDeleteFileLog> ossDeleteFileLogs = new ArrayList<>();
            ossDeleteFileLogs.add(new OssDeleteFileLog(ossFile));
            taskExecutor.execute(new OssFileDeleteLogTask(ossDeleteFileLogService, ossDeleteFileLogs));
        }


        return Message.pass(schema.getCallback());
    }

    @Override
    public Message changeTmpFile(CommonSchema schema) throws UnsupportedEncodingException {
        if (StringUtil.hasEmpty(schema.getCode(), schema.getPolicy(), schema.getSignature())) {
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        OssTripartiteSystem ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());
        if (ossTripartiteSystem == null) {
            return Message.error(Info.PERSMISSION_DENIED.getInfo(), schema.getCallback());
        }
        String policyStr = CalcUtil.getFromBase64(schema.getPolicy());//base64解码

        if (!CalcUtil.hamcsha1(policyStr, ossTripartiteSystem.getKey()).toLowerCase().equals(CalcUtil.getFromBase64(schema.getSignature()).toLowerCase())) {
            return Message.error(Info.TAMPER_INFO.getInfo(), schema.getCallback());
        }
        if (policyStr.indexOf("\n") < 0) {
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }

        String[] info = policyStr.split("\n");

        QueryMap queryMap = new QueryMap();
        try {
            if (info.length == 2) {
                int idx = info[1].indexOf("\\n");
                String tmp = info[1].substring(0, idx);
                String ids = info[1].substring(idx + 2);
                queryMap.put("tmp", Integer.valueOf(tmp));
                String[] tmpStr = ids.split(",");
                int[] ints = new int[tmpStr.length];
                for (int i = 0; i < tmpStr.length; i++) {
                    ints[i] = Integer.valueOf(tmpStr[i]);
                }
                queryMap.put("ids", ints);
            } else if (info.length == 3) {
                queryMap.put("tmp", Integer.valueOf(info[1]));
                String[] tmpStr = info[2].split(",");
                int[] ints = new int[tmpStr.length];
                for (int i = 0; i < tmpStr.length; i++) {
                    ints[i] = Integer.valueOf(tmpStr[i]);
                }
                queryMap.put("ids", ints);
            } else {
                return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }

        //验证是否超时
        Message message = verifyUtil.verifyTime(info[0], schema);
        if (message != null)
            return message;

        ossFileService.changeTmp(queryMap);

        return Message.pass(schema.getCallback());
    }

    @Override
    public Message adminFileDelete(Integer id) {
        if (!NumberUtil.hasEmpty(id)) {

            OssFile ossFile = ossFileService.getOssFile(id);
            if (ossFile == null) {
                return Message.error(Info.FILE_EMPTY.getInfo());
            }
            ossFileService.delete(ossFile);
            taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.DELETE)));
            List<OssDeleteFileLog> ossDeleteFileLogs = new ArrayList<>();
            ossDeleteFileLogs.add(new OssDeleteFileLog(ossFile));
            taskExecutor.execute(new OssFileDeleteLogTask(ossDeleteFileLogService, ossDeleteFileLogs));
            return Message.pass();
        } else {
            return Message.error(Info.FILE_EMPTY.getInfo());
        }
    }
}
