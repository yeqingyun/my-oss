package com.gionee.oss.util;

import com.gionee.gnif.file.util.CalcUtil;
import com.gionee.gnif.file.util.FileUtil;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.cache.Cache;
import com.gionee.oss.constant.FileInfo;
import com.gionee.oss.constant.Info;
import com.gionee.oss.dto.AdminUploadSchema;
import com.gionee.oss.dto.Schema;
import com.gionee.oss.dto.UploadSchema;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yeqy on 2017/6/7.
 */
public class VerifyUtil {
    private final Logger logger = Logger.getLogger(VerifyUtil.class);
    @Autowired
    private OssFileService ossFileService;
    @Autowired
    private UploadUtil uploadUtil;
    @Autowired
    private Cache<String, OssTripartiteSystem> systemKeyCache;

    public final int OVERTIME = 30000;

    /**
     * 判断参数，验证权限
     *
     * @param schema
     * @return
     * @throws Exception
     */
    public Message verifySchema(UploadSchema schema) throws UnsupportedEncodingException {
        if (StringUtil.hasEmpty(schema.getCode(), schema.getSignature(), schema.getFileInfo())) {//缺少参数
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        OssTripartiteSystem ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());
        if (ossTripartiteSystem == null) {//系统编码不存在
            return Message.error(Info.PERSMISSION_DENIED.getInfo(), schema.getCallback());
        }
        String fileInfoStr = CalcUtil.getFromBase64(schema.getFileInfo());//base64解码

        if (!CalcUtil.getBase64(CalcUtil.hamcsha1(fileInfoStr, ossTripartiteSystem.getKey())).toLowerCase().equals(schema.getSignature().toLowerCase())) {//签名错误
            return Message.error(Info.TAMPER_INFO.getInfo(), schema.getCallback());
        }
        FileInfo fileInfo;
        try {
            fileInfo = EncryptUtil.decodeFileInfo(fileInfoStr);
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }

        if (fileInfo == null || !fileInfo.isSafe()) {//文件信息有误
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }

        long subTime = fileInfo.getDate().getTime() - System.currentTimeMillis();
        if (Math.abs(subTime) > OVERTIME) {//时间差5分钟以上
            return Message.error(Info.OVERTIME.getInfo(), schema.getCallback());
        }


        OssFile file = ossFileService.getByMd5AndSize(fileInfo.getFileMd5(), fileInfo.getFileSize());
        if (file != null) {//数据库中文件已存在
            if (deleteFileIfExsits(file, schema.getTmp(), schema.getFileName()))
                return null;

            Map<String, Object> map = new HashMap();
            map.put("fileNo", file.getId());
            map.put("fileName", file.getName());
            map.put("fileSize", file.getSize());
            return Message.repeatUpload(schema.getCallback(), map);
        }


        return null;
    }


    public Message verifySchema(AdminUploadSchema schema) {
        if (StringUtil.hasEmpty(schema.getFileMd5(), schema.getFileName()) || NumberUtil.hasEmpty(schema.getFileSize())) {
            return Message.error(Info.EINVAL.getInfo());
        }

        OssFile file = ossFileService.getByMd5AndSize(schema.getFileMd5(), schema.getFileSize());
        if (file != null) {//数据库中文件已存在
            if (deleteFileIfExsits(file, schema.getTmp(), schema.getFileName()))
                return null;

            Map<String, Object> map = new HashMap();
            map.put("ossFile", file);
            return Message.repeatUpload(map);
        }


        return null;
    }


    public Message verifyTime(String time, Schema schema) {
        try {
            long subTime = com.gionee.gnif.file.util.DateUtil.getGMTFromString(time).getTime() - System.currentTimeMillis();
            if (Math.abs(subTime) > OVERTIME) {//时间差5分钟以上
                return Message.error(Info.OVERTIME.getInfo(), schema.getCallback());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        return null;
    }

    /**
     * 验证文件信息
     *
     * @param fileInfo
     * @param schema
     * @param multipartFile
     * @return
     * @throws Exception
     */
    public Message verifyFileInfo(FileInfo fileInfo, MultipartFile multipartFile, UploadSchema schema) throws Exception {
        if (fileInfo.getChunkSize() != multipartFile.getSize() ||
                !fileInfo.getChunkMd5().toLowerCase().equals(FileUtil.getFileMd5(multipartFile).toLowerCase())) {//对比文件信息
            return Message.error(Info.TAMPER_INFO.getInfo(), schema.getCallback());
        }
        return null;
    }


    private boolean deleteFileIfExsits(OssFile file, int tmp, String fileName) {
        if (new File(file.getPath()).exists()) {//文件实体存在
            if (logger.isDebugEnabled())
                logger.debug("文件[" + fileName + "]已存在");

            uploadUtil.checkTmpFile(tmp, file);
            return false;
        } else {//文件实体不存在
            ossFileService.removeOssFile(file.getId());
            return true;
        }
    }
}
