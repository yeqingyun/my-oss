package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.file.util.CalcUtil;
import com.gionee.gnif.file.util.FileUtil;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.model.OssDeleteFileLog;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssCommonUploadService;
import com.gionee.oss.biz.service.OssCoreUploadFileService;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.cache.Cache;
import com.gionee.oss.constant.*;
import com.gionee.oss.dto.AdminUploadSchema;
import com.gionee.oss.dto.UploadSchema;
import com.gionee.oss.integration.dao.OssDeleteFileLogDao;
import com.gionee.oss.integration.dao.OssFileDao;
import com.gionee.oss.task.OssDateLogTask;
import com.gionee.oss.util.EncryptUtil;
import com.gionee.oss.util.NumberUtil;
import com.gionee.oss.util.StringUtil;
import com.gionee.oss.util.VerifyUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Created by yeqy on 2017/5/27.
 */
public class OssCoreUploadFileServiceImpl implements OssCoreUploadFileService {
    private final Logger logger = Logger.getLogger(OssCoreUploadFileServiceImpl.class);
    @Autowired
    private PropertiesConfig propertyConfig;
    @Autowired
    private OssCommonUploadService ossCommonUploadService;
    @Autowired
    private OssFileDao ossFileDao;
    @Autowired
    private OssDeleteFileLogDao ossDeleteFileLogDao;
    @Autowired
    private OssDateLogService ossDateLogService;
    @Autowired
    private Cache<String, OssTripartiteSystem> systemKeyCache;
    @Autowired
    private VerifyUtil verifyUtil;
    @Autowired
    @Qualifier("threadPool")
    private TaskExecutor taskExecutor;

    @Override
    public Message fileExist(UploadSchema schema) throws UnsupportedEncodingException {
        Message message = verifyUtil.verifySchema(schema);
        if (message != null)
            return message;
        return Message.pass(schema.getCallback());
    }

    @Override
    public Message chunkFileExist(UploadSchema schema) {
        String base = propertyConfig.getRootPath();
        //文件信息不全
        if (StringUtil.hasEmpty(schema.getFileName(), schema.getFileMd5()) || NumberUtil.hasEmpty(schema.getChunkOrder(), schema.getFileSize())) {
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        String filePath = new StringBuilder(base).append(PropertiesConfig.fileSeparator).append(schema.getFileMd5()).append(PropertiesConfig.directorySeparator).append(schema.getFileSize()).toString();
        if (!FileUtil.exist(filePath)) {//文件临时目录不存在
            return Message.pass(schema.getCallback());
        } else {
            String cacheFilePath = new StringBuilder(filePath).append(PropertiesConfig.fileSeparator).append(schema.getChunkOrder()).toString();
            if (!FileUtil.exist(cacheFilePath)) {//文件临时目录存在，文件块不存在
                return Message.pass(schema.getCallback());
            } else {
                File file = new File(cacheFilePath);
                long currentLength = file.length();
                if (currentLength == schema.getChunkSize()) {//文件临时目录存在，文件块存在且长度一致
                    return Message.repeatUpload(schema.getCallback(), null);
                } else {//文件临时目录存在，文件块不存在但长度一致
                    file.delete();
                    return Message.pass(schema.getCallback());
                }
            }
        }
    }

    @Override
    public Message chunkFileUpload(UploadSchema schema, HttpServletRequest request) throws Exception {
        //参数不全
        if (StringUtil.hasEmpty(schema.getFileInfo(), schema.getSignature(), schema.getCode(), schema.getChunk())) {
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }

        OssTripartiteSystem ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());
        if (ossTripartiteSystem == null) {//系统编码不存在
            return Message.error(Info.PERSMISSION_DENIED.getInfo(), schema.getCallback());
        }

        String fileInfoStr = CalcUtil.getFromBase64(schema.getFileInfo());//base64解码

        if (!CalcUtil.getBase64(CalcUtil.hamcsha1(fileInfoStr, ossTripartiteSystem.getKey())).equals(schema.getSignature())) {//签名错误
            return Message.error(Info.PERSMISSION_DENIED.getInfo(), schema.getCallback());
        }
        FileInfo fileInfo;
        try {
            fileInfo = EncryptUtil.decodeFileInfo(fileInfoStr);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        if (fileInfo == null || !fileInfo.isSafe()) {//文件信息有误
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        long subTime = fileInfo.getDate().getTime() - System.currentTimeMillis();
        if (subTime > 300000 || subTime < -300000) {//时间差5分钟以上
            return Message.error(Info.OVERTIME.getInfo(), schema.getCallback());
        }

        HttpServletRequest processedRequest = propertyConfig.getMultipleReuqest(request);
        MultipartFile multipartFile = null;

        try {
            Map<String, MultipartFile> MultipartFileMap = ((DefaultMultipartHttpServletRequest) processedRequest).getFileMap();
            for (Map.Entry<String, MultipartFile> entry : MultipartFileMap.entrySet()) {
                multipartFile = entry.getValue();
            }
        } catch (Exception e) {//MultipartFile转换失败
            logger.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo(), schema.getCallback());
        }

        if (multipartFile == null || multipartFile.getSize() <= 0) {//文件为空
            return Message.error(Info.FILE_EMPTY.getInfo(), schema.getCallback());
        }
        //TODO 多线程优化 --1.计算文件块md5 2.上传文件
        //验证文件信息
        Message message = verifyUtil.verifyFileInfo(fileInfo, multipartFile, schema);
        if (message != null)
            return message;


        String base = propertyConfig.getRootPath();
        try {
            if (!StringUtil.hasEmpty(fileInfo.getFileMd5(), schema.getChunk())) {
                String dirStr = new StringBuilder(base).append(PropertiesConfig.fileSeparator).append(fileInfo.getFileMd5()).append(PropertiesConfig.directorySeparator).append(fileInfo.getFileSize()).toString();
                File fileDir = new File(dirStr);
                if (!fileDir.exists()) {
                    fileDir.mkdir();
                }
                File chunkFile = new File(new StringBuilder(dirStr).append(PropertiesConfig.fileSeparator).append(schema.getChunk()).toString());
                multipartFile.transferTo(chunkFile);
                return Message.pass(schema.getCallback());
            } else {
                return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo(), schema.getCallback());
        }



        /*  //多线程优化后的速度不如NIO优化过的IO操作
        long start = System.currentTimeMillis();
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        Message messageMd5 = null;
        Message messageUpload = null;

        String base = propertyConfig.getRootPath();

        ThreadGroup threadGroup = new ThreadGroup("block Task");


        InputStream inputStream = multipartFile.getInputStream();
        InputStream bakOne = null;
        InputStream bakTwo;
        ByteArrayOutputStream outputStream = null;
        try {
            //inputStream 重用
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            byte[] bytes = outputStream.toByteArray();
            bakOne = new BufferedInputStream(new ByteArrayInputStream(bytes));
            bakTwo = new BufferedInputStream(new ByteArrayInputStream(bytes));


            Md5Task md5Task = new Md5Task(threadGroup, messageMd5, fileInfo, schema, bakOne, verifyUtil, countDownLatch);
            ChunkUploadTask chunkUploadTask = new ChunkUploadTask(threadGroup, base, messageUpload, fileInfo, schema, bakTwo, countDownLatch);

            threadPool.execute(md5Task);
            threadPool.execute(chunkUploadTask);
            countDownLatch.await();

            messageUpload = chunkUploadTask.getMessage();
            messageMd5 = md5Task.getMessage();

            System.out.println((System.currentTimeMillis()-start));
            if (messageUpload == null && messageMd5 == null) {
                return Message.pass(schema.getCallback());
            } else {
                if (messageMd5 != null)
                    return messageMd5;
                else
                    return messageUpload;
            }
        } finally {//释放资源
            try {

                if (bakOne != null)
                    bakOne.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Message mergeChunkFile(UploadSchema schema, Message message) throws UnsupportedEncodingException, ParseException {
        return ossCommonUploadService.upload(schema, message);
    }


    //管理员上传接口


    @Override
    public Message fileExistWithOutAuth(AdminUploadSchema schema) {
        Message message = verifyUtil.verifySchema(schema);
        if (message != null && !message.getIsSuccess()) {
            return message;
        } else if (message != null) {//文件重复
            Map<String, Object> map = message.getAttributes();
            Object object = map.get("ossFile");
            if (object != null && object instanceof OssFile) {
                OssFile ossFile = (OssFile) object;


                if (!NumberUtil.hasEmpty(schema.getFileId())) {//替换文件
                    OssFile file = ossFileDao.get(schema.getFileId());
                    String oldPath = file.getPath();
                    String oldMd5 = file.getMd5();
                    Integer oldSize = file.getSize();

                    if (ossFile.getMd5().toLowerCase().equals(file.getMd5().toLowerCase()) && ossFile.getSize().intValue() == file.getSize().intValue()) {
                        return message;
                    }
                    file.setPath(ossFile.getPath());
                    file.setName(ossFile.getName());
                    file.setMd5(ossFile.getMd5());
                    file.setSize(ossFile.getSize());
                    if (ossFileDao.update(file) >= 1) {//删除被替换的文件实体
                        deleteWhenSingle(oldMd5, oldSize, oldPath);
                    }
                } else if (!NumberUtil.hasEmpty(schema.getDeleteFileId())) {//恢复文件
                    OssDeleteFileLog ossDeleteFileLog = ossDeleteFileLogDao.get(schema.getDeleteFileId());
                    OssFile file = new OssFile(ossDeleteFileLog.getFileId(), ossDeleteFileLog.getSystemCode(), ossFile.getName(),
                            ossFile.getMd5(), ossFile.getSize(), ossFile.getPath(), ossDeleteFileLog.getFileTmp(), ossDeleteFileLog.getFileRefer());
                    if (ossFileDao.insertWithId(file) >= 1) {
                        ossDeleteFileLogDao.delete(ossDeleteFileLog.getId());
                        taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.UPLOAD)));
                    }
                }
                return message;

            }
        }


        return Message.pass();
    }


    @Override
    public Message adminUploadChunkFileExist(AdminUploadSchema schema) {
        if (StringUtil.hasEmpty(schema.getFileMd5())) {
            Message.error(Info.EINVAL.getInfo());
        }
        if (NumberUtil.hasEmpty(schema.getFileSize(), schema.getChunkOrder()) || schema.getFileSize() <= 0 || schema.getChunkOrder() <= 0) {
            Message.error(Info.EINVAL.getInfo());
        }
        String base = propertyConfig.getRootPath();
        String filePath = new StringBuilder(base).append(PropertiesConfig.fileSeparator).append(schema.getFileMd5()).append(PropertiesConfig.directorySeparator).append(schema.getFileSize()).toString();
        if (!FileUtil.exist(filePath)) {//文件临时目录不存在
            return Message.pass();
        } else {
            String cacheFilePath = new StringBuilder(filePath).append(PropertiesConfig.fileSeparator).append(schema.getChunkOrder()).toString();
            if (!FileUtil.exist(cacheFilePath)) {//文件临时目录存在，文件块不存在
                return Message.pass();
            } else {
                File file = new File(cacheFilePath);
                long currentLength = file.length();
                if (currentLength == schema.getChunkSize()) {//文件临时目录存在，文件块存在且长度一致
                    return Message.repeatUpload(null);
                } else {//文件临时目录存在，文件块不存在但长度一致
                    file.delete();
                    return Message.pass();
                }
            }
        }
    }

    @Override
    public Message adminUploadChunkFileUpload(AdminUploadSchema schema, HttpServletRequest request) {
        HttpServletRequest processedRequest = propertyConfig.getMultipleReuqest(request);
        MultipartFile multipartFile = null;

        try {
            Map<String, MultipartFile> MultipartFileMap = ((DefaultMultipartHttpServletRequest) processedRequest).getFileMap();
            for (Map.Entry<String, MultipartFile> entry : MultipartFileMap.entrySet()) {
                multipartFile = entry.getValue();
            }
        } catch (Exception e) {//MultipartFile转换失败
            logger.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }

        if (multipartFile == null) {
            return Message.error(Info.FILE_EMPTY.getInfo());
        }

        String base = propertyConfig.getRootPath();
        try {
            if (!StringUtil.hasEmpty(schema.getFileMd5(), schema.getChunk())) {
                String dirStr = new StringBuilder(base).append(PropertiesConfig.fileSeparator).append(schema.getFileMd5()).append(PropertiesConfig.directorySeparator).append(schema.getFileSize()).toString();
                File fileDir = new File(dirStr);
                if (!fileDir.exists()) {
                    fileDir.mkdir();
                }
                File chunkFile = new File(new StringBuilder(dirStr).append(PropertiesConfig.fileSeparator).append(schema.getChunk()).toString());
                multipartFile.transferTo(chunkFile);
                return Message.pass();
            } else {
                return Message.error(Info.EINVAL.getInfo());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }
    }

    @Override
    @Transactional
    public Message adminUploadFile(AdminUploadSchema schema, Message message) {

        if (!NumberUtil.hasEmpty(schema.getFileId())) {//替换文件
            return adminReplaceFile(schema, message);
        } else if (!NumberUtil.hasEmpty(schema.getDeleteFileId())) {//恢复文件
            return adminRecoverFile(schema, message);
        } else if (!StringUtil.hasEmpty(schema.getFileMd5(), schema.getFileName(), schema.getCode()) && !NumberUtil.hasEmpty(schema.getFileSize())) {//上传新文件
            return adminUploadNewFile(schema, message);
        } else {
            return Message.error(Info.EINVAL.getInfo());
        }
    }


    private Message adminReplaceFile(AdminUploadSchema schema, Message message) {
        if (message != null) {
            Map<String, Object> attributes = message.getAttributes();
            if (message.getIsSuccess() && attributes != null) {
                Object ossFile = attributes.get("ossFile");
                if (ossFile != null && ossFile instanceof OssFile) {
                    OssFile file = (OssFile) ossFile;//被替换的文件
                    String oldMd5 = file.getMd5();
                    Integer oldSize = file.getSize();
                    String oldPath = file.getPath();
                    file.setPath(schema.getSavePath());
                    if (!StringUtil.hasEmpty(schema.getFileName(), schema.getFileMd5()) && !NumberUtil.hasEmpty(schema.getFileSize())) {
                        file.setName(schema.getFileName());
                        file.setMd5(schema.getFileMd5());
                        file.setSize(schema.getFileSize());
                    }
                    if (ossFileDao.update(file) >= 1) {
                        //只有这一个文件时删除
                        deleteWhenSingle(oldMd5, oldSize, oldPath);

                        return Message.pass();
                    }
                }
            } else if (message.getIsSuccess()) {
                OssFile file = ossFileDao.get(schema.getFileId());//被替换的文件
                if (file == null)
                    return Message.error(Info.FILE_NOTEXIST.getInfo());
                String oldPath = file.getPath();
                String oldMd5 = file.getMd5();
                Integer oldSize = file.getSize();
                file.setPath(schema.getSavePath());
                if (!StringUtil.hasEmpty(schema.getFileName(), schema.getFileMd5()) && !NumberUtil.hasEmpty(schema.getFileSize())) {
                    file.setName(schema.getFileName());
                    file.setMd5(schema.getFileMd5());
                    file.setSize(schema.getFileSize());
                }
                if (ossFileDao.update(file) >= 1) {
                    deleteWhenSingle(oldMd5, oldSize, oldPath);
                    return Message.pass();
                }
            } else {
                return message;
            }
        }

        return message;
    }

    private void deleteWhenSingle(String oldMd5, Integer oldSize, String oldPath) {
        List<OssFile> ossFiles = ossFileDao.getByMd5AndSize(oldMd5, oldSize);
        if (ossFiles.size() == 0)
            new File(oldPath).delete();
    }


    private Message adminRecoverFile(AdminUploadSchema schema, Message message) {
        if (message != null) {
            Map<String, Object> attributes = message.getAttributes();
            if (message.getIsSuccess() && attributes != null) {
                Object ossFileLog = attributes.get("ossDeleteFileLog");
                if (ossFileLog != null && ossFileLog instanceof OssDeleteFileLog) {
                    OssDeleteFileLog ossDeleteFileLog = (OssDeleteFileLog) ossFileLog;
                    OssFile ossFile = new OssFile(ossDeleteFileLog.getFileId(), ossDeleteFileLog.getSystemCode(), schema.getFileName(),
                            schema.getFileMd5(), schema.getFileSize(), schema.getSavePath(), ossDeleteFileLog.getFileTmp(), ossDeleteFileLog.getFileRefer());
                    if (ossFileDao.insertWithId(ossFile) >= 1) {
                        ossDeleteFileLogDao.delete(ossDeleteFileLog.getId());
                        taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.UPLOAD)));
                        return Message.pass();
                    }

                }
            } else if (message.getIsSuccess()) {
                OssDeleteFileLog ossDeleteFileLog = ossDeleteFileLogDao.get(schema.getDeleteFileId());
                if (ossDeleteFileLog == null)
                    return Message.error(Info.FILE_NOTEXIST.getInfo());
                OssFile file = new OssFile(ossDeleteFileLog.getFileId(), ossDeleteFileLog.getSystemCode(), schema.getFileName(),
                        schema.getFileMd5(), schema.getFileSize(), schema.getSavePath(), ossDeleteFileLog.getFileTmp(), ossDeleteFileLog.getFileRefer());
                if (ossFileDao.insertWithId(file) >= 1) {
                    ossDeleteFileLogDao.delete(ossDeleteFileLog.getId());
                    taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.UPLOAD)));
                    return Message.pass();
                }
            } else {
                return message;
            }
        }

        return message;
    }

    private Message adminUploadNewFile(AdminUploadSchema schema, Message message) {
        if (message != null)
            return message;
        if (schema.getTmp() != TmpFile.UNTMP_FILE.getType()) {
            schema.setTmp(TmpFile.TMP_FILE.getType());
        }
        OssFile ossFile = new OssFile(schema.getCode(), schema.getFileName(),
                schema.getFileMd5(), schema.getFileSize(), schema.getSavePath(), schema.getTmp(), schema.getRefer());
        if (ossFileDao.insert(ossFile) == 1) {
            taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(schema.getTmp() == TmpFile.UNTMP_FILE.getType() ? HandleType.UPLOAD : HandleType.UPLOAD_TMP)));
            return Message.pass();
        }
        return Message.error(Info.SYSTEM_ERROR.getInfo());
    }


}
