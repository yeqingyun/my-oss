package com.gionee.oss.util;

import com.gionee.gnif.file.util.CalcUtil;
import com.gionee.gnif.file.util.FileUtil;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.model.OssDeleteFileLog;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssDeleteFileLogService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.cache.Cache;
import com.gionee.oss.constant.FileInfo;
import com.gionee.oss.constant.Info;
import com.gionee.oss.constant.PropertiesConfig;
import com.gionee.oss.constant.TmpFile;
import com.gionee.oss.dto.AdminUploadSchema;
import com.gionee.oss.dto.UploadSchema;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;

/**
 * Created by yeqy on 2017/6/7.
 */
public class UploadUtil {
    private final Logger logger = Logger.getLogger(UploadUtil.class);
    @Autowired
    private Cache<String, OssTripartiteSystem> systemKeyCache;
    @Autowired
    private VerifyUtil verifyUtil;
    @Autowired
    private PropertiesConfig propertyConfig;
    @Autowired
    private OssFileService ossFileService;
    @Autowired
    private OssDeleteFileLogService ossDeleteFileLogService;


    private Message fileParamUplaod(UploadSchema schema) throws IOException, ParseException {
        byte[] bs;
        FileInfo fileInfo = EncryptUtil.decodeFileInfo(CalcUtil.getFromBase64(schema.getFileInfo()));
        if (fileInfo == null || !fileInfo.isSafe()) {//文件信息有误
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }
        bs = Base64.encodeBase64(schema.getF().getBytes("UTF-8"));
        OssTripartiteSystem ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());

        StringBuilder saveDir = buildSavePath(schema, ossTripartiteSystem);
        if (saveDir == null) {
            return Message.error(Info.SAVEPATH_NOTDIR.getInfo(), schema.getCallback());
        }

        String targetFilePath = new StringBuilder(saveDir).append(PropertiesConfig.fileSeparator).append(fileInfo.getFileName()).toString();
        Path targetFile = Paths.get(targetFilePath);

        schema.setSavePath(targetFilePath);

        if (Files.exists(targetFile)) {//如果文件已存在，则生成文件名为 当前时间戳+原文件名
            String savePath = saveDir.append(PropertiesConfig.fileSeparator).append(System.currentTimeMillis()).append(fileInfo.getFileName()).toString();
            targetFile = Paths.get(savePath);
            schema.setSavePath(savePath);
        }

        targetFile = Files.createFile(targetFile);
        try (OutputStream out = Files.newOutputStream(targetFile);
             BufferedOutputStream bos = new BufferedOutputStream(out)) {
            bos.write(bs);
        } finally {
            bs = null;
        }
        return null;
    }

    public Message commonUpload(UploadSchema schema, HttpServletRequest request) throws Exception {
        Message message = verifyUtil.verifySchema(schema);//判断参数，验证权限
        if (message != null)
            return message;

//        if(StringUtil.isNotEmpty(schema.getF())){//通过f参数上传
//            return fileParamUplaod(schema);
//        }

        HttpServletRequest processedRequest = propertyConfig.getMultipleReuqest(request);
        MultipartFile multipartFile = null;

        try {
            Map<String, MultipartFile> MultipartFileMap = ((DefaultMultipartHttpServletRequest) processedRequest).getFileMap();
            for (Map.Entry<String, MultipartFile> entry : MultipartFileMap.entrySet()) {
                multipartFile = entry.getValue();
            }
        } catch (Exception e) {//MultipartFile转换失败
            return Message.error(Info.SYSTEM_ERROR.getInfo(), schema.getCallback());
        }


        FileInfo fileInfo = EncryptUtil.decodeFileInfo(CalcUtil.getFromBase64(schema.getFileInfo()));

        if (fileInfo == null || !fileInfo.isSafe()) {//文件信息有误
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        }

        if (multipartFile == null) {//文件为空
            return Message.error(Info.FILE_EMPTY.getInfo(), schema.getCallback());
        }

        if (multipartFile.getSize() > propertyConfig.getFileSizeMax()) {//超过最大分片大小
            return Message.error(Info.FILE_MORETHANFIFTY.getInfo(), schema.getCallback());
        }

        if (fileInfo.getFileSize() != multipartFile.getSize() || !fileInfo.getFileMd5().toLowerCase().equals(FileUtil.getFileMd5(multipartFile).toLowerCase())) {//对比文件信息
            return Message.error(Info.TAMPER_INFO.getInfo(), schema.getCallback());
        }

        OssTripartiteSystem ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());

        StringBuilder saveDir = buildSavePath(schema, ossTripartiteSystem);
        if (saveDir == null) {
            return Message.error(Info.SAVEPATH_NOTDIR.getInfo(), schema.getCallback());
        }

        Path targetFile = getPath(schema, fileInfo, saveDir);

        doUpload(multipartFile, saveDir, targetFile);

        return null;
    }

    private void doUpload(MultipartFile multipartFile, StringBuilder saveDir, Path targetFile) throws IOException {
        try {
            multipartFile.transferTo(targetFile.toFile());
        } catch (FileNotFoundException e) {
            Files.createDirectories(Paths.get(saveDir.toString()));
            multipartFile.transferTo(targetFile.toFile());
        }
    }

    private Path getPath(UploadSchema schema, FileInfo fileInfo, StringBuilder saveDir) {
        String targetFilePath = new StringBuilder(saveDir).append(PropertiesConfig.fileSeparator).append(fileInfo.getFileName()).toString();
        Path targetFile = Paths.get(targetFilePath);
        schema.setSavePath(targetFilePath);
        if (Files.exists(targetFile)) {//如果文件已存在，则生成文件名为 当前时间戳+原文件名
            String savePath = saveDir.append(PropertiesConfig.fileSeparator).append(System.currentTimeMillis()).append(fileInfo.getFileName()).toString();
            targetFile = Paths.get(savePath);
            schema.setSavePath(savePath);
        }
        return targetFile;
    }


    public Message commonUpload(AdminUploadSchema schema, HttpServletRequest request) throws Exception {
        Message message = null;
        OssFile file = ossFileService.getByMd5AndSize(schema.getFileMd5(), schema.getFileSize());
        if (file != null) {//数据库中文件已存在
            if (new File(file.getPath()).exists()) {//文件实体存在
                if (logger.isDebugEnabled())
                    logger.debug("文件[" + schema.getFileName() + "]已存在");

                //检查重复
                checkTmpFile(schema.getTmp(), file);

                schema.setSavePath(file.getPath());
                return Message.pass();
            } else {//文件实体不存在
                ossFileService.removeOssFile(file.getId());
                return null;
            }
        }


        HttpServletRequest processedRequest = propertyConfig.getMultipleReuqest(request);
        MultipartFile multipartFile = null;
        try {
            Map<String, MultipartFile> MultipartFileMap = ((DefaultMultipartHttpServletRequest) processedRequest).getFileMap();
            for (Map.Entry<String, MultipartFile> entry : MultipartFileMap.entrySet()) {
                multipartFile = entry.getValue();
            }
        } catch (Exception e) {//MultipartFile转换失败
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }

        if (multipartFile == null) {
            return Message.error(Info.FILE_EMPTY.getInfo());
        }

        StringBuilder saveDir;
        OssTripartiteSystem ossTripartiteSystem = null;
        if (StringUtil.isEmpty(schema.getCode())) {
            if (!NumberUtil.hasEmpty(schema.getFileId())) {//替换文件

                OssFile ossFile = ossFileService.getOssFile(schema.getFileId());
                if (ossFile == null) {
                    return Message.error(Info.FILE_NOTEXIST.getInfo());
                } else {
                    message = Message.pass();
                    Map<String, Object> ossFileMap = new HashMap<>();
                    ossFileMap.put("ossFile", ossFile);
                    message.setAttributes(ossFileMap);
                }
                ossTripartiteSystem = systemKeyCache.getAndSysnc(ossFile.getSystemCode());

            } else if (!NumberUtil.hasEmpty(schema.getDeleteFileId())) {//恢复文件
                OssDeleteFileLog ossDeleteFileLog = ossDeleteFileLogService.getOssDeleteFileLog(schema.getDeleteFileId());
                if (ossDeleteFileLog == null) {
                    return Message.error(Info.FILE_NOTEXIST.getInfo());
                } else {
                    message = Message.pass();
                    Map<String, Object> ossFileMap = new HashMap<>();
                    ossFileMap.put("ossDeleteFileLog", ossDeleteFileLog);
                    message.setAttributes(ossFileMap);
                }
                ossTripartiteSystem = systemKeyCache.getAndSysnc(ossDeleteFileLog.getSystemCode());
            }
        } else {//新增文件
            ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());
        }

        if (ossTripartiteSystem == null) {
            return Message.error(Info.PERSMISSION_DENIED.getInfo());
        }

        if (!StringUtil.isEmpty(schema.getSavePath())) {//savepath 自定义路径
            saveDir = new StringBuilder(ossTripartiteSystem.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(schema.getSavePath());
            Path fileEnty = Paths.get(saveDir.toString());
            if (!Files.exists(fileEnty) && !fileEnty.toFile().mkdirs()) {
                return Message.error(Info.SAVEPATH_NOTDIR.getInfo());
            }
        } else {//系统默认路径
            saveDir = new StringBuilder(ossTripartiteSystem.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(DateUtil.getCurrentYearMonth());
        }
        String targetFilePath = new StringBuilder(saveDir).append(PropertiesConfig.fileSeparator).append(schema.getFileName()).toString();
        schema.setSavePath(targetFilePath);
        Path targetFile = Paths.get(targetFilePath);
        if (Files.exists(targetFile)) {//如果文件已存在，则生成文件名为 当前时间戳+原文件名
            String savePath = saveDir.append(PropertiesConfig.fileSeparator).append(System.currentTimeMillis()).append(schema.getFileName()).toString();
            targetFile = Paths.get(savePath);
            schema.setSavePath(savePath);
        }
        multipartFile.transferTo(targetFile.toFile());

        if (!NumberUtil.hasEmpty(schema.getFileId()) || !NumberUtil.hasEmpty(schema.getDeleteFileId())) {
            return message;
        }

        return null;
    }


    public Message mergeChunk(UploadSchema schema) throws Exception {
        String base = propertyConfig.getRootPath();
        if (StringUtil.hasEmpty(schema.getFileMd5(), schema.getFileName(), schema.getCode()) || NumberUtil.hasEmpty(schema.getFileSize())) {
            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
        } else {
            OssTripartiteSystem ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());
            StringBuilder saveDir;
            if (!StringUtil.isEmpty(schema.getSavePath())) {//savepath 自定义路径
                saveDir = new StringBuilder(ossTripartiteSystem.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(schema.getSavePath());
                if (createSavePath(saveDir)) return Message.error(Info.SAVEPATH_NOTDIR.getInfo(), schema.getCallback());
            } else {
                saveDir = new StringBuilder(ossTripartiteSystem.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(DateUtil.getCurrentYearMonth());
            }


            Path targetFile = getPath(schema, saveDir);

            //得到所有文件块
            String srcDirFilePath = new StringBuilder(base).append(PropertiesConfig.fileSeparator).append(schema.getFileMd5()).append(PropertiesConfig.directorySeparator).append(schema.getFileSize()).toString();
            Path srcDirFile = Paths.get(srcDirFilePath);
            DefaultFileVisitor defaultFileVisitor = new DefaultFileVisitor();
            Files.walkFileTree(srcDirFile, defaultFileVisitor);
            List<Path> paths = defaultFileVisitor.getFiles();

            return doMerge(schema, targetFile, srcDirFile, paths);

        }
    }

    private boolean createSavePath(StringBuilder saveDir) throws IOException {
        Path file = Paths.get(saveDir.toString());
        if (!Files.isDirectory(file)) {
            return true;
        }
        if (!Files.exists(file)) {
            Files.createDirectories(file);
        }
        return false;
    }

    private Path getPath(UploadSchema schema, StringBuilder saveDir) {
        String targetFilePath = new StringBuilder(saveDir).append(PropertiesConfig.fileSeparator).append(schema.getFileName()).toString();
        schema.setSavePath(targetFilePath);
        Path targetFile = Paths.get(targetFilePath);
        if (Files.exists(targetFile)) {
            String savePath = saveDir.append(PropertiesConfig.fileSeparator).append(System.currentTimeMillis()).append(schema.getFileName()).toString();
            targetFile = Paths.get(savePath);
            schema.setSavePath(savePath);
        }
        return targetFile;
    }

    private Message doMerge(UploadSchema schema, Path targetFile, Path srcDirFile, List<Path> paths) throws IOException, NoSuchAlgorithmException {
        if (Files.isDirectory(srcDirFile) && Files.exists(srcDirFile)) {
            long start = System.currentTimeMillis();
            //合并文件
            mergeFile(paths, targetFile);

            if (logger.isDebugEnabled())
                logger.debug(new StringBuilder("合并文件[").append(schema.getFileName()).append("],长度:").append(schema.getFileSize() / 1024 / 1024).append("m,耗时").append((System.currentTimeMillis() - start)).append("毫秒").toString());
            //检测MD5码是否一致 TODO 线程优化
            start = System.currentTimeMillis();
            String targetFileMd5 = FileUtil.getFileMd5(schema.getSavePath());

            if (logger.isDebugEnabled())
                logger.debug("md5计算耗时" + (System.currentTimeMillis() - start));

            if (!schema.getFileMd5().toLowerCase().equals(targetFileMd5.toLowerCase())) {
                Files.delete(targetFile);
                return Message.error(Info.MERGE_FILE_ERROR.getInfo(), schema.getCallback());
            } else {
                DeleteFileVisitor deleteFileVisitor = new DeleteFileVisitor();
                Files.walkFileTree(srcDirFile, deleteFileVisitor);
                if (deleteFileVisitor.isSuccess()) {
                    Files.delete(srcDirFile);
                }
            }
            return null;
        } else {
            return Message.error(Info.FILE_EMPTY.getInfo(), schema.getCallback());
        }
    }


    public Message mergeChunk(AdminUploadSchema schema) throws Exception {
        Message message;
        String base = propertyConfig.getRootPath();
        if (StringUtil.hasEmpty(schema.getFileMd5(), schema.getFileName()) || NumberUtil.hasEmpty(schema.getFileSize())) {
            return Message.error(Info.EINVAL.getInfo());
        } else {
            OssTripartiteSystem ossTripartiteSystem;

            if (!NumberUtil.hasEmpty(schema.getFileId())) {//替换文件
                OssFile ossFile = ossFileService.getOssFile(schema.getFileId());
                if (ossFile == null)
                    return Message.error(Info.FILE_NOTEXIST.getInfo());
                ossTripartiteSystem = systemKeyCache.getAndSysnc(ossFile.getSystemCode());
                message = Message.pass();
                Map<String, Object> ossFileMap = new HashMap<>();
                ossFileMap.put("ossFile", ossFile);
                message.setAttributes(ossFileMap);
            } else if (!NumberUtil.hasEmpty(schema.getDeleteFileId())) {//恢复文件
                OssDeleteFileLog ossDeleteFileLog = ossDeleteFileLogService.getOssDeleteFileLog(schema.getDeleteFileId());
                if (ossDeleteFileLog == null)
                    return Message.error(Info.FILE_NOTEXIST.getInfo());
                message = Message.pass();
                Map<String, Object> ossFileMap = new HashMap<>();
                ossFileMap.put("ossDeleteFileLog", ossDeleteFileLog);
                message.setAttributes(ossFileMap);
                ossTripartiteSystem = systemKeyCache.getAndSysnc(ossDeleteFileLog.getSystemCode());
            } else if (!StringUtil.hasEmpty(schema.getFileMd5(), schema.getFileName(), schema.getCode()) && !NumberUtil.hasEmpty(schema.getFileSize())) {//上传新文件
                ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());
            } else {
                return Message.error(Info.EINVAL.getInfo());
            }

            StringBuilder saveDir;

            if (!StringUtil.isEmpty(schema.getSavePath())) {//savepath 自定义路径
                saveDir = new StringBuilder(ossTripartiteSystem.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(schema.getSavePath());
                if (createSavePath(saveDir)) return Message.error(Info.SAVEPATH_NOTDIR.getInfo());
            } else {
                saveDir = new StringBuilder(ossTripartiteSystem.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(DateUtil.getCurrentYearMonth());
            }


            String targetFilePath = new StringBuilder(saveDir).append(PropertiesConfig.fileSeparator).append(schema.getFileName()).toString();
            schema.setSavePath(targetFilePath);
            Path targetFile = Paths.get(targetFilePath);
            if (Files.exists(targetFile)) {
                String savePath = saveDir.append(PropertiesConfig.fileSeparator).append(System.currentTimeMillis()).append(schema.getFileName()).toString();
                targetFile = Paths.get(savePath);
                schema.setSavePath(savePath);
            }
            String srcDirFilePath = new StringBuilder(base).append(PropertiesConfig.fileSeparator).append(schema.getFileMd5()).append(PropertiesConfig.directorySeparator).append(schema.getFileSize()).toString();


            Path srcDirFile = Paths.get(srcDirFilePath);
            DefaultFileVisitor defaultFileVisitor = new DefaultFileVisitor();
            Files.walkFileTree(srcDirFile, defaultFileVisitor);
            List<Path> paths = defaultFileVisitor.getFiles();

            if (Files.isDirectory(srcDirFile) && Files.exists(srcDirFile)) {
                long start = System.currentTimeMillis();
                //合并文件
                mergeFile(paths, targetFile);

                if(logger.isDebugEnabled())
                    logger.debug(new StringBuilder("合并文件[").append(schema.getFileName()).append("],长度:").append(schema.getFileSize() / 1024 / 1024).append("m,耗时").append((System.currentTimeMillis() - start)).append("毫秒").toString());
                //检测MD5码是否一致 TODO 线程优化
                String targetFileMd5 = FileUtil.getFileMd5(targetFilePath);

                if (!schema.getFileMd5().toLowerCase().equals(targetFileMd5.toLowerCase())) {
                    Files.delete(targetFile);
                    return Message.error(Info.MERGE_FILE_ERROR.getInfo());
                } else {//上传成功
                    DeleteFileVisitor deleteFileVisitor = new DeleteFileVisitor();
                    Files.walkFileTree(srcDirFile, deleteFileVisitor);
                    if (deleteFileVisitor.isSuccess()) {
                        Files.delete(srcDirFile);
                    }
                    return null;
                }
            } else {
                return Message.error(Info.FILE_EMPTY.getInfo());
            }
        }
    }

    public void checkTmpFile(int tmp, OssFile file) {
        if (tmp == TmpFile.UNTMP_FILE.getType() && file.getTmp() == TmpFile.TMP_FILE.getType()) {
            file.setRefer(1);
            file.setTmp(TmpFile.UNTMP_FILE.getType());
            ossFileService.save(file);
        } else if (tmp == file.getTmp()) {
            file.setRefer(file.getRefer() + 1);
            ossFileService.save(file);
        }
    }

    private StringBuilder buildSavePath(UploadSchema schema, OssTripartiteSystem ossTripartiteSystem) {
        StringBuilder saveDir;
        if (!StringUtil.isEmpty(schema.getSavePath())) {//savepath 自定义路径
            saveDir = new StringBuilder(ossTripartiteSystem.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(schema.getSavePath());
            File dir = new File(saveDir.toString());
            if (!dir.exists() && !dir.mkdirs()) {
                return null;
            }
        } else {//系统默认路径
            saveDir = new StringBuilder(ossTripartiteSystem.getSystemRootpath()).append(PropertiesConfig.fileSeparator).append(DateUtil.getCurrentYearMonth());
        }

        return saveDir;
    }

    private void mergeFile(List<Path> paths, Path targetFile) throws IOException {
        if (paths.size() == 1) {
            Files.move(paths.get(0), targetFile,
                    StandardCopyOption.REPLACE_EXISTING);
        } else {
            try (FileOutputStream resultFileOutputStream = new FileOutputStream(targetFile.toFile(), true);
                 FileChannel resultFileChannel = resultFileOutputStream.getChannel()) {
                Path[] ps = paths.toArray(new Path[paths.size()]);
                Arrays.sort(ps, new Comparator<Path>() {
                    public int compare(Path o1, Path o2) {
                        int o1Index = Integer.parseInt(o1.getFileName().toString());
                        int o2Index = Integer.parseInt(o2.getFileName().toString());
                        return o1Index - o2Index;
                    }
                });
                for (Path file : ps) {
                    try (FileInputStream fileInputStream = new FileInputStream(file.toFile());
                         FileChannel blk = fileInputStream.getChannel()) {
                        resultFileChannel.transferFrom(blk, resultFileChannel.size(), blk.size());
                    }
                }
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public static class DefaultFileVisitor extends SimpleFileVisitor<Path> {
        List<Path> list = new ArrayList<>();

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            list.add(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getFiles() {
            return list;
        }
    }

    public static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
        boolean success = true;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            boolean flag = Files.deleteIfExists(file);
            success = flag && success;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        public boolean isSuccess() {
            return success;
        }
    }

}



