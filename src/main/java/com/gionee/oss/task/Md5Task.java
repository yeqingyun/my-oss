package com.gionee.oss.task;

import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.constant.FileInfo;
import com.gionee.oss.dto.UploadSchema;
import com.gionee.oss.util.VerifyUtil;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yeqy on 2017/6/20.
 */
public class Md5Task extends Thread {
    volatile private Message message;
    private FileInfo fileInfo;
    private UploadSchema schema;
    private InputStream inputStream;
    private VerifyUtil verifyUtil;
    private CountDownLatch countDownLatch;

    public Md5Task(ThreadGroup group, Message message, FileInfo fileInfo, UploadSchema schema, InputStream inputStream, VerifyUtil verifyUtil, CountDownLatch countDownLatch) {
        super(group, "md5Task");
        this.message = message;
        this.fileInfo = fileInfo;
        this.schema = schema;
        this.inputStream = inputStream;
        this.verifyUtil = verifyUtil;
        this.countDownLatch = countDownLatch;
    }

    /**
     * 前提，md5和文件块上传谁都可能先完成
     */
    @Override
    public void run() {
//        try {
//            //message = verifyUtil.verifyFileInfo(fileInfo, multipartFile, schema);
//            //message = verifyUtil.verifyFileInfo(fileInfo, inputStream, schema);
//            if (message != null) {
//                this.getThreadGroup().interrupt();
//                synchronized (countDownLatch){
//                    countDownLatch.notify();
//                }
//            } else {
//                countDownLatch.countDown();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            message = Message.error(Info.FILE_ERROR.getInfo(), schema.getCallback());
//            countDownLatch.countDown();
//            countDownLatch.countDown();
//        }
    }

    public Message getMessage() {
        return this.message;
    }

}
