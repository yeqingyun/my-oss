package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.file.util.CalcUtil;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.service.OssCommonUploadService;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.constant.*;
import com.gionee.oss.dto.UploadSchema;
import com.gionee.oss.task.OssDateLogTask;
import com.gionee.oss.util.EncryptUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yeqy on 2017/6/6.
 */
@Service
public class OssCommonUploadServiceImpl implements OssCommonUploadService {
    private final Logger logger = Logger.getLogger(OssCommonUploadServiceImpl.class);
    @Autowired
    private OssFileService ossFileService;
    @Autowired
    private OssDateLogService ossDateLogService;
    @Autowired
    @Qualifier("threadPool")
    private TaskExecutor taskExecutor;

    @Override
    public Message upload(UploadSchema schema, Message message) throws UnsupportedEncodingException, ParseException {
        if (message != null)
            return message;

        OssFile ossFile;

        if (schema.getTmp() != TmpFile.UNTMP_FILE.getType()) {
            schema.setTmp(TmpFile.TMP_FILE.getType());
        }

        if (schema.getStep() != 0) {
            ossFile = new OssFile(schema.getCode(), schema.getFileName(), schema.getFileMd5(), schema.getFileSize().intValue(), schema.getSavePath(), schema.getTmp(), 1);
            OssFile file = ossFileService.getByMd5AndSize(schema.getFileMd5(), schema.getFileSize().intValue());
            if (file == null) {
                ossFileService.save(ossFile);
            }
            if(logger.isDebugEnabled())
                logger.debug(schema.getCode() + "上传了" + schema.getFileName());
        } else {
            FileInfo fileInfo = EncryptUtil.decodeFileInfo(CalcUtil.getFromBase64(schema.getFileInfo()));
            ossFile = new OssFile(schema.getCode(), fileInfo.getFileName(), fileInfo.getFileMd5(), fileInfo.getFileSize().intValue(), schema.getSavePath(), schema.getTmp(), 1);
            if(logger.isDebugEnabled())
                logger.debug(schema.getCode() + "上传了" + fileInfo.getFileName());
            ossFileService.save(ossFile);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("fileNo", ossFile.getId());
        map.put("fileSize", ossFile.getSize());
        map.put("fileName", ossFile.getName());


        taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(schema.getTmp() == TmpFile.UNTMP_FILE.getType() ? HandleType.UPLOAD : HandleType.UPLOAD_TMP)));

        return Message.pass(schema.getCallback(), map, Info.UPLOAD_SUCCESS.getInfo());
    }

}
