package com.gionee.oss.task;

import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.constant.FileInfo;
import com.gionee.oss.dto.UploadSchema;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yeqy on 2017/6/20.
 */
public class ChunkUploadTask extends Thread {
    private FileInfo fileInfo;
    private UploadSchema schema;
    private InputStream inputStream;
    private CountDownLatch downLatch;
    volatile private Message message;
    private String rootPath;


    public ChunkUploadTask(ThreadGroup group, String rootPath, Message message, FileInfo fileInfo, UploadSchema schema, InputStream inputStream, CountDownLatch downLatch) {
        super(group, "chunkUploadTask");
        this.rootPath = rootPath;
        this.message = message;
        this.fileInfo = fileInfo;
        this.schema = schema;
        this.inputStream = inputStream;
        this.downLatch = downLatch;
    }

    @Override
    public void run() {

//        if (!StringUtil.hasEmpty(fileInfo.getFileMd5(), schema.getChunk())) {
//            String dirStr = rootPath + PropertiesConfig.fileSeparator + fileInfo.getFileMd5() + PropertiesConfig.directorySeparator + fileInfo.getFileSize();
//            File fileDir = new File(dirStr);
//            if (!fileDir.exists()) {
//                fileDir.mkdir();
//            }
//            File chunkFile = new File(dirStr + PropertiesConfig.fileSeparator + schema.getChunk());
//            try {
//                FileUtils.copyInputStreamToFile(inputStream, chunkFile);
//                downLatch.countDown();
//            } catch (IOException e) {
//                e.printStackTrace();
//                message = Message.error(Info.SYSTEM_ERROR.getInfo(), schema.getCallback());
//                this.getThreadGroup().interrupt();
//                downLatch.countDown();
//                downLatch.countDown();
//            }
//        } else {
//            message = Message.error(Info.EINVAL.getInfo(), schema.getCallback());
//            this.getThreadGroup().interrupt();
//            synchronized (downLatch){
//                downLatch.notify();
//            }
//        }

    }

    public Message getMessage() {
        return this.message;
    }
}
